/*
 *  Copyright 2011-2013 Hippo B.V. (http://www.onehippo.com)
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
package org.hippoecm.hst.pagecomposer.jaxrs.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.hippoecm.hst.core.parameters.DocumentLink;
import org.hippoecm.hst.core.parameters.DropDownList;
import org.hippoecm.hst.core.parameters.JcrPath;
import org.hippoecm.hst.core.parameters.Parameter;
import org.hippoecm.hst.core.parameters.ParametersInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParametersInfoProcessor {

    static final Logger log = LoggerFactory.getLogger(ParametersInfoProcessor.class);

    private final Set<CacheKey> failedBundlesToLoad = Collections.newSetFromMap(new ConcurrentHashMap<CacheKey, Boolean>());

    public List<ContainerItemComponentPropertyRepresentation> getProperties(ParametersInfo parameterInfo, Locale locale, String currentMountCanonicalContentPath) {
        final List<ContainerItemComponentPropertyRepresentation> properties = new ArrayList<ContainerItemComponentPropertyRepresentation>();

        final Class<?> classType = parameterInfo.type();
        if (classType == null) {
            return properties;
        }

        ResourceBundle[] resourceBundles = getResourceBundles(parameterInfo, locale);

        for (Method method : classType.getMethods()) {
            if (method.isAnnotationPresent(Parameter.class)) {
                final Parameter propAnnotation = method.getAnnotation(Parameter.class);
                final ContainerItemComponentPropertyRepresentation prop = new ContainerItemComponentPropertyRepresentation();
                prop.setName(propAnnotation.name());
                prop.setDefaultValue(propAnnotation.defaultValue());
                prop.setDescription(propAnnotation.description());
                prop.setRequired(propAnnotation.required());
                prop.setHiddenInChannelManager(propAnnotation.hideInChannelManager());
                boolean labelSet = false;
                for (ResourceBundle resourceBundle : resourceBundles) {
                    if (resourceBundle.containsKey(propAnnotation.name())) {
                        prop.setLabel(resourceBundle.getString(propAnnotation.name()));
                        labelSet = true;
                        break;
                    }
                }
                if (!labelSet) {
                    if (propAnnotation.displayName().equals("")) {
                        prop.setLabel(propAnnotation.name());
                    } else {
                        prop.setLabel(propAnnotation.displayName());
                    }
                }

                final Annotation annotation = ParameterType.getTypeAnnotation(method);
                if (annotation instanceof DocumentLink) {
                    // for DocumentLink we need some extra processing
                    final DocumentLink documentLink = (DocumentLink) annotation;
                    prop.setDocType(documentLink.docType());
                    prop.setDocLocation(documentLink.docLocation());
                    prop.setAllowCreation(documentLink.allowCreation());
                } else if (annotation instanceof JcrPath) {
                    // for JcrPath we need some extra processing too
                    final JcrPath jcrPath = (JcrPath) annotation;
                    prop.setPickerConfiguration(jcrPath.pickerConfiguration());
                    prop.setPickerInitialPath(jcrPath.pickerInitialPath());
                    prop.setPickerRootPath(currentMountCanonicalContentPath);
                    prop.setPickerPathIsRelative(jcrPath.isRelative());
                    prop.setPickerRemembersLastVisited(jcrPath.pickerRemembersLastVisited());
                    prop.setPickerSelectableNodeTypes(jcrPath.pickerSelectableNodeTypes());
                } else if (annotation instanceof DropDownList) {
                    final DropDownList dropDownList = (DropDownList) annotation;
                    final String values[] = dropDownList.value();
                    final String[] displayValues = new String[values.length];

                    for (int i = 0; i < values.length; i++) {
                        final String resourceKey = propAnnotation.name() + "/" + values[i];
                        boolean displayValueSet = false;
                        for (ResourceBundle resourceBundle : resourceBundles) {
                            if (resourceBundle.containsKey(resourceKey)) {
                                displayValues[i] = resourceBundle.getString(resourceKey);
                                displayValueSet = true;
                                break;
                            }
                        }
                        if (!displayValueSet) {
                            displayValues[i] = values[i];
                        }
                    }

                    prop.setDropDownListValues(values);
                    prop.setDropDownListDisplayValues(displayValues);
                }

                final ParameterType type = ParameterType.getType(method, annotation);
                prop.setType(type);

                // Set the value to be default value before setting it with original value
                properties.add(prop);
            }
        }

        return properties;
    }

    /**
     *
     * @return the ResourceBundles for <code><parameterInfo.type()</code> including the bundles for the super interfaces for
     * <code><parameterInfo.type()</code> and <code>locale</code>. The resource bundles are ordered according the interface
     * hierarchy BREADTH FIRST traversal. Empty array if there are no resource bundles at all
     */
    protected final ResourceBundle[] getResourceBundles(final ParametersInfo parameterInfo, final Locale locale) {
        List<ResourceBundle> resourceBundles = new ArrayList<ResourceBundle>();

        final List<Class<?>> breadthFirstInterfaceHierarchy = getBreadthFirstInterfaceHierarchy(parameterInfo.type());
        for (Class<?> clazz : breadthFirstInterfaceHierarchy) {
            ResourceBundle bundle = getResourceBundle(clazz, locale);
            if (bundle != null) {
                resourceBundles.add(bundle);
            }
        }
        return resourceBundles.toArray(new ResourceBundle[resourceBundles.size()]);
    }

    List<Class<?>> getBreadthFirstInterfaceHierarchy(final Class<?> clazz) {
        final List<Class<?>> interfaceHierarchList = new ArrayList<Class<?>>();
        interfaceHierarchList.add(clazz);
        populateBreadthFirstSuperInterfaces(clazz.getInterfaces(), interfaceHierarchList);
        return interfaceHierarchList;
    }

    private void populateBreadthFirstSuperInterfaces(final Class<?>[] interfaces, final List<Class<?>> populatedSuperInterfaces) {
        for (Class<?> clazz : interfaces) {
            populatedSuperInterfaces.add(clazz);
        }
        List<Class<?>> superInterfaces = new ArrayList<Class<?>>();
        for (Class<?> clazz : interfaces) {
            superInterfaces.addAll(Arrays.asList(clazz.getInterfaces()));
        }
        if (superInterfaces.size() == 0) {
            return;
        }
        populateBreadthFirstSuperInterfaces(superInterfaces.toArray(new Class[superInterfaces.size()]), populatedSuperInterfaces);
    }

    /**
     * @return the ResourceBundle for <code><parameterInfo.type()</code> and <code>locale</code> or <code>null</code> when
     * it cannot be loaded
     */
    protected final ResourceBundle getResourceBundle(final ParametersInfo parameterInfo, final Locale locale) {
        return getResourceBundle(parameterInfo.type(), locale);
    }

    private ResourceBundle getResourceBundle(final Class<?> clazz, final Locale locale) {
        final Locale localeOrDefault;
        if (locale == null) {
            localeOrDefault = Locale.getDefault();
        } else {
            localeOrDefault = locale;
        }
        final String typeName = clazz.getName();
        CacheKey bundleKey = new CacheKey(typeName, localeOrDefault);
        if (failedBundlesToLoad.contains(bundleKey)) {
            return null;
        }
        try {
            return ResourceBundle.getBundle(typeName, localeOrDefault);
        } catch (MissingResourceException e) {
            log.info("Could not find a resource bundle for class '{}', locale '{}'. The template composer " +
                    "properties panel will show displayName values instead of internationalized labels.", typeName, locale);
            failedBundlesToLoad.add(bundleKey);
            return null;
        }
    }

    private static class CacheKey {
        private final String type;
        private final Locale locale;

        private CacheKey(final String type, final Locale locale) {
            if (type == null || locale == null) {
                throw new IllegalArgumentException("Both type and locale are not allowed to be null");
            }
            this.type = type;
            this.locale = locale;
        }

        @Override
        public int hashCode() {
            return type.hashCode() * 7 + locale.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof CacheKey) {
                CacheKey other = (CacheKey) obj;
                return other.type.equals(type) && other.locale.equals(locale);
            }
            return false;
        }
    }

}
