package com.ccloomi.rada.exceptions;

import com.ccloomi.rada.bean.Message;

/**@类名 ExceptionErrors
 * @说明 
 * @作者 Chenxj
 * @邮箱 chenios@foxmail.com
 * @日期 2017年3月2日-下午1:37:25
 */
public interface ExceptionErrors {
	public static final Message timeoutMsg=new Message().code("1000").data("接口调用超时");
	public static final Message serverErrorMsg=new Message().code("1001").data("服务不可用");
}
