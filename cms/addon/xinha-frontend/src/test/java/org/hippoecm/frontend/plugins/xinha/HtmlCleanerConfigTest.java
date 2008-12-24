/*
 *  Copyright 2008 Hippo.
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
package org.hippoecm.frontend.plugins.xinha;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Map;

import javax.jcr.Node;

import junit.framework.Assert;
import nl.hippo.htmlcleaner.ElementDescriptor;
import nl.hippo.htmlcleaner.HtmlCleaner;
import nl.hippo.htmlcleaner.HtmlCleanerTemplate;
import nl.hippo.htmlcleaner.OutputElementDescriptor;

import org.apache.wicket.util.io.IOUtils;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.plugin.config.impl.JcrPluginConfig;
import org.hippoecm.frontend.plugins.xinha.htmlcleaner.JCRHtmlCleanerTemplateBuilder;
import org.hippoecm.repository.TestCase;
import org.hippoecm.repository.api.WorkflowManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HtmlCleanerConfigTest extends TestCase {
	@SuppressWarnings("unused")
	private final static String SVN_ID = "$Id$";

	Node root, cleanerConfigNode;
	WorkflowManager manager;

	@Before
	public void setUp() throws Exception {
		super.setUp(true);
		root = session.getRootNode();

	}

	@After
	public void tearDown() throws Exception {
		super.tearDown();
	}

	protected IPluginConfig getPluginConfig() throws Exception {
		Node cleanerConfigNode = root
				.getNode("cleaner.config");
		JcrNodeModel nodeModel = new JcrNodeModel(cleanerConfigNode);
		return new JcrPluginConfig(nodeModel);
	}

	@Test
	public void testHtmlCleanerTemplateBuilder() throws Exception {
		JCRHtmlCleanerTemplateBuilder builder = new JCRHtmlCleanerTemplateBuilder();
		
		HtmlCleanerTemplate template = builder.buildTemplate(getPluginConfig());
		
		Assert.assertEquals(JCRHtmlCleanerTemplateBuilder.SCHEMA_TRANSITIONAL,template.getXhtmlSchema());
		Assert.assertEquals(5,template.getAllowedSpanClasses().size());
        Assert.assertEquals(0,template.getAllowedDivClasses().size());
        Assert.assertEquals(3,template.getAllowedParaClasses().size());
        Assert.assertEquals(3,template.getAllowedPreClasses().size());
        
        Assert.assertNull(template.getImgAlternateSrcAttr());
        
        Map descriptors = template.getDescriptors();
        Assert.assertEquals(32,descriptors.size());

        ElementDescriptor span = (ElementDescriptor) descriptors.get("span");
        Assert.assertNotNull(span);
        Assert.assertEquals(2,span.getAttributeNames().length);
        
        Map outputElements = template.getOutputElementDescriptors();
        Assert.assertNotNull(outputElements);
        
        Assert.assertEquals(17, outputElements.size());
        
        OutputElementDescriptor html = (OutputElementDescriptor) outputElements.get("html");
        Assert.assertNotNull(html);
        Assert.assertEquals(1, html.getNewLinesAfterOpenTag());
        Assert.assertEquals(1, html.getNewLinesBeforeCloseTag());
        Assert.assertEquals(0, html.getNewLinesAfterCloseTag());
        Assert.assertEquals(0, html.getNewLinesBeforeOpenTag());
        Assert.assertEquals(JCRHtmlCleanerTemplateBuilder.DEFAULT_INLINE, html.isInline());
        
        Assert.assertEquals(80,template.getMaxLineWidth());
        
	}

}
