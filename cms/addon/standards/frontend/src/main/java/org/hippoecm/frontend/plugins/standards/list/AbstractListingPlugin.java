/*
 *  Copyright 2008 Hippo.
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
package org.hippoecm.frontend.plugins.standards.list;

import java.util.Iterator;

import javax.jcr.Node;

import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.IModel;
import org.hippoecm.frontend.PluginRequestTarget;
import org.hippoecm.frontend.model.IModelReference;
import org.hippoecm.frontend.model.event.IEvent;
import org.hippoecm.frontend.model.event.IObservable;
import org.hippoecm.frontend.model.event.IObserver;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.plugins.standards.icon.BrowserStyle;
import org.hippoecm.frontend.plugins.standards.list.datatable.ListDataTable;
import org.hippoecm.frontend.plugins.standards.list.datatable.ListPagingDefinition;
import org.hippoecm.frontend.plugins.standards.list.datatable.ListDataTable.TableSelectionListener;
import org.hippoecm.frontend.service.render.RenderPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for displaying a list of nodes.   This class will take care of observing the
 * provider, instantiating the datatable.  Subclasses must provide a table definition and a
 * data provider.  
 */
public abstract class AbstractListingPlugin<T> extends RenderPlugin<T> implements TableSelectionListener<Node> {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(AbstractListingPlugin.class);

    private final IModelReference<Node> documentReference;
    protected ListDataTable<Node> dataTable;
    private ListPagingDefinition pagingDefinition;
    private ISortableDataProvider<Node> provider;
    private IObserver<?> providerObserver;

    @SuppressWarnings("unchecked")
    public AbstractListingPlugin(IPluginContext context, IPluginConfig config) {
        super(context, config);

        if (config.getString("model.document") != null) {
            documentReference = context.getService(config.getString("model.document"), IModelReference.class);
        } else {
            documentReference = null;
            log.warn("No document model service configured (model.document)");
        }

        add(new EmptyPanel("table"));
        
        add(BrowserStyle.getStyleSheet());
    }

    protected IModel<Node> getSelectedModel() {
        if(documentReference != null) {
            return documentReference.getModel();
        }
        return null;
    }

    protected void setSelectedModel(IModel<Node> model){
        if(documentReference != null) {
            documentReference.setModel(model);
        }
    }

    private void init() {
        IPluginConfig config = getPluginConfig();
        IPluginContext context = getPluginContext();

        pagingDefinition = new ListPagingDefinition(config);
        dataTable = getListDataTable("table", getTableDefinition(), getDataProvider(), this, isOrderable(),
                pagingDefinition);
        replace(dataTable);
        dataTable.init(context);

        if (documentReference != null) {
            context.registerService(new IObserver<IModelReference<Node>>() {
                private static final long serialVersionUID = 1L;

                public IModelReference<Node> getObservable() {
                    return documentReference;
                }

                public void onEvent(Iterator<? extends IEvent<IModelReference<Node>>> event) {
                    updateSelection(documentReference.getModel());
                }

            }, IObserver.class.getName());
        }
    }
    
    protected ListDataTable<Node> getListDataTable(String id, TableDefinition<Node> tableDefinition,
                                                   ISortableDataProvider<Node> dataProvider, TableSelectionListener<Node> selectionListener, final boolean triState,
                                                   ListPagingDefinition pagingDefinition) {
        return new ListDataTable<Node>(id, tableDefinition, dataProvider, selectionListener, triState, pagingDefinition);
    }

    private ISortableDataProvider<Node> getDataProvider() {
        if (provider == null) {
            provider = newDataProvider();
            if (provider instanceof IObservable) {
                providerObserver = new IObserver<IObservable>() {
                    private static final long serialVersionUID = 1L;

                    public IObservable getObservable() {
                        return (IObservable) provider;
                    }

                    public void onEvent(Iterator<? extends IEvent<IObservable>> event) {
                        redraw();
                    }

                };
                getPluginContext().registerService(providerObserver, IObserver.class.getName());
            }
        }
        return provider;
    }

    private void dumpDataProvider() {
        if (provider != null) {
            if (providerObserver != null) {
                getPluginContext().unregisterService(providerObserver, IObserver.class.getName());
                providerObserver = null;
            }
            provider = null;
        }
    }

    protected abstract ISortableDataProvider<Node> newDataProvider();

    protected abstract TableDefinition<Node> getTableDefinition();

    @SuppressWarnings("unchecked")
    public void selectionChanged(IModel model) {
        IPluginConfig config = getPluginConfig();
        if (config.getString("model.document") != null) {
            IModelReference<IModel> documentService = getPluginContext().getService(config.getString("model.document"),
                    IModelReference.class);
            if (documentService != null) {
                documentService.setModel(model);
                if (model != dataTable.getDefaultModel() && (model == null || !model.equals(dataTable.getDefaultModel()))) {
                    log.info("Did not receive model change notification for model.document ({})", config
                            .getString("model.document"));
                    updateSelection(model);
                }
            } else {
                updateSelection(model);
            }
        } else {
            updateSelection(model);
        }
    }

    public void updateSelection(IModel<Node> model) {
        dataTable.setDefaultModel(model);
        onSelectionChanged(model);
    }

    protected void onSelectionChanged(IModel<Node> model) {
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onModelChanged() {
        if (dataTable == null) {
            return;
        }

        dataTable.destroy();
        dumpDataProvider();

        dataTable = getListDataTable("table", getTableDefinition(), getDataProvider(), this, isOrderable(),
                pagingDefinition);
        replace(dataTable);
        dataTable.init(getPluginContext());

        IPluginConfig config = getPluginConfig();
        if (config.getString("model.document") != null) {
            IModelReference<IModel> documentService = getPluginContext().getService(config.getString("model.document"),
                    IModelReference.class);
            if (documentService != null) {
                dataTable.setDefaultModel(documentService.getModel());
            }
        }
        redraw();
    }

    @Override
    public void render(PluginRequestTarget target) {
        super.render(target);
        if (dataTable == null) {
            init();
        }
        dataTable.render(target);
    }

    protected boolean isOrderable() {
        return false;
    }

}
