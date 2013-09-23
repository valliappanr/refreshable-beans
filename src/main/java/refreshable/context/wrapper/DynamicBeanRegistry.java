package refreshable.context.wrapper;

import java.io.IOException;

import org.codehaus.groovy.control.CompilationFailedException;
import org.springframework.context.ApplicationContext;

public interface DynamicBeanRegistry {
	
	void registerDynamicBean(String beanName, String location,
			String refreshCheckDelay) throws CompilationFailedException, IOException, ClassNotFoundException, Exception;

	ApplicationContext getAppContext();

}
