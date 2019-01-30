package com;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ccloomi.rada.annotation.RadaReference;
import com.s.UserService;

/**© 2015-2019 Chenxj Copyright
 * 类    名：TestController
 * 类 描 述：
 * 作    者：chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2019年1月25日-下午9:59:17
 */
@Controller
public class TestController {
	@RadaReference
	private UserService us;
	@RequestMapping("/hello")
	@ResponseBody
	public Object hello(String name) {
		return us.hello(name);
	}
	@RequestMapping("/bc")
	@ResponseBody
	public String benchmark(String name) {
		for(long i=0;i<1000000;i++) {
			us.hello(name);
		}
		return name;
	}
}
