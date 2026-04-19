package com.nongcang.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.assistant")
public class AssistantProperties {

	private boolean enabled = true;

	private int chatMaxHistory = 12;

	private int toolMaxRows = 10;

	private final DeepseekProperties deepseek = new DeepseekProperties();

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public int getChatMaxHistory() {
		return chatMaxHistory;
	}

	public void setChatMaxHistory(int chatMaxHistory) {
		this.chatMaxHistory = chatMaxHistory;
	}

	public int getToolMaxRows() {
		return toolMaxRows;
	}

	public void setToolMaxRows(int toolMaxRows) {
		this.toolMaxRows = toolMaxRows;
	}

	public DeepseekProperties getDeepseek() {
		return deepseek;
	}

	public static class DeepseekProperties {

		private String baseUrl = "https://api.deepseek.com";

		private String apiKey = "";

		private String model = "deepseek-chat";

		private int connectTimeoutMillis = 5000;

		private int readTimeoutMillis = 30000;

		public String getBaseUrl() {
			return baseUrl;
		}

		public void setBaseUrl(String baseUrl) {
			this.baseUrl = baseUrl;
		}

		public String getApiKey() {
			return apiKey;
		}

		public void setApiKey(String apiKey) {
			this.apiKey = apiKey;
		}

		public String getModel() {
			return model;
		}

		public void setModel(String model) {
			this.model = model;
		}

		public int getConnectTimeoutMillis() {
			return connectTimeoutMillis;
		}

		public void setConnectTimeoutMillis(int connectTimeoutMillis) {
			this.connectTimeoutMillis = connectTimeoutMillis;
		}

		public int getReadTimeoutMillis() {
			return readTimeoutMillis;
		}

		public void setReadTimeoutMillis(int readTimeoutMillis) {
			this.readTimeoutMillis = readTimeoutMillis;
		}
	}
}
