package com.ccloomi.rada.handler;

import java.lang.reflect.Method;

/**© 2015-2017 CCLooMi.Inc Copyright
 * 类    名：MDHandler
 * 类 描 述：
 * 作    者：Chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2017年2月25日-下午5:20:15
 */
public class MQInvokeHandler {
	private Method method;
	private Object target;
	public MQInvokeHandler(Method method,Object target) {
		this.setMethod(method);
		this.setTarget(target);
	}
	public Object execute(Object[]args) throws Exception{
		return method.invoke(target, args);
	}
	/**获取 method*/
	public Method getMethod() {
		return method;
	}
	/**设置 method*/
	public void setMethod(Method method) {
		this.method = method;
		this.method.setAccessible(true);
	}
	/**获取 target*/
	public Object getTarget() {
		return target;
	}
	/**设置 target*/
	public void setTarget(Object target) {
		this.target = target;
	}
}
