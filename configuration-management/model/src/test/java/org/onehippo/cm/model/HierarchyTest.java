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
package org.onehippo.cm.model;

import java.io.IOException;
import java.util.Map;

import org.junit.Test;
import org.onehippo.cm.model.impl.ConfigDefinitionImpl;
import org.onehippo.cm.model.impl.ContentDefinitionImpl;
import org.onehippo.cm.model.impl.DefinitionNodeImpl;
import org.onehippo.cm.model.impl.GroupImpl;
import org.onehippo.cm.model.impl.ModuleImpl;
import org.onehippo.cm.model.impl.NamespaceDefinitionImpl;
import org.onehippo.cm.model.impl.ProjectImpl;
import org.onehippo.cm.model.impl.SourceImpl;
import org.onehippo.cm.model.parser.ParserException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class HierarchyTest extends AbstractBaseTest {

    @Test
    public void expect_hierarchy_test_loads() throws IOException, ParserException {
        final PathConfigurationReader.ReadResult result = readFromTestJar("/parser/hierarchy_test/"+ Constants.HCM_MODULE_YAML);
        final Map<String, GroupImpl> groups = result.getGroups();
        assertEquals(2, groups.size());

        final GroupImpl base = assertGroup(groups, "base", new String[0], 1);
        final ProjectImpl project1 = assertProject(base, "project1", new String[0], 1);
        final ModuleImpl module1 = assertModule(project1, "module1", new String[0], 3);
        final SourceImpl source1 = assertSource(module1, "config.yaml", 6);
        final SourceImpl contentSource1 = assertSource(module1, "content.yaml", 1);

        final NamespaceDefinitionImpl namespace = assertDefinition(source1, 0, NamespaceDefinitionImpl.class);
        assertEquals("myhippoproject", namespace.getPrefix());
        assertEquals("http://www.onehippo.org/myhippoproject/nt/1.0", namespace.getURI().toString());
        assertEquals("example.cnd", namespace.getCndPath().getString());

        final ConfigDefinitionImpl source1definition1 = assertDefinition(source1, 1, ConfigDefinitionImpl.class);
        final DefinitionNodeImpl rootDefinition1 = assertNode(source1definition1, "/", "", source1definition1, 6, 1);
        assertProperty(rootDefinition1, "/root-level-property", "root-level-property",
                source1definition1, ValueType.STRING, "root-level-property-value");
        final DefinitionNodeImpl nodeWithSingleProperty = assertNode(rootDefinition1, "/node-with-single-property",
                "node-with-single-property", source1definition1, 0, 1);
        assertProperty(nodeWithSingleProperty, "/node-with-single-property/property", "property",
                source1definition1, ValueType.STRING, "node-with-single-property-value");
        final DefinitionNodeImpl nodeWithMultipleProperties = assertNode(rootDefinition1, "/node-with-multiple-properties",
                "node-with-multiple-properties", source1definition1, 0, 3);
        assertProperty(nodeWithMultipleProperties, "/node-with-multiple-properties/single", "single",
                source1definition1, ValueType.STRING, "value1");
        assertProperty(nodeWithMultipleProperties, "/node-with-multiple-properties/multiple", "multiple",
                source1definition1, ValueType.STRING, new String[]{"value2","value3"});
        assertProperty(nodeWithMultipleProperties, "/node-with-multiple-properties/empty-multiple", "empty-multiple",
                source1definition1, ValueType.STRING, new String[0]);
        final DefinitionNodeImpl nodeWithSubNode =
                assertNode(rootDefinition1, "/node-with-sub-node", "node-with-sub-node", source1definition1, 1, 0);
        final DefinitionNodeImpl subNode =
                assertNode(nodeWithSubNode, "/node-with-sub-node/sub-node", "sub-node", source1definition1, 0, 1);
        assertProperty(subNode, "/node-with-sub-node/sub-node/property", "property", source1definition1, ValueType.STRING, "sub-node-value");
        assertNode(rootDefinition1, "/node-delete", "node-delete", source1definition1, true, null, 0, 0);
        assertNode(rootDefinition1, "/node-order-before", "node-order-before", source1definition1, false, "node", 1, 1);
        assertNull(rootDefinition1.getNodes().get("node-order-before").getIgnoreReorderedChildren());
        assertTrue(rootDefinition1.getNodes().get("node-ignore-reordered-children").getIgnoreReorderedChildren());

        final ConfigDefinitionImpl source1definition2 = assertDefinition(source1, 2, ConfigDefinitionImpl.class);
        assertNode(source1definition2, "/path/to/node-delete", "node-delete", source1definition2, true, null, 0, 0);

        final ConfigDefinitionImpl source1definition3 = assertDefinition(source1, 3, ConfigDefinitionImpl.class);
        assertNode(source1definition3, "/path/to/node-order-before", "node-order-before", source1definition3, false, "node", 0, 0);

        final ConfigDefinitionImpl source1definition4 = assertDefinition(source1, 4, ConfigDefinitionImpl.class);
        final DefinitionNodeImpl node = assertNode(source1definition4, "/path/to/node", "node", source1definition4, 2, 5);
        assertProperty(node, "/path/to/node/delete-property", "delete-property", source1definition4,
                PropertyOperation.DELETE, ValueType.STRING, new String[0]);
        assertProperty(node, "/path/to/node/add-property", "add-property", source1definition4, PropertyOperation.ADD,
                ValueType.STRING, new String[]{"addme"});
        assertProperty(node, "/path/to/node/replace-property-single-string", "replace-property-single-string",
                source1definition4, PropertyOperation.REPLACE, ValueType.STRING, "single");
        assertProperty(node, "/path/to/node/replace-property-map", "replace-property-map", source1definition4,
                PropertyOperation.REPLACE, ValueType.BINARY, new String[]{"folder/image.png"}, true, false);
        assertProperty(node, "/path/to/node/override-property", "override-property", source1definition4,
                PropertyOperation.OVERRIDE, ValueType.STRING, "single");
        final DefinitionNodeImpl nodeWithNewType =
                assertNode(node, "/path/to/node/node-with-new-type", "node-with-new-type", source1definition4, 0, 2);
        assertProperty(nodeWithNewType, "/path/to/node/node-with-new-type/jcr:primaryType", "jcr:primaryType",
                source1definition4, PropertyOperation.OVERRIDE, ValueType.NAME, "some:type");
        assertProperty(nodeWithNewType, "/path/to/node/node-with-new-type/jcr:mixinTypes", "jcr:mixinTypes",
                source1definition4, PropertyOperation.OVERRIDE, ValueType.NAME, new String[]{"some:mixin"});
        final DefinitionNodeImpl nodeWithMixinAdd = assertNode(node, "/path/to/node/node-with-mixin-add", "node-with-mixin-add",
                source1definition4, 0, 1);
        assertProperty(nodeWithMixinAdd, "/path/to/node/node-with-mixin-add/jcr:mixinTypes", "jcr:mixinTypes",
                source1definition4, PropertyOperation.ADD, ValueType.NAME, new String[]{"some:mixin"});

        final ContentDefinitionImpl contentDefinition = assertDefinition(contentSource1, 0, ContentDefinitionImpl.class);
        assertNode(contentDefinition, "/content/documents/myhippoproject", "myhippoproject", contentDefinition, 0, 1);

        final SourceImpl source2 = assertSource(module1, "folder/resources.yaml", 2);
        final ConfigDefinitionImpl source2definition = assertDefinition(source2, 0, ConfigDefinitionImpl.class);
        final DefinitionNodeImpl resourceNode = assertNode(source2definition, "/resources", "resources", source2definition, 0, 3);
        assertProperty(resourceNode, "/resources/single-value-string-resource", "single-value-string-resource",
                source2definition, PropertyOperation.REPLACE, ValueType.STRING, "string.txt", true, false);
        assertProperty(resourceNode, "/resources/single-value-binary-resource", "single-value-binary-resource",
                source2definition, PropertyOperation.REPLACE, ValueType.BINARY, "image.png", true, false);
        assertProperty(resourceNode, "/resources/multi-value-resource", "multi-value-resource", source2definition,
                PropertyOperation.REPLACE, ValueType.STRING, new String[]{"/root.txt","folder/relative.txt"}, true, false);

//        final ConfigDefinition source2definition2 = assertDefinition(source2, 1, ConfigDefinition.class);
//        assertNode(source2definition2, "/hippo:configuration/hippo:queries/hippo:templates/new-image", "new-image", source2definition2, 1, 2);

        final GroupImpl myhippoproject = assertGroup(groups, "myhippoproject", new String[]{"base"}, 1);
        final ProjectImpl project2 = assertProject(myhippoproject, "project2", new String[]{"project1", "foo/bar"}, 1);
        final ModuleImpl module2 = assertModule(project2, "module2", new String[0], 1);
        final SourceImpl baseSource = assertSource(module2, "config.yaml", 1);
        final ConfigDefinitionImpl baseDefinition = assertDefinition(baseSource, 0, ConfigDefinitionImpl.class);

        final DefinitionNodeImpl rootDefinition2 =
                assertNode(baseDefinition, "/node-with-sub-node/sub-node", "sub-node", baseDefinition, 0, 1);
        assertProperty(rootDefinition2, "/node-with-sub-node/sub-node/property", "property", baseDefinition, ValueType.STRING, "override");
    }

}
