/*
 *  Copyright 2011-2014 Hippo B.V. (http://www.onehippo.com)
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
package org.hippoecm.repository.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import org.apache.jackrabbit.core.state.ItemStateException;
import org.apache.jackrabbit.spi.Event;
import org.hippoecm.repository.api.WorkflowException;
import org.hippoecm.repository.concurrent.action.Action;
import org.hippoecm.repository.concurrent.action.ActionContext;
import org.hippoecm.repository.concurrent.action.ActionFailure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ActionRunner extends Thread {

    private static boolean stopRunning = false;

    private final Session session;
    protected final Logger log;
    protected final ActionContext context;
    protected final List<Class<? extends Action>> actions;
    private final long duration;
    private final long throttle;

    private long endTime = -1;
    private boolean terminate = false;
    private int misses = 0;
    private int successes = 0;
    private final List<ActionFailure> failures = new ArrayList<ActionFailure>();

    public ActionRunner(Session session, Logger log, List<Class<? extends Action>> actions, long duration, long throttle) {
        this.session = session;
        this.log = log;
        this.context = new ActionContext(session, log);
        this.actions = actions;
        this.duration = duration;
        this.throttle = throttle;
    }

    public void terminate(ExecutorService executor) throws InterruptedException, ExecutionException {
        if (isAlive()) {
            terminate = true;
            sleep(1000);
            if (isAlive()) {
                StringBuilder sb = new StringBuilder();
                for (StackTraceElement elt : getStackTrace()) {
                    sb.append("\t").append(elt.getClassName()).append(".").append(elt.getMethodName()).append(":").append(elt.getLineNumber());
                }
                log.error("FAILURE: thread " + getName() + " seems to hang. Stacktrace:" + sb);
            }
        }
        try {
            // log out on a separate thread to prevent deadlock
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    session.logout();
                }
            }).get(3, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.warn("Unable to log out session on thread " + getName() + ": timed out");
        }
    }

    @Override
    public void run() {
        if (duration > 0) {
            endTime = System.currentTimeMillis() + duration;
        }
        final EventListener eventListener = new EventListener() {
            @Override
            public void onEvent(EventIterator events) {
                // NOP
            }
        };
        try {
            // adding an event listener emulates typical CMS usage
            // for one thing, the access manager is invoked to see if
            // the user has access to the node the event was on
            session.getWorkspace().getObservationManager().addEventListener(eventListener, Event.ALL_TYPES, "/", true, null, null, true);
        } catch (RepositoryException ex) {
            log.error("Failed to add event listener for thread " + Thread.currentThread().getName(), ex);
        }
        Node node = null;
        while (keepRunning()) {
            if (node == null) {
                try {
                    session.refresh(false);
                    node = context.getDocumentBase();
                } catch (RepositoryException e) {
                    log.error("Failed to initialize runner", e);
                    stopRunning = true;
                }
            }
            if (node != null) {
                node = step(node);
            }
        }
        try {
            session.getWorkspace().getObservationManager().removeEventListener(eventListener);
        } catch (RepositoryException ex) {
            log.error("Failed to remove event listener for thread " + Thread.currentThread().getName());
        }
    }

    public int getMisses() {
        return misses;
    }

    public int getSuccesses() {
        return successes;
    }

    public List<ActionFailure> getFailures() {
        return failures;
    }

    public Session getSession() {
        return session;
    }

    public ActionContext getContext() {
        return context;
    }

    private Node step(Node node) {
        final Action action = getAction(node);
        if (action != null) {
            try {
                final Node result = action.execute(node);
                successes++;
                throttle();
                return result;
            } catch (Throwable t) {
                handleThrowable(t, action, node);
            }
        }
        return null;
    }

    protected abstract Action getAction(final Node node);

    private void throttle() {
        try {
            Thread.sleep(throttle);
        } catch (InterruptedException ignore) {
        }
    }

    private boolean keepRunning() {
        if (terminate) {
            return false;
        }
        if (stopRunning) {
            log.error("Stopping RandomActionRunner[" + Thread.currentThread().getName() + "] because of unrecoverable error");
            return false;
        }
        if (endTime > 0 && endTime < System.currentTimeMillis()) {
            log.info("Stopping RandomActionRunner[" + Thread.currentThread().getName() + "] because of end of test run");
            return false;
        }
        if (failures.size() > 50) {
            log.warn("Stopping RandomActionRunner[" + Thread.currentThread().getName() + "] because of too many failures");
            return false;
        }
        return true;
    }

    private void handleThrowable(Throwable t, Action action, Node node) {
        if (t instanceof Exception) {
            if (isRecoverableException((Exception)t)) {
                if (log.isDebugEnabled()) {
                    log.debug("Recoverable exception in action " + action.getClass().getSimpleName(), t);
                }
                misses++;
                action.addMissed();
                return;
            }
        }
        String path = null;
        try {
            path = node.getPath();
        } catch (Exception ignore) {
        }
        log.error("FAILURE in thread " + Thread.currentThread().getName() + " performing action "
                + action.getClass().getSimpleName() + " on path " + path, t);
        ActionFailure failure = new ActionFailure(t, action, path);
        failures.add(failure);
    }

    private boolean isRecoverableException(Exception e) {
        if (e instanceof RepositoryException) {
            return !(e.getCause() != null && !(e.getCause() instanceof RepositoryException || e.getCause() instanceof ItemStateException));
        }
        if (e instanceof ItemStateException) {
            return !(e.getCause() != null && !(e.getCause() instanceof RepositoryException || e.getCause() instanceof ItemStateException));
        }
        if (e instanceof WorkflowException) {
            if (!e.getMessage().equals("Cannot rename document to same name")) {
                return true;
            }
        }
        return false;
    }
}
