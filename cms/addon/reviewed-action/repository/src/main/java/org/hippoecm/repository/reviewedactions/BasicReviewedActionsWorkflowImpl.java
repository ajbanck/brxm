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
package org.hippoecm.repository.reviewedactions;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;

import javax.jcr.RepositoryException;

import org.hippoecm.repository.api.Document;
import org.hippoecm.repository.api.MappingException;
import org.hippoecm.repository.api.WorkflowException;
import org.hippoecm.repository.ext.WorkflowImpl;
import org.hippoecm.repository.standardworkflow.VersionWorkflow;

public class BasicReviewedActionsWorkflowImpl extends WorkflowImpl implements BasicReviewedActionsWorkflow {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";

    private static final long serialVersionUID = 1L;

    protected String username;
    protected PublicationRequest current;
    protected PublicationRequest current2;
    protected PublishableDocument published;
    protected PublishableDocument unpublished;
    protected PublishableDocument draft;

    public BasicReviewedActionsWorkflowImpl() throws RemoteException {
    }

    public Document obtainEditableInstance() throws WorkflowException {
        ReviewedActionsWorkflowImpl.log.info("obtain editable instance on document ");
        if(draft == null) {
            try {
                if(unpublished != null) {
                    draft = (PublishableDocument) unpublished.clone();
                } else {
                    draft = (PublishableDocument) published.clone();
                }
                draft.state = PublishableDocument.DRAFT;
                draft.setOwner(getWorkflowContext().getUserIdentity());
                if(unpublished != null) {
                    unpublished.setOwner(getWorkflowContext().getUserIdentity());
                }
                if(published != null) {
                    published.setOwner(getWorkflowContext().getUserIdentity());
                }
                username = getWorkflowContext().getUserIdentity();
            } catch(CloneNotSupportedException ex) {
                throw new WorkflowException("document is not a publishable document");
            }
        } else {
            if(draft.username != null && !getWorkflowContext().getUserIdentity().equals(draft.username))
                throw new WorkflowException("document already being edited");
        }
        return draft;
    }

    public void commitEditableInstance() throws WorkflowException {
        ReviewedActionsWorkflowImpl.log.info("commit editable instance of document ");
        if(draft != null) {
            unpublished = null;
            draft.setState(PublishableDocument.UNPUBLISHED);
        } else {
            throw new WorkflowException("no draft version of publication");
        }
    }

    public void disposeEditableInstance() throws WorkflowException {
        ReviewedActionsWorkflowImpl.log.info("dispose editable instance on document ");
        draft = null;
    }

    public void requestDeletion() throws WorkflowException {
        ReviewedActionsWorkflowImpl.log.info("deletion request on document ");
        if(current == null) {
            current = new PublicationRequest(PublicationRequest.DELETE, unpublished, getWorkflowContext().getUserIdentity());
        } else {
            throw new WorkflowException("request deletion failure");
        }
    }

    public void requestPublication() throws WorkflowException {
        ReviewedActionsWorkflowImpl.log.info("publication request on document ");
        if(current == null) {
            current = new PublicationRequest(PublicationRequest.PUBLISH, unpublished, getWorkflowContext().getUserIdentity());
        } else {
            throw new WorkflowException("publication request already pending");
        }
    }

    public void requestDepublication() throws WorkflowException {
        ReviewedActionsWorkflowImpl.log.info("depublication request on document ");
        if(current == null) {
            current = new PublicationRequest(PublicationRequest.DEPUBLISH, published, getWorkflowContext().getUserIdentity());
        } else {
            throw new WorkflowException("publication request already pending");
        }
    }

    public void publish(Date publicationDate) throws WorkflowException, MappingException, RepositoryException, RemoteException {
        ReviewedActionsWorkflowImpl.log.info("publication on document ");
        throw new WorkflowException("unsupported");
    }

    public void publish(Date publicationDate, Date depublicationDate) throws WorkflowException {
        ReviewedActionsWorkflowImpl.log.info("publication on document ");
        throw new WorkflowException("unsupported");
    }

    public void requestPublication(Date publicationDate) throws WorkflowException {
        ReviewedActionsWorkflowImpl.log.info("publication request on document ");
        throw new WorkflowException("unsupported");
    }

    public void requestPublication(Date publicationDate, Date depublicationDate) throws WorkflowException {
        ReviewedActionsWorkflowImpl.log.info("publication request on document ");
        throw new WorkflowException("unsupported");
    }

    public void requestDepublication(Date publicationDate) throws WorkflowException {
        ReviewedActionsWorkflowImpl.log.info("depublication request on document ");
        throw new WorkflowException("Unsupported operation");
    }

    PublishableDocument getRejectedDocument() throws WorkflowException {
        try {
            PublishableDocument rejected = (PublishableDocument) unpublished.clone();
            rejected.setState(PublishableDocument.STALE);
            return rejected;
        } catch(CloneNotSupportedException ex) {
            throw new WorkflowException("document is not a publishable document");
        }
    }

    public void restore(Calendar historic) throws WorkflowException {
        if(unpublished != null || published == null) {
            throw new WorkflowException("Unable to retrieve historic version of document when document not in live state");
        }
        try {
            Map replacements = new TreeMap(); // note not to use JDK1.5 types here, BCEL does not support them
            replacements.put("./hippostd:state", new String[] { "unpublished" });
            VersionWorkflow versionWorkflow = (VersionWorkflow) getWorkflowContext().getWorkflow("versioning", published);
            versionWorkflow.restore(historic, replacements);
        } catch(MappingException ex) {
            throw new WorkflowException("Restore historic document failed");
        } catch(RemoteException ex) {
            throw new WorkflowException("Restore historic document failed");
        } catch(RepositoryException ex) {
            throw new WorkflowException("Restore historic document failed");
        }
    }
}
