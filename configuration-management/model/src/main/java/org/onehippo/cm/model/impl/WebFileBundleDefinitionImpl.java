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

package org.onehippo.cm.model.impl;

import org.onehippo.cm.model.DefinitionType;
import org.onehippo.cm.model.WebFileBundleDefinition;

public class WebFileBundleDefinitionImpl extends AbstractDefinitionImpl implements WebFileBundleDefinition {

    private final String name;

    public WebFileBundleDefinitionImpl(final SourceImpl source, final String name) {
        super(source);

        this.name = name;
    }

    @Override
    public DefinitionType getType() {
        return DefinitionType.WEBFILEBUNDLE;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ConfigSourceImpl getSource() {
        return (ConfigSourceImpl) super.getSource();
    }

    public String toString() {
        return getClass().getSimpleName()+"{name='"+name+", origin="+getOrigin()+"'}";
    }
}
