/**
 * Copyright 2013-2017 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hippoecm.hst.resourcebundle.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.lang.LocaleUtils;
import org.hippoecm.hst.resourcebundle.ResourceBundleFamily;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DefaultMutableResourceBundleRegistry
 *
 * The "get" logic inside this class corresponds to the "put" logic of the {@link HippoRepositoryResourceBundleFamilyFactory}
 * in the sense that both do no longer use the #*ForPreview methods of a ResourceBundleFamily. Instead, a separate
 * instance of a ResourceBundleFamily is kept in a separate data structure in order to retrieve, cache and evict
 * resource bundles.
 */
public class DefaultMutableResourceBundleRegistry implements MutableResourceBundleRegistry {

    private static final Logger logger = LoggerFactory.getLogger(DefaultMutableResourceBundleRegistry.class);

    private final Map<String, ResourceBundleFamily> liveBundleFamilyByBasename = new HashMap<>();
    private final Map<String, ResourceBundleFamily> previewBundleFamilyByBasename = new HashMap<>();
    private final Map<String, ResourceBundleFamily> liveBundleFamilyByIdentifier = new HashMap<>();
    private final Map<String, ResourceBundleFamily> previewBundleFamilyByIdentifier = new HashMap<>();

    // As an optimization, whenever a look-up for a specific basename fails, we remember that negative result
    // by adding the basename to the appropriate "unfound" set in order to avoid doing the lookup again.
    // Since these basenames can not be correlated with resource bundle identifiers, whenever an identifier
    // is "unregistered" which wasn't cached, we wipe the unfound set.
    // A use case where this matters is a resource bundle that is being referenced by name, but which doesn't
    // exist yet. Once it gets created (or the basename of the bundle gets "fixed"), a next retrieval of that
    // basename should trigger a new look-up in the repository.
    private final Set<String> liveUnfoundBasenames = new HashSet<>();
    private final Set<String> previewUnfoundBasenames = new HashSet<>();

    private ResourceBundleFamilyFactory resourceBundleFamilyFactory;

    /**
     * Flag whether or not to fallback to the default Java standard resource bundles
     * when the registry cannot find a matched resource bundle family.
     */
    private boolean fallbackToJavaResourceBundle = true;

    public DefaultMutableResourceBundleRegistry() {
    }

    public ResourceBundleFamilyFactory getResourceBundleFamilyFactory() {
        return resourceBundleFamilyFactory;
    }

    public void setResourceBundleFamilyFactory(ResourceBundleFamilyFactory resourceBundleFamilyFactory) {
        this.resourceBundleFamilyFactory = resourceBundleFamilyFactory;
    }

    public boolean isFallbackToJavaResourceBundle() {
        return fallbackToJavaResourceBundle;
    }

    public void setFallbackToJavaResourceBundle(boolean fallbackToJavaResourceBundle) {
        this.fallbackToJavaResourceBundle = fallbackToJavaResourceBundle;
    }

    @Override
    @Deprecated
    public void registerBundleFamily(String basename, ResourceBundleFamily bundleFamily) {
        registerBundleFamily(basename, false, bundleFamily);
    }

    @Override
    public synchronized void registerBundleFamily(String basename, boolean preview, ResourceBundleFamily bundleFamily) {
        if (bundleFamily == null) {
            unfoundBasenames(preview).add(basename);
            logger.info("Failed to load resource bundle {} for {} scope", basename, preview ? "preview" : "live");
        } else {
            logger.info("Registering resource bundle {} for {} scope", basename, preview ? "preview" : "live");
            byBasename(preview).put(basename, bundleFamily);
            if (bundleFamily instanceof DefaultMutableResourceBundleFamily) {
                final String identifier = ((DefaultMutableResourceBundleFamily)bundleFamily).getIdentifier();
                if (identifier != null) {
                    byIdentifier(preview).put(identifier, bundleFamily);
                }
            }
        }
    }

    @Override
    @Deprecated
    public void unregisterBundleFamily(String basename) {
        logger.info("Unregistering resource bundle {}", basename);
        unregisterBundleFamilyByBasename(basename, true);
        unregisterBundleFamilyByBasename(basename, false);
    }

