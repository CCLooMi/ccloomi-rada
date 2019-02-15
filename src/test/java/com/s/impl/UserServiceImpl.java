package com.s.impl;

import com.ccloomi.rada.annotation.RadaService;
import com.s.UserService;

/**© 2015-2019 Chenxj Copyright
 * 类    名：UserServiceImpl
 * 类 描 述：
 * 作    者：chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2019年1月27日-上午10:34:36
 */
@RadaService(autoDelete=true)
public class UserServiceImpl implements UserService{
	
	@Override
	public Object hello(String name) {
		return "Hello "+name;
	}

	@Override
	public int amount(int age) {
		return age+100;
	}

	@Override
	public String type() {
		return "UserServerType";
	}

}
