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
import java.util.List;

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;

public class DialogWindow extends ModalWindow implements IDialogService {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";
    private static final long serialVersionUID = 1L;

    private class Callback implements ModalWindow.WindowClosedCallback {
        private static final long serialVersionUID = 1L;

        Dialog dialog;

        Callback(Dialog dialog) {
            this.dialog = dialog;
        }

        public void onClose(AjaxRequestTarget target) {
            dialog.onClose();
            if (pending.size() > 0) {
                Dialog dialog = pending.remove(0);
                setContent(dialog.getComponent());
                setTitle(dialog.getTitle());
                setWindowClosedCallback(new Callback(dialog));
                show(target);
            }
        }
    }

    private List<Dialog> pending;

    public DialogWindow(String id) {
        super(id);

        pending = new LinkedList<Dialog>();
    }

    public void show(Dialog dialog) {
        if (isShown()) {
            pending.add(dialog);
        } else {
            setContent(dialog.getComponent());
            setTitle(dialog.getTitle());
            setWindowClosedCallback(new Callback(dialog));

            IRequestTarget target = RequestCycle.get().getRequestTarget();
            if (AjaxRequestTarget.class.isAssignableFrom(target.getClass())) {
                show((AjaxRequestTarget) target);
            }
        }
    }

    public void close() {
        IRequestTarget target = RequestCycle.get().getRequestTarget();
        if (AjaxRequestTarget.class.isAssignableFrom(target.getClass())) {
            close((AjaxRequestTarget) target);
        }
        remove(getContentId());
    }

}
