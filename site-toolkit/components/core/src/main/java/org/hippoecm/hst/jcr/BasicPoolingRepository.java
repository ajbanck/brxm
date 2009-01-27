package org.hippoecm.hst.jcr;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.hippoecm.repository.HippoRepository;
import org.hippoecm.repository.HippoRepositoryFactory;

/**
 * <p>Basic implementation of <code>javax.jcr.Repository</code> that is
 * configured via JavaBeans properties.</p>
 *
 * @author <a href="mailto:w.ko@onehippo.com">Woonsan Ko</a>
 * @version $Id$
 */
public class BasicPoolingRepository implements Repository
{
    protected HippoRepository repository;
    protected SimpleCredentials simpleCredentials;
    
    public String getDescriptor(String arg0)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String[] getDescriptorKeys()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Session login() throws LoginException, RepositoryException
    {
        return null;
    }

    /**
     * <strong>BasicPoolingRepository does NOT support this method. </strong>
     *
     * @throws UnsupportedOperationException
     * @throws LoginException
     * @throws RepositoryException
     * @return nothing - always throws UnsupportedOperationException
     */
    public Session login(Credentials arg0) throws LoginException, RepositoryException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * <strong>BasicPoolingRepository does NOT support this method. </strong>
     *
     * @throws UnsupportedOperationException
     * @throws LoginException
     * @throws RepositoryException
     * @return nothing - always throws UnsupportedOperationException
     */
    public Session login(String arg0) throws LoginException, NoSuchWorkspaceException, RepositoryException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * <strong>BasicPoolingRepository does NOT support this method. </strong>
     *
     * @throws UnsupportedOperationException
     * @throws LoginException
     * @throws RepositoryException
     * @return nothing - always throws UnsupportedOperationException
     */
    public Session login(Credentials arg0, String arg1) throws LoginException, NoSuchWorkspaceException, RepositoryException
    {
        throw new UnsupportedOperationException();
    }

    
    // Pool implementation
    
    /**
     * The object pool that internally manages our sessions.
     */
    protected GenericObjectPool sessionPool;
    /**
     * The maximum number of active sessions that can be allocated from
     * this pool at the same time, or negative for no limit.
     */
    protected int maxActive = GenericObjectPool.DEFAULT_MAX_ACTIVE;
    /**
     * The maximum number of sessions that can remain idle in the
     * pool, without extra ones being released, or negative for no limit.
     */
    protected int maxIdle = GenericObjectPool.DEFAULT_MAX_IDLE;
    /**
     * The minimum number of active sessions that can remain idle in the
     * pool, without extra ones being created, or 0 to create none.
     */
    protected int minIdle = GenericObjectPool.DEFAULT_MIN_IDLE;
    /**
     * The initial number of sessions that are created when the pool
     * is started.
     */
    protected int initialSize = 0;
    /**
     * The maximum number of milliseconds that the pool will wait (when there
     * are no available sessions) for a session to be returned before
     * throwing an exception, or <= 0 to wait indefinitely.
     */
    protected long maxWait = GenericObjectPool.DEFAULT_MAX_WAIT;
    /**
     * The indication of whether objects will be validated before being
     * borrowed from the pool.  If the object fails to validate, it will be
     * dropped from the pool, and we will attempt to borrow another.
     */
    protected boolean testOnBorrow = true;
    /**
     * The indication of whether objects will be validated before being
     * returned to the pool.
     */
    protected boolean testOnReturn = false;
    /**
     * The number of milliseconds to sleep between runs of the idle object
     * evictor thread.  When non-positive, no idle object evictor thread will
     * be run.
     */
    protected long timeBetweenEvictionRunsMillis =
        GenericObjectPool.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS;
    /**
     * The number of objects to examine during each run of the idle object
     * evictor thread (if any).
     */
    protected int numTestsPerEvictionRun =
        GenericObjectPool.DEFAULT_NUM_TESTS_PER_EVICTION_RUN;
    /**
     * The minimum amount of time an object may sit idle in the pool before it
     * is eligable for eviction by the idle object evictor (if any).
     */
    protected long minEvictableIdleTimeMillis =
        GenericObjectPool.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS;
    /**
     * The indication of whether objects will be validated by the idle object
     * evictor (if any).  If an object fails to validate, it will be dropped
     * from the pool.
     */
    protected boolean testWhileIdle = false;
    /**
     * The password to be passed to our repository to establish
     * a session.
     */
    protected String password = null;
    /**
     * The repository location to be passed to our repository to establish
     * a session.
     */
    protected String location = null;
    /**
     * The username to be passed to our repository to
     * establish a session.
     */
    protected String username = null;
    /**
     * The query that will be used to validate sessions from this pool
     * before returning them to the caller.  If specified, this query
     * <strong>MUST</strong> be a valid statement.
     */
    protected String validationQuery = null;
    
