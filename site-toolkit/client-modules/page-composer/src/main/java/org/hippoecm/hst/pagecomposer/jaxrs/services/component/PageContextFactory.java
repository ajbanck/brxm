/*
 * Copyright 2020 Bloomreach
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.hippoecm.hst.pagecomposer.jaxrs.services.component;

import javax.jcr.RepositoryException;

import org.hippoecm.hst.configuration.hosting.Mount;
import org.hippoecm.hst.configuration.internal.CanonicalInfo;
import org.hippoecm.hst.configuration.internal.ConfigurationLockInfo;
import org.hippoecm.hst.configuration.site.HstSite;
import org.hippoecm.hst.configuration.sitemap.HstSiteMapItem;
import org.hippoecm.hst.pagecomposer.jaxrs.services.PageComposerContextService;
import org.hippoecm.hst.pagecomposer.jaxrs.services.helpers.SiteMapHelper;
import org.hippoecm.hst.util.HstSiteMapUtils;

final class PageContextFactory {

    private final SiteMapHelper siteMapHelper;

    PageContextFactory(SiteMapHelper siteMapHelper) {
        this.siteMapHelper = siteMapHelper;
    }

    PageContext make(ActionStateContext context) throws RepositoryException {
        final PageComposerContextService contextService = context.getContextService();
        final String siteMapItemUuid = context.getSiteMapItemUuid();
        final String userId = context.getUserId();
        final HstSiteMapItem siteMapItem = siteMapHelper.getConfigObject(siteMapItemUuid);
        final Mount mount = contextService.getEditingMount();
        final String homePagePathInfo = HstSiteMapUtils.getPath(mount, mount.getHomePage());
        final CanonicalInfo canonicalInfo = (CanonicalInfo) siteMapItem;
        final String lockedBy = ((ConfigurationLockInfo) siteMapItem).getLockedBy();
        final HstSite hstSite = mount.getHstSite();
        return new PageContext()
                .setHomePage(HstSiteMapUtils.getPath(siteMapItem, null).equals(homePagePathInfo))
                .setLocked(lockedBy != null && !userId.equals(lockedBy))
                .setInherited(!canonicalInfo.getCanonicalPath().startsWith(hstSite.getConfigurationPath() + "/"))
                .setWorkspaceConfigured(canonicalInfo.isWorkspaceConfiguration());
    }
}
