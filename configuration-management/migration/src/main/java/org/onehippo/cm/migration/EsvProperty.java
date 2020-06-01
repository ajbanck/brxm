/*
 *  Copyright 2016-2017 Hippo B.V. (http://www.onehippo.com)
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
package org.onehippo.cm.migration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.onehippo.cm.model.impl.tree.SourceLocationImpl;

public class EsvProperty {

    private static final HashSet<String> KNOWN_MULTIPLE_PROPERTIES = new HashSet<>(Arrays.asList(
            "autoexport:modules",
            "autoexport:reloadonstartuppaths",
            "autoexport:filteruuidpaths",
            "autoexport:excluded",
            "editor:extension",
            "frontend:services",
            "frontend:references",
            "frontend:properties",
            "frontend:nodetypes",
            "frontend:uuids",
            "hippo:modes",
            "hippo:values",
            "hippo:facets",
            "hippo:availability",
            "hippo:discriminator",
            "hippocollection:exceptions",
            "hippocollection:uuids",
            "hippofacnav:filters",
            "hippofacnav:modes",
            "hippofacnav:sortfunction",
            "hippofacnav:facets",
            "hippofacnav:filters",
            "hippofacnav:facetnodenames",
            "hippofacnav:sortby",
            "hippofacnav:sortorder",
            "hippohtmlcleaner:attributes",
            "hippohtmlcleaner:allowedDivClasses",
            "hippohtmlcleaner:allowedSpanClasses",
            "hippohtmlcleaner:allowedPreClasses",
            "hippohtmlcleaner:allowedParaClasses",
            "hippolog:eventArguments",
            "hipposched:attributeValues",
            "hipposched:attributeNames",
            "hipposearch:includes",
            "hipposearch:orderBy",
            "hipposearch:ascDesc",
            "hipposearch:excludes",
            "hippostd:foldertype",
            "hippostd:gallerytype",
            "hippostd:modify",
            "hippostd:tags",
            "hipposys:users",
            "hipposys:groups",
            "hipposys:excluded",
            "hipposys:privileges",
            "hipposys:members",
            "hipposys:previouspasswords",
            "hipposys:roles",
            "hipposys:search",
            "hipposysedit:validators",
            "hipposysedit:supertype",
            "hippotaxonomy:keys",
            "hippotaxonomy:synonyms",
            "hippotaxonomy:locales",
            "hippotaxonomy:synonyms",
            "hst:parameternameprefixes",
            "hst:parametervalues",
            "hst:parameternames",
            "hst:devices",
            "hst:users",
            "hst:roles",
            "hst:defaultsitemapitemhandlerids",
            "hst:containers",
            "hst:componentconfigurationmappingvalues",
            "hst:componentconfigurationmappingnames",
            "hst:sitemapitemhandlerids",
            "hst:inheritsfrom",
            "hst:formfieldmessages",
            "hst:formfielddata",
            "hst:diagnosticsforips",
            "hst:prefixexclusions",
            "hst:suffixexclusions",
            "poll:option",
            "reporting:parametervalues",
            "reporting:parameternames",
            "resourcebundle:messages",
            "resourcebundle:keys",
            "robotstxt:disallow",
            "robotstxt:sitemap",
            "targeting:propertynames",
            "targeting:propertyvalues",
            "targeting:skipUserAgents",
            "watchedModules"
    ));

    private final String name;
    private int type;
    private final SourceLocationImpl location;
    private Boolean multiple;
    private EsvMerge merge;
    private String mergeLocation;
    private List<EsvValue> values = new ArrayList<>();

    public static boolean isKnownMultipleProperty(String name) {
        return KNOWN_MULTIPLE_PROPERTIES.contains(name);
    }

    public EsvProperty(final String name, final int type, final SourceLocationImpl location) {
        this.name = name;
        this.type = type;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public SourceLocationImpl getSourceLocation() {
        return location;
    }

    public Boolean getMultiple() {
        return multiple;
    }

    public boolean isMultiple() {
        return (multiple != null && multiple) || (multiple == null && getValues().size() > 1);
    }

    public boolean isSingle() {
        return (multiple == null || !multiple) && getValues().size() == 1;
    }

    public void setMultiple(final Boolean multiple) {
        this.multiple = multiple;
    }

    public EsvMerge getMerge() {
        return merge;
    }

    public void setMerge(final EsvMerge merge) {
        this.merge = merge;
        if (isMergeAppend()) {
            multiple = Boolean.TRUE;
        }
    }

    public boolean isMergeSkip() {
        return EsvMerge.SKIP == merge;
    }

    public boolean isMergeOverride() {
        return EsvMerge.OVERRIDE == merge;
    }

    public boolean isMergeAppend() {
        return EsvMerge.APPEND == merge;
    }

    public String getMergeLocation() {
        return mergeLocation;
    }

    public void setMergeLocation(final String mergeLocation) {
        this.mergeLocation = mergeLocation;
    }

    public List<EsvValue> getValues() {
        return values;
    }

    public String getValue() {
        return getValues().size() == 1 ? getValues().get(0).getString() : null;
    }
}
