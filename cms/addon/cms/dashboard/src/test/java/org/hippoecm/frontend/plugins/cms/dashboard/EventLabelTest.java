/*
 *  Copyright 2009 Hippo.
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
package org.hippoecm.frontend.plugins.cms.dashboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Calendar;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.hippoecm.frontend.PluginTest;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.repository.api.HippoNodeType;
import org.junit.Before;
import org.junit.Test;

public class EventLabelTest extends PluginTest {

    static class TestLabel extends Label {
        private static final long serialVersionUID = 1L;

        public TestLabel(IModel model) {
            super("label", model);
        }

    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        root.addNode("test", "nt:unstructured");
    }

    Node createEventNode(Long timestamp, String method, String user) throws NoSuchNodeTypeException, LockException,
            VersionException, ConstraintViolationException, RepositoryException {
        return createEventNode(timestamp, method, user, new Value[0]);
    }

    Node createEventNode(Long timestamp, String method, String user, Value[] arguments) throws ItemExistsException,
            PathNotFoundException, NoSuchNodeTypeException, LockException, VersionException,
            ConstraintViolationException, RepositoryException {
        Node node = root.getNode("test").addNode(timestamp.toString(), "hippolog:item");
        node.setProperty("hippolog:timestamp", timestamp);
        node.setProperty("hippolog:eventClass", EventLabelTest.class.getName());
        node.setProperty("hippolog:eventMethod", method);
        node.setProperty("hippolog:eventUser", user);
        node.setProperty("hippolog:eventArguments", arguments);
        return node;
    }

    @Test
    public void testEventWithoutDocument() throws Exception {
        Long timestamp = Calendar.getInstance().getTimeInMillis();
        Node eventNode = createEventNode(timestamp, "testMethod", "testUser");

        DocumentEvent parser = new DocumentEvent(new JcrNodeModel(eventNode));
        assertNull(parser.getDocumentPath());

        EventModel label = new EventModel(new JcrNodeModel(eventNode));
        TestLabel testLabel = new TestLabel(label);
        assertEquals("One minute ago, testUser called test method", testLabel.getModelObject());
    }

    @Test
    public void testEventWithSource() throws Exception {
        Node docNode = root.getNode("test").addNode("testDocument");
        Long timestamp = Calendar.getInstance().getTimeInMillis();
        Node eventNode = createEventNode(timestamp, "testDocumentMethod", "testUser");
        eventNode.setProperty("hippolog:eventDocument", docNode.getPath());

        DocumentEvent parser = new DocumentEvent(new JcrNodeModel(eventNode));
        assertEquals("/test/testDocument", parser.getDocumentPath());

        EventModel label = new EventModel(new JcrNodeModel(eventNode), new JcrNodeModel(parser.getDocumentPath()));
        TestLabel testLabel = new TestLabel(label);
        assertEquals("One minute ago, testUser called test method on testDocument", testLabel.getModelObject());
    }

    @Test
    public void testEventWithTarget() throws Exception {
        Node docNode = root.getNode("test").addNode("testDocument");
        docNode.addMixin("mix:referenceable");
        session.save();

        Long timestamp = Calendar.getInstance().getTimeInMillis();
        Node eventNode = createEventNode(timestamp, "testDocumentMethod", "testUser");
        eventNode.setProperty("hippolog:eventReturnValue", "document[uuid=" + docNode.getUUID() + ",path='"
                + docNode.getPath() + "']");

        DocumentEvent parser = new DocumentEvent(new JcrNodeModel(eventNode));
        assertEquals("/test/testDocument", parser.getDocumentPath());

        EventModel label = new EventModel(new JcrNodeModel(eventNode), new JcrNodeModel(parser.getDocumentPath()));
        TestLabel testLabel = new TestLabel(label);
        assertEquals("One minute ago, testUser called test method on testDocument", testLabel.getModelObject());
    }

    @Test
    public void testWorkflowWithRemovedTarget() throws Exception {
        Node handleNode = root.getNode("test").addNode("testDocument", HippoNodeType.NT_HANDLE);
        handleNode.addMixin(HippoNodeType.NT_HARDHANDLE);
        Node docNode = handleNode.addNode("testDocument", HippoNodeType.NT_DOCUMENT);
        docNode.addMixin(HippoNodeType.NT_HARDDOCUMENT);
        session.save();

        Long timestamp = Calendar.getInstance().getTimeInMillis();
        Node eventNode = createEventNode(timestamp, "testDocumentMethod", "testUser");
        eventNode.setProperty("hippolog:eventReturnValue", "document[uuid=" + docNode.getUUID() + ",path='"
                + docNode.getPath() + "']");
        docNode.remove();
        session.save();

        DocumentEvent parser = new DocumentEvent(new JcrNodeModel(eventNode));
        String path = parser.getDocumentPath();
        assertEquals("/test/testDocument", path);

        EventModel label = new EventModel(new JcrNodeModel(eventNode), new JcrNodeModel(parser.getDocumentPath()));
        TestLabel testLabel = new TestLabel(label);
        assertEquals("One minute ago, testUser called test method on testDocument", testLabel.getModelObject());
    }

}
