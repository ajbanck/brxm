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
package org.onehippo.cm.impl.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.onehippo.cm.api.model.Module;
import org.onehippo.cm.api.model.Project;
import org.onehippo.cm.api.model.Source;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

public class ModuleImpl implements Module {

    private String name;
    private Project project;
    private List<String> after = new ArrayList<>();
    private Map<String, SourceImpl> modifiableSources = new LinkedHashMap<>();
    private Map<String, Source> sources = Collections.unmodifiableMap(modifiableSources);
    private List<ContentDefinitionImpl> sortedContentDefinitions = new LinkedList<>();

    public ModuleImpl(final String name, final ProjectImpl project) {
        if (name == null) {
            throw new IllegalArgumentException("Parameter 'name' cannot be null");
        }
        this.name = name;

        if (project == null) {
            throw new IllegalArgumentException("Parameter 'project' cannot be null");
        }
        this.project = project;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Project getProject() {
        return project;
    }

    @Override
    public List<String> getAfter() {
        return unmodifiableList(after);
    }

    public ModuleImpl setAfter(final List<String> after) {
        this.after = new ArrayList<>(after);
        return this;
    }

    @Override
    public Map<String, Source> getSources() {
        return sources;
    }

    public Map<String, SourceImpl> getModifiableSources() {
        return modifiableSources;
    }

    public SourceImpl addSource(final String path) {
        final SourceImpl source = new SourceImpl(path, this);
        modifiableSources.put(path, source);
        return source;
    }

    public List<ContentDefinitionImpl> getSortedContentDefinitions() {
        return sortedContentDefinitions;
    }

    public void setSortedContentDefinitions(final List<ContentDefinitionImpl> sortedContentDefinitions) {
        this.sortedContentDefinitions = sortedContentDefinitions;
    }
}
