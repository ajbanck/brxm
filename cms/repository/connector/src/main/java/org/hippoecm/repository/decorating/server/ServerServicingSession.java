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
package org.hippoecm.repository.decorating.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;

import javax.jcr.NamespaceException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NoSuchNodeTypeException;

import org.apache.jackrabbit.rmi.remote.RemoteIterator;
import org.apache.jackrabbit.rmi.remote.RemoteNode;
import org.apache.jackrabbit.rmi.server.ServerSession;
import org.hippoecm.repository.api.HippoSession;
import org.hippoecm.repository.decorating.remote.RemoteServicingSession;

public class ServerServicingSession extends ServerSession implements RemoteServicingSession {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";

    private HippoSession session;

    public ServerServicingSession(Session session, RemoteServicingAdapterFactory factory) throws RemoteException {
        super(session, factory);
        this.session = (HippoSession) session;
    }

    public RemoteNode copy(String originalPath, String absPath) throws RepositoryException, RemoteException {
        try {
            return getRemoteNode(session.copy(session.getRootNode().getNode(originalPath.substring(1)), absPath));
        } catch (RepositoryException ex) {
            throw getRepositoryException(ex);
        }
    }

    public RemoteIterator pendingChanges(String absPath, String nodeType, boolean prune) throws NamespaceException,
                                                                 NoSuchNodeTypeException, RepositoryException, RemoteException {
        try {
            Node node = (absPath == null || "/".equals(absPath)) ? session.getRootNode()
                                                                 : session.getRootNode().getNode(absPath.substring(1)); 
            return getFactory().getRemoteNodeIterator(session.pendingChanges(node, nodeType, prune));
        } catch (NamespaceException ex) {
            throw getRepositoryException(ex);
        } catch (NoSuchNodeTypeException ex) {
            throw getRepositoryException(ex);
        } catch (RepositoryException ex) {
            throw getRepositoryException(ex);
        }
    }

    public byte[] exportDereferencedView(String path, boolean binaryAsLink, boolean noRecurse)
            throws IOException, RepositoryException, RemoteException {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            session.exportDereferencedView(path, buffer, binaryAsLink, noRecurse);
            return buffer.toByteArray();
        } catch (RepositoryException ex) {
            throw getRepositoryException(ex);
        }
    }
    
    public void importDereferencedXML(String path, byte[] xml, int uuidBehavior, int referenceBehavior,
            int mergeBehavior) throws IOException, RepositoryException, RemoteException {
        try {
            session.importDereferencedXML(path, new ByteArrayInputStream(xml), uuidBehavior, referenceBehavior, mergeBehavior);
        } catch (RepositoryException ex) {
            throw getRepositoryException(ex);
        }
    }
}
