/*
 *  Copyright 2008-2013 Hippo B.V. (http://www.onehippo.com)
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
package org.hippoecm.frontend.plugins.richtext.jcr;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.hippoecm.frontend.PluginTest;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.model.properties.JcrPropertyModel;
import org.hippoecm.frontend.model.properties.JcrPropertyValueModel;
import org.hippoecm.frontend.plugins.richtext.RichTextException;
import org.hippoecm.frontend.plugins.richtext.RichTextLink;
import org.hippoecm.frontend.plugins.richtext.RichTextModel;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JcrRichTextLinkFactoryTest extends PluginTest {

    String[] content = {
            "/test", "nt:unstructured",
                "/test/target", "hippo:handle",
                    "jcr:mixinTypes", "mix:referenceable",
                    "/test/target/target", "hippo:document",
                        "jcr:mixinTypes", "hippo:harddocument",
                "/test/source", "hippo:handle",
                    "jcr:mixinTypes", "mix:referenceable",
                    "/test/source/source", "richtexttest:testdocument",
                        "jcr:mixinTypes", "hippo:harddocument",
                        "/test/source/source/richtexttest:html", "hippostd:html",
                            "hippostd:content", "testing 1 2 3"
    };

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        build(session, content);
        session.save();
    }

    @Test
    public void linkLifecycleTest() throws RichTextException, RepositoryException {
        Node html = root.getNode("test/source/source/richtexttest:html");
        JcrRichTextLinkFactory factory = new JcrRichTextLinkFactory(new JcrNodeModel(html));
        Node target = root.getNode("test/target");
        RichTextLink link = factory.createLink(new JcrNodeModel(target));

        assertTrue(root.hasNode("test/source/source/richtexttest:html/target"));

        RichTextModel model = new RichTextModel(new JcrPropertyValueModel(new JcrPropertyModel(html.getProperty("hippostd:content"))));
        model.setLinkFactory(factory);
        model.setObject(model.getObject());
        assertFalse(root.hasNode("test/source/source/richtexttest:html/target"));
    }

}
