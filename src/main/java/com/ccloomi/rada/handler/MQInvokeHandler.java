package com.ccloomi.rada.handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

//jdk 10
//import jdk.internal.reflect.MethodAccessor;
//jdk 8
import sun.reflect.MethodAccessor;

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
	private MethodAccessor methodAccessor;
	public MQInvokeHandler(Method method,Object target) {
		this.setMethod(method);
		this.setTarget(target);
	}
	@SuppressWarnings("unchecked")
	public <T>T execute(Object[]args){
		try {
			return (T)methodAccessor.invoke(target, args);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**获取 method*/
	public Method getMethod() {
		return method;
	}
	/**设置 method*/
	public void setMethod(Method method) {
		this.method = method;
		try{
			Method getMethodAccessor=Method.class.getDeclaredMethod("getMethodAccessor");
			getMethodAccessor.setAccessible(true);
			Method acquireMethodAccessor=Method.class.getDeclaredMethod("acquireMethodAccessor");
			acquireMethodAccessor.setAccessible(true);
			this.methodAccessor=(MethodAccessor)getMethodAccessor.invoke(method);
			if(this.methodAccessor==null){
				this.methodAccessor=(MethodAccessor) acquireMethodAccessor.invoke(method);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
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
