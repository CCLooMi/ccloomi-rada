package com;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.ccloomi.rada.spring.RadaAutoConfiguration;

/**© 2015-2019 Chenxj Copyright
 * 类    名：Configure
 * 类 描 述：
 * 作    者：chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2019年1月25日-下午7:25:35
 */
@Configuration
@ImportAutoConfiguration({RadaAutoConfiguration.class})
@ComponentScan("com.*")
public class Configure {
	
}
