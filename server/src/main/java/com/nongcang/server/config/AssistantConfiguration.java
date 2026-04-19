package com.nongcang.server.config;

import java.time.Duration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(AssistantProperties.class)
public class AssistantConfiguration {

	@Bean
	public RestClient assistantRestClient(RestClient.Builder builder, AssistantProperties assistantProperties) {
		AssistantProperties.DeepseekProperties deepseekProperties = assistantProperties.getDeepseek();
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(Duration.ofMillis(deepseekProperties.getConnectTimeoutMillis()));
		requestFactory.setReadTimeout(Duration.ofMillis(deepseekProperties.getReadTimeoutMillis()));

		return builder
				.baseUrl(deepseekProperties.getBaseUrl())
				.requestFactory(requestFactory)
				.build();
	}
}
