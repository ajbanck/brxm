/*
 *  Copyright 2009 Hippo.
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
package org.hippoecm.frontend.editor.workflow.dialog;

import org.apache.wicket.extensions.wizard.WizardModel;
import org.apache.wicket.extensions.wizard.WizardStep;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.hippoecm.frontend.editor.layout.ILayoutProvider;
import org.hippoecm.frontend.editor.workflow.action.NewCompoundTypeAction;
import org.hippoecm.frontend.widgets.TextFieldWidget;

public class CreateCompoundTypeDialog extends CreateTypeDialog {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";

    private static final long serialVersionUID = 1L;

    class TypeDetailStep extends WizardStep {
        private static final long serialVersionUID = 1L;

        TypeDetailStep(NewCompoundTypeAction action) {
            super(new ResourceModel("type-detail-title"), new ResourceModel("type-detail-summary"));
            add(new TextFieldWidget("name", new PropertyModel(action, "name")));
        }
    }

    public CreateCompoundTypeDialog(NewCompoundTypeAction action, ILayoutProvider layouts) {
        super(action, layouts);

        WizardModel wizardModel = new WizardModel();
        wizardModel.add(new TypeDetailStep(action));
        wizardModel.add(new SelectLayoutStep(new PropertyModel(action, "layout"), layouts));
        init(wizardModel);
    }

}
