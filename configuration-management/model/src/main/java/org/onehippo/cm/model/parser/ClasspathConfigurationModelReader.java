/*
 * Copyright 2017 Hippo B.V. (http://www.onehippo.com)
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
package org.onehippo.cm.model.parser;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.onehippo.cm.model.Constants;
import org.onehippo.cm.model.impl.ConfigurationModelImpl;
import org.onehippo.cm.model.impl.GroupImpl;
import org.onehippo.cm.model.impl.ModuleImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClasspathConfigurationModelReader {

    private static final Logger log = LoggerFactory.getLogger(ClasspathConfigurationModelReader.class);

    /**
     * Searches the classpath for module manifest files and uses these as entry points for loading HCM module
     * configuration and content into a ConfigurationModel.
     *
     * @param classLoader the ClassLoader which will be searched for HCM modules
     * @return a ConfigurationModel of configuration and content definitions
     * @throws IOException
     * @throws ParserException
     */
    public ConfigurationModelImpl read(final ClassLoader classLoader)
            throws IOException, ParserException, URISyntaxException {
        return read(classLoader, false);
    }

    /**
     * Searches the classpath for module manifest files and uses these as entry points for loading HCM module
     * configuration and content into a ConfigurationModel.
     *
     * @param classLoader the ClassLoader which will be searched for HCM modules
     * @param verifyOnly when true use 'verify only' yaml parsing, allowing (but warning on) certain model errors
     * @return a ConfigurationModel of configuration and content definitions
     * @throws IOException
     * @throws ParserException
     */
    public ConfigurationModelImpl read(final ClassLoader classLoader, final boolean verifyOnly)
            throws IOException, ParserException, URISyntaxException {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        final ConfigurationModelImpl model = new ConfigurationModelImpl();

        // load modules that are packaged on the classpath
        final Pair<Set<FileSystem>, List<GroupImpl>> classpathGroups =
                readModulesFromClasspath(classLoader, verifyOnly);

        // add classpath modules to model
        classpathGroups.getRight().forEach(model::addGroup);

        // add filesystems to the model
        model.setFileSystems(classpathGroups.getLeft());

        // build the merged model
        model.build();

        stopWatch.stop();
        log.info("ConfigurationModel loaded in {}", stopWatch.toString());

        return model;
    }

    /**
     * Read modules that are packaged on the classpath for the given ClassLoader.
     * @param classLoader the classloader to search for packaged config modules
     * @param verifyOnly TODO
     * @return a Map of FileSystems that will need to be closed after processing the modules and the corresponding PathConfigurationReader result
     */
    protected Pair<Set<FileSystem>, List<GroupImpl>> readModulesFromClasspath(final ClassLoader classLoader, final boolean verifyOnly)
            throws IOException, ParserException, URISyntaxException {
        final Pair<Set<FileSystem>, List<GroupImpl>> groups = new MutablePair<>(new HashSet<>(), new ArrayList<>());

        // find all the classpath resources with a filename that matches the expected module descriptor filename
        // and also located at the root of a classpath entry
        final Enumeration<URL> resources = classLoader.getResources(Constants.HCM_MODULE_YAML);
        while (resources.hasMoreElements()) {
            final URL resource = resources.nextElement();
            final String resourcePath = resource.getPath();

            // look for the marker that indicates this is a path within a jar file
            // this is the normal case when we load modules that were packaged and deployed in a Tomcat container
            final int jarContentMarkerIdx = resourcePath.lastIndexOf("!/");
            if (jarContentMarkerIdx > 0) {
                // note: the below mapping of resource url to path assumes the jar physically exists on the filesystem,
                // using a non-exploded war based classloader might fail here, but that is (currently) not supported anyway

                // First convert the resourcePath to a platform native one, e.g. on Windows this 'fixes' /C:/ to C:\
                // Furthermore, it also properly decodes encoded special characters like spaces (%20) as well as for example '+' characters.
                // without needing to use URLDecoder.decode() (as often times is suggested).
                // URLDecoder.decode() typically will incorrectly replace '+' with ' '!
                // (it also could throw UnsupportedException, but most/all implementations handle this example 'silently')
                final File archiveFile = new File(new URL(resourcePath.substring(0, jarContentMarkerIdx)).toURI());
                final String nativePath = archiveFile.getPath();
                final Path jarPath = Paths.get(nativePath);

                // Jar-based FileSystems must remain open for the life of a ConfigurationModel, and must be closed when
                //  processing is complete via ConfigurationModel.close()!
                final FileSystem fs = FileSystems.newFileSystem(jarPath, null);

                // since this FS represents a jar, we should look for the descriptor at the root of the FS
                final Path moduleDescriptorPath = fs.getPath(Constants.HCM_MODULE_YAML);
                final PathConfigurationReader.ReadResult result =
                        new PathConfigurationReader().read(moduleDescriptorPath, verifyOnly);
                final ModuleImpl moduleImpl = result.getModuleContext().getModule();
                moduleImpl.setArchiveFile(archiveFile);

                // Hang onto a reference to this FS, so we can close it later with ConfigurationModel.close()
                groups.getLeft().add(fs);

                groups.getRight().add(moduleImpl.getProject().getGroup());
            }
            else {
                // if part of the classpath is a raw dir on the native filesystem, just use the default FileSystem
                // this is useful for loading a module for testing purposes without packaging it into a jar
                // since this FS is a normal native FS, we need to use the full resource path to load the descriptor
                final Path moduleDescriptorPath = Paths.get(resource.toURI());
                final PathConfigurationReader.ReadResult result =
                        new PathConfigurationReader().read(moduleDescriptorPath, verifyOnly);

                groups.getRight().add(result.getModuleContext().getModule().getProject().getGroup());
            }
        }
        return groups;
    }
}
