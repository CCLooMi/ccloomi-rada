package com.ccloomi.rada.util.digest;
/**
 * © 2015-2019 Chenxj Copyright
 * 类    名：MessageDigestAlgorithmsEnum
 * 类 描 述：
 * 作    者：Chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2019年8月7日-下午3:00:23
 */

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
/**
 * © 2015-2019 Chenxj Copyright
 * 类    名：MessageDigestAlgorithmsEnum
 * 类 描 述：
 * 作    者：Chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2019年8月7日-下午3:09:17
 */
public enum MessageDigestAlgorithmsEnum {
	MD2("MD2"),MD5("MD5"),
	SHA_1("SHA-1"),SHA_224("SHA-224"),SHA_256("SHA-256"),
	SHA_384("SHA-384"),SHA_512("SHA-512"),
	SHA3_224("SHA3-224"),SHA3_256("SHA3-256"),
	SHA3_384("SHA3-384"),SHA3_512("SHA3-512");
	private String name;
	private MessageDigestAlgorithmsEnum(String name) {
		this.name=name;
	}
	public MessageDigest digest() {
		try {
            return MessageDigest.getInstance(this.name);
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
	}
}
