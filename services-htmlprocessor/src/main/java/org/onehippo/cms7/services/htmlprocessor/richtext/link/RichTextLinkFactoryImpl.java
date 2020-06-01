/*
 *  Copyright 2010-2017 Hippo B.V. (http://www.onehippo.com)
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
package org.onehippo.cms7.services.htmlprocessor.richtext.link;

import java.util.Set;
import java.util.TreeSet;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.hippoecm.repository.api.HippoNodeType;
import org.hippoecm.repository.api.NodeNameCodec;
import org.hippoecm.repository.util.JcrUtils;
import org.onehippo.cms7.services.htmlprocessor.model.Model;
import org.onehippo.cms7.services.htmlprocessor.util.FacetUtil;
import org.onehippo.cms7.services.htmlprocessor.util.StringUtil;
import org.onehippo.cms7.services.htmlprocessor.richtext.RichTextException;
import org.onehippo.cms7.services.htmlprocessor.richtext.jcr.NodeFactory;
import org.onehippo.repository.util.JcrConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RichTextLinkFactoryImpl implements RichTextLinkFactory {

    private final static Logger log = LoggerFactory.getLogger(RichTextLinkFactoryImpl.class);

    private final Model<Node> nodeModel;
    private final NodeFactory nodeFactory;

    private transient Set<String> links = null;

    public RichTextLinkFactoryImpl(final Model<Node> nodeModel, final NodeFactory nodeFactory) {
        this.nodeModel = nodeModel;
        this.nodeFactory = nodeFactory;
    }

    @Override
    public RichTextLink loadLink(final String uuid) throws RichTextException {
        if (StringUtil.isEmpty(uuid)) {
            throw new IllegalArgumentException("Link path is empty");
        }
        final Node node = nodeModel.get();
        try {
            final Model<Node> model = nodeFactory.getNodeModelByIdentifier(uuid);
            return new RichTextLink(model, uuid);
        } catch (final RepositoryException e) {
            throw new RichTextException("Rich text link factory for node '" + JcrUtils.getNodePathQuietly(node) + "'"
                    + " cannot find linked node with UUID '" + uuid + "'", e);
        }
    }

    @Override
    public RichTextLink createLink(final Model<Node> targetModel) throws RichTextException {
        if (targetModel == null) {
            throw new IllegalArgumentException("Target is null");
        }
        try {
            final Node targetNode = targetModel.get();
            if (targetNode == null) {
                throw new RichTextException("Link target node does not exist");
            }
            final String name = NodeNameCodec.encode(targetNode.getName());
            final Node node = nodeModel.get();
            FacetUtil.createFacet(node, name, targetNode);
            final String targetUuid = targetNode.getIdentifier();
            return new RichTextLink(targetModel, targetUuid);
        } catch (final RepositoryException e) {
            throw new RichTextException("Could not create link", e);
        }
    }

    @Override
    public boolean isValid(final Model<Node> targetModel) {
        if (targetModel == null) {
            return false;
        }
        final Node node = targetModel.get();
        if (node == null) {
            return false;
        }
        try {
            return node.isNodeType(JcrConstants.MIX_REFERENCEABLE);
        } catch (final RepositoryException e) {
            log.error(e.getMessage());
            return false;
        }
    }

    @Override
    public Set<String> getLinkUuids() {
        if (links == null) {
            links = new TreeSet<>();
            try {
                final NodeIterator iter = nodeModel.get().getNodes();
                while (iter.hasNext()) {
                    final Node child = iter.nextNode();
                    if (child.isNodeType(HippoNodeType.NT_FACETSELECT)) {
                        final String uuid = child.getProperty(HippoNodeType.HIPPO_DOCBASE).getString();
                        links.add(uuid);
                    }
                }
            } catch (final RepositoryException ex) {
                log.error("Error retrieving links", ex);
            }
        }
        return links;
    }

    @Override
    public void release() {
        nodeModel.release();
        links = null;
    }
}
