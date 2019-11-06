package com.ccloomi.rada.endpoint;
import static com.ccloomi.rada.util.BytesUtil.readBytesAsObjectWithCompress;
import static com.ccloomi.rada.util.BytesUtil.writeValueAsBytesWithCompress;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.web.context.request.async.DeferredResult;

import com.ccloomi.rada.exceptions.ExceptionErrors;
import com.ccloomi.rada.exceptions.TaskTimeoutException;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

/**© 2015-2017 CCLooMi.Inc Copyright
 * 类    名：RadaProxyServer
 * 类 描 述：
 * 作    者：Chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2017年2月25日-下午5:04:12
 */
public class RadaProxyServer extends RadaRpcEndpoint implements ExceptionErrors{
	protected final LoadingCache<String, DeferredResult<Object>>handlerCache;
	public RadaProxyServer(){
		//TODO 待优化
		handlerCache=CacheBuilder
				.newBuilder()
				.concurrencyLevel(8)
				.expireAfterWrite(30, TimeUnit.SECONDS)
				.initialCapacity(1024)
				.removalListener((n)->{})
				.build(new CacheLoader<String, DeferredResult<Object>>() {
					@Override
					public DeferredResult<Object> load(String key) throws Exception {
						return null;
					}
				});
	}
	@Override
	public void startup() {
		init();
		while(true) {
			try{
				String queueName=new StringBuilder()
				.append("QUEUE_RETURN_")
				.append(group.toUpperCase())
				.append('_')
				.append(appName.toUpperCase())
				.append('_')
				.append(routingKey.toUpperCase())
				.toString();
				DeclareOk ok=returnChannel.queueDeclare(queueName, false, false, true, null);
				returnChannel.queueBind(ok.getQueue(), exReturnName, routingKey);
				returnChannel.basicConsume(ok.getQueue(), true, new DefaultConsumer(returnChannel){
					@Override
					public void handleDelivery(String consumerTag, Envelope envelope,AMQP.BasicProperties properties, byte[] body)throws IOException {
						onMessage(properties, body);
					}
				});
				break;
			}catch (Exception e) {
				log.error("Service startup failure", e);
				try {
					Thread.sleep(1000);
					log.info("Try restart service...");
				} catch (InterruptedException e1) {
					e1.printStackTrace();
					break;
				}
			}
		}
	}
	@Override
	public void onMessage(BasicProperties properties, byte[] body) {
		Object result=readBytesAsObjectWithCompress(body);
		DeferredResult<Object> deferredResult=handlerCache.getIfPresent(properties.getMessageId());
		if(deferredResult!=null){
			deferredResult.setResult(result);
		}
	}
	public void sendMessageWithNoReturn(String server,long timeout,boolean sync,int method,Object[] args){
		Map<String, Object>headers=new HashMap<>(2);
		headers.put("method", method);
		try {
			groupChannel.basicPublish(exGroupName, server,
					new BasicProperties().builder().headers(headers).build(),
					writeValueAsBytesWithCompress(args));
		}catch (Exception e) {
			if(groupChannel==null) {
				log.error("Message sending failed, please check if the service is started!", e);
			}else {
				log.error("Message sending failed", e);
			}
		}
	}
	public Object sendMessage(String server,long timeout,boolean sync,int method,Object[] args){
		DeferredResult<Object> deferredResult=new DeferredResult<>(timeout);
		String msid=Integer.toHexString(deferredResult.hashCode());
		Map<String, Object>headers=new HashMap<>(2);
		headers.put("method", method);
		BasicProperties bp=new BasicProperties()
				.builder()
				.replyTo(routingKey)
				.messageId(msid)
				.headers(headers)
				.build();
		try {
			handlerCache.put(msid, deferredResult);
			groupChannel.basicPublish(exGroupName, server, bp, writeValueAsBytesWithCompress(args));
			deferredResult.onTimeout(()->{
				log.info("Asynchronous call timeout (is the micro service started?)", new TaskTimeoutException());
				deferredResult.setResult(timeoutMsg);
			});
		} catch (Exception e) {
			if(groupChannel==null) {
				log.error("Message sending failed, please check if the service is started!", e);
			}else {
				log.error("Message sending failed", e);
			}
			deferredResult.setResult(serverErrorMsg);
		}
		if(!sync) {
			return deferredResult;
		}else {
			long t=0;
			while(t++<timeout) {
				if(deferredResult.hasResult()) {
					return deferredResult.getResult();
				}
				try {
					Thread.sleep(1);
				}catch (Exception e) {
					e.printStackTrace();
					deferredResult.setResult(serverErrorMsg);
					return null;
				}
			}
			log.info("Synchronous invocation timeout (is the micro service started?)", new TaskTimeoutException());
			return null;
		}
	}
}
