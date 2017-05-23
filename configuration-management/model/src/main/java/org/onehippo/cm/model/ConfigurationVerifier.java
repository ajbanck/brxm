/*
 *  Copyright 2017 Hippo B.V. (http://www.onehippo.com)
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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.onehippo.cm.model.impl.GroupImpl;
import org.onehippo.cm.model.builder.ConfigurationModelBuilder;

public class ConfigurationVerifier {

    public static void main(String[] args) throws Exception {

        if (args.length != 1) {
            System.out.println("usage: <hcm-module.yaml>\n" +
                    "<hcm-module.yaml>: hcm-module.yaml location");
            return;
        }
        final Path moduleDescriptorPath = Paths.get(args[0]);
        final PathConfigurationReader.ReadResult result = new PathConfigurationReader().read(moduleDescriptorPath, true);
        final Map<String, GroupImpl> groups = result.getGroups();
        ConfigurationModelBuilder builder = new ConfigurationModelBuilder();
        for (GroupImpl group : groups.values()) {
            builder.push(group);
        }
        builder.build();
    }
}
