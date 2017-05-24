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

import java.io.IOException;
import java.nio.file.Path;

import org.onehippo.cm.model.FileResourceOutputProvider;
import org.onehippo.cm.model.Module;

/**
 * Aggregated modules support
 */
public class AggregatedModuleContext extends LegacyModuleContext {
    public AggregatedModuleContext(Module module, Path moduleDescriptorPath, boolean multiModule) throws IOException {
        super(module, moduleDescriptorPath, multiModule);
    }

    @Override
    public void createOutputProviders(Path destinationPath) {
        configOutputProvider = new FileResourceOutputProvider(destinationPath);
        contentOutputProvider = new FileResourceOutputProvider(destinationPath);
    }
}
