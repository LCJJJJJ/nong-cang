package com.nongcang.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

	private String issuer = "nong-cang-server";

	private String jwtSecret = "change-this-demo-jwt-secret-to-a-very-long-random-string-2026";

	private long accessTokenExpireSeconds = 1800;

	private long refreshTokenExpireSeconds = 604800;

	private Long adminUserId = 1L;

	private String adminUsername = "admin";

	private String adminPhone = "13800000000";

	private String adminDisplayName = "系统管理员";

	private String adminPassword = "Admin@123456";

	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	public String getJwtSecret() {
		return jwtSecret;
	}

	public void setJwtSecret(String jwtSecret) {
		this.jwtSecret = jwtSecret;
	}

	public long getAccessTokenExpireSeconds() {
		return accessTokenExpireSeconds;
	}

	public void setAccessTokenExpireSeconds(long accessTokenExpireSeconds) {
		this.accessTokenExpireSeconds = accessTokenExpireSeconds;
	}

	public long getRefreshTokenExpireSeconds() {
		return refreshTokenExpireSeconds;
	}

	public void setRefreshTokenExpireSeconds(long refreshTokenExpireSeconds) {
		this.refreshTokenExpireSeconds = refreshTokenExpireSeconds;
	}

	public Long getAdminUserId() {
		return adminUserId;
	}

	public void setAdminUserId(Long adminUserId) {
		this.adminUserId = adminUserId;
	}

	public String getAdminUsername() {
		return adminUsername;
	}

	public void setAdminUsername(String adminUsername) {
		this.adminUsername = adminUsername;
	}

	public String getAdminPhone() {
		return adminPhone;
	}

	public void setAdminPhone(String adminPhone) {
		this.adminPhone = adminPhone;
	}

	public String getAdminDisplayName() {
		return adminDisplayName;
	}

	public void setAdminDisplayName(String adminDisplayName) {
		this.adminDisplayName = adminDisplayName;
	}

	public String getAdminPassword() {
		return adminPassword;
	}

	public void setAdminPassword(String adminPassword) {
		this.adminPassword = adminPassword;
	}
}
