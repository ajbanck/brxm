/*
 *  Copyright 2016-2017 Hippo B.V. (http://www.onehippo.com)
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
package org.onehippo.cm.migration;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.jcr.PropertyType;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.onehippo.cm.model.impl.ModuleImpl;
import org.onehippo.cm.model.impl.definition.ContentDefinitionImpl;
import org.onehippo.cm.model.impl.source.ConfigSourceImpl;
import org.onehippo.cm.model.impl.source.ContentSourceImpl;
import org.onehippo.cm.model.impl.source.SourceImpl;
import org.onehippo.cm.model.impl.tree.DefinitionNodeImpl;
import org.onehippo.cm.model.impl.tree.DefinitionPropertyImpl;
import org.onehippo.cm.model.tree.DefinitionNode;
import org.onehippo.cm.model.tree.PropertyOperation;
import org.onehippo.cm.model.tree.Value;
import org.onehippo.cm.model.tree.ValueType;
import org.onehippo.cm.model.util.SnsUtils;

import static org.apache.jackrabbit.JcrConstants.JCR_MIXINTYPES;
import static org.apache.jackrabbit.JcrConstants.JCR_PRIMARYTYPE;

public class SourceInitializeInstruction extends ContentInitializeInstruction {

    private static final String PATH_REFERENCE_POSTFIX = "___pathreference";

    private EsvNode sourceNode;

    public SourceInitializeInstruction(final EsvNode instructionNode, final Type type,
                                       final InitializeInstruction combinedWith, final String[] contentRoots,
                                       final Set<String> newContentRoots)
            throws EsvParseException
    {
        super(instructionNode, type, combinedWith, contentRoots, newContentRoots);
    }

    public EsvNode getSourceNode() {
        return sourceNode;
    }

    private String contentRoot;

    public void prepareSource(final EsvParser parser) throws IOException, EsvParseException {
        prepareResource(parser.getBaseDir(), true);
        setSourcePath(FilenameUtils.removeExtension(getResourcePath()) + ".yaml");

        try (final FileInputStream fileInputStream = new FileInputStream(getResource())) {
            sourceNode = parser.parse(fileInputStream, getResource().getCanonicalPath());
        }
        contentRoot = "/" + normalizePath(getPropertyValue("hippo:contentroot", PropertyType.STRING, true));
        setContentPath(contentRoot + "/" + sourceNode.getName());
        migrationFixes();
    }

    private void migrationFixes() throws EsvParseException {
        EsvNode eformsConfig = getContentNode("/hippo:configuration/hippo:modules/eforms/hippo:moduleconfig");
        if (eformsConfig != null) {
            // all eforms module config nodes must be of type hipposys:moduleconfig
            fixPrimaryTypes(eformsConfig, "hipposys:moduleconfig");
        }
    }

    private void fixPrimaryTypes(final EsvNode node, final String newType) {
        fixPrimaryType(node, newType);
        for (EsvNode child : node.getChildren()) {
            fixPrimaryTypes(child, newType);
        }
    }

    private void fixPrimaryType(final EsvNode node, final String newType) {
        EsvProperty prop = node.getProperty(JcrConstants.JCR_PRIMARYTYPE);
        if (prop != null && !newType.equals(prop.getValue())) {
            EsvValue value;
            if (prop.getValues().size() > 0) {
                value = prop.getValues().get(0);
            } else {
                value = new EsvValue(prop.getType(), false, node.getSourceLocation());
                prop.getValues().add(value);
            }
            value.setString(newType);
        }
    }

    private EsvNode getContentNode(final String path) {
        String[] elements = path.substring(1).split("/");
        String nodePath = "/" + elements[0];
        int index = 0;
        EsvNode node = null;
        while (getContentPath().startsWith(nodePath)) {
            if (elements.length > index+1) {
                if (getContentPath().startsWith(nodePath+"/"+elements[index+1])) {
                    index++;
                    nodePath = nodePath + "/" + elements[index];
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        if (getContentPath().equals(nodePath)) {
            index++;
            node = sourceNode;
            for (; index < elements.length; index++) {
                boolean found = false;
                for (EsvNode child : node.getChildren()) {
                    if (child.getName().equals(elements[index])) {
                        node = child;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    node = null;
                    break;
                }
            }
        }
        return node;
    }

    public boolean isDeltaMerge() {
        return sourceNode != null && sourceNode.isDeltaMerge();
    }

    public boolean isDeltaSkip() {
        return sourceNode != null && sourceNode.isDeltaSkip();
    }

    public void processSource(final ModuleImpl module,
                              final Map<MinimallyIndexedPath, DefinitionNodeImpl> nodeDefinitions,
                              final Set<DefinitionNode> deltaNodes) throws EsvParseException {
        final SourceImpl source;
        if (isContent(getContentPath(), true)) {
            source = module.addContentSource(getSourcePath());
        } else {
            source = module.addConfigSource(getSourcePath());
        }
        log.info("Processing " + getType().getPropertyName() + " " + getContentPath() + " from file " + getResourcePath());
        final String path = calculatePath(sourceNode, contentRoot, nodeDefinitions, Collections.emptyList());
        processNode(sourceNode, path, source, null, nodeDefinitions, deltaNodes);
    }

    /**
     * Calculates the target path for the given node, most importantly, determines if an SNS index for this node is
     * necessary.
     */
    private String calculatePath(final EsvNode node, final String parentPath,
                                 final Map<MinimallyIndexedPath, DefinitionNodeImpl> nodeDefinitions,
                                 final List<EsvNode> siblings) {

        final String base = StringUtils.appendIfMissing(parentPath, "/") + node.getName();
        if (node.getMerge() != null) {
            return base;
        }

        // No merge defined, pre-12 behavior was to create new SNS in case path already exists.
        // TODO: add capability to check whether another module defined a node at this path.
        DefinitionNodeImpl def = nodeDefinitions.get(new MinimallyIndexedPath(base));
        if (def != null) {
            if (def.isDeleted()) {
                return base;
            }
            int index = 2;
            while (nodeDefinitions.containsKey(new MinimallyIndexedPath(SnsUtils.createIndexedName(base, index)))) {
                index++;
            }
            return SnsUtils.createIndexedName(base, index);
        }

        // No earlier definitions at this path found, check if this node has siblings with the same name. If so, make
        // sure the first one gets an explicit index of 1.
        int count = 0;
        for (EsvNode sibling : siblings) {
            if (sibling.getName().equals(node.getName())) {
                count++;
            }
        }

        if (count > 1) {
            return SnsUtils.createIndexedName(base, 1);
        } else {
            return base;
        }
    }

    private void processNode(final EsvNode node, final String path, final SourceImpl source,
                             DefinitionNodeImpl parentNode,
                             final Map<MinimallyIndexedPath, DefinitionNodeImpl> nodeDefinitions,
                             final Set<DefinitionNode> deltaNodes) throws EsvParseException {

        DefinitionNodeImpl defNode = nodeDefinitions.get(new MinimallyIndexedPath(path));
        if (defNode != null && !defNode.isDelete()) {
            if (node.isDeltaSkip()) {
                log.warn("Skipping node " + path + " at " + node.getSourceLocation() + ": already defined before at " +
                        defNode.getSourceLocation() + ".");
                return;
            }
            if (!node.isDeltaMerge()) {
                throw new EsvParseException("Node " + path + " at " + node.getSourceLocation() + " already defined before at " +
                        defNode.getSourceLocation() + ".");
            }
        }
        final boolean newNode = defNode == null || defNode.isDeleted();
        if (newNode) {
            final boolean deleted = defNode != null && defNode.isDelete();
            if (deleted && defNode.isRoot()) {
                // earlier hippo:contentdelete
                deleteDefinition(defNode.getDefinition());
                defNode = null;
            }
            String parentPath = null;
            if (parentNode == null && node.getMerge() != null) {
                if (defNode != null) {
                    parentNode = defNode.getParent();
                } else {
                    parentPath = StringUtils.substringBeforeLast(path, "/");
                    if (parentPath.equals("")) {
                        parentPath = "/";
                    }
                    parentNode = nodeDefinitions.get(new MinimallyIndexedPath(parentPath));
                }
            }
            if (node.isDeltaInsert() && parentNode == null) {
                parentNode = addDeltaRootNode(source, parentPath, StringUtils.substringAfterLast(parentPath, "/"), nodeDefinitions, deltaNodes);
            }
            final String newNodeName = StringUtils.substringAfterLast(path, "/");
            if (parentNode != null) {
                defNode = parentNode.addNode(newNodeName);
            } else {
                ContentDefinitionImpl def;
                if (isContent(path)) {
                    def = ((ContentSourceImpl)source).addContentDefinition();
                } else {
                    def = ((ConfigSourceImpl)source).addConfigDefinition();
                }
                defNode = new DefinitionNodeImpl(path, newNodeName, def);
                def.setNode(defNode);
            }
            if (node.getMerge() != null) {
                deltaNodes.add(defNode);
            }

            defNode.getSourceLocation().copy(node.getSourceLocation());

            nodeDefinitions.put(new MinimallyIndexedPath(path), defNode);

            if (node.isDeltaInsert()) {
                if (deltaNodes.contains(parentNode)) {
                    defNode.setOrderBefore(node.getMergeLocation());
                } else {
                    boolean hasAfter = parentNode.getModifiableNodes().size() > 1 &&
                            (node.getMergeLocation().equals("") || parentNode.getNode(node.getMergeLocation()) != null);
                    if (hasAfter) {
                        LinkedHashMap<String, DefinitionNodeImpl> childNodes = new LinkedHashMap<>(parentNode.getModifiableNodes());
                        childNodes.remove(defNode.getName());
                        parentNode.getModifiableNodes().clear();
                        if (node.getMergeLocation().equals("")) {
                            parentNode.getModifiableNodes().put(defNode.getName(), defNode);
                            parentNode.getModifiableNodes().putAll(childNodes);
                        } else {
                            boolean found = false;
                            for (String name : childNodes.keySet()) {
                                if (!found && node.getMergeLocation().equals(name)) {
                                    found = true;
                                    parentNode.getModifiableNodes().put(defNode.getName(), defNode);
                                }
                                parentNode.getModifiableNodes().put(name, childNodes.get(name));
                            }
                        }
                    } else {
                        log.warn("Ordering node " + path + " defined at " + defNode.getSourceLocation() +
                                " before sibling \"" + node.getMergeLocation() + "\" ignored: sibling not found.");
                    }
                }
            }
        }
        EsvProperty prop = node.getProperty(JCR_PRIMARYTYPE);
        if (prop == null) {
            if (!node.isDeltaCombine()) {
                throw new EsvParseException("Missing required property " + JCR_PRIMARYTYPE + " for node " + path + " at " +
                        node.getSourceLocation());
            }
        }
        final boolean deltaNode = deltaNodes.contains(defNode);
        for (EsvProperty property : node.getProperties()) {
            processProperty(node, defNode, property, deltaNode);
        }
        for (EsvNode child : node.getChildren()) {
            final String childPath = calculatePath(child, path, nodeDefinitions, node.getChildren());
            if (!isContent(path) && isContent(childPath)) {
                throw new EsvParseException(
                        "Migration to content definitions is only supported when the entire resource is content; node "
                        + childPath + " defined at " + child.getSourceLocation()
                        + " is content but its parent is configuration");
            } else {
                processNode(child, childPath, source, defNode, nodeDefinitions, deltaNodes);
            }
        }
    }

    private void processProperty(final EsvNode node, final DefinitionNodeImpl defNode, final EsvProperty property, final boolean deltaNode)
            throws EsvParseException {
        final boolean isPathReference = property.getName().endsWith(PATH_REFERENCE_POSTFIX);
        final String propertyName = isPathReference ? StringUtils.substringBefore(property.getName(), PATH_REFERENCE_POSTFIX) : property.getName();
        DefinitionPropertyImpl prop = defNode.getProperty(propertyName);
        PropertyOperation op = PropertyOperation.REPLACE;
        if (prop != null) {
            if (PropertyOperation.DELETE == prop.getOperation()) {
                // will be replaced with incoming property
                prop = null;
            } else {
                if (property.isMergeSkip()) {
                    log.warn("Skipping property " + prop.getJcrPath() + " which already is defined at " + prop.getSourceLocation());
                    return;
                }
                if (property.isMergeAppend()) {
                    if (prop.getValueType().ordinal() != property.getType()) {
                        throw new EsvParseException("Invalid esv:merge=\"append\" for property " + prop.getJcrPath() + " with different type " +
                                ValueType.fromJcrType(property.getType()).name() + " at " + property.getSourceLocation() +
                                " (from " + prop.getValueType().toString() + " at " + prop.getSourceLocation() + ")");
                    }
                    op = PropertyOperation.ADD;
                }
                if (JCR_PRIMARYTYPE.equals(propertyName)) {
                    String newType = property.getValue();
                    String oldType = prop.getValue().getString();
                    if (!oldType.equals(newType)) {
                        if (!node.isDeltaOverlay()) {
                            throw new EsvParseException("Redefining node " + defNode.getJcrPath() + " type to " + newType + " at " +
                                    property.getSourceLocation() + " (from " + oldType + " at " + prop.getSourceLocation() +
                                    ") not allowed: requires esv:merge=\"overlay\").");
                        } else {
                            op = PropertyOperation.OVERRIDE;
                        }
                    } else {
                        // no change
                        return;
                    }
                } else if (JCR_MIXINTYPES.equals(propertyName)) {
                    Set<String> oldMixins = Arrays.stream(prop.getValues()).map(Value::getString).collect(Collectors.toSet());
                    Set<String> newMixins = property.getValues().stream().map(EsvValue::getString).collect(Collectors.toSet());
                    if (!oldMixins.equals(newMixins)) {
                        if (!node.isDeltaOverlay()) {
                            throw new EsvParseException("Redefining node " + defNode.getJcrPath() + " mixins to " + newMixins +
                                    " at " + property.getSourceLocation() + " (from " + oldMixins + " at " + prop.getSourceLocation() +
                                    ") not allowed: requires esv:merge=\"overlay\").");
                        } else {
                            op = PropertyOperation.OVERRIDE;
                        }
                    } else {
                        // no change
                        return;
                    }
                }
            }
        } else if (deltaNode) {
            if ((JCR_PRIMARYTYPE.equals(propertyName) || JCR_MIXINTYPES.equals(propertyName)) && node.isDeltaOverlay()) {
                op = PropertyOperation.OVERRIDE;
            } else if (property.isMergeAppend()) {
                op = PropertyOperation.ADD;
            } else if (property.isMergeOverride()) {
                op = PropertyOperation.OVERRIDE;
            } else if (property.isMergeSkip()) {
                // TODO: implement PropertyOperation.SKIP
            }
        }
        addProperty(defNode, property, propertyName, prop, op, isPathReference);
    }
}