    public synchronized void initialize() throws Exception
    {
        if (sessionPool != null)
        {
            try
            {
                sessionPool.close();
            }
            catch (Exception e)
            {
                
            }
        }
        
        sessionPool = new GenericObjectPool(new SessionFactory());
        
        sessionPool.setMaxActive(maxActive);
        sessionPool.setMaxIdle(maxIdle);
        sessionPool.setMinIdle(minIdle);
        sessionPool.setMaxWait(maxWait);
        sessionPool.setTestOnBorrow(testOnBorrow);
        sessionPool.setTestOnReturn(testOnReturn);
        sessionPool.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        sessionPool.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
        sessionPool.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        sessionPool.setTestWhileIdle(testWhileIdle);

        for (int i = 0 ; i < initialSize ; i++) 
        {
            sessionPool.addObject();
        }
    }        
    
    /**
     * Close and release all sessions that are currently stored in the
     * session pool associated with our data source.  All open (active)
     * session remain open until closed.  Once the pooling repository has
     * been closed, no more sessions can be obtained.
     * @throws Exception 
     */
    public synchronized void close() throws Exception
    {
        sessionPool.close();
        sessionPool = null;
    }
    
    /**
     * <p>Returns the maximum number of active connections that can be
     * allocated at the same time.
     * </p>
     * <p>A negative number means that there is no limit.</p>
     * 
     * @return the maximum number of active connections
     */
    public synchronized int getMaxActive() 
    {
        return this.maxActive;
    }

    /**
     * Sets the maximum number of active connections that can be
     * allocated at the same time. Use a negative value for no limit.
     * 
     * @param maxActive the new value for maxActive
     * @see #getMaxActive()
     */
    public synchronized void setMaxActive(int maxActive) 
    {
        this.maxActive = maxActive;
        
        if (sessionPool != null) 
        {
            sessionPool.setMaxActive(maxActive);
        }
    }

    /**
     * <p>Returns the maximum number of connections that can remain idle in the
     * pool.
     * </p>
     * <p>A negative value indicates that there is no limit</p>
     * 
     * @return the maximum number of idle connections
     */
    public synchronized int getMaxIdle() {
        return this.maxIdle;
    }

    /**
     * Sets the maximum number of connections that can remain idle in the
     * pool.
     * 
     * @see #getMaxIdle()
     * @param maxIdle the new value for maxIdle
     */
    public synchronized void setMaxIdle(int maxIdle) 
    {
        this.maxIdle = maxIdle;
        
        if (sessionPool != null) 
        {
            sessionPool.setMaxIdle(maxIdle);
        }
    }

    /**
     * Returns the minimum number of idle connections in the pool
     * 
     * @return the minimum number of idle connections
     * @see GenericObjectPool#getMinIdle()
     */
    public synchronized int getMinIdle() 
    {
        return this.minIdle;
    }

