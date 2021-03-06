/*
 *  Copyright 2017-2018 Hippo B.V. (http://www.onehippo.com)
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
package org.hippoecm.frontend.plugins.console.menu.content;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.io.FileUtils;
import org.apache.wicket.model.IModel;
import org.hippoecm.frontend.session.UserSession;
import org.hippoecm.frontend.widgets.download.DownloadLink;
import org.hippoecm.repository.api.HippoSession;
import org.hippoecm.repository.api.NodeNameCodec;
import org.onehippo.cm.ConfigurationService;
import org.onehippo.cms7.services.HippoServiceRegistry;

import static org.onehippo.cms7.utilities.io.FilePathUtils.cleanFileName;

public class DownloadExportYamlLink extends DownloadLink<Node> {

    private static final long serialVersionUID = 1L;

    private File file;

    public DownloadExportYamlLink(final String id, final IModel<Node> model) {
        super(id, model);
    }

    @Override
    protected String getFilename() {
        try {
            return cleanFileName(NodeNameCodec.decode(getModel().getObject().getName())) + ".zip";
        } catch (RepositoryException e) {
            error("Unable to get node name for file name, using default");
        }
        return null;
    }

    @Override
    protected void onDownloadTargetDetach() {
        FileUtils.deleteQuietly(file);
    }

    @Override
    protected InputStream getContent() {
        final HippoSession session = (HippoSession) UserSession.get().getJcrSession();
        
        try {
            final ConfigurationService service = HippoServiceRegistry.getService(ConfigurationService.class);
            final Node nodeToExport = session.getNode(getModelObject().getPath());
            file = service.exportZippedContent(nodeToExport);
            return new FileInputStream(file);
        } catch (RepositoryException e) {
            error("Repository exception during export: " + e.getMessage());
        } catch (IOException e) {
            error("IOException during export: " + e.getMessage());
        }
        return null;
    }
}