package com.example.OpenSchoolT1Starter.aspect;

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
		log("Before: {}.{}() with args = {}",
				joinPoint.getSignature().getDeclaringTypeName(),
				joinPoint.getSignature().getName(),
				joinPoint.getArgs());
	}

	@AfterThrowing(
			pointcut = "execution(* com.example.OpenSchoolT1Starter.service.TaskService.*(..))",
			throwing = "ex"
	)
	public void logAfterThrowing(JoinPoint joinPoint, Exception ex) {
		log("AfterThrowing: {}.{}() with exception = {}",
				joinPoint.getSignature().getDeclaringTypeName(),
				joinPoint.getSignature().getName(),
				ex.getMessage());

	}

	@AfterReturning(
			pointcut = "@annotation(com.example.OpenSchoolT1Starter.annotation.Loggable)",
			returning = "result"
	)
	public void logAfterReturning(JoinPoint joinPoint, Object result) {
		log("AfterReturning: from {}.{}() with result = {}",
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
			log("Around: {}.{}() executed in {} ms",
					joinPoint.getSignature().getDeclaringTypeName(),
					joinPoint.getSignature().getName(),
					executionTime);

			return result;
		} catch (Exception e) {
			log("Method failure", e);
			throw e;
		}
	}

	@Around("@annotation(com.example.OpenSchoolT1Starter.annotation.HttpLog)")
	public Object logHttpRequest(ProceedingJoinPoint joinPoint) throws Throwable {
		log("HTTP method: {} called", joinPoint.getSignature().getName());
		Object[] args = joinPoint.getArgs();

		for (int i = 0; i < args.length; i++) {
			Object arg = args[i];
			log("Argument {} = {}", i, arg);
		}

		Object result;
		try {
			result = joinPoint.proceed();
			log("Method returned: {}", result);
		} catch (Exception ex) {
			log("Exception occurred: {}", ex.getMessage());
			throw ex;
		}
		return result;
	}

	private void log(String message, Object... args) {
		switch (properties.getLogLevel()) {
			case DEBUG -> logger.debug(message, args);
			case WARN -> logger.warn(message, args);
			case ERROR -> logger.error(message, args);
			default -> logger.info(message, args);
		}
	}

}