    /**
     * Sets the minimum number of idle connections in the pool.
     * 
     * @param minIdle the new value for minIdle
     * @see GenericObjectPool#setMinIdle(int)
     */
    public synchronized void setMinIdle(int minIdle) 
    {
       this.minIdle = minIdle;
       
       if (sessionPool != null) 
       {
           sessionPool.setMinIdle(minIdle);
       }
    }

    /**
     * Returns the initial size of the connection pool.
     * 
     * @return the number of connections created when the pool is initialized
     */
    public synchronized int getInitialSize() 
    {
        return this.initialSize;
    }
    
    /**
     * <p>Sets the initial size of the connection pool.</p>
     * <p>
     * Note: this method currently has no effect once the pool has been
     * initialized.  The pool is initialized the first time one of the
     * following methods is invoked: <code>getConnection, setLogwriter,
     * setLoginTimeout, getLoginTimeout, getLogWriter.</code></p>
     * 
     * @param initialSize the number of connections created when the pool
     * is initialized
     */
    public synchronized void setInitialSize(int initialSize) 
    {
        this.initialSize = initialSize;
    }

    /**
     * <p>Returns the maximum number of milliseconds that the pool will wait
     * for a connection to be returned before throwing an exception.
     * </p>
     * <p>A value less than or equal to zero means the pool is set to wait
     * indefinitely.</p>
     * 
     * @return the maxWait property value
     */
    public synchronized long getMaxWait() 
    {
        return this.maxWait;
    }

    /**
     * <p>Sets the maxWait property.
     * </p>
     * <p>Use -1 to make the pool wait indefinitely.
     * </p>
     * 
     * @param maxWait the new value for maxWait
     * @see #getMaxWait()
     */
    public synchronized void setMaxWait(long maxWait) 
    {
        this.maxWait = maxWait;
        
        if (sessionPool != null) 
        {
            sessionPool.setMaxWait(maxWait);
        }
    }

    /**
     * Returns the {@link #testOnBorrow} property.
     * 
     * @return true if objects are validated before being borrowed from the
     * pool
     * 
     * @see #testOnBorrow
     */
    public synchronized boolean getTestOnBorrow() 
    {
        return this.testOnBorrow;
    }

    /**
     * Sets the {@link #testOnBorrow} property. This property determines
     * whether or not the pool will validate objects before they are borrowed
     * from the pool. For a <code>true</code> value to have any effect, the 
     * <code>validationQuery</code> property must be set to a non-null string.
     * 
     * @param testOnBorrow new value for testOnBorrow property
     */
    public synchronized void setTestOnBorrow(boolean testOnBorrow) 
    {
        this.testOnBorrow = testOnBorrow;
        
        if (sessionPool != null) 
        {
            sessionPool.setTestOnBorrow(testOnBorrow);
        }
    }

    /**
     * Returns the value of the {@link #testOnReturn} property.
     * 
     * @return true if objects are validated before being returned to the
     * pool
     * @see #testOnReturn
     */
    public synchronized boolean getTestOnReturn() 
    {
        return this.testOnReturn;
    }

    /**
     * Sets the <code>testOnReturn</code> property. This property determines
     * whether or not the pool will validate objects before they are returned
     * to the pool. For a <code>true</code> value to have any effect, the 
     * <code>validationQuery</code> property must be set to a non-null string.
     * 
     * @param testOnReturn new value for testOnReturn property
     */
    public synchronized void setTestOnReturn(boolean testOnReturn) 
    {
        this.testOnReturn = testOnReturn;
        
        if (sessionPool != null) 
        {
            sessionPool.setTestOnReturn(testOnReturn);
        }
    }

    /**
     * Returns the value of the {@link #timeBetweenEvictionRunsMillis}
     * property.
     * 
     * @return the time (in miliseconds) between evictor runs
     * @see #timeBetweenEvictionRunsMillis
     */
    public synchronized long getTimeBetweenEvictionRunsMillis() 
    {
        return this.timeBetweenEvictionRunsMillis;
    }

