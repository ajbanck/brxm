/*
 *  Copyright 2010-2018 Hippo B.V. (http://www.onehippo.com)
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
package org.onehippo.cms7.services.htmlprocessor.richtext.image;

import java.io.Serializable;

import javax.jcr.Node;

import org.onehippo.cms7.services.htmlprocessor.model.Model;
import org.onehippo.cms7.services.htmlprocessor.richtext.RichTextException;

public interface RichTextImageFactory extends Serializable {

    boolean isValid(Model<Node> linkTarget);

    RichTextImage createImageItem(Model<Node> model) throws RichTextException;

    RichTextImage loadImageItem(String uuid, String type) throws RichTextException;

}
