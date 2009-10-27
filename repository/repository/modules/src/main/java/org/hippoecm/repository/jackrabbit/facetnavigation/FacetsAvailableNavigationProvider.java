package org.hippoecm.repository.jackrabbit.facetnavigation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.jcr.NamespaceException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.core.NodeId;
import org.apache.jackrabbit.core.state.NodeState;
import org.apache.jackrabbit.core.state.PropertyState;
import org.apache.jackrabbit.core.value.InternalValue;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.commons.conversion.IllegalNameException;
import org.apache.jackrabbit.spi.commons.conversion.MalformedPathException;
import org.hippoecm.repository.FacetedNavigationEngine;
import org.hippoecm.repository.FacetedNavigationEngine.HitsRequested;
import org.hippoecm.repository.api.HippoNodeType;
import org.hippoecm.repository.api.NodeNameCodec;
import org.hippoecm.repository.jackrabbit.FacetResultSetProvider;
import org.hippoecm.repository.jackrabbit.HippoNodeId;
import org.hippoecm.repository.jackrabbit.KeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FacetsAvailableNavigationProvider extends AbstractFacetNavigationProvider {

    private final Logger log = LoggerFactory.getLogger(FacetsAvailableNavigationProvider.class);
    
	protected FacetSubNavigationProvider facetsSubNavigationProvider = null; 
    protected FacetResultSetProvider subNodesProvider = null;
	
    @Override
    public void initialize() throws RepositoryException {
        super.initialize();
        facetsSubNavigationProvider = (FacetSubNavigationProvider) lookup(FacetSubNavigationProvider.class.getName());
        subNodesProvider  = (FacetResultSetProvider) lookup(FacetResultSetProvider.class.getName());
        virtualNodeName = resolveName(HippoNodeType.NT_FACETSAVAILABLENAVIGATION);
        register(null, virtualNodeName);
    }

 
    @Override
    public NodeState populate(NodeState state) throws RepositoryException {
    	NodeId nodeId = state.getNodeId();
    	if (nodeId instanceof FacetNavigationNodeId) {
    		FacetNavigationNodeId facetNavigationNodeId = (FacetNavigationNodeId)nodeId;
    		Map<String, String> currentSearch = facetNavigationNodeId.currentSearch;
    		String currentFacet = facetNavigationNodeId.currentFacet;
    		String[] availableFacets = facetNavigationNodeId.availableFacets;
    	    String docbase = facetNavigationNodeId.docbase;
    	    String[] ancestorAndSelfSubNavNames = facetNavigationNodeId.ancestorAndSelfSubNavNames; 
    	    
    	    Map<Name,String> inheritedFilter = facetNavigationNodeId.view;
    	    
    	    String resolvedFacet = null;
            try {
            	resolvedFacet = resolvePath(currentFacet).toString();
            } catch (IllegalNameException e) {
            	log.warn("Cannot resolve path for facet: '{}' : {}", currentFacet, e.getMessage());
            } catch (NamespaceException e) {
            	log.warn("Cannot resolve path for facet: '{}' : {}", currentFacet, e.getMessage());
            } catch (MalformedPathException e) {
            	log.warn("Cannot resolve path for facet: '{}' : {}", currentFacet, e.getMessage());
            }
            if(resolvedFacet == null) {
            	return state;
            }
    	    
    		Map<String, Map<String, FacetedNavigationEngine.Count>> facetSearchResultMap;
            facetSearchResultMap = new TreeMap<String, Map<String, FacetedNavigationEngine.Count>>();
            Map<String, FacetedNavigationEngine.Count> facetSearchResult;
            facetSearchResult = new TreeMap<String, FacetedNavigationEngine.Count>();
            
            facetSearchResultMap.put(resolvedFacet, facetSearchResult);
            
            FacetedNavigationEngine.Query initialQuery;
            initialQuery = (docbase != null ? facetedEngine.parse(docbase) : null);

            HitsRequested hitsRequested = new HitsRequested();
            hitsRequested.setResultRequested(false);
            hitsRequested.setCountOnlyForFacetExists(true);
            
            FacetedNavigationEngine.Result facetedResult;
            facetedResult = facetedEngine.view(null, initialQuery, facetedContext, currentSearch, null,
                    facetSearchResultMap, inheritedFilter , hitsRequested);
         
            int count = facetedResult.length();

            PropertyState propState = createNew(countName, state.getNodeId());
            propState.setType(PropertyType.LONG);
            propState.setDefinitionId(subCountPropDef.getId());
            propState.setValues(new InternalValue[] { InternalValue.create(count) });
            propState.setMultiValued(false);
            state.addPropertyName(countName);
            
            // add child node subnavigation:
            
            // facetSearchResult logicals default order is the natural descending order of the count. Therefore, we need to create sort the facetSearchResult first.
            FacetNavigationEntry[] facetNavigationEntry = new FacetNavigationEntry[facetSearchResult.size()];
            int i = 0;
            for (Map.Entry<String, FacetedNavigationEngine.Count> entry : facetSearchResult.entrySet()) {
                facetNavigationEntry[i] = new FacetNavigationEntry(entry.getKey(), entry.getValue());
                i++;
            }
            // sort according count
            Arrays.sort(facetNavigationEntry);
            
            for(FacetNavigationEntry entry : facetNavigationEntry) {
                if (entry.facetValue.length() > 1) {
                	Map<String, String> newSearch = new HashMap<String, String>(currentSearch);
                	
                    // nextTerm is the next facet value to search for (skip last char because this is the facet type constant)
                    String nextFacet = entry.facetValue.substring(0, entry.facetValue.length() - 1);
                    Character facetTypeConstant = entry.facetValue.charAt(entry.facetValue.length() - 1);
                    
                    newSearch.put(resolvedFacet, nextFacet);
                    List<KeyValue<String,String>> usedFacetValueCombis = new ArrayList<KeyValue<String,String>>(facetNavigationNodeId.usedFacetValueCombis);     
                    KeyValue<String, String> facetValueCombi = new FacetKeyValue(currentFacet, nextFacet);
                    
                    boolean stopSubNavigation = facetNavigationNodeId.stopSubNavigation;
                    if(!usedFacetValueCombis.contains(facetValueCombi)) {
                        usedFacetValueCombis.add(facetValueCombi);
                        /*
                         * perhaps stopNavigation was already true, but, since it is a new unique facetvalue combi, we need
                         * to populate it further: this happens when a facet has multiple property values
                         */ 
                        stopSubNavigation = false;
                    }
                    try { 
                        String name = getDisplayName(nextFacet, facetTypeConstant);
                        Name childName = resolveName(NodeNameCodec.encode(name));
                        FacetNavigationNodeId childNodeId = new FacetNavigationNodeId(facetsSubNavigationProvider, state.getNodeId(), childName);
                        state.addChildNodeEntry(childName, childNodeId);
                        childNodeId.docbase = docbase;
                        childNodeId.availableFacets = availableFacets;
                        childNodeId.currentSearch = newSearch;
                        childNodeId.currentFacet = currentFacet;
                        
                        String[] newAncestorAndSelfSubNavNames = new String[ancestorAndSelfSubNavNames != null ? ancestorAndSelfSubNavNames.length + 1 : 1];
                        if (ancestorAndSelfSubNavNames != null && ancestorAndSelfSubNavNames.length > 0) {
                            System.arraycopy(ancestorAndSelfSubNavNames, 0, newAncestorAndSelfSubNavNames, 0, ancestorAndSelfSubNavNames.length);
                        }
                        newAncestorAndSelfSubNavNames[newAncestorAndSelfSubNavNames.length - 1] = name;
                        
                        childNodeId.ancestorAndSelfSubNavNames = newAncestorAndSelfSubNavNames;
                        childNodeId.usedFacetValueCombis = usedFacetValueCombis;
                        childNodeId.facetNodeNames = facetNavigationNodeId.facetNodeNames;
                        childNodeId.stopSubNavigation = stopSubNavigation;
                		childNodeId.view = facetNavigationNodeId.view;
        				childNodeId.order = facetNavigationNodeId.order;
        				childNodeId.singledView = facetNavigationNodeId.singledView;
                    } catch (RepositoryException ex) {
                        log.warn("cannot add virtual child in facet search: " + ex.getMessage());
                    }
                } else {
                    log.debug("facet value with only facet type constant found. Skip result");
                }
            }
            
            // add child node resultset:
            // we add to the search now the current facet with no value: this will make sure that 
            // the result set nodes at least contain the facet
            
            Name resultSetChildName = resolveName(HippoNodeType.HIPPO_RESULTSET);
            Map<String, String> resultSetSearch = new HashMap<String, String>(currentSearch);
            // we add here the 'facet' without value, to make sure we only get results having at least the current facet as property
            if(!resultSetSearch.containsKey(resolvedFacet)) {
                resultSetSearch.put(resolvedFacet, null);
            }
            
            FacetResultSetProvider.FacetResultSetNodeId childNodeId;
            childNodeId = subNodesProvider.new FacetResultSetNodeId(state.getNodeId(), resultSetChildName, null,
                    docbase, resultSetSearch, count);
            state.addChildNodeEntry(resultSetChildName, childNodeId);
            
    	}
        
        return state;
    }
    
    @Override
    public NodeState populate(HippoNodeId nodeId, NodeId parentId) throws RepositoryException {
        NodeState state = createNew(nodeId, virtualNodeName, parentId);
        state.setDefinitionId(lookupNodeDef(getNodeState(parentId), resolveName(HippoNodeType.NT_FACETSAVAILABLENAVIGATION),
                nodeId.name).getId());
        state.setNodeTypeName(resolveName(HippoNodeType.NT_FACETSAVAILABLENAVIGATION));

        return populate(state);
    }
}
