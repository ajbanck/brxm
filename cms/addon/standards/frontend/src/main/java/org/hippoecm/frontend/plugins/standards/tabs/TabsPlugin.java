/*
 * Copyright 2007 Hippo
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
package org.hippoecm.frontend.plugins.standards.tabs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.hippoecm.frontend.dialog.DialogPageCreator;
import org.hippoecm.frontend.dialog.DialogWindow;
import org.hippoecm.frontend.model.ExceptionModel;
import org.hippoecm.frontend.model.IPluginModel;
import org.hippoecm.frontend.model.JcrItemModel;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.model.PluginModel;
import org.hippoecm.frontend.plugin.Plugin;
import org.hippoecm.frontend.plugin.PluginDescriptor;
import org.hippoecm.frontend.plugin.PluginFactory;
import org.hippoecm.frontend.plugin.channel.Channel;
import org.hippoecm.frontend.plugin.channel.Notification;
import org.hippoecm.frontend.plugin.channel.Request;
import org.hippoecm.frontend.plugin.parameters.ParameterValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @deprecated use org.hippoecm.frontend.plugins.standards.sa.tabs.* instead
 */
@Deprecated
public class TabsPlugin extends Plugin {
    private static final long serialVersionUID = 1L;

    static final Logger log = LoggerFactory.getLogger(TabsPlugin.class);

    public static final int MAX_TAB_TITLE_LENGTH = 11;

    private Map<JcrNodeModel, Tab> editors;
    private TabbedPanel tabbedPanel;
    private int selectCount;

    private String editPerspective;
    private PluginDescriptor editDescriptor;
    private DialogWindow dialogWindow;
    private OnCloseDialog onCloseDialog;

    public TabsPlugin(PluginDescriptor pluginDescriptor, IPluginModel model, Plugin parentPlugin) {
        super(pluginDescriptor, model, parentPlugin);

        editors = new HashMap<JcrNodeModel, Tab>();
        List<Tab> tabs = new ArrayList<Tab>();
        tabbedPanel = new TabbedPanel("tabs", tabs, this);

        add(tabbedPanel);

        Channel proxy = getPluginManager().getChannelFactory().createChannel();
        dialogWindow = new DialogWindow("onclose", null, getBottomChannel(), proxy);
        add(dialogWindow);

        List<String> parameter = pluginDescriptor.getParameter("editor").getStrings();
        if (parameter != null && parameter.size() > 0) {
            editPerspective = parameter.get(0);
        } else {
            editPerspective = null;
        }

        selectCount = 0;
    }

    // invoked by the TabbedPanel when a tab is selected    
    protected void onSelect(AjaxRequestTarget target, Tab tabbie) {
        tabbie.select();

        // notify children of focus event
        Channel channel = getBottomChannel();
        if (channel != null) {
            PluginModel model = new PluginModel();
            model.put("plugin", tabbie.getPlugin().getPluginPath());
            Notification notification = channel.createNotification("focus", model);
            channel.publish(notification);
            notification.getContext().apply(target);
        }

        if (target != null) {
            target.addComponent(tabbedPanel);
        }
    }

    protected void onClose(AjaxRequestTarget target, Tab tabbie) {
        JcrNodeModel closedJcrNodeModel = null;
        ArrayList<JcrNodeModel> jcrNewNodeModelList = new ArrayList<JcrNodeModel>();
        if (editors.containsValue(tabbie)) {
            for (Map.Entry<JcrNodeModel, Tab> entry : editors.entrySet()) {
                if (entry.getValue().equals(tabbie)) {
                    closedJcrNodeModel = entry.getKey();
                } else if (((JcrNodeModel) entry.getKey()).getNode().isNew()) {
                    // keep track of new added nodes, because if child of the closed tab
                    // they need to be closed as well if the parent is *not* saved
                    jcrNewNodeModelList.add(entry.getKey());
                }
            }
        }
        if (closedJcrNodeModel != null) {
            try {
                if (closedJcrNodeModel.getNode().getSession().hasPendingChanges()) {
                    dialogWindow.setModel(closedJcrNodeModel);
                    onCloseDialog = new OnCloseDialog(dialogWindow, jcrNewNodeModelList, editors);
                    dialogWindow.setPageCreator(new DialogPageCreator(onCloseDialog));
                    dialogWindow.show(target);
                } else {
                    // safe to close without informing and without saving
                    tabbie.destroy();
                }
            } catch (RepositoryException e) {
                if (target != null) {
                    Channel channel = getTopChannel();
                    Request request = channel.createRequest("exception", new ExceptionModel(e));
                    channel.send(request);
                    request.getContext().apply(target);
                }
                log.info(e.getClass().getName() + ": " + e.getMessage());
            }
        }
        if (target != null) {
            target.addComponent(this);
        }
    }

    @Override
    public Plugin addChild(final PluginDescriptor childDescriptor) {
        if (editPerspective == null || !editPerspective.equals(childDescriptor.getWicketId())) {
            Tab tab = new Tab(childDescriptor, getPluginModel(), false);
            return tab.getPlugin();
        }
        editDescriptor = childDescriptor;
        return null;
    }

