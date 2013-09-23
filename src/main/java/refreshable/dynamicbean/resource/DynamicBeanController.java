package refreshable.dynamicbean.resource;

import java.io.File;
import java.io.IOException;

import org.apache.qpid.util.FileUtils;
import org.codehaus.groovy.control.CompilationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import refreshable.context.wrapper.DynamicBeanRegistry;


import javax.jms.JMSException;
import javax.jms.Message;

@Controller
@RequestMapping(value = "/dynamic-bean")
public class DynamicBeanController {
	
	private static final Logger LOG = LoggerFactory.getLogger(DynamicBeanController.class);
	private JmsTemplate jmsTemplate;
	
	private DynamicBeanRegistry beanRegistry;
	
	private static final String DEFAULT_SCRIPT_PATH = "/tmp/scripts/";
	
	public JmsTemplate getJmsTemplate() {
		return jmsTemplate;
	}

	@Autowired
	public DynamicBeanController(JmsTemplate jmsTemplate, DynamicBeanRegistry beanRegistry) {
		this.jmsTemplate = jmsTemplate;
		this.beanRegistry = beanRegistry;
	}

	public DynamicBeanController() {
		this(null,null);
	}
	
	public DynamicBeanRegistry getBeanRegistry() {
		return beanRegistry;
	}


	@RequestMapping(value = "/status", method = RequestMethod.GET)
	@ResponseBody
    public String registerBean() {
    	return "Available Services : registry, send-message, replace";
    }
	
	@RequestMapping(value = "/registry/{name}", method = RequestMethod.POST)
	@ResponseBody
    public String status(@PathVariable("name") final String name) {
		try {
			beanRegistry.registerDynamicBean(name, 
					DEFAULT_SCRIPT_PATH + name + ".groovy",
					"5000");
		} catch (CompilationFailedException e) {
			LOG.error("Script compiler exception");
		} catch (IOException e) {
			LOG.error(String.format("IO Exception : %s", e.getMessage()));
		} catch (ClassNotFoundException e) {
			LOG.error(String.format("ClassNotFoundException : %s", e.getMessage()));
		} catch (Exception e) {
			LOG.error(String.format("Exception : %s", e.getMessage()));
		}
		return "registered Successfully";
    }

	@RequestMapping(value = "/send-message/{sequence}", method = RequestMethod.POST)
	@ResponseBody
    public String sendRequest(@PathVariable("sequence") final String sequence) {
		jmsTemplate.convertAndSend((Object)sequence,new MessagePostProcessor() {
	        public Message postProcessMessage(Message message) throws JMSException {
	            message.setStringProperty("CamelBeanMethodName", "execute");
	            return message;
	        }
	    });		
		return "Message sent successfuly";
    }

	@RequestMapping(value = "/replace/{oldBean}/{newBean}", method = RequestMethod.POST)
	@ResponseBody
    public String replaceGroovyBean(@PathVariable("oldBean") String oldBean,
    		@PathVariable("newBean") String newBean) throws IOException {
		File newFile = new File(DEFAULT_SCRIPT_PATH + newBean + ".groovy");
		File oldFile = new File(DEFAULT_SCRIPT_PATH + oldBean + ".groovy");
		if (newFile.exists()) {
			FileUtils.copy(newFile, oldFile);
		}
		return "replaced Successfully";
    }
	
	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	public void setBeanRegistry(DynamicBeanRegistry beanRegistry) {
		this.beanRegistry = beanRegistry;
	}
	
}