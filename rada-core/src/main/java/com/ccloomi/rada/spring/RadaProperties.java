package com.ccloomi.rada.spring;

public class RadaProperties {
	private String host;
	private Integer port;
	private String username;
	private String password;
	private String virtualHost;
	private String group;
	private String appName;
	/**获取 host*/
	public String getHost() {
		return host;
	}
	/**设置 host*/
	public void setHost(String host) {
		this.host = host;
	}
	
	/**获取 port*/
	public Integer getPort() {
		return port;
	}
	/**获取 port*/
	public Integer getPortWithDefault(int dport) {
		if(port==null||port<0) {
			return dport;
		}else {
			return port;
		}
	}
	/**设置 port*/
	public void setPort(Integer port) {
		this.port = port;
	}
	/**获取 username*/
	public String getUsername() {
		return username;
	}
	/**设置 username*/
	public void setUsername(String username) {
		this.username = username;
	}
	/**获取 password*/
	public String getPassword() {
		return password;
	}
	/**设置 password*/
	public void setPassword(String password) {
		this.password = password;
	}
	/**获取 virtualHost*/
	public String getVirtualHost() {
		return virtualHost;
	}
	/**设置 virtualHost*/
	public void setVirtualHost(String virtualHost) {
		this.virtualHost = virtualHost;
	}
	/**获取 group*/
	public String getGroup() {
		return group;
	}
	/**设置 group*/
	public void setGroup(String group) {
		this.group = group;
	}
	/**获取 appName*/
	public String getAppName() {
		return appName;
	}
	/**设置 appName*/
	public void setAppName(String appName) {
		this.appName = appName;
	}
}
