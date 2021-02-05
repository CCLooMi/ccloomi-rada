package com.ccloomi.rada.endpoint;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP;
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
	protected Channel channel;
	protected void init(){
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
				channel=connection.createChannel();
				break;
			}catch (Exception e) {
				log.error("Service initialization failed cause by[{}],\n please check whether the service configuration is correct or the network is normal or the guard wall port configuration",
						e.getMessage());
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
	public void run() {
		this.startup();
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		this.start();
	}
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext=applicationContext;
	}

	protected void pub(String queueName,AMQP.BasicProperties properties,byte[]data) {
		int c=3;
		while(c-->0) {
			try {
				channel.basicPublish("", queueName, properties,data);
				break;
			}catch (Exception e) {
				if(c>0) {
					try {Thread.sleep(1000);}
					catch (InterruptedException e1) {break;}
				}else {
					log.error("Message sending failed", e);
				}
			}
		}
	}
	protected void pub(String queueName,AMQP.BasicProperties properties,byte[]data,CompletableFuture<?>f) {
		int c=3;
		while(c-->0) {
			try {
				channel.basicPublish("", queueName, properties,data);
				break;
			}catch (Exception e) {
				if(c>0) {
					try {Thread.sleep(1000);}
					catch (InterruptedException e1) {break;}
				}else {
					f.complete(null);
					log.error("Message sending failed", e);
				}
			}
		}
	}
	protected void replyNull(AMQP.BasicProperties properties) {
		reply(properties, byteNull);
	}
	protected void reply(AMQP.BasicProperties properties,byte[]data) {
		int c=3;
		while(c-->0) {
			try {
				channel.basicPublish("", properties.getReplyTo(), properties,data);
				break;
			}catch (Exception e) {
				if(c>0) {
					try {Thread.sleep(1000);}
					catch (InterruptedException e1) {break;}
				}else {
					log.error("Message sending failed", e);
				}
			}
		}
	}
}
