/*
 * Copyright 2008 Hippo
 *
 * Licensed under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hippoecm.frontend.sa.template.plugins.field;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.IClusterable;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.hippoecm.frontend.sa.plugin.IPlugin;
import org.hippoecm.frontend.sa.plugin.IPluginContext;
import org.hippoecm.frontend.sa.plugin.config.IPluginConfig;
import org.hippoecm.frontend.sa.plugin.impl.RenderPlugin;
import org.hippoecm.frontend.sa.plugin.impl.ListViewPlugin;
import org.hippoecm.frontend.sa.service.ServiceTracker;
import org.hippoecm.frontend.sa.template.FieldDescriptor;
import org.hippoecm.frontend.sa.template.ITemplateConfig;
import org.hippoecm.frontend.sa.template.ITemplateEngine;
import org.hippoecm.frontend.sa.template.TypeDescriptor;
import org.hippoecm.frontend.sa.template.model.AbstractProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FieldPlugin<P extends IModel, C extends IModel> extends ListViewPlugin {
    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(FieldPlugin.class);

    public static final String FIELD = "field";

    protected String mode;
    protected FieldDescriptor field;
    protected AbstractProvider<C> provider;

    private ServiceTracker<ITemplateEngine> engineTracker;
    private String fieldName;
    private TemplateController controller;

    protected FieldPlugin() {
        engineTracker = new ServiceTracker<ITemplateEngine>(ITemplateEngine.class);
        controller = new TemplateController();
    }

    @Override
    public void init(IPluginContext context, IPluginConfig config) {
        engineTracker.open(context, config.getString(ITemplateEngine.ENGINE));

        mode = config.getString(ITemplateEngine.MODE);
        fieldName = config.getString(FieldPlugin.FIELD);

        super.init(context, config);
    }

    @Override
    public void destroy() {
        controller.stop();
        super.destroy();
        engineTracker.close();
    }

    @Override
    public void onModelChanged() {
        super.onModelChanged();

        ITemplateEngine engine = engineTracker.getService();
        if (engine != null) {
            P model = (P) getModel();
            TypeDescriptor type = engine.getType(model);
            field = type.getField(fieldName);
            if (field != null) {
                TypeDescriptor subType = engine.getType(field.getType());
                controller.stop();
                provider = newProvider(field, subType, model);
                controller.start(provider);
            } else {
                log.warn("Unknown field {} in type {}", field, type.getName());
            }
        } else {
            log.warn("No engine found to display new model");
        }
    }

    protected abstract AbstractProvider<C> newProvider(FieldDescriptor descriptor, TypeDescriptor type, P parentModel);

    public void onAddItem(AjaxRequestTarget target) {
        provider.addNew();

        controller.update();

        // refresh
        modelChanged();
    }

    public void onRemoveItem(C childModel, AjaxRequestTarget target) {
        provider.remove(childModel);

        controller.update();

        // refresh
        modelChanged();
    }

    public void onMoveItemUp(C model, AjaxRequestTarget target) {
        provider.moveUp(model);

        // FIXME: support reordering
        // controller.update();
        modelChanged();
    }

    protected ITemplateEngine getTemplateEngine() {
        return engineTracker.getService();
    }

    protected void configureTemplate(ITemplateConfig config, C model) {
        final IPluginConfig myConfig = getPluginConfig();

        for (String property : config.getPropertyKeys()) {
            Object value = myConfig.get("template." + property);
            if (value != null) {
                config.put(property, value);
            }
        }

        config.put(RenderPlugin.WICKET_ID, myConfig.getString(ITEM));
        config.put(RenderPlugin.DIALOG_ID, myConfig.getString(RenderPlugin.DIALOG_ID));
    }

    private class TemplateController implements IClusterable {
        private static final long serialVersionUID = 1L;

        private Map<C, IPlugin> plugins;

        TemplateController() {
            plugins = new HashMap<C, IPlugin>();
        }

        void start(AbstractProvider<C> provider) {
            Iterator<C> iter = provider.iterator(0, provider.size());
            while (iter.hasNext()) {
                addModel(iter.next());
            }
        }

        void update() {
            Set<C> current = Collections.unmodifiableSet(plugins.keySet());
            Iterator<C> iter = provider.iterator(0, provider.size());
            while (iter.hasNext()) {
                C model = iter.next();
                if (!current.contains(model)) {
                    addModel(model);
                }
            }
            for (C model : current) {
                if (!plugins.containsKey(model)) {
                    removeModel(model);
                }
            }
        }

        void stop() {
            for (C model : Collections.unmodifiableSet(plugins.keySet())) {
                removeModel(model);
            }
        }

        private void addModel(final C model) {
            ITemplateEngine engine = getTemplateEngine();
            ITemplateConfig config = engine.getTemplate(engine.getType(field.getType()), mode);
            FieldPlugin.this.configureTemplate(config, model);
            IPlugin plugin = engine.start(config, model);
            plugins.put(model, plugin);
        }

        private void removeModel(C model) {
            IPlugin plugin = plugins.remove(model);
            if (plugin != null) {
                plugin.stop();
            }
        }
    }

}
