/*
 * Copyright 2017 Hippo B.V. (http://www.onehippo.com)
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
package org.onehippo.services.lock;

import org.onehippo.cms7.services.lock.LockException;
import org.onehippo.cms7.services.lock.LockManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemoryLockManager extends AbstractLockManager implements LockManager {

    private static final Logger log = LoggerFactory.getLogger(MemoryLockManager.class);

    @Override
    Logger getLogger() {
        return log;
    }

    @Override
    AbstractLock createLock(final String key) throws LockException {
        return new MemoryLock(key);
    }
}
