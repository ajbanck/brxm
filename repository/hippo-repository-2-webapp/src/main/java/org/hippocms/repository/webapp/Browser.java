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
package org.hippocms.repository.webapp;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.wicket.markup.html.WebPage;
import org.hippocms.repository.webapp.editor.EditorPanel;
import org.hippocms.repository.webapp.menu.Menu;
import org.hippocms.repository.webapp.model.JcrNodeModel;
import org.hippocms.repository.webapp.model.JcrSessionProvider;
import org.hippocms.repository.webapp.tree.TreePanel;

public class Browser extends WebPage {
    private static final long serialVersionUID = 1L;
    
    private EditorPanel editorPanel;
    private TreePanel treePanel;
    private Menu menu;

    public Browser() throws RepositoryException {
        setOutputMarkupId(true);
        
        JcrSessionProvider sessionProvider = (JcrSessionProvider)getSession();
        Node root = sessionProvider.getSession().getRootNode();
        JcrNodeModel model = new JcrNodeModel(root);

        treePanel = new TreePanel("treePanel", model);
        add(treePanel);
        
        editorPanel = new EditorPanel("editorPanel", model);
        add(editorPanel);
        
        menu = new Menu("menu", model); 
        add(menu);
    }

    public EditorPanel getEditorPanel() {
        return editorPanel;
    }

    public TreePanel getTreePanel() {
        return treePanel;
    }

    public Menu getMenu() {
        return menu;
    }

}
