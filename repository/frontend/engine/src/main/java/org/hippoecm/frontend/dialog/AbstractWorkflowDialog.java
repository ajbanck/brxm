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
package org.hippoecm.frontend.dialog;

import java.util.Map;

import javax.jcr.RepositoryException;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.hippoecm.frontend.IStringResourceProvider;
import org.hippoecm.frontend.i18n.SearchingTranslatorPlugin;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.model.WorkflowsModel;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.workflow.AbstractWorkflowPlugin;
import org.hippoecm.frontend.service.IJcrService;
import org.hippoecm.frontend.service.ITranslateService;
import org.hippoecm.frontend.session.UserSession;
import org.hippoecm.repository.api.HippoNodeType;
import org.hippoecm.repository.api.MappingException;
import org.hippoecm.repository.api.Workflow;
import org.hippoecm.repository.api.WorkflowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A dialog operating in a workflow context. Each workflow action should
 * extend this class and implement the doOk() method.
 *
 */
public abstract class AbstractWorkflowDialog extends AbstractDialog implements IStringResourceProvider {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";

    static protected Logger log = LoggerFactory.getLogger(AbstractWorkflowDialog.class);

    private WorkflowsModel model;
    private AbstractWorkflowPlugin plugin;
    private ITranslateService translator;

    public AbstractWorkflowDialog(AbstractWorkflowPlugin plugin, IModel message) {
        this.model = (WorkflowsModel) plugin.getModel();
        this.plugin = plugin;

        if (model.getNodeModel().getNode() == null) {
            ok.setVisible(false);
        }

        Label notification = new Label("notification");
        if (message != null) {
            notification.setModel(message);
        } else {
            notification.setVisible(false);
        }
        add(notification);

        // FIXME: refactor the plugin so that we can use a service instead here
        IPluginContext context = plugin.getPluginContext();
        translator = new SearchingTranslatorPlugin(context, null);
    }

    public AbstractWorkflowDialog(AbstractWorkflowPlugin plugin) {
        this(plugin, null);
    }

    @Override
    public void onDetach() {
        model.detach();
        super.onDetach();
    }

    public String getString(Map<String, String> criteria) {
        return translator.translate(criteria);
    }

    protected AbstractWorkflowPlugin getPlugin() {
        return plugin;
    }

    protected Workflow getWorkflow() {
        try {
            WorkflowManager manager = ((UserSession) Session.get()).getWorkflowManager();
            return manager.getWorkflow(model.getWorkflowDescriptor());
        } catch (MappingException e) {
            log.error(e.getMessage());
        } catch (RepositoryException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    @Override
    protected void onOk() {
        try {
            JcrNodeModel handle = model.getNodeModel();
            while (handle != null && !handle.getNode().isNodeType(HippoNodeType.NT_HANDLE)) {
                handle = handle.getParentModel();
            }
            if (handle == null) {
                handle = model.getNodeModel().getParentModel();
                if (handle == null) {
                    handle = model.getNodeModel();
                }
            }
            handle.getNode().getSession().save();
            execute();

            ((UserSession) Session.get()).getJcrSession().refresh(true);

            IJcrService jcrService = plugin.getPluginContext().getService(IJcrService.class.getName(),
                    IJcrService.class);
            if (jcrService != null) {
                jcrService.flush(handle);
            }
        } catch (Exception e) {
            String msg = e.getClass().getName() + ": " + e.getMessage();
            log.error(msg);
            error(msg);
            if (log.isDebugEnabled()) {
                log.debug("Error from repository: ", e);
            }
        }
    }

    /**
     * This abstract method is called from ok() and should implement
     * the action to be performed when the dialog's ok button is clicked.
     */
    protected abstract void execute() throws Exception;

}
