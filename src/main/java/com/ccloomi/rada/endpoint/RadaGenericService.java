/**
 * 
 */
package com.ccloomi.rada.endpoint;

import org.springframework.beans.factory.annotation.Autowired;

import com.ccloomi.rada.util.digest.DigestUtils;

/**
 * 类    名：RadaGenericService
 * 类 描 述：Rada服务泛化调用服务
 * 作    者：Chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2020年4月3日-下午4:05:36
 */
public class RadaGenericService {
	private static final String defaultServer=DigestUtils.MD5Hex("default");
	@Autowired
	private RadaProxyServer proxyServer;
	public Object invoke(String className,String methodName,String jsonArgs,
			long timeout,boolean sync,String...paraTypes) {
		String server=null;
		int lastIndex=className.lastIndexOf('.');
		if(lastIndex>0) {
			server=DigestUtils.MD5Hex(className.substring(0, lastIndex));
		}else {
			server=defaultServer;
		}
		String method=DigestUtils.MD5Hex(getMethodLongName(className, methodName, paraTypes));
		return proxyServer.sendJsonMessage(server, timeout, sync, method, jsonArgs);
	}
	public void invoke_void(String className,String methodName,String jsonArgs,
			long timeout,boolean sync,String...paraTypes) {
		String server=null;
		int lastIndex=className.lastIndexOf('.');
		if(lastIndex>0) {
			server=DigestUtils.MD5Hex(className.substring(0, lastIndex));
		}else {
			server=defaultServer;
		}
		String method=DigestUtils.MD5Hex(getMethodLongName(className, methodName, paraTypes));
		proxyServer.sendJsonMessageWithNoReturn(server, timeout, sync, method, jsonArgs);
	}

	private static String getMethodLongName(String className,String methodName,String...paraTypes) {
		StringBuilder sb=new StringBuilder();
		sb.append(className).append('.')
		.append(methodName).append('(');
		if(paraTypes.length>0) {
			for(int i=0;i<paraTypes.length;i++) {
				sb.append(paraTypes[i]).append(',');
			}
			sb.setCharAt(sb.length()-1, ')');
		}else {
			sb.append(')');
		}
		return sb.toString();
	}
}
