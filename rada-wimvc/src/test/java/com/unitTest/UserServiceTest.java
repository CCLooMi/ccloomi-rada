package com.unitTest;

import java.util.concurrent.CompletableFuture;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.Configure;
import com.ccloomi.rada.annotation.RadaReference;
import com.ccloomi.rada.endpoint.RadaGenericService;
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
	@Autowired
	private RadaGenericService radaGenericService;
	
	@Test
	public void testHello() {
		@SuppressWarnings("unchecked")
		CompletableFuture<Object>f=(CompletableFuture<Object>) us.hello("rada");
		f.thenAcceptAsync(r->System.out.println(r));
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
	@Test
	public void genericServiceTest() {
		//JSON泛化调用测试
		String className=UserService.class.getName();
		String methodName="hello";
		String jsonArgs="['Seemie']";
		Assert.assertEquals(radaGenericService
				.invoke(className, methodName, jsonArgs,
						6000, true,String.class.getName()),
				"Hello Seemie");
	}
}
