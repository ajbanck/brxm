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
package org.hippoecm.frontend.plugins.xinha.services.images;

import java.util.Map;

import org.apache.wicket.model.IDetachable;
import org.apache.wicket.util.string.Strings;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.plugins.richtext.IRichTextImageFactory;
import org.hippoecm.frontend.plugins.richtext.RichTextImage;
import org.hippoecm.frontend.plugins.richtext.RichTextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class XinhaImageService implements IDetachable {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";

    static final Logger log = LoggerFactory.getLogger(XinhaImageService.class);

    final static String BINARIES_PREFIX = "binaries";

    private IRichTextImageFactory factory;

    public XinhaImageService(IRichTextImageFactory factory) {
        this.factory = factory;
    }

    //Attach an image with only a JcrNodeModel. Method return json object wich 
    public String attach(JcrNodeModel model) {
        //TODO: fix drag-drop replacing
        RichTextImage item = createImageItem(model);
        if (item.save()) {
            StringBuilder sb = new StringBuilder(80);
            sb.append("xinha_editors.").append(getXinhaName()).append(".plugins.InsertImage.instance.insertImage(");
            sb.append("{ ");
            sb.append(XinhaImage.URL).append(": '").append(item.getUrl()).append("'");
            sb.append(", ").append(XinhaImage.FACET_SELECT).append(": '").append(item.getFacetSelectPath()).append("'");
            sb.append(" }, false)");
            return sb.toString();
        }
        return null;
    }

    protected abstract String getXinhaName();

    public XinhaImage createXinhaImage(Map<String, String> p) {
        RichTextImage rti = loadImageItem(p);
        return new XinhaImage(p, rti != null ? rti.getNodeModel() : null) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isValid() {
                return super.isValid() && factory.isValid(getLinkTarget());
            }

            public void save() {
                if (isAttacheable()) {
                    if (isReplacing()) {
                        RichTextImage remove = loadImageItem(getInitialValues());
                        if (remove != null) {
                            remove.delete();
                        }
                    }
                    RichTextImage item = createImageItem(getLinkTarget());
                    if (item.save()) {
                        setFacetSelectPath(item.getFacetSelectPath());
                        setUrl(item.getUrl());
                    }
                }
            }

            public void delete() {
                RichTextImage item = loadImageItem(this);
                item.delete();
                setFacetSelectPath("");
                setUrl("");
            }

        };
    }

    public void detach() {
        factory.detach();
    }
    
    private RichTextImage loadImageItem(Map<String, String> values) {
        String path = values.get(XinhaImage.FACET_SELECT);
        if (!Strings.isEmpty(path)) {
            path = RichTextUtil.decode(path);
        } else { 
            path = values.get(XinhaImage.URL);
            if (!Strings.isEmpty(path) && path.startsWith(BINARIES_PREFIX)) {
                path = RichTextUtil.decode(path.substring(BINARIES_PREFIX.length()));
            }
        }

        return factory.loadImageItem(path);
    }

    private RichTextImage createImageItem(IDetachable nodeModel) {
        return factory.createImageItem(nodeModel);
    }
}
