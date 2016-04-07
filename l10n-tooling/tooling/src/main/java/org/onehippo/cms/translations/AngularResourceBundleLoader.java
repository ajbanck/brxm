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
package org.onehippo.cms.translations;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import net.sf.json.JSONObject;

class AngularResourceBundleLoader extends ResourceBundleLoader {

    private static final Pattern pattern = Pattern.compile("angular/.*/i18n/.*.json");

    AngularResourceBundleLoader(final Collection<String> locales) {
        super(locales);
    }

    @Override
    protected void collectResourceBundles(final ArtifactInfo artifactInfo, final Collection<ResourceBundle> bundles) throws IOException {
        for (String entry : artifactInfo.getEntries()) {
            if (pattern.matcher(entry).matches()) {
                final Properties properties = new Properties();
                final JSONObject jsonObject = JSONObject.fromObject(loadJsonResource(entry));
                for (Object o : jsonObject.keySet()) {
                    properties.put(o, jsonObject.get(o));
                }
                final String fileName = StringUtils.substringAfterLast(entry, "/");
                final String locale = StringUtils.substringBefore(fileName, ".json");
                if (locales.contains(locale)) {
                    bundles.add(new AngularResourceBundle(entry, entry, artifactInfo, locale, properties));
                }
            }
        }

    }

    private String loadJsonResource(String resource) throws IOException {
        try (StringWriter writer = new StringWriter(); InputStream in = getClass().getClassLoader().getResourceAsStream(resource)) {
            IOUtils.copy(in, writer);
            return writer.toString();
        }
    }

}
