package refreshable.context.wrapper.impl;

import java.io.File;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scripting.groovy.GroovyScriptFactory;
import org.springframework.scripting.support.RefreshableScriptTargetSource;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.scripting.support.ScriptFactoryPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import refreshable.context.wrapper.DynamicBeanRegistry;

@Component
public class SpringContextWrapper implements ApplicationContextAware, BeanFactoryPostProcessor, DynamicBeanRegistry {
	
	private ApplicationContext appContext;
	private ConfigurableListableBeanFactory factory;

	CamelContext camelContext;
	
	
	private static final String DEFAULT_SCRIPT_FACTORY_NAME = "scriptFactory.";
	private static final String DEFAULT_SCRIPT_FACTORY_METHOD_NAME = "getScriptedObject";
	private static final String DEFAULT_SCRIPT_FACTORY_CLASS_NAME = "org.springframework.scripting.groovy.GroovyScriptFactory";
	private static final String DEFAULT_SCRIPT_INTERFACE_NAME = "refreshable.service.FibonacciService";
	private static final String DEFAULT_SCRIPT_METHOD_NAME = "execute";

	public void postProcessBeanFactory(ConfigurableListableBeanFactory factory)
	             throws BeansException {
		this.factory = factory;
	}
	
	public void setApplicationContext(ApplicationContext c)
	     throws BeansException {
		this.appContext = c;
		camelContext = appContext.getBean(CamelContext.class);
	}
	
	public void registerDynamicBean(String beanName, String location,
			String refreshCheckDelay) throws Exception {
		BeanDefinitionRegistry registry = ((BeanDefinitionRegistry )factory);

		createAndRegisterScriptBeanFactoryBean(registry, beanName, location);
		
		ClassLoader classLoader = ClassUtils.getDefaultClassLoader();
		
		ResourceScriptSource rs = new ResourceScriptSource(
				new FileSystemResource(new File(location)));
		RefreshableScriptTargetSource ts = new RefreshableScriptTargetSource(
				factory, beanName, new GroovyScriptFactory(location),rs , false);
		ts.setRefreshCheckDelay(Long.valueOf(refreshCheckDelay));

		Class<?>[] interfaces = new Class[1]; 
				interfaces[0] = Class.forName(DEFAULT_SCRIPT_INTERFACE_NAME);
		
		createProxyFactory(ts, interfaces, classLoader);
		
		createAndRegisterBean(registry, beanName, interfaces, rs, Long.valueOf(refreshCheckDelay));
		
		addCamelRoute(beanName);
	}
	
	private void addCamelRoute(final String beanName) throws Exception {
		camelContext.addRoutes(new RouteBuilder() {
	          public void configure() {
	          	from("jms:queue:testqueue").to("bean:" + beanName + "?method=" + DEFAULT_SCRIPT_METHOD_NAME);            	
	          }
	      });
		
	}
	
	private void createAndRegisterBean(BeanDefinitionRegistry registry,
			String beanName,  Class<?>[] interfaces, ResourceScriptSource rs,
			long refreshCheckDelay) {
		GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
		beanDefinition.setFactoryBeanName(DEFAULT_SCRIPT_FACTORY_NAME + beanName);
		beanDefinition.setFactoryMethodName(DEFAULT_SCRIPT_FACTORY_METHOD_NAME);
		beanDefinition.getConstructorArgumentValues().clear();
		beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(0, rs);
		beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(1, interfaces);
		beanDefinition.setAutowireCandidate(true);
		beanDefinition.setScope(BeanDefinition.SCOPE_PROTOTYPE);
		beanDefinition.setAttribute(
				ScriptFactoryPostProcessor.REFRESH_CHECK_DELAY_ATTRIBUTE, refreshCheckDelay);		
	    registry.registerBeanDefinition(beanName,beanDefinition);		

	}
	
	private void createProxyFactory(RefreshableScriptTargetSource ts, 
			Class<?>[] interfaces, ClassLoader loader) {
		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.setTargetSource(ts);
		proxyFactory.setInterfaces(interfaces);
		DelegatingIntroductionInterceptor introduction = new DelegatingIntroductionInterceptor(ts);
		introduction.suppressInterface(TargetSource.class);
		proxyFactory.addAdvice(introduction);
		proxyFactory.getProxy(loader);
		
	}
	private void createAndRegisterScriptBeanFactoryBean(BeanDefinitionRegistry registry,
			String beanName, String location) {
		GenericBeanDefinition scriptBd = new GenericBeanDefinition();
		scriptBd.setBeanClassName(DEFAULT_SCRIPT_FACTORY_CLASS_NAME);
		ConstructorArgumentValues cav = scriptBd.getConstructorArgumentValues();
		int constructorArgNum = 0;
		cav.addIndexedArgumentValue(constructorArgNum++, location);
		
		scriptBd.getConstructorArgumentValues().addArgumentValues(cav);
		registry.registerBeanDefinition(DEFAULT_SCRIPT_FACTORY_NAME + beanName,scriptBd);
	}

	public ApplicationContext getAppContext() {
		return appContext;
	}
}