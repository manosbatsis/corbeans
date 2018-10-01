/**
 *     Corda-Spring: integration and other utilities for developers working with Spring-Boot and Corda.
 *     Copyright (C) 2018 Manos Batsis
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 */
package com.github.manosbatsis.corbeans.spring.boot.corda.util;

import com.github.manosbatsis.corbeans.spring.boot.corda.CordaNodeServiceImpl;

public class NodeParams {

	public String username;
	public String password;
	public String address;
	public String adminAddress;
	public Integer retries = 6;

	public Boolean lazy = false;
	public Long retryDelaySeconds = Long.valueOf(10);
	public String primaryServiceType = CordaNodeServiceImpl.class.getCanonicalName();

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

	public String getPrimaryServiceType() { return primaryServiceType; }

	public void setPrimaryServiceType(String primaryServiceType) { this.primaryServiceType = primaryServiceType; }


	public Boolean getLazy() {
		return lazy;
	}

	public void setLazy(Boolean lazy) {
		this.lazy = lazy;
	}

	@Override
	public String toString() {
		return "NodeParams{" +
				"username='" + username + '\'' +
				", password='" + password + '\'' +
				", address='" + address + '\'' +
				", adminAddress='" + adminAddress + '\'' +
				", lazy='" + lazy + '\'' +
				", retries=" + retries +
				", retryDelaySeconds=" + retryDelaySeconds +
				", primaryServiceType='" + primaryServiceType + '\'' +
				'}';
	}
}
