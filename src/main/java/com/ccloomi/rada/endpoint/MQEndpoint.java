package com.ccloomi.rada.endpoint;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.xerial.snappy.Snappy;

import com.ccloomi.rada.util.BytesUtil;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
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
			this.routingKey=randomId();
		}
		this.byteNull=writeValueAsBytesWithCompress((byte) 0, null);
	}
	/**设置 connectionFactory*/
	public MQEndpoint connectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
		return this;
	}
	protected String randomId() {
		byte[]kbs=new byte[16];
		new Random().nextBytes(kbs);
		return BytesUtil.bytesToHexString(kbs);
	}
	
	public final static byte[]writeValueAsBytesWithCompress(byte headFlag,Object obj){
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		ObjectOutputStream oos=null;
		try{
			bos.write(headFlag);
			switch (headFlag) {
			case 1:
				bos.write(((String)obj).getBytes());
				break;
			default:
				oos=new ObjectOutputStream(bos);
				oos.writeObject(obj);
				oos.flush();
				break;
			}
			return Snappy.compress(bos.toByteArray());
		}catch (Exception e) {
			e.printStackTrace();
		}
		return new byte[0];
	}
	public final static void writeValueAsBytesWithCompress(Object obj,byte[] b, int off,int len){
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		ObjectOutputStream oos=null;
		try{
			oos=new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.flush();
			System.arraycopy(Snappy.compress(bos.toByteArray()), 0, b, off, len);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	public final static <E>E readBytesAsObjectWithCompress(byte[]buf){
		if(buf!=null){
			return readBytesAsObjectWithCompress(buf, 0, buf.length);
		}
		return null;
	}
	@SuppressWarnings("unchecked")
	public final static <E>E readBytesAsObjectWithCompress(byte buf[], int offset, int length){
		ByteArrayInputStream byteInStream=null;
		ObjectInputStream ois=null;
		try{
	        byte[] result = new byte[Snappy.uncompressedLength(buf,offset,length)];
	        Snappy.uncompress(buf, offset, length, result, 0);
			switch (result[0]) {
			case 1:
				ObjectMapper om=new ObjectMapper();
				om.configure(Feature.ALLOW_SINGLE_QUOTES, true);
				return (E)om.readValue(result, 1, result.length-1, Object[].class);
			default:
				byteInStream=new ByteArrayInputStream(result,1,result.length-1);
				ois=new ObjectInputStream(byteInStream);
				return (E) ois.readObject();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
