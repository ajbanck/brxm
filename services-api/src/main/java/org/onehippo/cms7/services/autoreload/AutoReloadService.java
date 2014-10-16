/*
 * Copyright 2014 Hippo B.V. (http://www.onehippo.com)
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

package org.onehippo.cms7.services.autoreload;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

import javax.jcr.Session;

import org.onehippo.cms7.services.SingletonService;
import org.onehippo.cms7.services.WhiteboardService;
import org.onehippo.cms7.services.webresources.WebResourceBundle;
import org.onehippo.cms7.services.webresources.WebResourceException;

/**
 * Automatically reloads the current page in connected browsers. If auto-reload is disabled, nothing happens when
 * {@link #broadcastPageReload()} is called.
 */
@SingletonService
@WhiteboardService
@SuppressWarnings("UnusedDeclaration")
public interface AutoReloadService {

    /**
     * @return true if auto-reload is enabled, false otherwise.
     */
    boolean isEnabled();

    /**
     * Enables or disabled auto-reload.
     * @param isEnabled true when auto-reload should be enabled, false when it should be disabled.
     */
    void setEnabled(boolean isEnabled);

    /**
     * @return the JavaScript to include in a browser that handles the auto-reloading.
     */
    String getJavaScript();

    /**
     * Reloads the current page in all connected browsers. If auto-reload is disabled, nothing happens.
     */
    void broadcastPageReload();

}
