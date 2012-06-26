/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hippoecm.repository.jackrabbit.persistence;

import org.apache.jackrabbit.core.persistence.pool.Oracle9PersistenceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @deprecated Use {@link org.apache.jackrabbit.core.persistence.pool.Oracle9PersistenceManager} instead.
 */
@Deprecated
public class ForkedOracle9PersistenceManager extends Oracle9PersistenceManager {

    private static final Logger log = LoggerFactory.getLogger(ForkedOracle9PersistenceManager.class);

    public ForkedOracle9PersistenceManager() {
        log.warn("You are using a deprecated persistence manager. Please use org.apache.jackrabbit.core.persistence.pool.Oracle9PersistenceManager instead.");
    }
}
