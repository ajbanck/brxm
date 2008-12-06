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

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.hippoecm.repository.api.HippoNodeType;
import org.hippoecm.repository.api.HippoSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CopyNodeTest extends TestCase {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testVirtualTreeCopy() throws RepositoryException {
        Node node, root = session.getRootNode().addNode("test","nt:unstructured");
        root.addMixin("mix:referenceable");
        node = root.addNode("documents");
        node = node.addNode("document","hippo:testdocument");
        node.addMixin("hippo:harddocument");
        node.setProperty("aap", "noot");
        session.save();
        node = root.addNode("navigation");
        node = node.addNode("search",HippoNodeType.NT_FACETSELECT);
        node.setProperty(HippoNodeType.HIPPO_DOCBASE, session.getRootNode().getNode("test").getUUID());
        node.setProperty(HippoNodeType.HIPPO_FACETS, new String[0]);
        node.setProperty(HippoNodeType.HIPPO_VALUES, new String[0]);
        node.setProperty(HippoNodeType.HIPPO_MODES, new String[0]);
        session.save();

        assertTrue(root.getNode("navigation").getNode("search").hasNode("documents"));
        assertTrue(root.getNode("navigation").getNode("search").getNode("documents").hasNode("document"));

        ((HippoSession)session).copy(root.getNode("navigation"), "/test/copy");
        session.save();
        session.refresh(false);

        assertTrue(root.getNode("copy").getNode("search").hasNode("documents"));
        assertTrue(root.getNode("copy").getNode("search").getNode("documents").hasNode("document"));
    }
}
