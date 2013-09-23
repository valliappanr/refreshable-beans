package refreshable.service;

import javax.jms.JMSException;
import javax.jms.Message;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests for the {@link Helper} class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/spring/applicationContext.xml" })
public class RefreshableServiceSendTest {

	@Autowired
	private JmsTemplate jmsTemplate;
	
	@Test
	public void testSendMessage() {
		final String messageContent="10";
		jmsTemplate.convertAndSend((Object)messageContent,new MessagePostProcessor() {
	        public Message postProcessMessage(Message message) throws JMSException {
	            message.setStringProperty("CamelBeanMethodName", "execute");
	            return message;
	        }
	    });		
	}

}
