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
package org.hippoecm.hst.caching.observation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import org.hippoecm.hst.caching.Cache;
import org.hippoecm.hst.caching.CacheManager;
import org.hippoecm.hst.caching.EventCache;
import org.hippoecm.hst.caching.NamedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * This class should be Serializable always because is registered at the repository JVM over RMI
 */
public class EventListenerImpl implements EventListener, Serializable{

    private static final Logger log = LoggerFactory.getLogger(EventListenerImpl.class);
    private static final long serialVersionUID = 1L;
   
    public void onEvent(EventIterator events) {
        // Add named events to a set, to avoid double events which are the same
        Set<NamedEvent> namedEvents = new HashSet<NamedEvent>();
        while(events.hasNext()) {
            Event event = events.nextEvent();
            try {
                  String path =  event.getPath();
                  // for now hardcode these paths to ignore, because currently getting a jcr session logs 
                  // and created jcr nodes
                  if(!path.startsWith("/hippo:log") && !path.startsWith("/hippo:configuration")) {
                      namedEvents.addAll(createEvents(path));
                  }
            } catch (RepositoryException e) {
                log.error("RepositoryException  " + e.getMessage());
            }
        }
        
        // get the list of Event Caches first (to minimize the synchronized time):
        Map<String, Cache> caches = CacheManager.getCaches();
        List<EventCache> eventCaches = new ArrayList<EventCache>();
        synchronized(caches) {
            Iterator<Cache> cachesIt = caches.values().iterator();
            while(cachesIt.hasNext()) {
                Cache c = cachesIt.next();
                if(c instanceof EventCache) {
                    eventCaches.add((EventCache)c);
                }
            }
        }
        
        // process each event
        Iterator<NamedEvent> eventsIt = namedEvents.iterator();
        while(eventsIt.hasNext()) {
            NamedEvent nEvent = eventsIt.next();
            Iterator<EventCache> eventCacheIt = eventCaches.iterator();
            while(eventCacheIt.hasNext()) {
                EventCache ec = eventCacheIt.next();
                ec.processEvent(nEvent);
            }
        }
    }

    private Collection<NamedEvent> createEvents(String path) {
        List<NamedEvent> eventList = new ArrayList<NamedEvent>();
        String[] parts = path.split("/");
        StringBuffer eventPath = new StringBuffer("/");
        eventList.add(new NamedEvent(eventPath.toString()));
        for(int i = 0; i < parts.length; i++) {
            if(!eventPath.toString().equals("/")) {
                eventPath.append("/"); 
            }
            eventPath.append(parts[i]);
            eventList.add(new NamedEvent(eventPath.toString()));
        }
        return eventList;
    }

}
