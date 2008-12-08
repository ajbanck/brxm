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
package org.hippoecm.repository.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.jcr.InvalidSerializedDataException;
import javax.jcr.ItemExistsException;
import javax.jcr.NamespaceException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;

public interface HippoSession extends Session {
    final static String SVN_ID = "$Id$";

    public Node copy(Node original, String absPath) throws RepositoryException;

    /**
     * Obtains an iterator over the set of nodes that potentially contain
     * changes, starting (and not including) the indicated node.
     * Only nodes for which <code>Node.isNodeType(nodeType)</code> returns
     * true are included in the resulting set.  If the prune boolean value is
     * true, then the nodes matching in the hierarchy first are returned.  If
     * matching modified node exists beneath the nodes, these are not
     * included.
     *
     * @param node The starting node for which to look for changes, will not
     *             be included in result
     * @param nodeType Only nodes that are (derived) of this nodeType are
     *                 included in the result
     * @param prune Wheter only to return the first matching modified node in
     *              a subtree (true), or provide a depth search for all modified
     *              nodes (false)
     * @returns A NodeIterator instance which iterates over all modified
     *          nodes, not including the passed node
     */
    public NodeIterator pendingChanges(Node node, String nodeType, boolean prune) throws NamespaceException,
                                                                           NoSuchNodeTypeException, RepositoryException;

    /** Conveniance method for
     * <code>pendingChanges(Session.getRootNode(),nodeType,false)</code>
     *
     * @param node The starting node for which to look for changes, will not
     *             be included in result
     * @param nodeType Only nodes that are (derived) of this nodeType are
     *                 included in the result
     * @returns A NodeIterator instance which iterates over all modified
     *          nodes, not including the passed node
     * @see pendingChanges(Node,String,boolean)
     */
    public NodeIterator pendingChanges(Node node, String nodeType) throws NamespaceException, NoSuchNodeTypeException,
                                                                          RepositoryException;


    /** Largely a conveniance method for
     * <code>pendingChanges(Session.getRootNode(), "nt:base", false)</code> however
     * will also return the root node if modified.
     *
     * @returns A NodeIterator instance which iterates over all modified nodes, including the root
     * @see pendingChanges(Node,String,boolean)
     */
    public NodeIterator pendingChanges() throws RepositoryException;

    /**
     * DO NOT USE: api of this method has not yet stabilized!
     * Export a dereferenced view of a node. Node references will be rewritten to
     * absolute paths and some auto generated properties will be stripped, such as
     * versioning properties and hippo:paths. In every other way the method is
     * similar to <link>avax.jcr.Session.exportSystemView</link>.
     * @see org.hippoecm.repository.jackrabbit.xml.HippoSysViewSAXEventGenerator
     * @see javax.jcr.Session.exportSystemView(String,ContentHandler,boolean,boolean)
     */
//    public void exportDereferencedView(String absPath, ContentHandler contentHandler, boolean binaryAsLink,
//            boolean noRecurse) throws PathNotFoundException, SAXException, RepositoryException;

    /**
     * DO NOT USE: api of this method has not yet stabilized!
     * Export a dereferenced view of a node.
     * @see exportDereferencedView(String,ContentHandler,boolean,boolean)
     * @see org.hippoecm.repository.jackrabbit.xml.HippoSysViewSAXEventGenerator
     * @see javax.jcr.Session.exportSystemView(String,OutputStream,boolean,boolean)
     */
    public void exportDereferencedView(String absPath, OutputStream out, boolean binaryAsLink, boolean noRecurse)
            throws IOException, PathNotFoundException, RepositoryException;

    /**
     * DO NOT USE: api of this method has not yet stabilized!
     * Import a dereferenced export
     * @see exportDereferencedView(String,ContentHandler,boolean,boolean)
     * @see javax.jcr.Session.importXML(String,ContentHandler,boolean)
     * @see org.hippoecm.repository.api.ImportReferenceBehavior
     * @see org.hippoecm.repository.api.ImportMergeBehavior
     */
    public void importDereferencedXML(String parentAbsPath, InputStream in, int uuidBehavior, int referenceBehavior,
            int mergeBehavior) throws IOException, PathNotFoundException, ItemExistsException,
            ConstraintViolationException, VersionException, InvalidSerializedDataException, LockException,
            RepositoryException;

    /**
     * DO NOT USE: api of this method has not yet stabilized!
     * Get the content handler for a dereferenced export
     * @see importDerereferencedXML(String,InputStream,int,int)
     * @see javax.jcr.Session.importXML(String,ContentHandler,boolean)
     * @see org.hippoecm.repository.api.ImportReferenceBehavior
     * @see org.hippoecm.repository.api.ImportMergeBehavior
     */
//    public ContentHandler getDereferencedImportContentHandler(String parentAbsPath, int uuidBehavior,
//            int referenceBehavior, int mergeBehavior) throws PathNotFoundException, ConstraintViolationException,
//            VersionException, LockException, RepositoryException;

    /**
     * FIXME WARNING this call is not yet part of the API.
     * Probably it will change into getSessionClassLoader(Node) or similar
     * Get a classloader which uses the JCR  repository to load the classes from
     */
    public ClassLoader getSessionClassLoader() throws RepositoryException;
}
