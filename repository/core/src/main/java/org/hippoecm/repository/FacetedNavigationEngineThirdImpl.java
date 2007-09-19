/*
  THIS CODE IS UNDER CONSTRUCTION, please leave as is until
  work has proceeded to a stable level, at which time this comment
  will be removed.  -- Berry
*/ 

/*
 * Copyright 2007 Hippo.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hippoecm.repository;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.jcr.Session;

import org.apache.jackrabbit.core.SearchManager;
import org.apache.jackrabbit.core.query.lucene.NamespaceMappings;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.BooleanClause.Occur;
import org.hippoecm.repository.query.lucene.AuthorizationQuery;
import org.hippoecm.repository.query.lucene.FacetPropExistsQuery;
import org.hippoecm.repository.query.lucene.FacetResultCollector;
import org.hippoecm.repository.query.lucene.FacetsQuery;
import org.hippoecm.repository.query.lucene.ServicingIndexingConfiguration;
import org.hippoecm.repository.query.lucene.ServicingSearchIndex;
import org.hippoecm.repository.servicing.RepositoryDecorator;

public class FacetedNavigationEngineThirdImpl
  implements FacetedNavigationEngine<FacetedNavigationEngineThirdImpl.QueryImpl, FacetedNavigationEngineThirdImpl.ContextImpl>
{
  class QueryImpl extends FacetedNavigationEngine.Query {
    String xpath;
    public QueryImpl(String xpath) {
      this.xpath = xpath;
    }
    public String toString() {
      return xpath;
    }
  }
  
  class ResultImpl extends FacetedNavigationEngine.Result {
      int length;
      Iterator<String> iter = null;
      ResultImpl(int length, Set<String> result) {
          this.length = length;
          if(result!= null) {
              this.iter = result.iterator();
          }
      }
      public int length() {
          return length;
      }
      public Iterator<String> iterator() {
          return iter;
      }
      public String toString() {
          return getClass().getName()+"[length="+length+"]";
      }
  }
  
  class ContextImpl extends FacetedNavigationEngine.Context {
    Session session;
    String principal;
    Map<String,String[]> authorizationQuery;
    ContextImpl(Session session, String principal, Map<String,String[]> authorizationQuery) {
      this.session = session;
      this.principal = principal;
      this.authorizationQuery = authorizationQuery;
    }
  }
 
  private Map<IndexReader, Map<String,Map<Integer, String[]>>> tfvCache ;
  
  public FacetedNavigationEngineThirdImpl() {
      this.tfvCache = new WeakHashMap<IndexReader, Map<String,Map<Integer, String[]>>>();
  }

  public ContextImpl prepare(String principal, Map<String,String[]> authorizationQuery, List<QueryImpl> initialQueries, Session session) {
    return new ContextImpl(session, principal, authorizationQuery);
  }
  public void unprepare(ContextImpl authorization) {
    // deliberate ignore
  }
  public void reload(Map<String,String[]> facetValues) {
    // deliberate ignore
  }
  public boolean requiresReload() {
    return false;
  }
  public boolean requiresNotify() {
    return false;
  }
  public void notify(String docId, Map<String,String[]> oldFacets, Map<String,String[]> newFacets) {
    // deliberate ignore
  }
  public void purge() {
    // deliberate ignore
  }

  public Result view(String queryName, QueryImpl initialQuery, ContextImpl authorization,
                   Map<String,String> facetsQueryMap, QueryImpl openQuery,
                   Map<String,Map<String,Count>> resultset,
                   Map<Map<String,String>,Map<String,Map<String,Count>>> futureFacetsQueries,
                   HitsRequested hitsRequested) throws UnsupportedOperationException
  {
    try {
      Session session = authorization.session;
      
      SearchManager searchManager = ((RepositoryDecorator)session.getRepository()).getSearchManager(session.getWorkspace().getName()) ;
      ServicingSearchIndex index = (ServicingSearchIndex)searchManager.getQueryHandler();
      NamespaceMappings nsMappings = index.getNamespaceMappings();
    
      /*
       * facetsQuery: get the query for the facets that are asked for
       */
      FacetsQuery facetsQuery = new FacetsQuery(facetsQueryMap, nsMappings, (ServicingIndexingConfiguration)index.getIndexingConfig());
      
      /*
       * authorizationQuery: get the query for the facets the person is allowed to see (which
       * is again a facetsQuery)
       */
      
      AuthorizationQuery authorizationQuery = new AuthorizationQuery(authorization.authorizationQuery, 
                                                                     facetsQueryMap , 
                                                                     nsMappings, 
                                                                     (ServicingIndexingConfiguration)index.getIndexingConfig(),
                                                                     true); 
 
      FacetResultCollector collector = null; 
      IndexReader indexReader = null;
      IndexSearcher searcher = null; ;
      try {
          indexReader = index.getIndex().getIndexReader();
          searcher = new IndexSearcher(indexReader);
      
          // In principle, below, there is always one facet 
          for(String facet : resultset.keySet()) {
              /*
               * facetPropExists: the document must have the property as facet
               */
              FacetPropExistsQuery facetPropExists = new FacetPropExistsQuery(facet, nsMappings, (ServicingIndexingConfiguration)index.getIndexingConfig());
              
              BooleanQuery searchQuery = new BooleanQuery();
              searchQuery.add(facetPropExists.getQuery(), Occur.MUST);
              
              if(facetsQuery.getQuery().clauses().size() > 0){
                  searchQuery.add(facetsQuery.getQuery(), Occur.MUST);
              }
              // TODO perhaps create cached user specific filter for authorisation to gain speed
              if(authorizationQuery.getQuery().clauses().size() > 0){
                  searchQuery.add(authorizationQuery.getQuery(), Occur.MUST);
              }
              
              long start = System.currentTimeMillis();
             
              collector = new FacetResultCollector(indexReader, facet, resultset, hitsRequested, nsMappings);
              searcher.search(searchQuery, collector);
             
          }
          
      } catch (IOException e) {
          e.printStackTrace();
          
      } finally {
          if(searcher != null){
              try {
                 searcher.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
         if(indexReader != null) {
            try {
                indexReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
         }
      }

      return this . new ResultImpl(collector.getNumhits(), collector.getHits());
    } catch(javax.jcr.query.InvalidQueryException ex) {
      System.err.println(ex.getClass().getName()+": "+ex.getMessage());
      throw new UnsupportedOperationException(); // FIXME
    } catch(javax.jcr.ValueFormatException ex) {
      System.err.println(ex.getClass().getName()+": "+ex.getMessage());
      throw new UnsupportedOperationException(); // FIXME
    } catch(javax.jcr.RepositoryException ex) {
      System.err.println(ex.getClass().getName()+": "+ex.getMessage());
      throw new UnsupportedOperationException(); // FIXME
    }
  }

  public Result view(String queryName, QueryImpl initialQuery, ContextImpl authorization,
                     Map<String,String> facetsQuery, QueryImpl openQuery, HitsRequested hitsRequested)
  {
      return view(queryName, initialQuery, authorization, facetsQuery, openQuery, null, null, hitsRequested);
   
  }

  public QueryImpl parse(String query)
  {
    return this . new QueryImpl(query);
  }


}
