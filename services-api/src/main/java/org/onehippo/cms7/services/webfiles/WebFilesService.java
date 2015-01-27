/*
 * Copyright 2014-2015 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onehippo.cms7.services.webfiles;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

import javax.jcr.Session;

import org.onehippo.cms7.services.SingletonService;
import org.onehippo.cms7.services.WhiteboardService;

@SingletonService
@WhiteboardService
@SuppressWarnings("UnusedDeclaration")
public interface WebFilesService {

    public static final String JCR_ROOT_PATH = "/webfiles";

    /**
     * Creates a web files implementation based on JCR.
     *
     * @param session the JCR session used to access web files.
     * @param bundleName the name of the web files bundle.
     * @return a JCR-based web files implementation for <code>bundleName</code>.
     * @throw WebFileException if the {@link WebFileBundle} for <code>bundleName</code> cannot be found
     */
    WebFileBundle getJcrWebFileBundle(Session session, String bundleName) throws WebFileException;

    /**
     * Imports a web file bundle from a directory. The name of the directory is used as the name of the bundle.
     * Existing web files in JCR are replaced by the new ones. Missing web files are deleted from JCR.
     * The caller of this method is responsible for saving the changes made in the session.
     *
     * @param session the JCR session used to access web files.
     * @param directory the directory containing the web files to import.
     * @throws IOException if an I/O error occurs while reading web files from file system
     * @throws WebFileException if another error occurs while importing web files
     */
    void importJcrWebFileBundle(Session session, File directory) throws IOException, WebFileException;

    /**
     * Imports a web file bundle from the given zip file. The zip file should contain a single root directory entry
     * that contains all web files. The name of the root directory entry is used as the name of the bundle.
     * Existing web files in JCR are replaced by the new ones. Missing web files are deleted from JCR.
     * The caller of this method is responsible for saving the changes made in the session.
     *
     * @param session the JCR session used to access web files.
     * @param zip the ZIP file containing the web files to import.
     * @throws IOException if an I/O error occurs while reading web files from the ZIP file
     * @throws WebFileException if another error occurs while importing web files
     */
    void importJcrWebFileBundle(Session session, ZipFile zip) throws IOException, WebFileException;

    /**
     * Imports a sub-tree of a web file bundle from a directory. The sub-tree can consist of a directory or
     * a single file. The caller of this method is responsible for saving the changes made in the session.
     *
     * @param session the JCR session used to access web files.
     * @param bundleName the name of the web file bundle
     * @param bundleSubPath the relative sub-path in the web file bundle to import the resources into.
     * @param fileOrDirectory the file or directory to import web files from.
     * @throws WebFileException if an error occurs while importing web files
     */
    void importJcrWebFiles(Session session, String bundleName, String bundleSubPath, File fileOrDirectory) throws WebFileException;

}
