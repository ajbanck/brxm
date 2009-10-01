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
package org.hippoecm.repository.api;

import java.util.Map;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.version.VersionException;

/**
 * The HippoQuery is an extension to javax.jcr.query.Query to implement parameterized queries.
 * Its interface is compliant with the upcoming standard of JCR2 (JSR-283) which leveraged the most important extension implemented; parameterized (stored) queries.
 */
public interface HippoQuery extends Query {
    final static String SVN_ID = "$Id$";

    /**
     * This query language is not yet available as public API, but reserved for future use.
     */
    public static final String HIPPOQL = "HIPPOQL";

    /**
     * Obtains the session in which this query can be executed.
     * @return the javax.jcr.Session associated with this Query instance
     * @throws javax.jcr.RepositoryException if a generic repossitory error occurs
     */
    public Session getSession() throws RepositoryException;

    public Node storeAsNode(String absPath, String type) throws ItemExistsException, PathNotFoundException, VersionException,
            ConstraintViolationException, LockException, UnsupportedRepositoryOperationException, RepositoryException;

    public String[] getArguments() throws RepositoryException;

    /**
     * This addition has no counterpart in JSR-283, JSR-283 only compliant applications have no means to query which parameters should be bound before a query can be executed.  The extension of the HippoQuery class makes this possible.
     * @return the number of bindable parameters in the stored query
     * @throws javax.jcr.RepositoryException in case of an internal or connection error
     */
    public int getArgumentCount() throws RepositoryException;

    /**
     * Convenience method to bind all arguments of a query using a single map and immediately execute the query.
     * A JSR-283 only compliant application should use individual bindValue calls to bind each argument and then perform a no-argument execute call.
     * @param arguments a map of string keys of the free varia
     * @return the query result
     * @throws javax.jcr.RepositoryException in case of an internal or connection error
     */
    public QueryResult execute(Map<String,String> arguments) throws RepositoryException;

    public void bindValue(String varName, Value value) throws IllegalArgumentException, RepositoryException;

    public void setLimit(long limit) throws RepositoryException;

    public void setOffset(long offset) throws RepositoryException;

    /* FIXME
     * The following methods are part of JCR2, and should be supported
     * as well within the QueryManager class:
     *
     * public PreparedQuery QueryManager.createPreparedQuery(String statement, String language) throws InvalidQueryException, RepositoryException;
     */
}
