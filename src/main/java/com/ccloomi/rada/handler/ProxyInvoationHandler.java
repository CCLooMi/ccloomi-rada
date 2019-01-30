package com.ccloomi.rada.handler;

/**© 2015-2017 CCLooMi.Inc Copyright
 * 类    名：ProxyInvoationHandler
 * 类 描 述：
 * 作    者：Chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2017年2月26日-下午1:37:21
 */
public interface ProxyInvoationHandler {
	public Object invoke(String server,long timeout,boolean sync,int method,Object[]args);
	public boolean invoke_boolean(String server,long timeout,boolean sync,int method,Object[]args);
	public byte invoke_byte(String server,long timeout,boolean sync,int method,Object[]args);
	public char invoke_char(String server,long timeout,boolean sync,int method,Object[]args);
	public double invoke_double(String server,long timeout,boolean sync,int method,Object[]args);
	public float invoke_float(String server,long timeout,boolean sync,int method,Object[]args);
	public int invoke_int(String server,long timeout,boolean sync,int method,Object[]args);
	public long invoke_long(String server,long timeout,boolean sync,int method,Object[]args);
	public short invoke_short(String server,long timeout,boolean sync,int method,Object[]args);
}
