package com.example.OpenSchoolT1Starter.aspect;

import com.example.OpenSchoolT1Starter.properties.LogLevel;
import com.example.OpenSchoolT1Starter.properties.LoggableProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

@Component
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
		log(LogLevel.ERROR, "AfterThrowing in {}.{}() with exception = {}",
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
		if (!properties.isEnabled()) return joinPoint.proceed();
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
		if (!properties.isEnabled()) {
			return joinPoint.proceed();
		}
		ServletRequestAttributes attributes =
				(ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

		if (attributes == null) {
			log(LogLevel.WARN, "HttpLog triggered outside of HTTP request context");
			return joinPoint.proceed();
		}

		ContentCachingRequestWrapper request = new ContentCachingRequestWrapper(attributes.getRequest());
		ContentCachingResponseWrapper response = new ContentCachingResponseWrapper(attributes.getResponse());
		String method = request.getMethod();
		String uri = request.getRequestURI();

		log(LogLevel.INFO, "HTTP REQUEST: {} {}", method, uri);
		logHeaders(request);
		logRequestBody(request);

		Object result;
		try {
			result = joinPoint.proceed();
			logResponse(result);
			response.copyBodyToResponse();
			return result;
		} catch (Exception ex) {
			log(LogLevel.ERROR, "Exception occurred: {}", ex.getMessage());
			throw ex;
		} finally {
			response.copyBodyToResponse();
		}
	}

	private void logResponse(Object result) {
		if (!properties.isEnabled()) return;

		if (result != null) {
			if (result instanceof List) {
				log(LogLevel.INFO, "RESPONSE LIST [{} items]:", ((List<?>) result).size());
				((List<?>) result).forEach(item ->
						log(LogLevel.INFO, "  - {}", item)
				);
			} else {
				log(LogLevel.INFO, "RESPONSE: {}", result);
			}
		} else {
			log(LogLevel.INFO, "RESPONSE: [null]");
		}
	}

	private void logHeaders(HttpServletRequest request) {
		if (!properties.isEnabled()) return;

		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			String headerValue = request.getHeader(headerName);

			if (headerValue == null || headerValue.isBlank()) {
				log(LogLevel.WARN, "Header '{}' is present but has no value", headerName);
			} else {
				log(LogLevel.INFO, "Header: {}={}", headerName, headerValue);
				log(LogLevel.DEBUG, "Header-DEBUG: {}={}", headerName, headerValue);
			}
		}
	}

	private void logRequestBody(HttpServletRequest request) throws IOException {
		if (!properties.isEnabled()) return;

		if (!"POST".equalsIgnoreCase(request.getMethod()) &&
				!"PUT".equalsIgnoreCase(request.getMethod()) &&
				!"PATCH".equalsIgnoreCase(request.getMethod())) {
			return;
		}

		StringBuilder body = new StringBuilder();
		try (BufferedReader reader = request.getReader()) {
			String line;
			while ((line = reader.readLine()) != null) {
				body.append(line);
			}
		}
		if (body.length() > 0) {
			log(LogLevel.INFO, "BODY: {}", body.toString().replace("\n", "").trim());
		}
	}

	private void log(LogLevel level, String message, Object... args) {
		if (!properties.isLogEnabled(level)) return;
		System.out.println("level " + level);
		switch (level) {
			case DEBUG -> logger.debug(message, args);
			case WARN -> logger.warn(message, args);
			case ERROR -> logger.error(message, args);
			default -> logger.info(message, args);
		}
	}

}
