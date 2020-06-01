/*
 * Copyright 2017-2018 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onehippo.cm.model.impl;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.onehippo.cm.model.ConfigurationModel;
import org.onehippo.cm.model.Group;
import org.onehippo.cm.model.impl.definition.ConfigDefinitionImpl;
import org.onehippo.cm.model.impl.definition.ContentDefinitionImpl;
import org.onehippo.cm.model.impl.definition.NamespaceDefinitionImpl;
import org.onehippo.cm.model.impl.definition.WebFileBundleDefinitionImpl;
import org.onehippo.cm.model.path.JcrPath;
import org.onehippo.cm.model.path.JcrPathSegment;
import org.onehippo.cm.model.impl.tree.ConfigurationNodeImpl;
import org.onehippo.cm.model.impl.tree.ConfigurationPropertyImpl;
import org.onehippo.cm.model.impl.tree.ConfigurationTreeBuilder;
import org.onehippo.cm.model.path.JcrPaths;
import org.onehippo.cm.model.util.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationModelImpl implements ConfigurationModel {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationModelImpl.class);

    private static final OrderableByNameListSorter<Group> groupSorter = new OrderableByNameListSorter<>(Group.class);

    private final Map<String, GroupImpl> groupMap = new HashMap<>();
    private final List<GroupImpl> groups = new ArrayList<>();
    private final List<GroupImpl> sortedGroups = Collections.unmodifiableList(groups);

    private final Set<ModuleImpl> replacements = new HashSet<>();

    private ConfigurationNodeImpl configurationRootNode;

    private final List<NamespaceDefinitionImpl> modifiableNamespaceDefinitions = new ArrayList<>();
    private final List<NamespaceDefinitionImpl> namespaceDefinitions = Collections.unmodifiableList(modifiableNamespaceDefinitions);
    private final List<WebFileBundleDefinitionImpl> modifiableWebFileBundleDefinitions = new ArrayList<>();
    private final List<WebFileBundleDefinitionImpl> webFileBundleDefinitions = Collections.unmodifiableList(modifiableWebFileBundleDefinitions);
    private final List<ContentDefinitionImpl> modifiableContentDefinitions = new ArrayList<>();
    private final List<ContentDefinitionImpl> contentDefinitions = Collections.unmodifiableList(modifiableContentDefinitions);
    private final List<ConfigDefinitionImpl> modifiableConfigDefinitions = new ArrayList<>();
    private final Map<JcrPath, ConfigurationNodeImpl> modifiableDeletedConfigNodes = new HashMap<>();
    private final Map<JcrPath, ConfigurationNodeImpl> deletedConfigNodes = Collections.unmodifiableMap(modifiableDeletedConfigNodes);

    private final Map<JcrPath, ConfigurationPropertyImpl> modifiableDeletedConfigProperties = new HashMap<>();
    private final Map<JcrPath, ConfigurationPropertyImpl> deletedConfigProperties = Collections.unmodifiableMap(modifiableDeletedConfigProperties);

    // Used for cleanup when done with this ConfigurationModel
    private Set<FileSystem> filesystems = new HashSet<>();

    private long buildTimeStamp = 0;

    @Override
    public List<GroupImpl> getSortedGroups() {
        return sortedGroups;
    }

    /**
     * Convenience method for client code that wants to iterate over modules using streams.
     */
    public Stream<ModuleImpl> getModulesStream() {
        return getSortedGroups().stream()
                .flatMap(g -> g.getProjects().stream())
                .flatMap(p -> p.getModules().stream());
    }

    /**
     * Convenience method for client code that wants to iterate over modules using a for-each loop.
     * DO NOT reuse the return value in more than one loop, as the underlying stream will not support this!
     */
    public Iterable<ModuleImpl> getModules() {
        return getModulesStream()::iterator;
    }

    @Override
    public List<NamespaceDefinitionImpl> getNamespaceDefinitions() {
        return namespaceDefinitions;
    }

    @Override
    public List<WebFileBundleDefinitionImpl> getWebFileBundleDefinitions() {
        return webFileBundleDefinitions;
    }

    @Override
    public List<ContentDefinitionImpl> getContentDefinitions() {
        return contentDefinitions;
    }

    @Override
    public ConfigurationNodeImpl getConfigurationRootNode() {
        return configurationRootNode;
    }

    public Map<JcrPath, ConfigurationNodeImpl> getDeletedConfigNodes() {
        return deletedConfigNodes;
    }

    private void addContentDefinitions(final Collection<ContentDefinitionImpl> definitions) {
        modifiableContentDefinitions.addAll(definitions);
    }

    private void addConfigDefinitions(final Collection<ConfigDefinitionImpl> definitions) {
        modifiableConfigDefinitions.addAll(definitions);
    }

    private void addNamespaceDefinitions(final Collection<NamespaceDefinitionImpl> definitions) {
        modifiableNamespaceDefinitions.addAll(definitions);
    }

    private void addWebFileBundleDefinitions(final Collection<WebFileBundleDefinitionImpl> definitions) {
        for (WebFileBundleDefinitionImpl definition : definitions) {
            ensureUniqueBundleName(definition);
        }
        modifiableWebFileBundleDefinitions.addAll(definitions);
    }

    private void setConfigurationRootNode(final ConfigurationNodeImpl configurationRootNode) {
        this.configurationRootNode = configurationRootNode;
    }

    public ConfigurationModelImpl addGroup(final GroupImpl group) {
        if (!groupMap.containsKey(group.getName())) {
            groupMap.put(group.getName(), new GroupImpl(group.getName()));
        }
        final GroupImpl consolidatedGroup = groupMap.get(group.getName());
        consolidatedGroup.addAfter(group.getAfter());
        for (ProjectImpl project : group.getProjects()) {
            final ProjectImpl consolidatedProject = consolidatedGroup.getOrAddProject(project.getName());
            consolidatedProject.addAfter(project.getAfter());
            for (ModuleImpl module : project.getModules()) {
                if (!replacements.contains(module)) {
                    consolidatedProject.addModule(module);
                }
            }
        }
        return this;
    }

    /**
     * Note: calling this method directly, rather than addGroup(), has the important effect of disabling the normal
     * validation check to prevent adding two modules with the same full-name. This is intended to support the use-case
     * where a new clone of a module will be used to replace an existing module from an existing ConfigurationModel.
     * Call this method with any new replacement modules before adding the groups from the existing model instance.
     * @param module the new module to add as a replacement
     * @return this
     */
    public ConfigurationModelImpl addModule(final ModuleImpl module) {
        // this duplicates much of the logic of addGroup(),
        // but it's necessary to avoid grabbing undesired sibling modules
        final GroupImpl group = module.getProject().getGroup();
        if (!groupMap.containsKey(group.getName())) {
            groupMap.put(group.getName(), new GroupImpl(group.getName()));
        }
        final GroupImpl consolidatedGroup = groupMap.get(group.getName());
        consolidatedGroup.addAfter(group.getAfter());

        final ProjectImpl project = module.getProject();
        final ProjectImpl consolidatedProject = consolidatedGroup.getOrAddProject(project.getName());
        consolidatedProject.addAfter(project.getAfter());

        if (!replacements.contains(module)) {
            consolidatedProject.addModule(module);
        }

        // now that we've added the module, store a reference for filtering out new copies we might encounter later
        replacements.add(module);
        return this;
    }

    public ConfigurationModelImpl build() {
        buildTimeStamp = System.currentTimeMillis();

        buildModel();
        buildConfiguration();
        return this;
    }

    public ConfigurationModelImpl buildModel() {
        replacements.clear();
        groups.clear();
        groups.addAll(groupMap.values());
        groupSorter.sort(groups);
        groups.forEach(GroupImpl::sortProjects);
        return this;
    }

    public ConfigurationModelImpl buildConfiguration() {

        modifiableNamespaceDefinitions.clear();
        modifiableConfigDefinitions.clear();
        modifiableContentDefinitions.clear();
        modifiableWebFileBundleDefinitions.clear();
        modifiableDeletedConfigNodes.clear();
        modifiableDeletedConfigProperties.clear();

        final ConfigurationTreeBuilder configurationTreeBuilder = new ConfigurationTreeBuilder();
        for (GroupImpl g : groups) {
            for (ProjectImpl p : g.getProjects()) {
                for (ModuleImpl module : p.getModules()) {
                    log.info("Merging module {}", module.getFullName());
                    addNamespaceDefinitions(module.getNamespaceDefinitions());
                    addConfigDefinitions(module.getConfigDefinitions());
                    addContentDefinitions(module.getContentDefinitions());
                    addWebFileBundleDefinitions(module.getWebFileBundleDefinitions());
                    module.getConfigDefinitions().forEach(configurationTreeBuilder::push);
                    configurationTreeBuilder.finishModule();
                }
            }
        }
        setConfigurationRootNode(configurationTreeBuilder.build());

        modifiableDeletedConfigNodes.putAll(configurationTreeBuilder.getDeletedNodes());
        modifiableDeletedConfigProperties.putAll(configurationTreeBuilder.getDeletedProperties());

        return this;
    }

    /**
     * Resolve top deleted node
     */
    public ConfigurationNodeImpl resolveDeletedNode(final JcrPath path) {
        return deletedConfigNodes.get(path);
    }

    public ConfigurationNodeImpl resolveDeletedSubNodeRoot(final JcrPath path) {
        return deletedConfigNodes.values().stream()
                .filter(deletedRootNode -> !path.equals(deletedRootNode.getJcrPath()) && path.startsWith(deletedRootNode.getJcrPath()))
                .findFirst().orElse(null);
    }

    /**
     * Resolve child in deleted node
     */
    public ConfigurationNodeImpl resolveDeletedSubNode(final JcrPath path) {

        final ConfigurationNodeImpl deletedRootNode = resolveDeletedSubNodeRoot(path);
        if (deletedRootNode != null) {
            final JcrPath pathDiff = deletedRootNode.getJcrPath().relativize(path);
            ConfigurationNodeImpl currentNode = deletedRootNode;
            for (final JcrPathSegment jcrPathSegment : pathDiff) {
                ConfigurationNodeImpl nextNode = currentNode.getNode(jcrPathSegment);
                if (nextNode == null) {
                    nextNode = currentNode.getNode(jcrPathSegment.forceIndex());
                }

                currentNode = nextNode;
                if (currentNode == null) {
                    break; //wrong path
                } else if (currentNode.getJcrPath().equals(path)) {
                    return currentNode;
                }
            }
        }
        return null;
    }

    public ConfigurationPropertyImpl resolveDeletedProperty(final JcrPath path) {
        return deletedConfigProperties.get(path);
    }

    /**
     * Compile a manifest of contents. Format will be a YAML document as follows.
     * <pre>
     * for each Module:
     * [group-name]/[project-name]/[module-name]:
     *     for module descriptor:
     *     MODULE_MANIFEST_PATH:[MD5-digest]
     *     for actions file:
     *     ACTIONS_MANIFEST_PATH:[MD5-digest]
     *     for each configuration Source:
     *     [module-relative-path]:[MD5-digest]
     *         for each referenced configuration resource:
     *     [module-relative-path]:[MD5-digest]
     *     for each content Source:
     *     [module-relative-path]:[content-path]
     * </pre>
     * Note that to preserve stability of output, the manifest must be consistently sorted. This format will use
     * lexical order of the full group-project-module name string to sort Modules, and then lexical order of the path
     * strings to sort all Source and resource references together within a Module. Note also that resource paths will
     * be normalized to use Module-relative paths, rather than the mix of Module- and Source-relative
     * paths as used within the Source text. Resource paths will use 4-spaces indentation, and Modules will use none.
     * Lines will use a single "\n" line separator, and the final resource reference will end in a line separator.
     * @return String representation of complete manifest of contents
     */
    @Override
    public String getDigest() {
        TreeMap<ModuleImpl,TreeMap<String,String>> manifest = new TreeMap<>();
        // for each module, accumulate manifest items
        for (ModuleImpl m : getModules()) {
            m.compileManifest(this, manifest);
        }

        final String modelManifest = manifestToString(manifest);
        log.debug("model manifest:\n{}", modelManifest);

        return DigestUtils.computeManifestDigest(modelManifest);
    }
    
    /**
     * Helper for getDigest()
     * @param manifest
     * @return
     */
    protected static String manifestToString(final TreeMap<ModuleImpl, TreeMap<String, String>> manifest) {
        // print to final manifest String (with ~10k initial buffer size)
        StringBuilder sb = new StringBuilder(10000);

        manifest.forEach((m,items) -> {
            sb.append(m.getFullName());
            sb.append(":\n");
            items.forEach((p,d) -> {
                sb.append("    ");
                sb.append(p);
                sb.append(": ");
                sb.append(d);
                sb.append('\n');
            });
        });

        return sb.toString();
    }

    /**
     * Used for cleaning up resources in close(), when client code is done with this model.
     */
    public void setFileSystems(Set<FileSystem> fs) {
        this.filesystems = fs;
    }

    /**
     * Closes open FileSystems used by ResourceInputProviders.
     */
    public void close() {
        for (FileSystem fs : filesystems) {
            try {
                fs.close();
            } catch (IOException e) {
                log.warn("Failed to close ConfigurationModel file system", e);
            }
        }
        filesystems.clear();
    }

    private void ensureUniqueBundleName(final WebFileBundleDefinitionImpl newDefinition) {
        for (WebFileBundleDefinitionImpl existingDefinition : webFileBundleDefinitions) {
            if (existingDefinition.getName().equals(newDefinition.getName())) {
                final String msg = String.format(
                        "Duplicate web file bundle with name '%s' found in source files '%s' and '%s'.",
                        newDefinition.getName(),
                        existingDefinition.getOrigin(),
                        newDefinition.getOrigin());
                throw new IllegalStateException(msg);
            }
        }
    }

    public ConfigurationNodeImpl resolveNode(String path) {
        return resolveNode(JcrPaths.getPath(path));
    }

    /**
     * Find a ConfigurationNode by its absolute path.
     * @param path the path of a node
     * @return a ConfigurationNode or null, if no node exists with this path
     */
    @Override
    public ConfigurationNodeImpl resolveNode(JcrPath path) {
        // special handling for root node
        if (path.isRoot()) {
            return getConfigurationRootNode();
        }

        ConfigurationNodeImpl currentNode = getConfigurationRootNode();
        for (JcrPathSegment segment : path) {
            currentNode = currentNode.getNode(segment.forceIndex());
            if (currentNode == null) {
                return null;
            }
        }
        return currentNode;
    }

    public ConfigurationPropertyImpl resolveProperty(String path) {
        return resolveProperty(JcrPaths.getPath(path));
    }

    /**
     * Find a ConfigurationProperty by its absolute path.
     * @param path the path of a property
     * @return a ConfigurationProperty or null, if no property exists with this path
     */
    @Override
    public ConfigurationPropertyImpl resolveProperty(JcrPath path) {
        ConfigurationNodeImpl node = resolveNode(path.getParent());
        if (node == null) {
            return null;
        }
        else {
            return node.getProperty(path.getLastSegment().toString());
        }
    }

    public ContentDefinitionImpl getNearestContentDefinition(final String path) {
        return getNearestContentDefinition(JcrPaths.getPath(path));
    }

    @Override
    public ContentDefinitionImpl getNearestContentDefinition(final JcrPath path) {
        JcrPath originPath = JcrPaths.ROOT;
        ContentDefinitionImpl defValue = null;
        for (ContentDefinitionImpl def : getContentDefinitions()) {
            // this def is a better candidate if it has a starting substring match
            if (path.startsWith(def.getRootPath())
                    // and that subpath is longer than the previous match
                    && (def.getRootPath().getSegmentCount() > originPath.getSegmentCount())) {
                originPath = def.getRootPath();
                defValue = def;
            }
        }
        return defValue;
    }

    @Override
    public String toString() {
        return ConfigurationModelImpl.class.getSimpleName()
                + "{built="
                + ((buildTimeStamp == 0)?
                    "never"
                    :DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.format(buildTimeStamp))
                + "}";

    }

    /**
     * Combine the provided source modules with all of the other modules from an existing model.
     * @param sourceModules the new source modules
     * @param model model from which we want to extract all modules that don't overlap with source modules
     * @return a new, fully-built model combining modules from the params
     */
    public static ConfigurationModelImpl mergeWithSourceModules(final Collection<ModuleImpl> sourceModules,
                                                                final ConfigurationModelImpl model) {
        final ConfigurationModelImpl mergedModel = new ConfigurationModelImpl();

        // start with the source modules
        sourceModules.forEach(mergedModel::addModule);

        // then layer on top all of the other modules
        model.getSortedGroups().forEach(mergedModel::addGroup);

        return mergedModel.build();
    }

    /**
     * Combine the source modules from an existingModel with all of the other modules from a newModel.
     * @param existingModel model from which we want to extract source modules and no other modules
     * @param newModel model from which we want to extract all modules that don't overlap with source modules
     * @return a new, fully-built model combining modules from the params
     */
    public static ConfigurationModelImpl mergeWithSourceModules(final ConfigurationModelImpl existingModel,
                                                                final ConfigurationModelImpl newModel) {
        final ConfigurationModelImpl mergedModel = new ConfigurationModelImpl();

        // preserve the source modules
        for (ModuleImpl module : existingModel.getModules()) {
            if (module.getMvnPath() != null) {
                log.debug("Merging module: {}", module);
                mergedModel.addModule(module);
            }
        }

        // layer on top all of the other modules
        newModel.getSortedGroups().forEach(mergedModel::addGroup);

        return mergedModel.build();
    }
}
