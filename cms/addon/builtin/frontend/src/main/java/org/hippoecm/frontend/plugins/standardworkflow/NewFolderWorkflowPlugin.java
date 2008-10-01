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
package org.hippoecm.frontend.plugins.standardworkflow;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.hippoecm.frontend.dialog.AbstractDialog;
import org.hippoecm.frontend.dialog.AbstractWorkflowDialog;
import org.hippoecm.frontend.dialog.DialogAction;
import org.hippoecm.frontend.dialog.IDialogFactory;
import org.hippoecm.frontend.dialog.IDialogService;
import org.hippoecm.frontend.model.WorkflowsModel;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.session.UserSession;
import org.hippoecm.repository.api.WorkflowManager;
import org.hippoecm.repository.standardworkflow.FolderWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NewFolderWorkflowPlugin extends NewAbstractFolderWorkflowPlugin {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";
    private static final long serialVersionUID = 1L;
    transient Logger log = LoggerFactory.getLogger(NewFolderWorkflowPlugin.class);

    public NewFolderWorkflowPlugin(IPluginContext context, IPluginConfig config) {
        super(context, config);

        DialogAction action = new DialogAction(new IDialogFactory() {
            private static final long serialVersionUID = 1L;

            public AbstractDialog createDialog(IDialogService dialogService) {
                // FIXME: fixed (in code) dialog text
                String text = "Are you sure you want to delete ";
                try {
                    text += "folder ";
                    text += ((WorkflowsModel) NewFolderWorkflowPlugin.this.getModel()).getNodeModel().getNode()
                            .getName();
                } catch (RepositoryException ex) {
                    text += "this folder";
                }
                text += " and all of its contents permanently?";
                return new AbstractWorkflowDialog(NewFolderWorkflowPlugin.this, dialogService, "Delete folder", text) {
                    @Override
                    protected void execute() throws Exception {
                        // FIXME: this assumes that folders are always embedded in other folders
                        // and there is some logic here to look up the parent.  The real solution is
                        // in the visual component to merge two workflows.
                        WorkflowsModel model = (WorkflowsModel) NewFolderWorkflowPlugin.this.getModel();
                        Node node = model.getNodeModel().getNode();
                        WorkflowManager manager = ((UserSession) Session.get()).getWorkflowManager();
                        FolderWorkflow workflow = (FolderWorkflow) manager.getWorkflow("embedded", node.getParent());
                        workflow.delete(node.getName());
                    }
                };
            }
        }, getDialogService());

        addWorkflowAction("Delete Folder", "editmodel_ico", null, action);
    }

    @Override
    protected Component createDialogLinksComponent() {
        return new WorkflowActionComponentDropDownChoice(DIALOG_LINKS_COMPONENT_ID, templates);
    }
}
