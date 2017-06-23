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

import org.apache.commons.lang3.StringUtils;
import org.onehippo.cm.model.DefinitionItem;

public abstract class DefinitionItemImpl extends ModelItemImpl implements DefinitionItem {

    private String path;
    private DefinitionNodeImpl parent;
    private ContentDefinitionImpl definition;
    private SourceLocationImpl sourceLocation;

    public DefinitionItemImpl(final String path, final String name, final ContentDefinitionImpl definition) {
        if (StringUtils.isBlank(path)) {
            throw new IllegalArgumentException("Item path must not be blank! name="+name);
        }

        // The global root is a special case
        if (StringUtils.isBlank(name) && !path.equals("/")) {
            throw new IllegalArgumentException("Item name must not be blank! path="+path);
        }

        this.path = path;
        this.name = name;
        this.parent = null;
        this.definition = definition;
        this.sourceLocation = new SourceLocationImpl();
    }

    public DefinitionItemImpl(final String path, final ContentDefinitionImpl definition) {
        this(path, StringUtils.substringAfterLast(path, "/"), definition);
    }

    public DefinitionItemImpl(final String name, final DefinitionNodeImpl parent) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Item name must not be blank! path="+path);
        }

        if (parent == null) {
            throw new IllegalArgumentException("Item parent can be null only if a Definition is provided!");
        }

        this.name = name;
        this.parent = parent;
        this.definition = parent.getDefinition();
        this.sourceLocation = new SourceLocationImpl();

        final String parentPath = parent.getPath();
        path = parentPath + (parentPath.endsWith("/") ? "" : "/") + name;
    }

    @Override
    public SourceLocationImpl getSourceLocation() {
        return sourceLocation;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public DefinitionNodeImpl getParent() {
        if (parent == null) {
            throw new IllegalStateException("Root node does not have a parent");
        }
        return parent;
    }

    @Override
    public boolean isRoot() {
        return parent == null;
    }

    @Override
    public ContentDefinitionImpl getDefinition() {
        return definition;
    }

    @Override
    public String getOrigin() {
        return getDefinition().getOrigin();
    }

}
