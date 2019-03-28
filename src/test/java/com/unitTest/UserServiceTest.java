package com.unitTest;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.Configure;
import com.ccloomi.rada.annotation.RadaReference;
import com.ccloomi.rada.util.LogbackInit;
import com.ccloomi.rada.util.Paths;
import com.s.UserService;

/**© 2015-2019 Chenxj Copyright
 * 类    名：UserServiceTest
 * 类 描 述：
 * 作    者：chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2019年1月27日-上午10:38:22
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes= {Configure.class})
@TestPropertySource(locations= {"file:config/application.properties"})
public class UserServiceTest {
	static {
		LogbackInit.initLogback(Paths.getBaseUserDir("config","logback.xml"));
	}
	@RadaReference
	private UserService us;
	
	@Test
	public void testHello() {
		System.out.println(us.hello("rada"));
	}
	@Test
	public void testAmount() {
		Assert.assertTrue(us.amount(110)==210);
	}
	@Test
	public void testType() {
		System.out.println(us.type());
	}
	@Test
	//mockito模拟对象功能，完成功能测试
	public void mockTest() {
		UserService uss=Mockito.mock(UserService.class);
		Mockito.when(uss.amount(12)).thenReturn(112);
		Mockito.when(uss.amount(16)).thenReturn(116);
		System.out.println(uss.amount(12));
		System.out.println(uss.amount(16));
		System.out.println(uss.amount(18));
	}
	@Test
	public void voidTest() {
		us.notify("Hello world!");
	}
}
