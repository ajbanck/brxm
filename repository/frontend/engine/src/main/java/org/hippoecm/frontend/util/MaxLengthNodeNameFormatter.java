/*
 *  Copyright 2008,2009 Hippo.
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
package org.hippoecm.frontend.util;

import org.hippoecm.frontend.i18n.model.NodeTranslator;
import org.hippoecm.frontend.model.JcrNodeModel;

public class MaxLengthNodeNameFormatter extends MaxLengthStringFormatter {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";

    private static final long serialVersionUID = 1L;

    public MaxLengthNodeNameFormatter() {
        super();
    }

    public MaxLengthNodeNameFormatter(int maxLength, String split, int indentLength) {
        super(maxLength, split, indentLength);
    }

    public boolean isTooLong(JcrNodeModel nodeModel) {
        return isTooLong(nodeModel, 0);
    }

    public boolean isTooLong(JcrNodeModel nodeModel, int indent) {
        return super.isTooLong(getName(nodeModel), indent);
    }

    public String parse(JcrNodeModel nodeModel, int indent) {
        return super.parse(getName(nodeModel), indent);
    }

    public String parse(JcrNodeModel nodeModel) {
        return parse(nodeModel, 0);
    }

    protected String getName(JcrNodeModel nodeModel) {
        return (String) new NodeTranslator(nodeModel).getNodeName().getObject();
    }
}