    private synchronized void unregisterBundleFamilyByBasename(final String basename, final boolean preview) {
        final ResourceBundleFamily bundleFamily = byBasename(preview).remove(basename);
        if (bundleFamily instanceof DefaultMutableResourceBundleFamily) {
            final String identifier = ((DefaultMutableResourceBundleFamily) bundleFamily).getIdentifier();
            if (identifier != null) {
                byIdentifier(preview).remove(identifier);
            }
        }
        unfoundBasenames(preview).remove(basename);
    }

    @Override
    public synchronized void unregisterBundleFamily(final String identifier, boolean preview) {
        final ResourceBundleFamily bundleFamily = byIdentifier(preview).remove(identifier);
        if (bundleFamily != null) {
            final String basename = bundleFamily.getBasename();
            logger.info("Unregistering resource bundle {} for {} scope", basename, preview ? "preview" : "live");
            byBasename(preview).remove(basename);
        } else {
            unfoundBasenames(preview).clear();
        }
    }

    @Override
    @Deprecated
    public synchronized void unregisterAllBundleFamilies() {
        byBasename(true).clear();
        byBasename(false).clear();
        byIdentifier(true).clear();
        byIdentifier(false).clear();
        unfoundBasenames(true).clear();
        unfoundBasenames(false).clear();
    }

    @Override
    public ResourceBundle getBundle(String basename) {
        return getBundle(basename, null);
    }

    @Override
    public ResourceBundle getBundleForPreview(String basename) {
        return getBundleForPreview(basename, null);
    }

    @Override
    public ResourceBundle getBundle(String basename, Locale locale) {
        return getBundle(basename, locale, false);
    }

    @Override
    public ResourceBundle getBundleForPreview(String basename, Locale locale) {
        return getBundle(basename, locale, true);
    }

    protected ResourceBundle getBundle(final String basename, final Locale locale, final boolean preview) {
        final ResourceBundleFamily bundleFamily = getBundleFamilyUnlessUnfound(basename, preview);
        if (bundleFamily != null) {
            final ResourceBundle bundle = extractLocalizedBundle(bundleFamily, locale)
                    .orElseGet(bundleFamily::getDefaultBundle);
            if (bundle != null) {
                return bundle;
            }
        }

        if (fallbackToJavaResourceBundle) {
            if (locale == null) {
                return ResourceBundle.getBundle(basename, Locale.getDefault(), Thread.currentThread().getContextClassLoader());
            } else {
                return ResourceBundle.getBundle(basename, locale, Thread.currentThread().getContextClassLoader());
            }
        }
        throw new MissingResourceException("Cannot find resource bundle.", basename, "");
    }

    private ResourceBundleFamily getBundleFamilyUnlessUnfound(final String basename, final boolean preview) {
        boolean unfound;
        synchronized (this) {
            unfound = unfoundBasenames(preview).contains(basename);
        }

        return unfound ? null : getOrFindBundleFamily(basename, preview);
    }

    private ResourceBundleFamily getOrFindBundleFamily(final String basename, final boolean preview) {
        ResourceBundleFamily bundleFamily;

        // get from cache
        synchronized (this) {
            bundleFamily = byBasename(preview).get(basename);
        }

        if (bundleFamily == null && resourceBundleFamilyFactory != null) {
            // try to load
            bundleFamily = resourceBundleFamilyFactory.createBundleFamily(basename, preview);
            registerBundleFamily(basename, preview, bundleFamily);
        }

        return bundleFamily;
    }

    private Optional<ResourceBundle> extractLocalizedBundle(final ResourceBundleFamily bundleFamily, final Locale locale) {
        if (locale != null) {
            @SuppressWarnings("unchecked")
            List<Locale> lookupLocales = (List<Locale>) LocaleUtils.localeLookupList(locale);
            for (Locale loc : lookupLocales) {
                ResourceBundle bundle = bundleFamily.getLocalizedBundle(loc);
                if (bundle != null) {
                    return Optional.of(bundle);
                }
            }
        }
        return Optional.empty();
    }

    private Map<String, ResourceBundleFamily> byBasename(final boolean preview) {
        return preview ? previewBundleFamilyByBasename : liveBundleFamilyByBasename;
    }

    private Map<String, ResourceBundleFamily> byIdentifier(final boolean preview) {
        return preview ? previewBundleFamilyByIdentifier : liveBundleFamilyByIdentifier;
    }

    private Set<String> unfoundBasenames(final boolean preview) {
        return preview ? previewUnfoundBasenames : liveUnfoundBasenames;
    }
}
