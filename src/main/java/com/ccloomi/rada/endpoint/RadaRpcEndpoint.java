package com.ccloomi.rada.endpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;


/**© 2015-2017 CCLooMi.Inc Copyright
 * 类    名：RadaRpcEndpoint
 * 类 描 述：
 * 作    者：Chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2017年2月25日-下午4:52:22
 */
public abstract class RadaRpcEndpoint extends MQEndpoint implements InitializingBean,ApplicationContextAware{
	protected Logger log=LoggerFactory.getLogger(getClass());
	protected ApplicationContext applicationContext;
	@Value("${ccloomi.rada.group}")
	protected String group;
	@Value("${ccloomi.rada.appName}")
	protected String appName;
	protected String exGroupName;
	protected String exReturnName;
	protected Channel groupChannel;
	protected Channel returnChannel;
	protected void init(){
		this.exGroupName=new StringBuilder()
				.append("EX_GROUP_")
				.append(group.toUpperCase())
				.toString();
		this.exReturnName=new StringBuilder()
				.append("EX_RETURN_")
				.append(group.toUpperCase())
				.toString();
		try{
			Connection connection=connectionFactory.newConnection();
			groupChannel=connection.createChannel();
			groupChannel.exchangeDeclare(exGroupName, "direct", false, false, null);
			
			returnChannel=connection.createChannel();
			returnChannel.exchangeDeclare(exReturnName, "direct", false, false, null);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	public abstract void startup();
	public abstract void onMessage(BasicProperties properties,byte[] body);
	@Override
	public void afterPropertiesSet() throws Exception {
		this.startup();
	}
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext=applicationContext;
	}
}