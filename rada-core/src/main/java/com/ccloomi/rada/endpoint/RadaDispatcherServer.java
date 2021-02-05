package com.ccloomi.rada.endpoint;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

import com.ccloomi.rada.annotation.RadaService;
import com.ccloomi.rada.handler.MQInvokeHandler;
import com.ccloomi.rada.util.BytesUtil;
import com.ccloomi.rada.util.MethodUtil;
import com.ccloomi.rada.util.digest.DigestUtils;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

/**© 2015-2017 CCLooMi.Inc Copyright
 * 类    名：RadaDispatcherServer
 * 类 描 述：
 * 作    者：Chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2017年2月25日-下午5:03:57
 */
public class RadaDispatcherServer extends RadaRpcEndpoint{
	
	private Map<String, MQInvokeHandler>handlerMap;
	public RadaDispatcherServer() {
		handlerMap=new HashMap<>();
	}

	private void registerMappingHandler(Object obj) {
		Method[] methods=obj.getClass().getDeclaredMethods();
		for(int i=0;i<methods.length;i++){
			if((methods[i].getModifiers()&1)==1) {
				String methodName=MethodUtil.genericMethodLongName(methods[i]);
				String key=DigestUtils.MD5Hex(methodName);
				handlerMap.put(key, new MQInvokeHandler(methods[i],obj));
				log.info("mapping [{}] to [{}] ",key,methodName);
			}
		}
	}
	private void findInterfaces(Object o,Map<String, RadaService>m) {
		Class<?>c=o.getClass();
		RadaService rs=c.getAnnotation(RadaService.class);
		//支持实现多个接口，为多个接口提供服务
		Class<?>[]cs=c.getInterfaces();
		if(cs.length>0) {
			for(int i=0;i<cs.length;i++) {
				if(cs[i].getDeclaredMethods().length>0) {
					m.put(cs[i].getName(), rs);
				}
			}
		}
	}
	private Map<String, Object> generateArguments(RadaService rs){
		Map<String, Object>args=new HashMap<>();
		if(rs.x_message_ttl()>0) {
			args.put("x-message-ttl", rs.x_message_ttl());
		}
		if(rs.x_expires()>0) {
			args.put("x-expires", rs.x_expires());
		}
		if(rs.x_max_length()>0) {
			args.put("x-max-length", rs.x_max_length());
		}
		if(rs.x_max_length_bytes()>0) {
			args.put("x-max-length-bytes", rs.x_max_length_bytes());
		}
		if(rs.x_overflow().length()>0) {
			args.put("x-overflow", rs.x_overflow());
		}
		if(rs.x_dead_letter_exchange().length()>0) {
			args.put("x-dead-letter-exchange", rs.x_dead_letter_exchange());
		}
		if(rs.x_dead_letter_routing_key().length()>0) {
			args.put("x-dead-letter-routing-key", rs.x_dead_letter_routing_key());
		}
		if(rs.x_max_priority()>0) {
			args.put("x-max-priority", rs.x_max_priority());
		}
		if(rs.x_queue_mode().length()>0) {
			args.put("x-queue-mode", rs.x_queue_mode());
		}
		if(rs.x_queue_master_locator().length()>0) {
			args.put("x-queue-master-locator", rs.x_queue_master_locator());
		}
		return args;
	}
	@Override
	public void startup() {
		Map<String, RadaService>m=new HashMap<>();
		Map<String, Object>map=applicationContext.getBeansWithAnnotation(RadaService.class);
		for(Entry<String, Object>entry:map.entrySet()){
			registerMappingHandler(entry.getValue());
			findInterfaces(entry.getValue(), m);
		}
		init();
		while(true) {
			try{
				for(Entry<String, RadaService>e:m.entrySet()) {
					RadaService rs=e.getValue();
					String queueName=BytesUtil.bytesTob64String(DigestUtils.MD5("S-"+group+"-"+e.getKey()));
					DeclareOk ok=null;
					while(true) {
						try {
							ok=channel.queueDeclare(queueName, rs.durable(), rs.exclusive(), rs.autoDelete(), generateArguments(rs));
							break;
						}catch (Exception ex) {
							try {
								if(channel.isOpen()) {
									log.info("Queue[{}] declare failed,to redeclare we need delete it first",queueName);
									channel.queueDelete(queueName);
								}
							}catch (Exception exx) {
								if(channel.isOpen()) {
									log.warn("Queue[{}] delete failed cause:[{}]",
											queueName,exx.getMessage());
								}else {
									log.info("Waiting group channel recovery");
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
				}
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
		try {
			Map<String, Object>headers=properties.getHeaders();
			//headers返回的不是设置时的String对象，而是一个LongStringHelp对象
			String method=String.valueOf(headers.get("method"));
			MQInvokeHandler hander=handlerMap.get(method);
			if(properties.getReplyTo()!=null) {
				if(hander==null) {
					log.error("Target method[{}] not exist,Check API version consistency!",method);
					replyNull(properties);
					return;
				}
				Object result=hander.execute(readBytesAsObjectWithCompress(body));
				if(result==null) {
					replyNull(properties);
				}else if(result instanceof CompletableFuture) {
					((CompletableFuture<?>) result)
					.thenAcceptAsync(r->reply(properties,writeValueAsBytesWithCompress((byte) 0,result)));
				}else {
					reply(properties,writeValueAsBytesWithCompress((byte) 0,result));
				}
			}else {
				//不需要返回结果
				try {
					hander.execute(readBytesAsObjectWithCompress(body));
				}catch (Exception e) {
					log.error("", e);
				}
			}
		}catch (Exception e) {
			replyNull(properties);
			log.error("", e);
		}
	}
}
