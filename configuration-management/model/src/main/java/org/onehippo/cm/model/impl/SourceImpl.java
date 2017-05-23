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
package org.onehippo.cm.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.onehippo.cm.model.Definition;
import org.onehippo.cm.model.Module;
import org.onehippo.cm.model.Source;

public abstract class SourceImpl implements Source {

    private String path;
    private Module module;
    List<Definition> modifiableDefinitions = new ArrayList<>();
    private List<Definition> definitions = Collections.unmodifiableList(modifiableDefinitions);

    public SourceImpl(final String path, final ModuleImpl module) {
        if (path == null) {
            throw new IllegalArgumentException("Parameter 'path' cannot be null");
        }
        this.path = path;

        if (module == null) {
            throw new IllegalArgumentException("Parameter 'module' cannot be null");
        }
        this.module = module;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Module getModule() {
        return module;
    }

    @Override
    public List<Definition> getDefinitions() {
        return definitions;
    }

    public List<Definition> getModifiableDefinitions() {
        return modifiableDefinitions;
    }

    public void addWebFileBundleDefinition(final String name) {
        final WebFileBundleDefinitionImpl definition = new WebFileBundleDefinitionImpl(this, name);
        modifiableDefinitions.add(definition);
    }

    public void addDefinition(final ContentDefinitionImpl definition) {
        if (definition.getSource() != this) {
            throw new IllegalArgumentException("Definition does for this source");
        }
        for (Definition def : modifiableDefinitions) {
            if (def == definition) {
                throw new IllegalStateException("Definition already added to this source");
            }
        }
        modifiableDefinitions.add(definition);
    }

    @Override
    public String toString() {
        return "SourceImpl{" + "path='" + path + '\'' + ", module=" + module + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SourceImpl)) return false;
        if (!(this.getClass() == o.getClass())) return false;
        SourceImpl source = (SourceImpl) o;
        return Objects.equals(path, source.path) &&
                Objects.equals(module, source.module);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, module, this.getClass());
    }
}
