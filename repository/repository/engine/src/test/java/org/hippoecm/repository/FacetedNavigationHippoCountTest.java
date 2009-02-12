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
package org.hippoecm.repository;

import static org.junit.Assert.assertTrue;

import javax.jcr.RepositoryException;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;

public class FacetedNavigationHippoCountTest extends TestCase
{
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";

    private static String[] contents1 = new String[] {
        "/test", "nt:unstructured",
        "/test/documents", "nt:unstructured",
        "jcr:mixinTypes", "mix:referenceable",
        "/test/documents/document", "hippo:testdocument",
        "type", "text",
        "/test/documents/document1", "hippo:testdocument",
        "type", "html"
    };
    private static String[] contents2 = new String[] {
        "/test/docsearch", "nt:unstructured",
        "/test/docsearch/byType", "hippo:facetsearch",
        "hippo:docbase", "/test/documents",
        "hippo:queryname", "byType",
        "hippo:facets", "type"
    };

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testHippoCount() {
        try {
            build(session, contents1);
            session.save();
            build(session, contents2);
            session.save();
            assertTrue(traverse(session, "/test/docsearch/byType/hippo:resultset").hasProperty("hippo:count"));
        } catch(RepositoryException ex) {
            ex.printStackTrace();
        }
    }
}
