package com.ccloomi.rada.bean;
/**
 * Copyright (c) 2015 CCLooMi.Inc
 * 类    名：Message
 * 类 描 述：和web端交互message
 * 作    者：Chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2015年6月27日-上午9:20:04
 */
public class Message extends BaseBean{
	private static final long serialVersionUID = 3237437916356473926L;
	private String code;
	private Object data;
	/**获取 code*/
	public String getCode() {
		return code;
	}
	/**设置 code*/
	public Message code(String code) {
		this.code = code;
		return this;
	}
	/**获取 data*/
	public Object getData() {
		return data;
	}
	/**设置 data*/
	public Message data(Object data) {
		this.data = data;
		return this;
	}
}
