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

import java.util.LinkedList;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.DefaultMarkupCacheKeyProvider;
import org.apache.wicket.markup.DefaultMarkupResourceStreamProvider;
import org.apache.wicket.markup.IMarkupCacheKeyProvider;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.MarkupNotFoundException;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDialog extends Form implements IDialogService.Dialog {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";
    static final Logger log = LoggerFactory.getLogger(AbstractDialog.class);

    @SuppressWarnings("unchecked")
    private class Container extends Panel implements IMarkupCacheKeyProvider, IMarkupResourceStreamProvider {
        private static final long serialVersionUID = 1L;

        private IMarkupCacheKeyProvider cacheKeyProvider;
        private IMarkupResourceStreamProvider streamProvider;

        public Container(String id) {
            super(id);
            cacheKeyProvider = new DefaultMarkupCacheKeyProvider();
            streamProvider = new DefaultMarkupResourceStreamProvider();
        }

        public String getCacheKey(MarkupContainer container, Class containerClass) {
            return cacheKeyProvider.getCacheKey(AbstractDialog.this, AbstractDialog.this.getClass());
        }

        // implement IMarkupResourceStreamProvider.
        public IResourceStream getMarkupResourceStream(MarkupContainer container, Class containerClass) {
            return streamProvider.getMarkupResourceStream(AbstractDialog.this, AbstractDialog.this.getClass());
        }

        // used for markup inheritance (<wicket:extend />)
        public MarkupStream getAssociatedMarkupStream(final boolean throwException) {
            try {
                return getApplication().getMarkupSettings().getMarkupCache().getMarkupStream(AbstractDialog.this,
                        false, throwException);
            } catch (MarkupException ex) {
                // re-throw it. The exception contains already all the information
                // required.
                throw ex;
            } catch (WicketRuntimeException ex) {
                // throw exception since there is no associated markup
                throw new MarkupNotFoundException(
                        exceptionMessage("Markup of type '"
                                + getMarkupType()
                                + "' for component '"
                                + getClass().getName()
                                + "' not found."
                                + " Enable debug messages for org.apache.wicket.util.resource to get a list of all filenames tried"),
                        ex);
            }
        }
    }

    protected FeedbackPanel feedback;
    private LinkedList<Button> buttons;
    protected final Button ok;
    protected final Button cancel;
    private IDialogService dialogService;
    private Panel container;
    private AjaxFormSubmitBehavior ajaxSubmit;

    public AbstractDialog() {
        super("form");

        setOutputMarkupId(true);

        container = new Container(IDialogService.DIALOG_WICKET_ID);
        container.add(this);

        feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        add(feedback);

        buttons = new LinkedList<Button>();
        add(new ListView("buttons", new Model(buttons)) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem item) {
                Button button = (Button) item.getModelObject();
                if (!button.getId().equals("button")) {
                    log.error("button's id not correct");
                }
                item.add(button);
            }
        });

        ok = new Button(getButtonId());
        ok.add(new Label("label", new ResourceModel("ok")));
        buttons.add(ok);

        setAjaxSubmit(true);

        cancel = new AjaxButton(getButtonId(), this) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                cancel();
                closeDialog();
            }
        }.setDefaultFormProcessing(false);
        cancel.add(new Label("label", new ResourceModel("cancel")));
        buttons.add(cancel);
    }

    @Override
    public boolean isTransparentResolver() {
        return true;
    }

    public void setAjaxSubmit(boolean isset) {
        if (isset) {
            if (ajaxSubmit == null) {
                ok.add(ajaxSubmit = new AjaxFormSubmitBehavior(this, "onclick") {
                    private static final long serialVersionUID = 1L;

                    protected CharSequence getEventHandler() {
                        return new AppendingStringBuffer(super.getEventHandler()).append("; return false;");
                    }

                    @Override
                    protected void onError(AjaxRequestTarget target) {
                        target.addComponent(feedback);
                    }

                    @Override
                    protected void onSubmit(AjaxRequestTarget target) {
                        target.addComponent(feedback);
                    }
                });
            }
        } else {
            if (ajaxSubmit != null) {
                ok.remove(ajaxSubmit);
                ajaxSubmit = null;
            }
        }
    }

    public void setDialogService(IDialogService dialogService) {
        this.dialogService = dialogService;
    }

    protected IDialogService getDialogService() {
        return dialogService;
    }

    protected final void closeDialog() {
        getDialogService().close();
    }

    protected String getButtonId() {
        return "button";
    }

    protected void addButton(Button button) {
        if (getButtonId().equals(button.getId())) {
            buttons.addFirst(button);
        } else {
            log.error("Failed to add button: component id is not '{}'", getButtonId());
        }
    }
    
    protected void removeButton(Button button) {
        buttons.remove(button);
    }
    
    @Override
    protected void onSubmit() {
        try {
            ok();
            closeDialog();
        } catch (Exception e) {
            String msg = e.getClass().getName() + ": " + e.getMessage();
            log.error(msg);
            error(e.getMessage());
            if (log.isDebugEnabled()) {
                log.debug("Error from repository: ", e);
            }
        }
    }

    protected void ok() throws Exception {
    }

    protected void cancel() {
    }

    public Component getComponent() {
        return container;
    }

    public void onClose() {
    }

}
