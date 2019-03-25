package com.ccloomi.rada.endpoint;

import static com.ccloomi.rada.util.BytesUtil.readBytesAsObjectWithCompress;
import static com.ccloomi.rada.util.BytesUtil.writeValueAsBytesWithCompress;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.web.context.request.async.DeferredResult;

import com.ccloomi.rada.annotation.RadaService;
import com.ccloomi.rada.handler.MQInvokeHandler;
import com.ccloomi.rada.util.MethodUtil;
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
	
	private Map<Integer, MQInvokeHandler>handlerMap;
	public RadaDispatcherServer() {
		handlerMap=new HashMap<>();
	}

	private void registerMappingHandler(Object obj) {
		Method[] methods=obj.getClass().getMethods();
		for(int i=0;i<methods.length;i++){
			if(!MethodUtil.isObjMethod(methods[i])) {
				String methodName=MethodUtil.genericMethodLongName(methods[i]);
				Integer key=methodName.hashCode();
				handlerMap.put(key, new MQInvokeHandler(methods[i],obj));
				log.info("register method[{}] to\t[{}]", methodName,key);
			}
		}
	}
	private void findPkg(Object o,Map<String, RadaService>m) {
		Package pkg=null;
		Class<?>c=o.getClass();
		RadaService rs=c.getAnnotation(RadaService.class);
		Class<?>[]cs=c.getInterfaces();
		if(cs.length>0) {
			pkg=cs[0].getPackage();
		}else {
			pkg=c.getPackage();
		}
		if(pkg!=null) {
			m.put(pkg.getName(),rs);
		}else {
			m.put("default", rs);
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
			findPkg(entry.getValue(), m);
		}
		init();
		try{
			for(Entry<String, RadaService>e:m.entrySet()) {
				RadaService rs=e.getValue();
				String queueName=new StringBuilder()
						.append("QUEUE_SERVER_")
						.append(group.toUpperCase())
						.append('_')
						.append(e.getKey().toUpperCase())
						.toString();
				DeclareOk ok=groupChannel.queueDeclare(queueName, rs.durable(), rs.exclusive(), rs.autoDelete(), generateArguments(rs));
				groupChannel.queueBind(ok.getQueue(), exGroupName, Integer.toHexString(e.getKey().hashCode()));
				groupChannel.basicConsume(ok.getQueue(), true, new DefaultConsumer(groupChannel){
					@Override
					public void handleDelivery(String consumerTag, Envelope envelope,AMQP.BasicProperties properties, byte[] body)throws IOException {
						onMessage(properties, body);
					}
				});
			}
		}catch (Exception e) {
			log.error("服务启动失败", e);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onMessage(BasicProperties properties, byte[] body) {
		try {
			Map<String, Object>headers=properties.getHeaders();
			Object method=headers.get("method");
			Integer key=null;
			if(method instanceof Long){
				key=((Long) method).intValue();
			}else{
				key=(Integer)method;
			}
			MQInvokeHandler hander=handlerMap.get(key);
			Object result=hander.execute(readBytesAsObjectWithCompress(body));
			if(result instanceof DeferredResult) {
				((DeferredResult<Object>) result).setResultHandler((r)->{
					try {returnChannel.basicPublish(exReturnName, properties.getReplyTo(), properties, writeValueAsBytesWithCompress(r));}
					catch (IOException e) {}
				});
			}else {
				returnChannel.basicPublish(exReturnName, properties.getReplyTo(), properties, writeValueAsBytesWithCompress(result));
			}
		}catch (Exception e) {
			if(!(e instanceof NullPointerException)) {
				log.error("", e);
			}
			try {returnChannel.basicPublish(exReturnName, properties.getReplyTo(), properties,byteNull);}
			catch (Exception e1) {}
		}
	}
}