    /**
     * Sets the {@link #timeBetweenEvictionRunsMillis} property.
     * 
     * @param timeBetweenEvictionRunsMillis the new time between evictor runs
     * @see #timeBetweenEvictionRunsMillis
     */
    public synchronized void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) 
    {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
        
        if (sessionPool != null) 
        {
            sessionPool.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        }
    }

    /**
     * Returns the value of the {@link #numTestsPerEvictionRun} property.
     * 
     * @return the number of objects to examine during idle object evictor
     * runs
     * @see #numTestsPerEvictionRun
     */
    public synchronized int getNumTestsPerEvictionRun() 
    {
        return this.numTestsPerEvictionRun;
    }

    /**
     * Sets the value of the {@link #numTestsPerEvictionRun} property.
     * 
     * @param numTestsPerEvictionRun the new {@link #numTestsPerEvictionRun} 
     * value
     * @see #numTestsPerEvictionRun
     */
    public synchronized void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) 
    {
        this.numTestsPerEvictionRun = numTestsPerEvictionRun;
        
        if (sessionPool != null) 
        {
            sessionPool.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
        }
    }

    /**
     * Returns the {@link #minEvictableIdleTimeMillis} property.
     * 
     * @return the value of the {@link #minEvictableIdleTimeMillis} property
     * @see #minEvictableIdleTimeMillis
     */
    public synchronized long getMinEvictableIdleTimeMillis() 
    {
        return this.minEvictableIdleTimeMillis;
    }

    /**
     * Sets the {@link #minEvictableIdleTimeMillis} property.
     * 
     * @param minEvictableIdleTimeMillis the minimum amount of time an object
     * may sit idle in the pool 
     * @see #minEvictableIdleTimeMillis
     */
    public synchronized void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) 
    {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
        
        if (sessionPool != null) 
        {
            sessionPool.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        }
    }

    /**
     * Returns the value of the {@link #testWhileIdle} property.
     * 
     * @return true if objects examined by the idle object evictor are
     * validated
     * @see #testWhileIdle
     */
    public synchronized boolean getTestWhileIdle() 
    {
        return this.testWhileIdle;
    }

    /**
     * Sets the <code>testWhileIdle</code> property. This property determines
     * whether or not the idle object evictor will validate connections.  For a
     * <code>true</code> value to have any effect, the 
     * <code>validationQuery</code> property must be set to a non-null string.
     * 
     * @param testWhileIdle new value for testWhileIdle property
     */
    public synchronized void setTestWhileIdle(boolean testWhileIdle) 
    {
        this.testWhileIdle = testWhileIdle;
        
        if (sessionPool != null) 
        {
            sessionPool.setTestWhileIdle(testWhileIdle);
        }
    }

    /**
     * [Read Only] The current number of active connections that have been
     * allocated from this data source.
     * 
     * @return the current number of active connections
     */
    public synchronized int getNumActive() 
    {
        if (sessionPool != null) 
        {
            return sessionPool.getNumActive();
        } 
        else 
        {
            return 0;
        }
    }


    /**
     * [Read Only] The current number of idle connections that are waiting
     * to be allocated from this data source.
     * 
     * @return the current number of idle connections
     */
    public synchronized int getNumIdle() 
    {
        if (sessionPool != null) 
        {
            return sessionPool.getNumIdle();
        } 
        else 
        {
            return 0;
        }
    }

    /**
     * Returns the password passed to the JDBC driver to establish connections.
     * 
     * @return the connection password
     */
    public synchronized String getPassword() 
    {
        return this.password;
    }

    /** 
     * <p>Sets the {@link #password}.</p>
     * <p>
     * Note: this method currently has no effect once the pool has been
     * initialized.  The pool is initialized the first time one of the
     * following methods is invoked: <code>getConnection, setLogwriter,
     * setLoginTimeout, getLoginTimeout, getLogWriter.</code></p>
     * 
     * @param password new value for the password
     */
    public synchronized void setPassword(String password) 
    {
        this.password = password;
        this.simpleCredentials = null;
    }

    /**
     * Returns the JDBC connection {@link #url} property.
     * 
     * @return the {@link #url} passed to the JDBC driver to establish
     * connections
     */
    public synchronized String getLocation() 
    {
        return this.location;
    }

    /** 
     * <p>Sets the {@link #url}.</p>
     * <p>
     * Note: this method currently has no effect once the pool has been
     * initialized.  The pool is initialized the first time one of the
     * following methods is invoked: <code>getConnection, setLogwriter,
     * setLoginTimeout, getLoginTimeout, getLogWriter.</code></p>
     * 
     * @param url the new value for the JDBC connection url
     */
    public synchronized void setLocation(String location) 
    {
        this.location = location;
    }

    /**
     * Returns the JDBC connection {@link #username} property.
     * 
     * @return the {@link #username} passed to the JDBC driver to establish
     * connections
     */
    public synchronized String getUsername() 
    {
        return this.username;
    }

    /** 
     * <p>Sets the {@link #username}.</p>
     * <p>
     * Note: this method currently has no effect once the pool has been
     * initialized.  The pool is initialized the first time one of the
     * following methods is invoked: <code>getConnection, setLogwriter,
     * setLoginTimeout, getLoginTimeout, getLogWriter.</code></p>
     * 
     * @param username the new value for the JDBC connection username
     */
    public synchronized void setUsername(String username) 
    {
        this.username = username;
        this.simpleCredentials = null;
    }

    /**
     * Returns the validation query used to validate connections before
     * returning them.
     * 
     * @return the SQL validation query
     * @see #validationQuery
     */
    public synchronized String getValidationQuery() 
    {
        return this.validationQuery;
    }

    /** 
     * <p>Sets the {@link #validationQuery}.</p>
     * <p>
     * Note: this method currently has no effect once the pool has been
     * initialized.  The pool is initialized the first time one of the
     * following methods is invoked: <code>getConnection, setLogwriter,
     * setLoginTimeout, getLoginTimeout, getLogWriter.</code></p>
     * 
     * @param validationQuery the new value for the validation query
     */
    public synchronized void setValidationQuery(String validationQuery) 
    {
        if ((validationQuery != null) && (validationQuery.trim().length() > 0)) 
        {
            this.validationQuery = validationQuery;
        } 
        else 
        {
            this.validationQuery = null;
        }
    }
    
    private HippoRepository getHippoRepository() throws RepositoryException
    {
        if (this.repository == null)
        {
            this.repository = HippoRepositoryFactory.getHippoRepository(location);
        }
        
        return this.repository;
    }
    
    private SimpleCredentials getSimpleCredentials()
    {
        if (this.simpleCredentials == null)
        {
            this.simpleCredentials = new SimpleCredentials(this.username, (this.password != null ? this.password.toCharArray() : null));
        }
        
        return this.simpleCredentials;
    }

    private class SessionFactory implements PoolableObjectFactory
    {
        public void activateObject(Object arg0) throws Exception
        {
        }

        public void destroyObject(Object arg0) throws Exception
        {
            Session session = (Session) arg0;
            
            try
            {
                session.logout();
            }
            catch (Exception e)
            {
            }
        }

        public Object makeObject() throws Exception
        {
            return getHippoRepository().login(getSimpleCredentials());
        }

        public void passivateObject(Object arg0) throws Exception
        {
        }

        public boolean validateObject(Object arg0)
        {
            boolean validated = true;
            
            String validationQuery = getValidationQuery();
            
            if (validationQuery != null)
            {
                Node nodeFound = null;
                
                try
                {
                    Session session = (Session) arg0;
                    nodeFound = session.getRootNode().getNode(validationQuery);
                }
                catch (Exception e)
                {
                }
                
                if (nodeFound == null)
                {
                    validated = false;
                }
            }
            
            return validated;
        }
    }
}
