package com.ccloomi.rada.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;

import com.ccloomi.rada.endpoint.RadaDispatcherServer;
import com.ccloomi.rada.endpoint.RadaGenericService;
import com.ccloomi.rada.endpoint.RadaProxyServer;
import com.rabbitmq.client.ConnectionFactory;

@ConditionalOnProperty(value="ccloomi.rada.enabled",havingValue="true")
@EnableConfigurationProperties(RadaProperties.class)
public class RadaAutoConfiguration {
	
	@Bean
	public ConnectionFactory connectionFactory(RadaProperties properties) {
		ConnectionFactory cf=new ConnectionFactory();
		cf.setHost(properties.getHost());
		cf.setPort(properties.getPortWithDefault(5672));
		cf.setUsername(properties.getUsername());
		cf.setPassword(properties.getPassword());
		cf.setVirtualHost(properties.getVirtualHost());
		cf.setAutomaticRecoveryEnabled(true);
		return cf;
	}
//	@Bean
//	public BeanPostProcessor beanPostProcessor() {
//		return new BeanPostProcessor() {
//			
//			@Override
//			public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
//				if(bean!=null) {
//					if(bean.getClass().getDeclaredAnnotation(Controller.class)!=null) {
//						System.out.println("bean:\t"+bean);
//					}
//				}
//				return bean;
//			}
//			
//			@Override
//			public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
//				return bean;
//			}
//		};
//	}
	@Bean
	public ConfigurationClassPostProcessor configurationClassPostProcessor() {
		return new ConfigurationClassPostProcessor() {
			@Override
			public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
				super.postProcessBeanDefinitionRegistry(registry);
				
				registry.registerBeanDefinition(RadaDispatcherServer.class.getName(),
						BeanDefinitionBuilder.genericBeanDefinition(RadaDispatcherServer.class)
						.getBeanDefinition());
				registry.registerBeanDefinition(RadaProxyServer.class.getName(),
						BeanDefinitionBuilder.genericBeanDefinition(RadaProxyServer.class)
						.getBeanDefinition());
				registry.registerBeanDefinition(RadaGenericService.class.getName(),
						BeanDefinitionBuilder.genericBeanDefinition(RadaGenericService.class)
						.getBeanDefinition());
				registry.registerBeanDefinition(ReferenceAnnotationBeanPostProcessor.class.getName(),
						BeanDefinitionBuilder.genericBeanDefinition(ReferenceAnnotationBeanPostProcessor.class)
						.getBeanDefinition());
			}
		};
	}
}
