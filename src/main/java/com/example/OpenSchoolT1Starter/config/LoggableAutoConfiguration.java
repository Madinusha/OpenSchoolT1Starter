package com.example.OpenSchoolT1Starter.config;

import com.example.OpenSchoolT1Starter.aspect.LoggingAspect;
import com.example.OpenSchoolT1Starter.properties.LoggableProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
@EnableConfigurationProperties(LoggableProperties.class)
public class LoggableAutoConfiguration {

	@Bean
	@ConditionalOnProperty(name = "loggable.enabled", havingValue = "true", matchIfMissing = false)
	public LoggingAspect loggingAspect(LoggableProperties properties) {
		return new LoggingAspect(properties);
	}
}