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
package org.hippoecm.frontend.plugins.console.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListItemModel;
import org.apache.wicket.markup.html.list.ListView;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.session.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class NodeTypesEditor extends CheckGroup {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";
    private static final long serialVersionUID = 1L;
    static final Logger log = LoggerFactory.getLogger(NodeTypesEditor.class);

    private JcrNodeModel nodeModel;

    NodeTypesEditor(String id, List<String> nodeTypes, JcrNodeModel nodeModel) {
        super(id, nodeTypes);
        this.nodeModel = nodeModel;
        
        add(new ListView("type", getAllNodeTypes()) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem item) {
                ListItemModel model = (ListItemModel) item.getModel();

                Check check = new Check("check", model);
                item.add(check);

                String type = (String) model.getObject();
                check.add(new Label("name", type));
            }
        });
    }

    @Override
    public void onModelChanged() {

    }

    @Override
    protected void onSelectionChanged(Collection selection) {
        if (getModelObject() instanceof List && nodeModel != null) {
            try {
                Set<String> actualTypes = new HashSet<String>();
                NodeType[] nodeTypes = nodeModel.getNode().getMixinNodeTypes();
                for (NodeType nodeType : nodeTypes) {
                    actualTypes.add(nodeType.getName());
                }

                HashSet toBeAdded = new HashSet<String>(selection);
                toBeAdded.removeAll(actualTypes);
                for (String add : new ArrayList<String>(toBeAdded)){
                    nodeModel.getNode().addMixin(add);
                }

                actualTypes.removeAll(new HashSet<String>(selection));
                for (String remove : new ArrayList<String>(actualTypes)){
                    nodeModel.getNode().removeMixin(remove);
                }
            } catch (RepositoryException e) {
                log.error(e.getMessage());
            }
        }
    }

    @Override
    protected boolean wantOnSelectionChangedNotifications() {
        return true;
    }

    // (package) privates

    private List<String> getAllNodeTypes() {
        List<String> list = new ArrayList<String>();
        try {
            UserSession session = (UserSession) getSession();
            NodeTypeManager ntmgr = session.getJcrSession().getWorkspace().getNodeTypeManager();
            NodeTypeIterator iterator = ntmgr.getMixinNodeTypes();
            while (iterator.hasNext()) {
                list.add(iterator.nextNodeType().getName());
            }
        } catch (RepositoryException e) {
            log.error(e.getMessage());
        }
        Collections.sort(list);
        return list;
    }

    void setNodeModel(JcrNodeModel newModel) {
        this.nodeModel = newModel;
    }
}
