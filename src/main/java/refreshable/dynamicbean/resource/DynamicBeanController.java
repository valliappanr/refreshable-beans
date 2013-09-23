package refreshable.dynamicbean.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.codehaus.groovy.control.CompilationFailedException;
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
    	return "Available Services : registry, sendMessage, replace";
    }
	
	@RequestMapping(value = "/registry/{name}", method = RequestMethod.POST)
	@ResponseBody
    public String status(@PathVariable("name") final String name) {
		try {
			beanRegistry.registerDynamicBean(name, 
					DEFAULT_SCRIPT_PATH + name + ".groovy",
					"5000");
		} catch (CompilationFailedException e) {
		} catch (IOException e) {
		} catch (ClassNotFoundException e) {
		} catch (Exception e) {
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
		FileInputStream inStream = null;
		FileChannel inChannel = null;
		FileOutputStream outStream = null;
		FileChannel  outChannel = null;
		if (newFile.exists()) {
			try {
			inStream = new FileInputStream(newFile);
	        inChannel = inStream.getChannel();
	        outStream = new  FileOutputStream(oldFile);        
	        outChannel = outStream.getChannel();
	        long bytesTransferred = 0;
	        while(bytesTransferred < inChannel.size()){
	          bytesTransferred += inChannel.transferTo(0, inChannel.size(), outChannel);
	        }
	      }
	      finally {
	        if (inChannel != null) inChannel.close();
	        if (outChannel != null) outChannel.close();
	        if (inStream != null) inStream.close();
	        if (outStream != null) outStream.close();
	      }
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