package com.ccloomi.rada.spring;

import static org.springframework.core.BridgeMethodResolver.findBridgedMethod;
import static org.springframework.core.BridgeMethodResolver.isVisibilityBridgeMethodPair;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static org.springframework.core.annotation.AnnotationUtils.getAnnotation;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import com.ccloomi.rada.annotation.RadaReference;

/**© 2015-2019 Chenxj Copyright
 * 类    名：ReferenceAnnotationBeanPostProcessor
 * 类 描 述：
 * 作    者：chenxj
 * 邮    箱：chenios@foxmail.com
 * 日    期：2019年1月25日-下午10:39:51
 */
public class ReferenceAnnotationBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter
		implements MergedBeanDefinitionPostProcessor, PriorityOrdered, ApplicationContextAware, BeanClassLoaderAware {
	private final ConcurrentMap<Integer, ReferenceInjectionMetadata> injectionMetadataCache = new ConcurrentHashMap<>(256);
	private final ConcurrentMap<Integer, RadaReferenceBean<?>> referenceBeansCache = new ConcurrentHashMap<>();
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private ClassLoader classLoader;
	private ApplicationContext applicationContext;
	@Override
	public int getOrder() {
		return LOWEST_PRECEDENCE;
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.classLoader=classLoader;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext=applicationContext;
	}

	@Override
	public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
		if(beanType!=null) {
			InjectionMetadata metadata=findReferenceMetadata(beanName, beanType, null);
			metadata.checkConfigMembers(beanDefinition);
		}
	}

	@Override
	public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean,
			String beanName) throws BeansException {
		InjectionMetadata metadata = findReferenceMetadata(beanName, bean.getClass(), pvs);
		try {
			metadata.inject(bean, beanName, pvs);
		} catch (BeanCreationException e) {
			throw e;
		} catch (Throwable e) {
			throw new BeanCreationException(beanName, "Injection of  @RadaReferencies failed", e);
		}
		return pvs;
	}

	private List<ReferenceFieldElement> findFieldRadaReferenceMetadata(final Class<?> beanClass) {
		final List<ReferenceFieldElement> elements = new LinkedList<ReferenceFieldElement>();
		ReflectionUtils.doWithFields(beanClass, new ReflectionUtils.FieldCallback() {
			@Override
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				RadaReference reference = getAnnotation(field, RadaReference.class);
				if (reference != null) {
					if (Modifier.isStatic(field.getModifiers())) {
						if (logger.isWarnEnabled()) {
							logger.warn("@Reference annotation is not supported on static fields: " + field);
						}
						return;
					}
					elements.add(new ReferenceFieldElement(field, reference));
				}
			}
		});

		return elements;

	}

	private List<ReferenceMethodElement> findMethodReferenceMetadata(final Class<?> beanClass) {
		final List<ReferenceMethodElement> elements = new LinkedList<ReferenceMethodElement>();
		ReflectionUtils.doWithMethods(beanClass, new ReflectionUtils.MethodCallback() {
			@Override
			public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
				Method bridgedMethod = findBridgedMethod(method);
				if (!isVisibilityBridgeMethodPair(method, bridgedMethod)) {
					return;
				}
				RadaReference reference = findAnnotation(bridgedMethod, RadaReference.class);
				if (reference != null && method.equals(ClassUtils.getMostSpecificMethod(method, beanClass))) {
					if (Modifier.isStatic(method.getModifiers())) {
						if (logger.isWarnEnabled()) {
							logger.warn("@Reference annotation is not supported on static methods: " + method);
						}
						return;
					}
					if (method.getParameterTypes().length == 0) {
						if (logger.isWarnEnabled()) {
							logger.warn(
									"@Reference  annotation should only be used on methods with parameters: " + method);
						}
					}
					PropertyDescriptor pd = BeanUtils.findPropertyForMethod(bridgedMethod, beanClass);
					elements.add(new ReferenceMethodElement(method, pd, reference));
				}
			}
		});

		return elements;

	}

	private ReferenceInjectionMetadata buildRadaReferenceMetadata(final Class<?> beanClass) {
		Collection<ReferenceFieldElement> fieldElements = findFieldRadaReferenceMetadata(beanClass);
		Collection<ReferenceMethodElement> methodElements = findMethodReferenceMetadata(beanClass);
		return new ReferenceInjectionMetadata(beanClass, fieldElements, methodElements);
	}

	private InjectionMetadata findReferenceMetadata(String beanName, Class<?> clazz, PropertyValues pvs) {
		Integer cacheKey = clazz.getName().hashCode();
		ReferenceInjectionMetadata metadata = this.injectionMetadataCache.get(cacheKey);
		if (InjectionMetadata.needsRefresh(metadata, clazz)) {
			synchronized (this.injectionMetadataCache) {
				metadata = this.injectionMetadataCache.get(cacheKey);
				if (InjectionMetadata.needsRefresh(metadata, clazz)) {
					if (metadata != null) {
						metadata.clear(pvs);
					}
					try {
						metadata = buildRadaReferenceMetadata(clazz);
						this.injectionMetadataCache.put(cacheKey, metadata);
					} catch (NoClassDefFoundError err) {
						throw new IllegalStateException("Failded to introspect bean class[" + clazz.getName()
								+ "] for radaReference metadata: could not find class that it depends on", err);
					}
				}
			}
		}
		return metadata;
	}

	private static class ReferenceInjectionMetadata extends InjectionMetadata {
		private final Collection<ReferenceFieldElement> fieldElements;
		private final Collection<ReferenceMethodElement> methodElements;
		public ReferenceInjectionMetadata(Class<?> targetClass, Collection<ReferenceFieldElement> fieldElements,
				Collection<ReferenceMethodElement> methodElements) {
			super(targetClass, combine(fieldElements, methodElements));
			this.fieldElements = fieldElements;
			this.methodElements = methodElements;
		}
		@SafeVarargs
		private static <T> Collection<T> combine(Collection<? extends T>... elements) {
			List<T> allElements = new ArrayList<T>();
			for (Collection<? extends T> e : elements) {
				allElements.addAll(e);
			}
			return allElements;
		}
		@SuppressWarnings("unused")
		public Collection<ReferenceFieldElement> getFieldElements() {
			return fieldElements;
		}
		@SuppressWarnings("unused")
		public Collection<ReferenceMethodElement> getMethodElements() {
			return methodElements;
		}
	}

	private class ReferenceMethodElement extends InjectionMetadata.InjectedElement {
		private final Method method;
		private final RadaReference reference;
		private volatile RadaReferenceBean<?> referenceBean;
		protected ReferenceMethodElement(Method method, PropertyDescriptor pd, RadaReference reference) {
			super(method, pd);
			this.method = method;
			this.reference = reference;
		}
		@Override
		protected void inject(Object bean, String beanName, PropertyValues pvs) throws Throwable {
			Class<?> referenceClass = pd.getPropertyType();
			referenceBean = buildReferenceBean(reference, referenceClass);
			ReflectionUtils.makeAccessible(method);
			method.invoke(bean, referenceBean.getObject());
		}
	}

	private class ReferenceFieldElement extends InjectionMetadata.InjectedElement {
		private final Field field;
		private final RadaReference reference;
		private volatile RadaReferenceBean<?> referenceBean;
		protected ReferenceFieldElement(Field field, RadaReference reference) {
			super(field, null);
			this.field = field;
			this.reference = reference;
		}

		@Override
		protected void inject(Object bean, String beanName, PropertyValues pvs) throws Throwable {
			Class<?> referenceClass = field.getType();
			referenceBean = buildReferenceBean(reference, referenceClass);
			ReflectionUtils.makeAccessible(field);
			field.set(bean, referenceBean.getObject());
		}
	}

	
	
	private RadaReferenceBean<?> buildReferenceBean(RadaReference reference, Class<?> referenceClass) throws Exception {
	        Integer cacheKey = referenceClass.hashCode();
	        RadaReferenceBean<?> referenceBean = referenceBeansCache.get(cacheKey);
	        if (referenceBean == null) {
	        	referenceBean = new RadaReferenceBean<>()
	        			.setApplicationContext(applicationContext)
	        			.setClassLoader(classLoader)
	        			.setReference(reference)
	        			.setReferenceClass(referenceClass);
	            referenceBeansCache.putIfAbsent(cacheKey, referenceBean);
	        }
	        return referenceBean;
	}
}
