/*
 * Copyright 2007 Hippo
 *
 * Licensed under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hippoecm.repository.jackrabbit;

import java.io.File;
import java.security.Principal;
import java.util.Collections;
import java.util.Set;

import javax.jcr.AccessDeniedException;
import javax.jcr.RepositoryException;
import javax.security.auth.Subject;

import org.apache.jackrabbit.core.HierarchyManager;
import org.apache.jackrabbit.core.config.AccessManagerConfig;
import org.apache.jackrabbit.core.config.WorkspaceConfig;
import org.apache.jackrabbit.core.security.AccessManager;
import org.apache.jackrabbit.core.security.AnonymousPrincipal;
import org.apache.jackrabbit.core.security.AuthContext;
import org.apache.jackrabbit.core.security.SystemPrincipal;
import org.apache.jackrabbit.core.security.UserPrincipal;
import org.apache.jackrabbit.core.state.LocalItemStateManager;
import org.apache.jackrabbit.core.state.SessionItemStateManager;
import org.apache.jackrabbit.core.state.SharedItemStateManager;
import org.hippoecm.repository.security.HippoAMContext;
import org.hippoecm.repository.security.principals.AdminPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XASessionImpl extends org.apache.jackrabbit.core.XASessionImpl {
    private static Logger log = LoggerFactory.getLogger(XASessionImpl.class);

    /**
     * the user ID that was used to acquire this session
     */
    private String userId;

    protected XASessionImpl(RepositoryImpl rep, AuthContext loginContext, WorkspaceConfig wspConfig)
            throws AccessDeniedException, RepositoryException {
        super(rep, loginContext, wspConfig);
        SetUserId();
        HippoLocalItemStateManager localISM = (HippoLocalItemStateManager) ((XAWorkspaceImpl)wsp).getItemStateManager();
        ((RepositoryImpl)rep).initializeLocalItemStateManager(localISM, this, loginContext.getSubject());
    }

    protected XASessionImpl(RepositoryImpl rep, Subject subject, WorkspaceConfig wspConfig) throws AccessDeniedException,
            RepositoryException {
        super(rep, subject, wspConfig);
        SetUserId();
        HippoLocalItemStateManager localISM = (HippoLocalItemStateManager) ((XAWorkspaceImpl)wsp).getItemStateManager();
        ((RepositoryImpl)rep).initializeLocalItemStateManager(localISM, this, subject);
    }

    @Override
    protected SessionItemStateManager createSessionItemStateManager(LocalItemStateManager manager) {
        return new HippoSessionItemStateManager(((RepositoryImpl) rep).getRootNodeId(), manager, this);
    }

    @Override
    protected org.apache.jackrabbit.core.WorkspaceImpl createWorkspaceInstance(WorkspaceConfig wspConfig,
          SharedItemStateManager stateMgr, org.apache.jackrabbit.core.RepositoryImpl rep,
          org.apache.jackrabbit.core.SessionImpl session) {
        return new XAWorkspaceImpl(wspConfig, stateMgr, rep, this);
    }

    @Override
    protected org.apache.jackrabbit.core.ItemManager createItemManager(SessionItemStateManager itemStateMgr, HierarchyManager hierMgr) {
        return new ItemManager(itemStateMgr, hierMgr, this, ntMgr.getRootNodeDefinition(), ((RepositoryImpl)rep).getRootNodeId());
        // return super.createItemManager(itemStateMgr, hierMgr);
    }

    @Override
    protected AccessManager createAccessManager(Subject subject, HierarchyManager hierMgr) throws AccessDeniedException, RepositoryException {
        AccessManagerConfig amConfig = rep.getConfig().getAccessManagerConfig();
        try {
            HippoAMContext ctx = new HippoAMContext( new File(((RepositoryImpl)rep).getConfig().getHomeDir()), 
                    ((RepositoryImpl)rep).getFileSystem(), 
                    subject, 
                    getItemStateManager().getAtticAwareHierarchyMgr(), 
                    ((RepositoryImpl)rep).getNamespaceRegistry(), 
                    wsp.getName(), 
                    ((RepositoryImpl)rep).getNodeTypeRegistry() 
                  );
            AccessManager accessMgr = (AccessManager) amConfig.newInstance();
            accessMgr.init(ctx);
            return accessMgr;
        } catch (AccessDeniedException ex) {
            throw ex;
        } catch (Exception ex) {
            String msg = "failed to instantiate AccessManager implementation: " + amConfig.getClassName();
            log.error(msg, ex);
            throw new RepositoryException(msg, ex);
        }
    }

    /**
     * Override jackrabbits default userid, because it just uses
     * the first principal it can find, which can lead to strange "usernames"
     */
    protected void SetUserId() {
        if (!subject.getPrincipals(SystemPrincipal.class).isEmpty()) {
            Principal principal = (Principal)  subject.getPrincipals(SystemPrincipal.class).iterator().next();
            userId = principal.getName();
        } else if (!subject.getPrincipals(AdminPrincipal.class).isEmpty()) {
            Principal principal = (Principal) subject.getPrincipals(AdminPrincipal.class).iterator().next();
            userId = principal.getName();
        } else if (!subject.getPrincipals(UserPrincipal.class).isEmpty()) {
            Principal principal = (Principal) subject.getPrincipals(UserPrincipal.class).iterator().next();
            userId = principal.getName();
        } else if (!subject.getPrincipals(AnonymousPrincipal.class).isEmpty()) {
            Principal principal = (Principal) subject.getPrincipals(AnonymousPrincipal.class).iterator().next();
            userId = principal.getName();
        } else {
            userId = "Unknown";
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getUserID() {
        return userId;
    }
    
    /**
     * Method to expose the authenticated users' principals
     * @return Set An unmodifialble set containing the principals
     */
    public Set<Principal> getUserPrincipals() {
        return Collections.unmodifiableSet(subject.getPrincipals());
    }
}
