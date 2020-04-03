package com;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import com.ccloomi.rada.util.Paths;

/**© 2015-2019 Chenxj Copyright
 * 类    名：App
 * 类 描 述：
 * 作    者：chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2019年1月25日-下午7:24:18
 */
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
public class App {
	public static Class<?> getAppClass() {
		return new Object() {
			public Class<?> getSuperObjectClass() {
				String className=getClass().getName();
				className=className.substring(0, className.lastIndexOf("$"));
				try {
					return Class.forName(className);
				} catch (ClassNotFoundException e) {
					return null;
				}
			}
		}.getSuperObjectClass();
	}
	public static void main(String[] args) {
		Properties properties=new Properties();
		File pfile=Paths.getUserDirFile("config","application.properties");
		try {
			FileInputStream fin=new FileInputStream(pfile);
			properties.load(fin);
			fin.close();
			SpringApplication app=new SpringApplication(getAppClass());
			app.setDefaultProperties(properties);
			app.setWebApplicationType(WebApplicationType.NONE);
			app.run(args);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}