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
package org.onehippo.cm.engine;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

import org.onehippo.cm.api.model.Configuration;
import org.onehippo.cm.api.model.ContentDefinition;
import org.onehippo.cm.api.model.Definition;
import org.onehippo.cm.api.model.DefinitionItem;
import org.onehippo.cm.api.model.DefinitionNode;
import org.onehippo.cm.api.model.DefinitionProperty;
import org.onehippo.cm.api.model.Module;
import org.onehippo.cm.api.model.Project;
import org.onehippo.cm.api.model.Source;
import org.onehippo.cm.api.model.Value;
import org.onehippo.cm.api.model.ValueFormatException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public abstract class AbstractBaseTest {

    private URL pathToUrl(final Path path) {
        try {
            return path.toUri().toURL();
        } catch (MalformedURLException e) {
            fail("Cannot convert path to URL " + e);
            return null;
        }
    }

    Path urlToPath(final URL url) {
        try {
            return Paths.get(url.toURI());
        } catch (URISyntaxException e) {
            fail("Cannot convert URL to path " + e);
            return null;
        }
    }

    static class TestFiles {
        URL repoConfig;
        List<URL> sources = new ArrayList<>();
    }

    Path findBase(final String repoConfigResourceName) throws IOException {
        final URL url = AbstractBaseTest.class.getResource(repoConfigResourceName);
        if (url == null) {
            fail("cannot find resource " + repoConfigResourceName);
        }
        return Paths.get(url.getFile()).getParent();
    }

    TestFiles collectFiles(final Path baseDirectory) throws IOException {
        final Path repoConfig = baseDirectory.resolve("repo-config.yaml");
        if (!Files.isRegularFile(repoConfig)) {
            throw new IOException("Could not find repo-config.yaml file in " + baseDirectory.toString());
        }
        final Path sourceDirectory = baseDirectory.resolve("repo-config");
        if (!Files.isDirectory(sourceDirectory)) {
            throw new IOException("Could not find directory repo-config in " + baseDirectory.toString());
        }

        final TestFiles result = new TestFiles();
        result.repoConfig = pathToUrl(repoConfig);
        final BiPredicate<Path, BasicFileAttributes> matcher =
                (filePath, fileAttr) -> filePath.toString().toLowerCase().endsWith("yaml") && fileAttr.isRegularFile();
        Files.find(sourceDirectory, Integer.MAX_VALUE, matcher)
                .forEachOrdered(path -> result.sources.add(pathToUrl(path)));
        return result;
    }

    TestFiles collectFilesFromResource(final String repoConfigResourceName) throws IOException {
        return collectFiles(findBase(repoConfigResourceName));
    }

    Configuration assertConfiguration(final Map<String, Configuration> parent, final String name, final String[] after, final int projectCount) {
        final Configuration configuration = parent.get(name);
        assertNotNull(configuration);
        assertEquals(name, configuration.getName());
        assertArrayEquals(after, configuration.getAfter().toArray());
        assertEquals(projectCount, configuration.getProjects().size());
        return configuration;
    }

    Project assertProject(final Configuration parent, final String name, final String[] after, final int moduleCount) {
        final Project project = parent.getProjects().get(name);
        assertNotNull(project);
        assertEquals(name, project.getName());
        assertArrayEquals(after, project.getAfter().toArray());
        assertEquals(parent, project.getConfiguration());
        assertEquals(moduleCount, project.getModules().size());
        return project;
    }

    Module assertModule(final Project parent, final String name, final String[] after, final int sourceCount) {
        final Module module = parent.getModules().get(name);
        assertNotNull(module);
        assertEquals(name, module.getName());
        assertArrayEquals(after, module.getAfter().toArray());
        assertEquals(parent, module.getProject());
        assertEquals(sourceCount, module.getSources().size());
        return module;
    }

    Source assertSource(final Module parent, final String path, final int definitionCount) {
        Source source = parent.getSources().get(path);
        assertNotNull(source);
        assertEquals(path, source.getPath());
        assertEquals(parent, source.getModule());
        assertEquals(definitionCount, source.getDefinitions().size());
        return source;
    }

    <T extends Definition> T assertDefinition(final Source parent, final int index, Class<T> definitionClass) {
        final Definition definition = parent.getDefinitions().get(index);
        return definitionClass.cast(definition);
    }

    DefinitionNode assertNode(final DefinitionNode parent,
                              final String path,
                              final String name,
                              final boolean isRoot,
                              final Definition definition,
                              final boolean isDeleted,
                              final int nodeCount,
                              final int propertyCount)
    {
        final DefinitionNode node = parent.getNodes().get(name);
        validateNode(node, path, name, parent, isRoot, definition, isDeleted, nodeCount, propertyCount);
        return node;
    }

    DefinitionNode assertNode(final ContentDefinition parent,
                              final String path,
                              final String name,
                              final Definition definition,
                              final boolean isDeleted,
                              final int nodeCount,
                              final int propertyCount)
    {
        final DefinitionNode node = parent.getNode();
        validateNode(node, path, name, null, true, definition, isDeleted, nodeCount, propertyCount);
        return node;
    }

    private void validateNode(final DefinitionNode node,
                              final String path,
                              final String name,
                              final DefinitionNode parent,
                              final boolean isRoot,
                              final Definition definition,
                              final boolean isDeleted,
                              final int nodeCount,
                              final int propertyCount)
    {
        validateItem(node, path, name, parent, isRoot, definition, isDeleted);
        assertEquals(nodeCount, node.getNodes().size());
        assertEquals(propertyCount, node.getProperties().size());
    }

    private void validateItem(final DefinitionItem item,
                              final String path,
                              final String name,
                              final DefinitionNode parent,
                              final boolean isRoot,
                              final Definition definition,
                              final boolean isDeleted)
    {
        assertNotNull(item);
        assertEquals(path, item.getPath());
        assertEquals(name, item.getName());
        if (isRoot) {
            try {
                item.getParent();
                fail("Expected IllegalStateException");
            } catch (IllegalStateException e) {
                // ignore
            }
        } else {
            assertEquals(parent, item.getParent());
        }
        assertEquals(isRoot, item.isRoot());
        assertEquals(definition, item.getDefinition());
        assertEquals(isDeleted, item.isDeleted());
    }

    DefinitionProperty assertProperty(final DefinitionNode parent,
                                      final String path,
                                      final String name,
                                      final Definition definition,
                                      final Object value)
    {
        return assertProperty(parent, path, name, definition, false, value, false);
    }

    DefinitionProperty assertProperty(final DefinitionNode parent,
                                      final String path,
                                      final String name,
                                      final Definition definition,
                                      final boolean isDeleted,
                                      final Object value,
                                      final boolean valueIsResource)
    {
        final DefinitionProperty property = parent.getProperties().get(name);
        validateItem(property, path, name, parent, false, definition, isDeleted);
        try {
            property.getValues();
            fail("Expected ValueFormatException");
        } catch (ValueFormatException e) {
            // ignore
        }
        assertValue(value, valueIsResource, property.getValue());
        return property;
    }

    DefinitionProperty assertProperty(final DefinitionNode parent,
                                      final String path,
                                      final String name,
                                      final Definition definition,
                                      final Object[] values)
    {
        return assertProperty(parent, path, name, definition, false, values, false);
    }

    DefinitionProperty assertProperty(final DefinitionNode parent,
                                      final String path,
                                      final String name,
                                      final Definition definition,
                                      final boolean isDeleted,
                                      final Object[] values,
                                      final boolean valuesAreResource)
    {
        final DefinitionProperty property = parent.getProperties().get(name);
        validateItem(property, path, name, parent, false, definition, isDeleted);
        try {
            property.getValue();
            fail("Expected ValueFormatException");
        } catch (ValueFormatException e) {
            // ignore
        }
        assertEquals(values.length, property.getValues().length);
        for (int i = 0; i < values.length; i++) {
            assertValue(values[i], valuesAreResource, property.getValues()[i]);
        }
        return property;
    }

    void assertValue(final Object expected, final boolean expectedResource, final Value actual) {
        if (expected instanceof byte[]) {
            assertArrayEquals((byte[]) expected, (byte[]) actual.getObject());
        } else {
            assertEquals(expected, actual.getObject());
        }
        assertEquals(expectedResource, actual.isResource());
    }

}
