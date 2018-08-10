package com.github.manosbatsis.corda.spring.beans.util;

public class NodeParams {

	public String username;
	public String password;
	public String address;
	public String adminAddress;
	public Integer retries = 6;
	public Long retryDelaySeconds = Long.valueOf(10);

	public NodeParams() {
	}

	public NodeParams(String address, String username, String password, String adminAddress, Integer retries, Long retryDelaySeconds) {
		this();
		this.address = address;
		this.username = username;
		this.password = password;
		this.adminAddress = adminAddress;
		this.retries = retries;
		this.retryDelaySeconds = retryDelaySeconds;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAdminAddress() {
		return adminAddress;
	}

	public void setAdminAddress(String adminAddress) {
		this.adminAddress = adminAddress;
	}

	public Integer getRetries() {
		return retries;
	}

	public void setRetries(Integer retries) {
		this.retries = retries;
	}

	public Long getRetryDelaySeconds() {
		return retryDelaySeconds;
	}

	public void setRetryDelaySeconds(Long retryDelaySeconds) {
		this.retryDelaySeconds = retryDelaySeconds;
	}

	@Override
	public String toString() {
		return "NodeParams{" +
				"username='" + username + '\'' +
				", password='" + password + '\'' +
				", address='" + address + '\'' +
				", adminAddress='" + adminAddress + '\'' +
				", retries=" + retries +
				", retryDelaySeconds=" + retryDelaySeconds +
				'}';
	}
}
