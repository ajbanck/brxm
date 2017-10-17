/*
 *  Copyright 2017 Hippo B.V. (http://www.onehippo.com)
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
package org.onehippo.cms.channelmanager.content.templatequery;


import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.InvalidQueryException;

import org.hippoecm.repository.api.HippoNodeType;
import org.onehippo.cms.channelmanager.content.documenttype.model.DocumentTypeInfo;
import org.onehippo.cms.channelmanager.content.documenttype.util.LocalizationUtils;
import org.onehippo.cms.channelmanager.content.error.ErrorWithPayloadException;
import org.onehippo.cms.channelmanager.content.error.InternalServerErrorException;
import org.onehippo.cms.channelmanager.content.error.NotFoundException;
import org.onehippo.repository.l10n.ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.onehippo.cms.channelmanager.content.templatequery.TemplateQueryUtils.executeQuery;

public class TemplateQueryServiceImpl implements TemplateQueryService {

    private static final Logger log = LoggerFactory.getLogger(TemplateQueryServiceImpl.class);

    private static final String HIPPO_TEMPLATES_PATH = "/hippo:configuration/hippo:queries/hippo:templates";
    private static final List<String> INVALID_PROTOTYPES = Arrays.asList(
            "hippo:", "hipposys:", "hipposysedit:", "reporting:", "nt:unstructured", "hippogallery:");

    private static final TemplateQueryService INSTANCE = new TemplateQueryServiceImpl();

    public static TemplateQueryService getInstance() {
        return INSTANCE;
    }

    private TemplateQueryServiceImpl() { }

    @Override
    public TemplateQuery getTemplateQuery(final String id, final Session session,
                                          final Locale locale) throws ErrorWithPayloadException {
        final List<DocumentTypeInfo> documentTypes = new ArrayList<>();
        final String templateQueryPath = HIPPO_TEMPLATES_PATH + "/" + id;
        try {
            if (!session.nodeExists(templateQueryPath)) {
                throw new NotFoundException();
            }

            final Node templateQueryNode = session.getNode(templateQueryPath);
            if (!templateQueryNode.isNodeType("nt:query")) {
                log.warn("Node '{}' is not of type nt:query", templateQueryPath);
                throw new NotFoundException();
            }

            final NodeIterator nodes = executeQuery(session, templateQueryNode);
            while (nodes.hasNext()) {
                final Node typeNode = nodes.nextNode();
                final String documentTypeName = getDocumentTypeName(typeNode);
                if (documentTypeName != null) {
                    final DocumentTypeInfo documentTypeInfo = createDocumentTypeInfo(documentTypeName, locale);
                    documentTypes.add(documentTypeInfo);
                }
            }
        } catch (final InvalidQueryException ex) {
            final String message = String.format("Failed to execute template query '%s'", id);
            log.debug(message, ex);
            throw new InternalServerErrorException(message);
        } catch (final RepositoryException e) {
            final String message = String.format("Failed to read document type data for template query '%s'", id);
            log.debug(message, e);
            throw new InternalServerErrorException(message);
        }

        final Collator collator = Collator.getInstance(locale);
        documentTypes.sort((o1, o2) -> collator.compare(o1.getDisplayName(), o2.getDisplayName()));

        return new TemplateQuery(documentTypes);
    }

    private DocumentTypeInfo createDocumentTypeInfo(final String id, final Locale locale) {
        final DocumentTypeInfo info = new DocumentTypeInfo(id);
        final Optional<ResourceBundle> resourceBundle = LocalizationUtils.getResourceBundleForDocument(id, locale);
        LocalizationUtils.determineDocumentDisplayName(id, resourceBundle).ifPresent(info::setDisplayName);
        return info;
    }

    // This is the same logic as defined in org.hippoecm.repository.standardworkflow.FolderWorkflowImpl
    private String getDocumentTypeName(final Node typeNode) throws RepositoryException, InternalServerErrorException {
        final String name = typeNode.getName();
        if (name.equals(HippoNodeType.HIPPO_PROTOTYPE)) {
            final String documentType = typeNode.getPrimaryNodeType().getName();
            return isValidPrototype(documentType) ? documentType : null;
        }
        return name;
    }

    private boolean isValidPrototype(final String documentType) {
        return INVALID_PROTOTYPES.stream().noneMatch(documentType::startsWith);
    }
}
