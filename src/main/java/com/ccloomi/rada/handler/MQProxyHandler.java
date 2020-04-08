package com.ccloomi.rada.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ccloomi.rada.endpoint.RadaProxyServer;

/**© 2015-2017 CCLooMi.Inc Copyright
 * 类    名：MQProxyHandler
 * 类 描 述：
 * 作    者：Chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2017年2月25日-下午5:30:32
 */
public class MQProxyHandler implements ProxyInvoationHandler{
	private Logger log = LoggerFactory.getLogger(getClass());
	private RadaProxyServer proxyServer;
	public MQProxyHandler(RadaProxyServer proxyServer) {
		this.proxyServer=proxyServer;
	}
	@Override
	public Object invoke(String server,long timeout,boolean sync,String method, Object[] args) {
		return proxyServer.sendMessage(server,timeout,sync,method,args);
	}
	@Override
	public void invoke_void(String server,long timeout,boolean sync,String method, Object[] args) {
		proxyServer.sendMessageWithNoReturn(server,timeout,sync,method,args);
	}
	@Override
	public boolean invoke_boolean(String server, long timeout, boolean sync,String method, Object[] args) {
		Object o=proxyServer.sendMessage(server,timeout,sync,method,args);
		if(o==null) {
			return false;
		}
		if(o instanceof Boolean) {
			return (boolean)o;
		}else {
			log.error("{}",o);
			return false;
		}
	}
	@Override
	public byte invoke_byte(String server, long timeout, boolean sync,String method, Object[] args) {
		Object o=proxyServer.sendMessage(server,timeout,sync,method,args);
		if(o==null) {
			return 0;
		}
		if(o instanceof Byte) {
			return (byte)o;
		}else {
			log.error("{}",o);
			return 0;
		}
	}
	@Override
	public char invoke_char(String server, long timeout,boolean sync, String method, Object[] args) {
		Object o=proxyServer.sendMessage(server,timeout,sync,method,args);
		if(o==null) {
			return 0;
		}
		if(o instanceof Character) {
			return (char)o;
		}else {
			log.error("{}",o);
			return 0;
		}
	}
	@Override
	public double invoke_double(String server, long timeout, boolean sync,String method, Object[] args) {
		Object o=proxyServer.sendMessage(server,timeout,sync,method,args);
		if(o==null) {
			return 0;
		}
		if(o instanceof Double) {
			return (double)o;
		}else {
			log.error("{}",o);
			return 0;
		}
	}
	@Override
	public float invoke_float(String server, long timeout,boolean sync, String method, Object[] args) {
		Object o=proxyServer.sendMessage(server,timeout,sync,method,args);
		if(o==null) {
			return 0;
		}
		if(o instanceof Float) {
			return (float)o;
		}else {
			log.error("{}",o);
			return 0;
		}
	}
	@Override
	public int invoke_int(String server, long timeout, boolean sync,String method, Object[] args) {
		Object o =proxyServer.sendMessage(server,timeout,sync,method,args);
		if(o==null) {
			return 0;
		}
		if(o instanceof Integer) {
			return (int)o;
		}else {
			log.error("{}",o);
			return 0;
		}
	}
	@Override
	public long invoke_long(String server, long timeout,boolean sync, String method, Object[] args) {
		Object o =proxyServer.sendMessage(server,timeout,sync,method,args);
		if(o==null) {
			return 0;
		}
		if(o instanceof Long) {
			return (long)o;
		}else {
			log.error("{}",o);
			return 0;
		}
	}
	@Override
	public short invoke_short(String server, long timeout, boolean sync,String method, Object[] args) {
		Object o = proxyServer.sendMessage(server,timeout,sync,method,args);
		if(o==null) {
			return -1;
		}
		if(o instanceof Short) {
			return (short)o;
		}else {
			log.error("{}",o);
			return 0;
		}
	}
}
