package com.example.OpenSchoolT1Starter.aspect;

import com.example.OpenSchoolT1Starter.properties.LogLevel;
import com.example.OpenSchoolT1Starter.properties.LoggableProperties;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class LoggingAspect {
	private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

	private final LoggableProperties properties;

	public LoggingAspect(LoggableProperties properties) {
		this.properties = properties;
	}

	@Before("@annotation(com.example.OpenSchoolT1Starter.annotation.Loggable)")
	public void logBefore(JoinPoint joinPoint) {
		log(LogLevel.INFO, "Before: {}.{}() with args = {}",
				joinPoint.getSignature().getDeclaringTypeName(),
				joinPoint.getSignature().getName(),
				joinPoint.getArgs());
	}

	@AfterThrowing(
			pointcut = "execution(* com.example.OpenSchoolT1Starter.service.TaskService.*(..))",
			throwing = "ex"
	)
	public void logAfterThrowing(JoinPoint joinPoint, Exception ex) {
		log(LogLevel.ERROR, "AfterThrowing: {}.{}() with exception = {}",
				joinPoint.getSignature().getDeclaringTypeName(),
				joinPoint.getSignature().getName(),
				ex.getMessage());

	}

	@AfterReturning(
			pointcut = "@annotation(com.example.OpenSchoolT1Starter.annotation.Loggable)",
			returning = "result"
	)
	public void logAfterReturning(JoinPoint joinPoint, Object result) {
		log(LogLevel.INFO, "AfterReturning: from {}.{}() with result = {}",
				joinPoint.getSignature().getDeclaringTypeName(),
				joinPoint.getSignature().getName(),
				result);
	}

	@Around("@annotation(com.example.OpenSchoolT1Starter.annotation.MeasureTime)")
	public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
		long start = System.currentTimeMillis();

		try {
			Object result = joinPoint.proceed();
			long executionTime = System.currentTimeMillis() - start;
			log(LogLevel.INFO, "Around: {}.{}() executed in {} ms",
					joinPoint.getSignature().getDeclaringTypeName(),
					joinPoint.getSignature().getName(),
					executionTime);

			return result;
		} catch (Exception e) {
			log(LogLevel.ERROR, "Method failure", e);
			throw e;
		}
	}

	@Around("@annotation(com.example.OpenSchoolT1Starter.annotation.HttpLog)")
	public Object logHttpRequest(ProceedingJoinPoint joinPoint) throws Throwable {
		log(LogLevel.INFO, "HTTP METHOD CALLED");
		Object[] args = joinPoint.getArgs();

		for (int i = 0; i < args.length; i++) {
			Object arg = args[i];
			log(LogLevel.DEBUG, "Argument {} = {}", i, arg);
		}

		Object result;
		try {
			result = joinPoint.proceed();
			log(LogLevel.INFO, "Method returned: {}", result);
		} catch (Exception ex) {
			log(LogLevel.ERROR, "Exception occurred: {}", ex.getMessage());
			throw ex;
		}
		return result;
	}

	private void log(LogLevel level, String message, Object... args) {
		if (!properties.isLogEnabled(level)) return;

		switch (level) {
			case DEBUG -> logger.debug(message, args);
			case WARN -> logger.warn(message, args);
			case ERROR -> logger.error(message, args);
			default -> logger.info(message, args);
		}
	}

}
