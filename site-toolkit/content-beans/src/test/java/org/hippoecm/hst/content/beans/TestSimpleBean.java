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
package org.hippoecm.hst.content.beans;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.jcr.Credentials;
import javax.jcr.Repository;
import javax.jcr.Session;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.collections.list.TreeList;
import org.hippoecm.hst.AbstractBeanSpringTestCase;
import org.hippoecm.hst.content.beans.manager.ObjectBeanManager;
import org.hippoecm.hst.content.beans.manager.ObjectBeanManagerImpl;
import org.hippoecm.hst.content.beans.manager.ObjectConverter;
import org.hippoecm.hst.content.beans.manager.ObjectConverterImpl;
import org.hippoecm.hst.content.beans.standard.HippoDirectory;
import org.hippoecm.hst.content.beans.standard.HippoDocument;
import org.hippoecm.hst.content.beans.standard.HippoFacetSearch;
import org.hippoecm.hst.content.beans.standard.HippoFacetSelect;
import org.hippoecm.hst.content.beans.standard.HippoFixedDirectory;
import org.hippoecm.hst.content.beans.standard.HippoFolder;
import org.hippoecm.hst.content.beans.standard.HippoHtml;
import org.hippoecm.hst.content.beans.standard.HippoItem;
import org.hippoecm.hst.util.DefaultKeyValue;
import org.hippoecm.hst.util.KeyValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestSimpleBean extends AbstractBeanSpringTestCase {

    protected Object repository;
    protected Credentials defaultCredentials;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        
        this.repository = getComponent(Repository.class.getName());
        this.defaultCredentials = getComponent(Credentials.class.getName());
    }
    
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        
        if (this.repository != null) {
            MethodUtils.invokeMethod(this.repository, "close", null);
        }
    }

    @Test
    public void testSimpleObjectGetting() throws Exception {
        
        // builds ordered mapping from jcrPrimaryNodeType to class or interface(s).
        List<KeyValue<String, Class[]>> jcrPrimaryNodeTypeClassPairs = new TreeList();


        addJcrPrimaryNodeTypeClassPair(jcrPrimaryNodeTypeClassPairs, SimpleTextPage.class);
        addJcrPrimaryNodeTypeClassPair(jcrPrimaryNodeTypeClassPairs, SimpleTextPageCopy.class);
        addJcrPrimaryNodeTypeClassPair(jcrPrimaryNodeTypeClassPairs, HippoDocument.class);
        addJcrPrimaryNodeTypeClassPair(jcrPrimaryNodeTypeClassPairs, HippoFolder.class);
        addJcrPrimaryNodeTypeClassPair(jcrPrimaryNodeTypeClassPairs, HippoFacetSearch.class);
        addJcrPrimaryNodeTypeClassPair(jcrPrimaryNodeTypeClassPairs, HippoFacetSelect.class);
        addJcrPrimaryNodeTypeClassPair(jcrPrimaryNodeTypeClassPairs, HippoDirectory.class);
        addJcrPrimaryNodeTypeClassPair(jcrPrimaryNodeTypeClassPairs, HippoFixedDirectory.class);
        addJcrPrimaryNodeTypeClassPair(jcrPrimaryNodeTypeClassPairs, HippoHtml.class);
        
        // builds a fallback jcrPrimaryNodeType array.
        String [] fallBackJcrPrimaryNodeTypes = new String [] {"hippo:document"};
        
        ObjectConverter objectConverter = new ObjectConverterImpl(jcrPrimaryNodeTypeClassPairs, fallBackJcrPrimaryNodeTypes);
        
        Session session = (Session) MethodUtils.invokeMethod(this.repository, "login", this.defaultCredentials);
        ObjectBeanManager obm = new ObjectBeanManagerImpl(session, objectConverter);

        HippoFolder folder = (HippoFolder) obm.getObject("/testcontent/testproject/Products");
        
     
        Object o = obm.getObject("/testcontent/testproject/Products/SomeProduct");
        assertTrue(" Object should be an instance of SimpleTextPage and not SimpleTextPageCopy, because SimpleTextPage is added first", o instanceof SimpleTextPage);
        
        SimpleTextPage productsPage =  (SimpleTextPage)obm.getObject("/testcontent/testproject/Products/SomeProduct");
        SimpleTextPage productsPage2 = (SimpleTextPage) obm.getObject("/testcontent/testproject/Products/SomeProduct/SomeProduct");

        assertTrue("Handle and Document should return true for equalCompare ", productsPage.equalCompare(productsPage2));
        assertFalse("Folder and Document should return false for equalCompare ",folder.equalCompare(productsPage2));
        
        assertNotNull(productsPage);
        assertNotNull(productsPage.getNode());
        
        System.out.println("node: " + productsPage.getNode());
        System.out.println("path: " + productsPage.getPath());
        System.out.println("title: " + productsPage.getTitle());
        System.out.println("body: " + productsPage.getBody().getContent());
        
        session.logout();
    }
    
    private static void addJcrPrimaryNodeTypeClassPair(List<KeyValue<String, Class[]>> jcrPrimaryNodeTypeClassPairs, Class clazz) {
        String jcrPrimaryNodeType = null;
        
        if (clazz.isAnnotationPresent(Node.class)) {
            Node anno = (Node) clazz.getAnnotation(Node.class);
            jcrPrimaryNodeType = anno.jcrType();
        }
        
        if (jcrPrimaryNodeType == null) {
            throw new IllegalArgumentException("There's no annotation for jcrType in the class: " + clazz);
        }
        
        jcrPrimaryNodeTypeClassPairs.add(new DefaultKeyValue(jcrPrimaryNodeType, new Class [] { clazz }, true));
    }
}
