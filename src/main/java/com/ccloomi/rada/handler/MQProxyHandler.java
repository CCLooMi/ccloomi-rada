package com.ccloomi.rada.handler;

import com.ccloomi.rada.endpoint.RadaProxyServer;

/**© 2015-2017 CCLooMi.Inc Copyright
 * 类    名：MQProxyHandler
 * 类 描 述：
 * 作    者：Chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2017年2月25日-下午5:30:32
 */
public class MQProxyHandler implements ProxyInvoationHandler{
	private RadaProxyServer proxyServer;
	public MQProxyHandler(RadaProxyServer proxyServer) {
		this.proxyServer=proxyServer;
	}
	@Override
	public Object invoke(String server,long timeout,boolean sync,int method, Object[] args) {
		return proxyServer.sendMessage(server,timeout,sync,method,args);
	}
	@Override
	public boolean invoke_boolean(String server, long timeout, boolean sync,int method, Object[] args) {
		return (boolean)proxyServer.sendMessage(server,timeout,sync,method,args);
	}
	@Override
	public byte invoke_byte(String server, long timeout, boolean sync,int method, Object[] args) {
		return (byte)proxyServer.sendMessage(server,timeout,sync,method,args);
	}
	@Override
	public char invoke_char(String server, long timeout,boolean sync, int method, Object[] args) {
		return (char)proxyServer.sendMessage(server,timeout,sync,method,args);
	}
	@Override
	public double invoke_double(String server, long timeout, boolean sync,int method, Object[] args) {
		return (double)proxyServer.sendMessage(server,timeout,sync,method,args);
	}
	@Override
	public float invoke_float(String server, long timeout,boolean sync, int method, Object[] args) {
		return (float)proxyServer.sendMessage(server,timeout,sync,method,args);
	}
	@Override
	public int invoke_int(String server, long timeout, boolean sync,int method, Object[] args) {
		return (int)proxyServer.sendMessage(server,timeout,sync,method,args);
	}
	@Override
	public long invoke_long(String server, long timeout,boolean sync, int method, Object[] args) {
		return (long)proxyServer.sendMessage(server,timeout,sync,method,args);
	}
	@Override
	public short invoke_short(String server, long timeout, boolean sync,int method, Object[] args) {
		return (short)proxyServer.sendMessage(server,timeout,sync,method,args);
	}
}
