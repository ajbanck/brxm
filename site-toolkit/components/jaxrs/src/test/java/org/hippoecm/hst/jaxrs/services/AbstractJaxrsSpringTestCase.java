/*
 *  Copyright 2010-2015 Hippo B.V. (http://www.onehippo.com)
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.hippoecm.hst.jaxrs.services;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.hippoecm.hst.container.ModifiableRequestContextProvider;
import org.hippoecm.hst.core.container.ComponentManager;
import org.hippoecm.hst.platform.model.HstModelRegistry;
import org.hippoecm.hst.site.HstServices;
import org.hippoecm.hst.site.container.SpringComponentManager;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockServletContext;

/**
 * <p>
 * AbstractJaxrsSpringTestCase
 * </p>
 * @version $Id$
 */
public abstract class AbstractJaxrsSpringTestCase
{

    protected final static Logger log = LoggerFactory.getLogger(AbstractJaxrsSpringTestCase.class);
    
    protected ComponentManager componentManager;

    @BeforeClass
    public static void setUpClass() throws Exception {
        //Enable legacy project structure mode (without extensions)
        System.setProperty("use.hcm.sites", "false");
    }

    @Before
    public void setUp() throws Exception {
        final Configuration containerConfiguration = getContainerConfiguration();
        containerConfiguration.addProperty("hst.configuration.rootPath", "/hst:hst");
        componentManager = new SpringComponentManager(containerConfiguration);
        componentManager.setConfigurationResources(getConfigurations());
        final MockServletContext servletContext = new MockServletContext();
        servletContext.setContextPath("/site");
        componentManager.setServletContext(servletContext);
        componentManager.initialize();
        componentManager.start();
        HstServices.setComponentManager(getComponentManager());

        final HstModelRegistry modelRegistry = componentManager.getComponent(HstModelRegistry.class);
        modelRegistry.registerHstModel("/site", componentManager, true);
    }

    @After
    public void tearDown() throws Exception {
        this.componentManager.stop();
        this.componentManager.close();
        ModifiableRequestContextProvider.clear();
        HstServices.setComponentManager(null);
    }

    /**
     * required specification of spring configurations
     * the derived class can override this.
     */
    protected String[] getConfigurations() {
        String classXmlFileName = getClass().getName().replace(".", "/") + ".xml";
        String classXmlFileName2 = getClass().getName().replace(".", "/") + "-*.xml";
        return new String[] { classXmlFileName, classXmlFileName2 };
    }
    
    protected ComponentManager getComponentManager() {
        return this.componentManager;
    }

    protected <T> T getComponent(String name) {
        return getComponentManager().getComponent(name);
    }
    
    protected Configuration getContainerConfiguration() {
        return new PropertiesConfiguration();
    }
}
