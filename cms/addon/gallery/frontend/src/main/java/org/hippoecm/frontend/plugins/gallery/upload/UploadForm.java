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
package org.hippoecm.frontend.plugins.gallery.upload;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.List;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeDefinition;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.lang.Bytes;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.plugins.gallery.Gallery;
import org.hippoecm.frontend.plugins.gallery.ImageInfo;
import org.hippoecm.frontend.plugins.gallery.ImageUtils;
import org.hippoecm.frontend.plugins.gallery.ThumbnailConstants;
import org.hippoecm.frontend.service.IJcrService;
import org.hippoecm.frontend.session.UserSession;
import org.hippoecm.frontend.widgets.NamespaceFriendlyChoiceRenderer;
import org.hippoecm.repository.api.Document;
import org.hippoecm.repository.api.MappingException;
import org.hippoecm.repository.api.WorkflowManager;
import org.hippoecm.repository.gallery.GalleryWorkflow;

class UploadForm extends Form {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id: $";
    private static final long serialVersionUID = 1L;

    private final UploadDialog uploadDialog;
    private final FileUploadField uploadField;
    private String type;
    private String description;

    public UploadForm(String id, UploadDialog uploadDialog) {
        super(id, uploadDialog.getModel());
        this.uploadDialog = uploadDialog;
        setMultiPart(true);
        setMaxSize(Bytes.megabytes(5));
        add(uploadField = new FileUploadField("input"));

        JcrNodeModel galleryNodeModel = (JcrNodeModel) uploadDialog.getModel();
        Node galleryNode = galleryNodeModel.getNode();
        List<String> galleryTypes = null;
        try {
            WorkflowManager manager = ((UserSession) Session.get()).getWorkflowManager();
            GalleryWorkflow workflow = (GalleryWorkflow) manager.getWorkflow(this.uploadDialog.getWorkflowCategory(),
                    galleryNode);
            if (workflow == null) {
                Gallery.log.error("No gallery workflow accessible");
            } else {
                galleryTypes = workflow.getGalleryTypes();
            }
        } catch (MappingException ex) {
            Gallery.log.error(ex.getMessage(), ex);
        } catch (RepositoryException ex) {
            Gallery.log.error(ex.getMessage(), ex);
        } catch (RemoteException ex) {
            Gallery.log.error(ex.getMessage(), ex);
        }
        if (galleryTypes != null && galleryTypes.size() > 1) {
            DropDownChoice folderChoice;
            type = galleryTypes.get(0);
            add(folderChoice = new DropDownChoice("type", new PropertyModel(this, "type"), galleryTypes,
                    new NamespaceFriendlyChoiceRenderer(galleryTypes)));
            folderChoice.setNullValid(false);
            folderChoice.setRequired(true);
        } else if (galleryTypes != null && galleryTypes.size() == 1) {
            type = galleryTypes.get(0);
            Component component;
            add(component = new Label("type", type));
            component.setVisible(false);
        } else {
            type = null;
            Component component;
            add(component = new Label("type", "default"));
            component.setVisible(false);
        }
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
    
    @Override
    protected void onSubmit() {
        final FileUpload upload = uploadField.getFileUpload();
        if (upload != null) {
            try {
                String filename = upload.getClientFileName();
                String mimetype = upload.getContentType();
                InputStream istream = upload.getInputStream();
                WorkflowManager manager = ((UserSession) Session.get()).getWorkflowManager();
                JcrNodeModel model = (JcrNodeModel) getModel();
                if (model != null && model.getNode() != null) {
                    try {
                        Node galleryNode = model.getNode();
                        GalleryWorkflow workflow = (GalleryWorkflow) manager.getWorkflow(this.uploadDialog
                                .getWorkflowCategory(), galleryNode);
                        Document document = workflow.createGalleryItem(filename, type);
                        Node node = (((UserSession) Session.get())).getJcrSession().getNodeByUUID(document.getIdentity());
                        Item item = node.getPrimaryItem();
                        if (item.isNode()) {
                            Node primaryChild = (Node) item;
                            if (primaryChild.isNodeType("hippo:resource")) {
                                primaryChild.setProperty("jcr:mimeType", mimetype);
                                primaryChild.setProperty("jcr:data", istream);
                            }
                            NodeDefinition[] childDefs = node.getPrimaryNodeType().getChildNodeDefinitions();
                            for (int i = 0; i < childDefs.length; i++) {
                                if (childDefs[i].getDefaultPrimaryType() != null
                                        && childDefs[i].getDefaultPrimaryType().isNodeType("hippo:resource")) {
                                    if (!node.hasNode(childDefs[i].getName())) {
                                        Node child = node.addNode(childDefs[i].getName());
                                        child.setProperty("jcr:data", primaryChild.getProperty("jcr:data").getStream());
                                        child.setProperty("jcr:mimeType", primaryChild.getProperty("jcr:mimeType")
                                                .getString());
                                        child.setProperty("jcr:lastModified", primaryChild.getProperty(
                                                "jcr:lastModified").getDate());
                                    }
                                }
                            }
                            description = ImageInfo.analyse(filename, primaryChild.getProperty("jcr:data").getStream());

                            makeThumbnail(primaryChild, primaryChild.getProperty("jcr:data").getStream(), primaryChild
                                    .getProperty("jcr:mimeType").getString());
                            node.save();
                        }

                        uploadDialog.getWizard().getWizardModel().next();
                        IJcrService jcrService = this.uploadDialog.getJcrService();
                        jcrService.flush(model);
                    } catch (MappingException ex) {
                        Gallery.log.error(ex.getMessage());
                        this.uploadDialog.setException("Workflow error: " + ex.getMessage());
                    } catch (RepositoryException ex) {
                        Gallery.log.error(ex.getMessage());
                        this.uploadDialog.setException("Workflow error: " + ex.getMessage());
                    }
                }
            } catch (IOException ex) {
                Gallery.log.info("upload of image truncated");
                this.uploadDialog.setException("Upload failed: " + ex.getMessage());
            }
        } else {
            this.uploadDialog.setException("No file uploaded");
        }
    }

    private void makeThumbnail(Node node, InputStream resourceData, String mimeType) throws RepositoryException {
        if (mimeType.startsWith("image")) {
            InputStream thumbNail = ImageUtils.createThumbnail(resourceData, ThumbnailConstants.THUMBNAIL_WIDTH,
                    mimeType);
            node.setProperty("jcr:data", thumbNail);
        } else {
            node.setProperty("jcr:data", resourceData);
        }
        node.setProperty("jcr:mimeType", mimeType);
    }

}