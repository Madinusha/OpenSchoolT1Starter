package com.example.OpenSchoolT1Starter.properties;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "loggable")
public class LoggableProperties {
	private final boolean enabled;
	private final String level;

	public LoggableProperties(boolean enabled, String level) {
		this.enabled = enabled;
		this.level = level;

		LogLevel.fromString(this.level);
	}

	public LogLevel getLogLevel() {
		return LogLevel.fromString(level);
	}
}