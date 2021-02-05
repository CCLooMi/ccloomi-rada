package com.ccloomi.rada.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;


/**© 2015-2017 CCLooMi.Inc Copyright
 * 类    名：RadaService
 * 类 描 述：
 * 作    者：Chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2017年2月25日-下午4:28:58
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component //该注解让spring能识别当前注解
public @interface RadaService {
	/**持久化*/
	boolean durable() default false;
	/**排它性*/
	boolean exclusive() default false;
	/**自动删除*/
	boolean autoDelete() default false;
	/**队列消息生存周期*/
	long x_message_ttl() default -1;
	/**队列过期时间*/
	long x_expires() default -1;
	/**队列最大长度*/
	long x_max_length() default -1;
	/**队列内存空间长度*/
	long x_max_length_bytes() default -1;
	/**设置队列溢出行为，drop-head，reject-publish*/
	String x_overflow() default "";
	/**队列过期或消息过期，推送到指定的路由*/
	String x_dead_letter_exchange() default "";
	/**队列过期或消息过期，推送到指定的路由指定的key队列中*/
	String x_dead_letter_routing_key() default "";
	/**队列支持的消息的最大优先级*/
	int x_max_priority() default -1;
	/**lazy:先将消息保存到磁盘，有消费者开始消费才加载到内存*/
	String x_queue_mode() default "";
	/**将队列设置为主位置模式*/
	String x_queue_master_locator() default "";
}
