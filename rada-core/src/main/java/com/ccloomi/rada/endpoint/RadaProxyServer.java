package com.ccloomi.rada.endpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.ccloomi.rada.exceptions.ExceptionErrors;
import com.ccloomi.rada.exceptions.TaskTimeoutException;
import com.ccloomi.rada.util.BytesUtil;
import com.ccloomi.rada.util.digest.DigestUtils;
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
	protected final LoadingCache<String, CompletableFuture<Object>>handlerCache;
	private Map<String, String>serverQueueMap;
	public RadaProxyServer(){
		serverQueueMap=new HashMap<>();
		handlerCache=CacheBuilder
				.newBuilder()
				.concurrencyLevel(8)
				.expireAfterWrite(30, TimeUnit.SECONDS)
				.initialCapacity(1024)
				.build(new CacheLoader<String, CompletableFuture<Object>>() {
					@Override
					public CompletableFuture<Object> load(String key) throws Exception {
						return null;
					}
				});
	}
	@Override
	public void startup() {
		init();
		while(true) {
			try{
				DeclareOk ok=null;
				while(true) {
					try {
						ok=channel.queueDeclare(id, false, false, true, null);
						break;
					}catch (Exception e) {
						try{
							if(channel.isOpen()) {
								log.info("Queue[{}] declare failed,to redeclare we need delete it first",id);
								channel.queueDelete(id);
							}
						}catch (Exception exx) {
							if(channel.isOpen()) {
								log.warn("Queue[{}] delete failed cause:[{}]",
										id,exx.getMessage());
							}else {
								log.info("Waiting return channel recovery");
							}
							Thread.sleep(1000);
						}
					}
				}
				channel.basicConsume(ok.getQueue(), true, new DefaultConsumer(channel){
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
		CompletableFuture<Object> f=handlerCache.getIfPresent(properties.getMessageId());
		if(f!=null){
			f.complete(readBytesAsObjectWithCompress(body));
		}
	}
	private String serverQueueName(String server) {
		String name=serverQueueMap.get(server);
		if(name!=null) {
			return name;
		}
		name=BytesUtil.bytesTob64String(DigestUtils.MD5("S-"+group+"-"+server));
		serverQueueMap.put(server, name);
		return name;
	}
	public void sendMessageWithNoReturn(String server,long timeout,boolean sync,String method,Object[] args){
		Map<String, Object>headers=new HashMap<>(2);
		headers.put("method", method);
		String serverQueueName=serverQueueName(server);
		while(true) {
			if(channel==null) {
				log.error("Waiting for service starting...");
				try {Thread.sleep(500);}
				catch (InterruptedException e) {}
				if(channel!=null) {
					log.error("Service start success");
				}
			}else {
				pub(serverQueueName,
						new BasicProperties()
						.builder()
						.headers(headers)
						.build(),
						writeValueAsBytesWithCompress((byte) 0,args));
				break;
			}
		}
	}
	public Object sendMessage(String server,long timeout,boolean sync,String method,Object[] args){
		CompletableFuture<Object> f;
		String msid=randomId();
		Map<String, Object>headers=new HashMap<>(2);
		headers.put("method", method);
		BasicProperties bp=new BasicProperties()
				.builder()
				.replyTo(id)
				.messageId(msid)
				.headers(headers)
				.build();
		String serverQueueName=serverQueueName(server);
		while(true) {
			if(channel==null) {
				log.error("Waiting for service starting...");
				try {Thread.sleep(500);}
				catch (InterruptedException e) {}
				if(channel!=null) {
					log.error("Service start success");
				}
			}else {
				f=new CompletableFuture<>();
				handlerCache.put(msid, f);
				pub(serverQueueName, bp, writeValueAsBytesWithCompress((byte) 0,args),f);
				break;
			}
		}
		if(!sync) {
			return f;
		}else {
			try {
				return f.get(timeout, TimeUnit.MILLISECONDS);
			}catch (TimeoutException e) {
				log.info("Synchronous invocation timeout (is the micro service started?)",
						new TaskTimeoutException());
				return null;
			}catch (Exception e) {
				log.error("",e);
				return null;
			}
		}
	}

	public void sendJsonMessageWithNoReturn(String server,long timeout,boolean sync,String method,String jsonArgs){
		Map<String, Object>headers=new HashMap<>(2);
		headers.put("method", method);
		String serverQueueName=serverQueueName(server);
		while(true) {
			if(channel==null) {
				log.error("Waiting for service starting...");
				try {Thread.sleep(500);}
				catch (InterruptedException e) {}
				if(channel!=null) {
					log.error("Service start success");
				}
			}else {
				pub(serverQueueName,
						new BasicProperties()
						.builder()
						.headers(headers)
						.build(),
						writeValueAsBytesWithCompress((byte) 1,jsonArgs));
				break;
			}
		}
	}
	public Object sendJsonMessage(String server,long timeout,boolean sync,String method,String jsonArgs){
		CompletableFuture<Object> f;
		String msid=randomId();
		Map<String, Object>headers=new HashMap<>(2);
		headers.put("method", method);
		BasicProperties bp=new BasicProperties()
				.builder()
				.replyTo(id)
				.messageId(msid)
				.headers(headers)
				.build();

		String serverQueueName=serverQueueName(server);
		while(true) {
			if(channel==null) {
				log.error("Waiting for service starting...");
				try {Thread.sleep(500);}
				catch (InterruptedException e) {}
				if(channel!=null) {
					log.error("Service start success");
				}
			}else {
				f=new CompletableFuture<>();
				handlerCache.put(msid, f);
				pub(serverQueueName, bp, writeValueAsBytesWithCompress((byte) 1,jsonArgs),f);
				break;
			}
		}
		if(!sync) {
			return f;
		}else {
			try {
				return f.get(timeout, TimeUnit.MILLISECONDS);
			}catch (TimeoutException e) {
				log.info("Synchronous invocation timeout (is the micro service started?)", new TaskTimeoutException());
				return null;
			}catch (Exception e) {
				log.error("",e);
				return null;
			}
		}
	}
}
