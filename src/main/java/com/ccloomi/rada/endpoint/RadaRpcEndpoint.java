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
import com.rabbitmq.client.Recoverable;
import com.rabbitmq.client.RecoveryListener;


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
				.append("S-")
				.append(group)
				.toString();
		this.exReturnName=new StringBuilder()
				.append("R-")
				.append(group)
				.toString();
		while(true) {
			try{
				Connection connection=connectionFactory.newConnection();
				connection.addShutdownListener(e->log.warn("连接中断，等待重连..."));
				((Recoverable)connection).addRecoveryListener(new RecoveryListener() {
					@Override
					public void handleRecoveryStarted(Recoverable recoverable) {
						log.info("开始重新连接...");
					}
					@Override
					public void handleRecovery(Recoverable recoverable) {
						log.info("重新连接成功^_^");
					}
				});
				groupChannel=connection.createChannel();
				while(true) {
					try {
						groupChannel.exchangeDeclare(exGroupName, "direct", false, false, null);
						break;
					}catch (Exception e) {
						try {
							if(groupChannel.isOpen()) {
								log.info("Exchange[{}] declare failed,to redeclare we need delete it first",exGroupName);
								groupChannel.exchangeDelete(exGroupName);
							}
						}catch (Exception exx) {
							if(groupChannel.isOpen()) {
								log.warn("Exchange[{}] delete failed cause:[{}]",
										exGroupName,exx.getMessage());
							}else {
								log.info("Waiting group channel recovery");
							}
							Thread.sleep(1000);
						}
					}
				}
				
				returnChannel=connection.createChannel();
				while(true) {
					try {
						returnChannel.exchangeDeclare(exReturnName, "direct", false, false, null);
						break;
					}catch (Exception e) {
						try {
							if(returnChannel.isOpen()) {
								log.info("Exchange[{}] declare failed,to redeclare we need delete it first",exReturnName);
								returnChannel.exchangeDelete(exReturnName);
							}
						}catch (Exception exx) {
							if(returnChannel.isOpen()) {
								log.warn("Exchange[{}] delete failed cause:[{}]",
										exReturnName,exx.getMessage());
							}else {
								log.info("Waiting return channel recovery");
							}
							Thread.sleep(1000);
						}
					}
				}
				break;
			}catch (Exception e) {
				log.error("Service initialization failed, please check whether the service configuration is correct or the network is normal or the guard wall port configuration");
				try {
					Thread.sleep(1000);
					log.info("Try reinit service...");
				} catch (InterruptedException e1) {
					e1.printStackTrace();
					break;
				}
			}
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