    @Override
    public void handle(Request request) {
        if ("edit".equals(request.getOperation())) {
            JcrNodeModel model = new JcrNodeModel(request.getModel());
            if (!editors.containsKey(model) && editDescriptor != null) {
                // create a descriptor for the plugin
                PluginDescriptor descriptor = editDescriptor.clone();
                try {
                    // set the title to the name of the node
                    List<String> titleParam = new LinkedList<String>();
                    titleParam.add(model.getNode().getName());
                    descriptor.addParameter("title", new ParameterValue(titleParam));
                } catch (RepositoryException ex) {
                    log.error("Couldn't obtain name of item " + ex.getMessage());
                }

                // add the plugin
                Tab tabbie = new Tab(descriptor, model, true);
                editors.put(model, tabbie);
                request.getContext().addRefresh(this);

                tabbie.getPlugin().addChildren();
            }

            // notify children; if tabs should be switched,
            // they should send a focus request.
            Channel channel = getBottomChannel();
            if (channel != null) {
                Notification notification = channel.createNotification(request);
                channel.publish(notification);
            }

            // don't send request to parent
            return;
        } else if ("focus".equals(request.getOperation())) {
            String pluginPath = (String) request.getModel().getMapRepresentation().get("plugin");
            Tab tab = getPluginTab(pluginPath);
            if (tab != null) {
                tab.select();
                request.getContext().addRefresh(this);

                // notify children of focus event
                Channel channel = getBottomChannel();
                if (channel != null) {
                    Notification notification = channel.createNotification(request);
                    channel.publish(notification);
                }
                return;
            }
        } else if ("close".equals(request.getOperation())) {
            String pluginPath = (String) request.getModel().getMapRepresentation().get("plugin");
            Tab tab = getPluginTab(pluginPath);
            if (tab != null) {
                tab.destroy();
                request.getContext().addRefresh(this);
                return;
            }
        } else if ("flush".equals(request.getOperation())) {
            JcrNodeModel model = new JcrNodeModel(request.getModel());
            for (Map.Entry<JcrNodeModel, Tab> entry : editors.entrySet()) {
                JcrItemModel itemModel = entry.getKey().getItemModel();
                if (itemModel.getPath().startsWith(model.getItemModel().getPath())) {
                    if (!itemModel.exists()) {
                        entry.getValue().destroy();
                    }
                }
            }
        }
        super.handle(request);
    }

    @Override
    public void onDetach() {
        Iterator<Tab> tabIter = tabbedPanel.getTabs().iterator();
        while (tabIter.hasNext()) {
            Tab tabbie = tabIter.next();
            tabbie.detach();
        }
        super.onDetach();
    }

    Tab getPluginTab(String pluginPath) {
        Plugin parent = this;
        while (parent.getParentPlugin() != null) {
            parent = parent.getParentPlugin();
        }
        Plugin plugin = parent.getChildPlugin(pluginPath);

        Iterator<Tab> tabIter = tabbedPanel.getTabs().iterator();
        while (tabIter.hasNext()) {
            Tab tabbie = tabIter.next();
            if (tabbie.getPlugin().equals(plugin)) {
                return tabbie;
            }
        }
        return null;
    }

    // tab implementation that manages a plugin and keeps track of the previously
    // selected tab.  (that one is selected when the current tab is destroyed)
    // Things will go wrong after 2<sup>31</sup>-1 tabs have been selected.
    class Tab implements ITab {
        private static final long serialVersionUID = 1L;

        Plugin plugin;
        int lastSelected;
        boolean close;

        Tab(PluginDescriptor descriptor, IPluginModel model, boolean close) {
            this.close = close;

            descriptor.setWicketId(TabbedPanel.TAB_PANEL_ID);
            PluginFactory pluginFactory = new PluginFactory(getPluginManager());
            plugin = pluginFactory.createPlugin(descriptor, model, TabsPlugin.this);

            // add to the list of tabs in the tabbed panel
            tabbedPanel.getTabs().add(this);
        }

        // implement ITab interface

        public Model getTitle() {
            PluginDescriptor descriptor = getPlugin().getDescriptor();
            String title = descriptor.getWicketId();
            ParameterValue param = descriptor.getParameter("title");

            if (param != null && param.getType() != param.TYPE_UNKNOWN) {
                String fulltitle = param.getStrings().get(0).toString();
                title = (fulltitle.length() < MAX_TAB_TITLE_LENGTH ? fulltitle : fulltitle.substring(0, MAX_TAB_TITLE_LENGTH-2) + "..");
            }
            return new Model(title);
        }

        public Panel getPanel(String panelId) {
            assert (panelId.equals(TabbedPanel.TAB_PANEL_ID));

            return getPlugin();
        }

        // package internals

        Plugin getPlugin() {
            return plugin;
        }

        boolean canClose() {
            return close;
        }

        void select() {
            tabbedPanel.setSelectedTab(tabbedPanel.getTabs().indexOf(this));
            lastSelected = ++TabsPlugin.this.selectCount;
        }

        void destroy() {
            if (editors.containsValue(this)) {
                for (Map.Entry<JcrNodeModel, Tab> entry : editors.entrySet()) {
                    if (entry.getValue().equals(this)) {
                        editors.remove(entry.getKey());
                        break;
                    }
                }
            }
            tabbedPanel.getTabs().remove(this);

            // let plugin clean up any resources
            getPlugin().destroy();

            // look for previously selected tab
            int lastCount = 0;
            Tab lastTab = null;
            Iterator<Tab> tabs = tabbedPanel.getTabs().iterator();
            while (tabs.hasNext()) {
                Tab tabbie = tabs.next();
                if (tabbie.lastSelected > lastCount) {
                    lastCount = tabbie.lastSelected;
                    lastTab = tabbie;
                }
            }
            if (lastTab != null) {
                lastTab.select();
            }

        }

        public void detach() {
            plugin.detach();
        }

        public void popup(AjaxRequestTarget target, RepositoryException e) {
            if (target != null) {
                Channel channel = getTopChannel();
                Request request = channel.createRequest("exception", new ExceptionModel(e));
                channel.send(request);
                request.getContext().apply(target);
            }
            log.info(e.getClass().getName() + ": " + e.getMessage());
        }
    }

}
