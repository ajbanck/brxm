/*
 * Copyright 2007 Hippo
 *
 * Licensed under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hippoecm.repository.servicing;

import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

import javax.jcr.NamespaceException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;
import javax.jcr.Workspace;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.QueryManager;
import javax.jcr.version.VersionException;

import org.apache.jackrabbit.core.nodetype.NodeTypeManagerImpl;
import org.apache.jackrabbit.core.nodetype.NodeTypeRegistry;
import org.apache.jackrabbit.spi.Name;
import org.hippoecm.repository.api.HippoNodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Remodeling
{
    protected final static Logger log = LoggerFactory.getLogger(Remodeling.class);

    /** The prefix of the namespace which has been changed
     */
    private String prefix;

    /** Paths to the changed nodes.
     */
    private Set<Node> changes;

    /** Reference to the session in which the changes are prepared
     */
    transient Session session;

    Remodeling(Session session, String prefix) throws RepositoryException {
        this.session = session;
        this.prefix = prefix;
        changes = new TreeSet<Node>();
    }

    public NodeIterator getNodes() {
        return new ChangedNodesIterator();
    }

    private class ChangedNodesIterator implements NodeIterator {
        Iterator<Node> iter;
        int index;
        ChangedNodesIterator() {
            iter = changes.iterator();
            index = 0;
        }
        public Node nextNode() {
            Node node = iter.next();
            ++index;
            return node;
        }
        public boolean hasNext() {
            return iter.hasNext();
        }
        public Object next() throws NoSuchElementException {
            Object object = iter.next();
            ++index;
            return object;
        }
        public void remove() throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }
        public void skip(long skipNum) {
            while(skipNum-- > 0) {
                iter.next();
                ++index;
            }
        }
        public long getSize() {
            return changes.size();
        }
        public long getPosition() {
            return index;
        }
    }

    protected void visit(Node source, Node target) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
        for(PropertyIterator iter = source.getProperties(); iter.hasNext(); ) {
            Property prop = iter.nextProperty();
            if(!prop.getDefinition().isProtected()) {
                target.setProperty(prop.getName(), prop.getValue());
            }
        }
    }

    protected void traverse(Set<String> types, Node node, boolean copy, Node target) throws RepositoryException {
        if(copy) {
            visit(node, target);
        }
        for(NodeIterator iter = node.getNodes(); iter.hasNext(); ) {
            Node child = iter.nextNode();
            NodeType nodeType = child.getPrimaryNodeType();
            boolean found = false;
            for(Iterator<String> find = types.iterator(); find.hasNext(); ) {
                String type = (String) find.next();
                if(nodeType.isNodeType(type)) {
                    found = true;
                    break;
                }
            }
            if(found) {
                Node newChild = target.addNode(child.getName(),
                                               prefix + nodeType.getName().substring(nodeType.getName().indexOf(":")));
                changes.add(newChild);
                traverse(types, child, true, newChild);
                if(!copy)
                    child.remove(); // iter.remove();
            } else if(copy) {
                Node newChild = target.addNode(child.getName(), nodeType.getName());
                traverse(types, child, true, newChild);
            } else {
                traverse(types, child, false, child);
            }
        }
    }

    public static Remodeling remodel(Session session, String prefix, InputStream cnd) throws NamespaceException, RepositoryException {
        Workspace workspace = session.getWorkspace();
        NamespaceRegistry nsreg = workspace.getNamespaceRegistry();
        NodeTypeManagerImpl ntmgr = (NodeTypeManagerImpl) workspace.getNodeTypeManager();
        NodeTypeRegistry ntreg = ntmgr.getNodeTypeRegistry();
        QueryManager qmgr = workspace.getQueryManager();

        // obtain namespace URI for prefix as in use
        String oldNamespaceURI = nsreg.getURI(prefix);

        // compute namespace URI for new model to be used
        int pos = oldNamespaceURI.lastIndexOf("/");
        if(pos < 0)
            throw new RepositoryException("Internal error; invalid namespace URI found in repository itself");
        if(oldNamespaceURI.lastIndexOf(".") > pos)
            pos = oldNamespaceURI.lastIndexOf(".");
        int newNamespaceVersion = Integer.parseInt(oldNamespaceURI.substring(pos+1));
        ++newNamespaceVersion;
        String newNamespaceURI = oldNamespaceURI.substring(0,pos+1) + newNamespaceVersion;

        // push new node type definition such that it will be loaded
        try {
            Node base = session.getRootNode().getNode(HippoNodeType.CONFIGURATION_PATH).getNode(HippoNodeType.INITIALIZE_PATH);
            Node node = base.getNode(prefix);
            node.setProperty(HippoNodeType.HIPPO_NAMESPACE, newNamespaceURI);
            node.setProperty(HippoNodeType.HIPPO_NODETYPES, cnd);
            session.save();

            // wait for node types to be reloaded
            session.refresh(true);
            while(base.getNode(prefix).hasProperty(HippoNodeType.HIPPO_NODETYPES) ||
                  base.getNode(prefix).hasProperty(HippoNodeType.HIPPO_NODETYPESRESOURCE)) {
                try {
                    Thread.sleep(500);
                } catch(InterruptedException ex) {
                }
                session.refresh(true);
            }
        } catch(ConstraintViolationException ex) {
            throw new RepositoryException("Hippo repository configuration not in order");
        } catch(LockException ex) {
            throw new RepositoryException("Hippo repository configuration not in order");
        } catch(ValueFormatException ex) {
            throw new RepositoryException("Hippo repository configuration not in order");
        } catch(VersionException ex) {
            throw new RepositoryException("Hippo repository configuration not in order");
        } catch(PathNotFoundException ex) {
            throw new RepositoryException("Hippo repository configuration not in order");
        }

        // compute old prefix, similar as in LocalHippoResository.initializeNamespace(NamespaceRegistry,String,uri)
        String oldPrefix = oldNamespaceURI.substring(oldNamespaceURI.lastIndexOf("/")+1);
        oldPrefix = prefix + "_" + oldPrefix;

        Set<Node> newNodes = new TreeSet<Node>();
        Set<String> changedNodeTypes = new TreeSet<String>();
        Name[] allNodeTypes = ntreg.getRegisteredNodeTypes();
        for(int i=0; i<allNodeTypes.length; i++) {
            if(allNodeTypes[i].getNamespaceURI().equals(oldNamespaceURI)) {
                changedNodeTypes.add(oldPrefix + ":" + allNodeTypes[i].getLocalName());
            }
        }
        Remodeling remodel = new Remodeling(session, prefix);
        remodel.traverse(changedNodeTypes, session.getRootNode(), false, session.getRootNode());
        return remodel;
    }
}
