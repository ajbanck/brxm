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
package org.hippoecm.frontend.plugins.admin.linkpicker;

import java.util.List;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.hippoecm.frontend.dialog.DialogWindow;
import org.hippoecm.frontend.dialog.lookup.InfoPanel;
import org.hippoecm.frontend.dialog.lookup.LookupDialog;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.model.tree.JcrTreeNode;
import org.hippoecm.frontend.plugin.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkPickerDialog extends LookupDialog {
    private static final long serialVersionUID = 1L;

    static final Logger log = LoggerFactory.getLogger(LinkPickerDialog.class);
    
    private List<String> nodetypes;
    public LinkPickerDialog(DialogWindow dialogWindow, Channel channel, List<String> nodetypes) {
        super("LinkPicker", new JcrTreeNode(dialogWindow.getNodeModel().findRootModel()),
              dialogWindow, channel);
        this.nodetypes = nodetypes;
    }
    
    @Override
    protected InfoPanel getInfoPanel(DialogWindow dialogWindow) {
        JcrNodeModel nodeModel = dialogWindow.getNodeModel();
        InfoPanel infoPanel = new LinkPickerDialogInfoPanel("info", nodeModel);
        add(infoPanel);
        if (nodeModel.getNode() == null) {
            ok.setVisible(false);
        }
        return infoPanel;
    }
    
    @Override
    public void ok() throws RepositoryException {
        JcrNodeModel sourceNodeModel = this.dialogWindow.getNodeModel();
        if (sourceNodeModel.getParentModel() != null) {
            JcrNodeModel targetNodeModel = getSelectedNode().getNodeModel();
            Node targetNode = targetNodeModel.getNode();
            boolean validType = false;
            for(int i = 0 ; i < nodetypes.size() ; i++){
                if(targetNode.isNodeType(nodetypes.get(i))){
                    validType = true;
                    break;
                }
            }
            if(validType) {
                String targetPath = targetNodeModel.getNode().getPath();
                sourceNodeModel.getNode().setProperty("hippo:docbase", targetPath);
            }
        }
    }

    @Override
    public void cancel() {
    }

    

    
}
