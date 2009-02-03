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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.rmi.RemoteException;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.hippoecm.repository.api.HippoWorkspace;
import org.hippoecm.repository.api.Workflow;
import org.hippoecm.repository.api.WorkflowException;
import org.hippoecm.repository.api.WorkflowManager;
import org.hippoecm.repository.standardworkflow.EditmodelWorkflow;
import org.junit.Test;

public class HREPTWO2182IssueTest extends SpiTestCase {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";

    @Test
    public void editType() throws RepositoryException, WorkflowException, RemoteException {

        HippoRepository repository = HippoRepositoryFactory.getHippoRepository("rmi://localhost:1099/hipporepository/spi");
        Session session = repository.login(new SimpleCredentials(SYSTEMUSER_ID, SYSTEMUSER_PASSWORD));

        Node root = session.getRootNode();
        Node typeNode = root.getNode("hippo:namespaces/editmodel/existing");

        // check initial conditions
        NodeIterator nodes = typeNode.getNode("hippo:nodetype").getNodes("hippo:nodetype");
        assertTrue(nodes.getSize() == 1);

        WorkflowManager workflowManager = ((HippoWorkspace) session.getWorkspace()).getWorkflowManager();
        Workflow workflow = workflowManager.getWorkflow("default", typeNode);
        assertTrue(workflow instanceof EditmodelWorkflow);

        ((EditmodelWorkflow) workflow).edit();
        session.refresh(false);

        nodes = typeNode.getNode("hippo:nodetype").getNodes("hippo:nodetype");
        // nodes.getSize() should return 2
        assertEquals(1, nodes.getSize());
    }

}
