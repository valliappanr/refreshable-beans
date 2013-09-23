/*******************************************************************************
 * Copyright (c) 2010 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Eisenberg     - Initial API and implementation
 *******************************************************************************/
package refreshable.service;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import refreshable.context.wrapper.DynamicBeanRegistry;



/**
 * Tests for the {@link Helper} class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/spring/applicationContext.xml" })
public class RefreshableServiceTest{
	
	@Autowired
	private DynamicBeanRegistry beanRegistry;

	@Test
    public void testCheckRewards() throws Exception {
		beanRegistry.registerDynamicBean("fibonacci", 
				"/tmp/scripts/Fibonacci.groovy",
				"5000");
      Thread.sleep(100000000);
    }
}
