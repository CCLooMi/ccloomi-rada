package com.ccloomi.rada.handler;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

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
	private JavaType[]types;
	public MQInvokeHandler(Method method,Object target) {
		this.setMethod(method);
		this.setTarget(target);
	}
	public Object execute(Object[]args) throws Exception{
		if(types.length>0) {
			return method.invoke(target, args);
		}
		return method.invoke(target, new Object[0]);
	}
	public Object execute(ObjectMapper om,Object[]args) throws Exception{
		if(types.length>0) {
			for(int i=0;i<args.length&&i<types.length;i++) {
				args[i]=om.convertValue(args[i], types[i]);
			}
		}
		return execute(args);
	}
	/**获取 method*/
	public Method getMethod() {
		return method;
	}
	/**设置 method*/
	public void setMethod(Method method) {
		this.method = method;
		this.method.setAccessible(true);

		if(method.getParameterCount()>0) {
			TypeFactory typeFactory=TypeFactory.defaultInstance();
			Parameter[]ps=method.getParameters();
			this.types=new JavaType[ps.length];
			for(int i=0;i<ps.length;i++) {
				types[i]=typeFactory.constructType(ps[i].getParameterizedType());
			}
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
