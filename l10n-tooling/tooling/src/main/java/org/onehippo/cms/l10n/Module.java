/*
 *  Copyright 2016-2016 Hippo B.V. (http://www.onehippo.com)
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
package org.onehippo.cms.l10n;

import java.io.File;

public class Module {
    
    private final File pom;
    private final String moduleName;
    private Registry registry;
    
    public Module(File pom) {
        this.pom = pom;
        final File directory = pom.getParentFile();
        moduleName = directory.getName();
    }
    
    public String getName() {
        return moduleName;
    }
    
    public Registry getRegistry() {
        if (registry == null && hasResources()) {
            registry = new Registry(getResources()); 
        }
        return registry;
    }
    
    public boolean hasResources() {
        return getResources().exists();
    }
    
    private File getResources() {
        return new File(pom.getParentFile(), "resources");
    }
    
}
