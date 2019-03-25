package com.ccloomi.rada.endpoint;
import static com.ccloomi.rada.util.BytesUtil.writeValueAsBytesWithCompress;

import org.springframework.beans.factory.annotation.Autowired;

import com.rabbitmq.client.ConnectionFactory;

/**© 2015-2017 CCLooMi.Inc Copyright
 * 类    名：MQEndpoint
 * 类 描 述：
 * 作    者：Chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2017年2月25日-下午4:43:28
 */
public abstract class MQEndpoint {
	@Autowired
	protected ConnectionFactory connectionFactory;
	protected String routingKey;
	protected byte[]byteNull;
	public MQEndpoint(String...routingKeys){
		if(routingKeys.length>0){
			this.routingKey=routingKeys[0];
		}else{
			this.routingKey=Integer.toHexString(this.hashCode());
		}
		this.byteNull=writeValueAsBytesWithCompress(null);
	}
	/**设置 connectionFactory*/
	public MQEndpoint connectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
		return this;
	}
	
}
