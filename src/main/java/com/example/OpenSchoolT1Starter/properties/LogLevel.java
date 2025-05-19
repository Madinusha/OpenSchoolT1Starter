package com.example.OpenSchoolT1Starter.properties;

public enum LogLevel {
	INFO,
	DEBUG,
	WARN,
	ERROR;

	public static LogLevel fromString(String value) {
		try {
			return value == null ? null : valueOf(value.toUpperCase());
		} catch (IllegalArgumentException ex) {
			throw new IllegalArgumentException("Invalid logging level: " + value +
					". Allowed values: info, debug, warn, error");
		}
	}
}