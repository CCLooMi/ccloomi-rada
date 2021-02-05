package com.ccloomi.rada.springboot;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;

import com.ccloomi.rada.endpoint.RadaDispatcherServer;
import com.ccloomi.rada.endpoint.RadaGenericService;
import com.ccloomi.rada.endpoint.RadaProxyServer;
import com.ccloomi.rada.spring.RadaProperties;
import com.ccloomi.rada.spring.ReferenceAnnotationBeanPostProcessor;
import com.rabbitmq.client.ConnectionFactory;

@ConditionalOnProperty(value="ccloomi.rada.enabled",havingValue="true")
@EnableConfigurationProperties
public class RadaAutoConfiguration {
	
	@Bean
	@ConfigurationProperties("ccloomi.rada")
	public RadaProperties radaProperties() {
		return new RadaProperties();
	}
	
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
