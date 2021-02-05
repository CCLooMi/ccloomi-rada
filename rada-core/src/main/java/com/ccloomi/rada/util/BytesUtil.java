package com.ccloomi.rada.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;

import org.xerial.snappy.Snappy;

/**© 2015-2016 CCLooMi.Inc Copyright
 * 类    名：BytesUtil
 * 类 描 述：
 * 作    者：Chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2016年6月25日-上午11:39:12
 */
public class BytesUtil {
	private static char[] e62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_-".toCharArray();
    private static byte[] d62 = new byte[256];
    private static final BigInteger b62=BigInteger.valueOf(62);
    
	private static final char[][] csm = new char[256][];
	private static final char[][] CSM = new char[256][];
	static {
		String cs = "0123456789abcdef";
		String CS = "0123456789ABCDEF";
		int i,j,n = 0;
		for (i = 0; i < 16; i++) {
			for (j = 0; j < 16; j++,n++) {
				csm[n] = new char[] {cs.charAt(i),cs.charAt(j)};
				CSM[n] = new char[] {CS.charAt(i),CS.charAt(j)};
			}
		}
		for (i = 0; i < e62.length; i++) {
            d62[e62[i]] = (byte) i;
        }
	}
	
	public static String bytesToHexString(byte[] byts) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < byts.length; i++) {
			sb.append(csm[byts[i]&0xff]);
		}
		return sb.toString();
	}
	public static String bytesToHEXString(byte[] byts) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < byts.length; i++) {
			sb.append(CSM[byts[i]&0xff]);
		}
		return sb.toString();
	}
	public static byte[] hexStringToBytes(String hex){
		int l=(hex.length()+1)/2;
		byte[]bytes=new byte[l];
		for(int i=0,j=0;i<hex.length();i++) {
			switch (hex.charAt(i)) {
			case '1':bytes[j]|=0x10>>((i&1)<<2);break;
			case '2':bytes[j]|=0x20>>((i&1)<<2);break;
			case '3':bytes[j]|=0x30>>((i&1)<<2);break;
			case '4':bytes[j]|=0x40>>((i&1)<<2);break;
			case '5':bytes[j]|=0x50>>((i&1)<<2);break;
			case '6':bytes[j]|=0x60>>((i&1)<<2);break;
			case '7':bytes[j]|=0x70>>((i&1)<<2);break;
			case '8':bytes[j]|=0x80>>((i&1)<<2);break;
			case '9':bytes[j]|=0x90>>((i&1)<<2);break;
			case 'a':case 'A':bytes[j]|=0xA0>>((i&1)<<2);break;
			case 'b':case 'B':bytes[j]|=0xB0>>((i&1)<<2);break;
			case 'c':case 'C':bytes[j]|=0xC0>>((i&1)<<2);break;
			case 'd':case 'D':bytes[j]|=0xD0>>((i&1)<<2);break;
			case 'e':case 'E':bytes[j]|=0xE0>>((i&1)<<2);break;
			case 'f':case 'F':bytes[j]|=0xF0>>((i&1)<<2);break;
			default:break;
			}
			j+=i&1;
		}
		return bytes;
	}
	public static String bytesTob62String(byte[]bytes) {
		StringBuilder sb=new StringBuilder((bytes.length<<3)/6+1);
        int pos = 0, val = 0;
        for (int i = 0; i < bytes.length; i++) {
            val = (val << 8) | (bytes[i] & 0xFF);
            pos += 8;
            while (pos > 5) {
                char c = e62[val >> (pos -= 6)];
                //使用za代替z，zb代替+，zc代替/,z定义为特殊字符，当然也可以使用别的字符
                switch (c) {
				case 'z':
					sb.append("za");
					break;
				case '-':
					sb.append("zb");
					break;
				case '_':
					sb.append("zc");
					break;
				default:
					sb.append(c);
					break;
				}
                val &= ((1 << pos) - 1);
            }
        }
        if (pos > 0) {
            char c = e62[val << (6 - pos)];
            switch (c) {
			case 'z':
				sb.append("za");
				break;
			case '-':
				sb.append("zb");
				break;
			case '_':
				sb.append("zc");
				break;
			default:
				sb.append(c);
				break;
			}
        }
		return sb.toString();
	}
    public static byte[] b62StringToBytes(String b62Str) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(b62Str.length());
        int pos = 0, val = 0;
        for (int i = 0; i < b62Str.length(); i++) {
        	char c = b62Str.charAt(i);
        	if(b62Str.charAt(i)=='z') {
        		switch (b62Str.charAt(++i)) {
				case 'a':
					c='z';
					break;
				case 'b':
					c='-';
					break;
				case 'c':
					c='_';
					break;
				default:
					c=b62Str.charAt(--i);
					break;
				}
        	}
            val = (val << 6) | d62[c];
            pos += 6;
            while (pos > 7) {
                baos.write(val >> (pos -= 8));
                val &= ((1 << pos) - 1);
            }
        }
        return baos.toByteArray();
    }
    /**字节数组转62进制*/
    public static String bytesTob62(byte[]bytes) {
    	return bigIntegerTob62(new BigInteger(1, bytes));
    }
    public static String bigIntegerTob62(BigInteger bi) {
    	StringBuilder sb=new StringBuilder();
		while(bi.compareTo(b62)>-1) {
			sb.append(e62[bi.mod(b62).intValue()]);
			bi=bi.divide(b62);
		}
		if(bi.compareTo(BigInteger.ZERO)>0) {
			sb.append(e62[bi.intValue()]);
		}
		return sb.toString();
    }
    public static String longTob62(long l) {
    	if(l>0) {
    		StringBuilder sb=new StringBuilder();
        	while(l>=62) {
        		sb.append(e62[(int) (l%62)]);
        		l=l/62;
        	}
        	if(l>0) {
        		sb.append(e62[(int) l]);
        	}
        	return sb.toString();
    	}
    	if(l==0) {
    		return "0";
    	}
    	return bytesTob62(BigInteger.valueOf(l).toByteArray());
    }
    public static String intTob62(int l) {
    	if(l>0) {
    		StringBuilder sb=new StringBuilder();
        	while(l>=62) {
        		sb.append(e62[l%62]);
        		l=l/62;
        	}
        	if(l>0) {
        		sb.append(e62[l]);
        	}
        	return sb.toString();
    	}
    	if(l==0) {
    		return "0";
    	}
    	return bytesTob62(BigInteger.valueOf(l).toByteArray());
    }
    public static String bytesTob64String(byte[]bytes) {
    	StringBuilder sb=new StringBuilder((bytes.length<<3)/6+1);
    	int pos = 0, val = 0;
        for (int i = 0; i < bytes.length; i++) {
            val = (val << 8) | (bytes[i] & 0xFF);
            pos += 8;
            while (pos > 5) {
                sb.append(e62[val >> (pos -= 6)]);
                val &= ((1 << pos) - 1);
            }
        }
        if (pos > 0) {
            sb.append(e62[val << (6 - pos)]);
        }
    	return sb.toString();
    }
    public static byte[] b64StringToBytes(String b64Str) {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream(b64Str.length());
    	int pos = 0, val = 0;
        for (int i = 0; i < b64Str.length(); i++) {
            val = (val << 6) | d62[b64Str.charAt(i)];
            pos += 6;
            while (pos > 7) {
                baos.write(val >> (pos -= 8));
                val &= ((1 << pos) - 1);
            }
        }
    	return baos.toByteArray();
    }
    
    /**
	 * 描述：字节数组转整形
	 * 作者：Chenxj
	 * 日期：2016年6月6日 - 下午10:26:02
	 * @param bytes 字节数组
	 * @param endianness 字节序( 1:Big Endian,-1:Little Endian)
	 * @return
	 */
	public static int readBytesToInt(byte[]bytes,int endianness){
		int a=0;
		int length=bytes.length;
		if(endianness==1){
			for(int i=length-1;i>-1;i--){
				a|=(bytes[i]&0xFF)<<(i*8);
			}
		}else if(endianness==-1){
			for(int i=0;i<length;i++){
				a|=(bytes[i]&0xFF)<<((length-1-i)*8);
			}
		}
		return a;
	}
	/**
	 * 描述：整形转字节数组
	 * 作者：Chenxj
	 * 日期：2016年6月6日 - 下午10:25:22
	 * @param a 整形
	 * @param length 字节数组长度
	 * @param endianness 字节序( 1:Big Endian,-1:Little Endian)
	 * @return
	 */
	public static byte[] intToBytes(int a,int length,int endianness){
		byte[]b=new byte[length];
		if(endianness==1){
			for(int i=length-1;i>-1;i--){
				b[i]= (byte) (a>>(8*i)&0xFF);
			}
		}else if(endianness==-1){
			for(int i=0;i<length;i++){
				b[i]= (byte) (a>>(8*(length-1-i))&0xFF);
			}
		}
		return b;
	}
	/**
	 * 描述：字节数组转长整形
	 * 作者：Chenxj
	 * 日期：2016年6月6日 - 下午10:31:49
	 * @param bytes
	 * @param endianness 字节序( 1:Big Endian,-1:Little Endian)
	 * @return
	 */
	public static long readBytesToLong(byte[]bytes,int endianness){
		long a=0;
		int length=bytes.length;
		if(endianness==1){
			for(int i=length-1;i>-1;i--){
				a|=((long)bytes[i]&0xFF)<<(i*8);
			}
		}else if(endianness==-1){
			for(int i=0;i<length;i++){
				a|=((long)bytes[i]&0xFF)<<((length-1-i)*8);
			}
		}
		return a;
	}
	/**
	 * 描述：长整形转字节数组
	 * 作者：Chenxj
	 * 日期：2016年6月6日 - 下午10:32:18
	 * @param a 长整形
	 * @param length 字节数组长度
	 * @param endianness 字节序( 1:Big Endian,-1:Little Endian)
	 * @return
	 */
	public static byte[] longToBytes(long a,int length,int endianness){
		byte[]b=new byte[length];
		if(endianness==1){
			for(int i=length-1;i>-1;i--){
				b[i]= (byte) (a>>(8*i)&0xFF);
			}
		}else if(endianness==-1){
			for(int i=0;i<length;i++){
				b[i]= (byte) (a>>((length-1-i)*8)&0xFF);
			}
		}
		return b;
	}
	/**
	 * 描述：字节数组转双精度类型
	 * 作者：Chenxj
	 * 日期：2016年6月6日 - 下午10:40:16
	 * @param bytes 字节数组
	 * @param endianness 字节序( 1:Big Endian,-1:Little Endian)
	 * @return
	 */
	public static double readBytesToDouble(byte[]bytes,int endianness){
		return Double.longBitsToDouble(readBytesToLong(bytes,endianness));
	}
	/**
	 * 描述：双精度类型转字节数组
	 * 作者：Chenxj
	 * 日期：2016年6月6日 - 下午10:42:16
	 * @param a 双精度类型
	 * @param length 字节数组长度
	 * @param endianness 字节序( 1:Big Endian,-1:Little Endian)
	 * @return
	 */
	public static byte[] doubleToBytes(double a,int length,int endianness){
		return longToBytes(Double.doubleToLongBits(a), length,endianness);
	}
	/**
	 * 描述：字节数组转Float
	 * 作者：Chenxj
	 * 日期：2016年6月25日 - 下午12:30:53
	 * @param bytes
	 * @param endianness 字节序( 1:Big Endian,-1:Little Endian)
	 * @return
	 */
	public static float readBytesToFloat(byte[]bytes,int endianness){
		int a=0;
		int length=bytes.length;
		if(endianness==1){
			for(int i=length-1;i>-1;i--){
				a|=((long)bytes[i]&0xFF)<<(i*8);
			}
		}else if(endianness==-1){
			for(int i=0;i<length;i++){
				a|=((long)bytes[i]&0xFF)<<((length-1-i)*8);
			}
		}
		return Float.intBitsToFloat(a);
	}
	/**
	 * 描述：Float转字节数组
	 * 作者：Chenxj
	 * 日期：2016年6月25日 - 下午12:31:24
	 * @param a Float
	 * @param length 字节数组长度
	 * @param endianness 字节序( 1:Big Endian,-1:Little Endian)
	 * @return
	 */
	public static byte[] floatToBytes(float a,int length,int endianness){
		long b=Float.floatToIntBits(a);
		return longToBytes(b, length,endianness);
	}

	public final static byte[]writeValueAsBytes(Object obj){
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		ObjectOutputStream oos=null;
		try{
			oos=new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.flush();
			return bos.toByteArray();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return new byte[0];
	}
	public final static void writeValueAsBytes(Object obj,byte[] b, int off,int len){
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		ObjectOutputStream oos=null;
		try{
			oos=new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.flush();
			System.arraycopy(bos.toByteArray(), 0, b, off, len);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	public final static <E>E readBytesAsObject(byte[]buf){
		if(buf!=null){
			return readBytesAsObject(buf, 0, buf.length);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public final static <E>E readBytesAsObject(byte buf[], int offset, int length){
		ByteArrayInputStream byteInStream=new ByteArrayInputStream(buf, offset, length);
		ObjectInputStream ois=null;
		try{
			ois=new ObjectInputStream(byteInStream);
			return (E) ois.readObject();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	

	public final static byte[]writeValueAsBytesWithCompress(Object obj){
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		ObjectOutputStream oos=null;
		try{
			oos=new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.flush();
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
			byteInStream=new ByteArrayInputStream(result);
			ois=new ObjectInputStream(byteInStream);
			return (E) ois.readObject();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
