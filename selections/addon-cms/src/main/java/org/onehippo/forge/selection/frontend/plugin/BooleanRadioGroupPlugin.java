/*
 * Copyright 2010-2019 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onehippo.forge.selection.frontend.plugin;

import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.onehippo.forge.selection.frontend.model.ListItem;
import org.onehippo.forge.selection.frontend.model.ValueList;

/**
 * Boolean Radio Group plugin. The radio group has two fixed items, namely true and false. The type of the underlying
 * property must be boolean.
 * <p>
 * The labels for the true and false values can be populated by a ValueList, through the source property. This gives the
 * option to display localized values.
 *
 * @author Dennis Dam
 */
public class BooleanRadioGroupPlugin extends RadioGroupPlugin {

    private ValueList valueList;

    /**
     * Constructor.
     *
     * @param context plugin context
     * @param config  plugin config
     */
    public BooleanRadioGroupPlugin(IPluginContext context, IPluginConfig config) {
        super(context, config);
    }

    /**
     * Returns a value list with two items, one for 'true' and one for 'false'.
     *
     * @return value list
     */
    @Override
    protected ValueList getValueList() {
        if (valueList == null) {
            valueList = new ValueList();
            final ValueList sourceList = super.getValueList();

            valueList.add(listItem(sourceList, "true", "true"));
            valueList.add(listItem(sourceList, "false", "false"));
        }

        return valueList;
    }

    private ListItem listItem(final ValueList sourceList, final String key, final String defaultValue) {
        final ListItem listItem = sourceList.getListItemByKey(key);
        if (listItem != null) {
            return listItem;
        }
        return new ListItem(key, defaultValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setSelectedItem(final ListItem listItem) {
        final String key = listItem.getKey();
        getValueModel().setObject(Boolean.parseBoolean(key));
    }

}
