/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.comm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import pt.ul.fc.di.navigators.trone.data.Request;
import pt.ul.fc.di.navigators.trone.utils.Log;

/**
 *
 * @author kreutz
 */
class RequestCache {

    private HashMap requestCache;
    private int minNumberOfCopies;
    private long maxTimeToLive;
    private EventCache eventCache;
    private long maxDelayForLeaderReplica;
    private Log logger;
    
    public RequestCache(int minimunNumberOfCopies, long eventTimeToLiveInMilliseconds, long allowedDelayForLeaderReplica) {
        requestCache = new HashMap<String, RequestInfo>();
        eventCache = new EventCache(minimunNumberOfCopies, eventTimeToLiveInMilliseconds, maxDelayForLeaderReplica);
        minNumberOfCopies = minimunNumberOfCopies;
        maxTimeToLive = eventTimeToLiveInMilliseconds;
        maxDelayForLeaderReplica = allowedDelayForLeaderReplica;
        logger = new Log(100);
        logger.initSpecificCounter("NREQSADDED", 0);
        logger.initSpecificCounter("NREQSREMOVED", 0);
        logger.initSpecificCounter("NREQSDISCHARGED", 0);
        logger.initSpecificCounter("NEVENTSADDED", 0);
        logger.initSpecificCounter("NEVENTSREMOVED", 0);
        logger.initSpecificCounter("NEVENTSDISCHARGED", 0);
    }

    // NOTE: should be synced if the cache will be managed by a specific threads
    public void addRequest(Request req) {
        
        RequestInfo ri;
        
        if (req.hasEvents()) {
            ArrayList aoe = req.getAllEvents();
            eventCache.addEvents(aoe, req.getReplicaId(), req.getChannelTag());
            logger.incrementSpecificCounter("NEVENTSADDED", aoe.size());
            Log.logDebug(this, "REQ ID: " + req.getUniqueId() + " ADDING EVENTS TO CACHE: " + aoe.size(), minNumberOfCopies);
            //req.cleanArrayOfEvents();
        } else {
            Log.logDebug(this, "REQUEST " + req.getUniqueId() + " HAS NO EVENTS", Log.getLineNumber());
        }
        
        if (requestCache.containsKey(req.getUniqueId())) {
            ri = (RequestInfo) requestCache.get(req.getUniqueId());
        } else {
            ri = new RequestInfo(req);
        }

        ri.incCounterAndUpdateTimestamp();
        
        logger.incrementSpecificCounter("NREQSADDED", 1);
        
        requestCache.put(ri.getRequest().getUniqueId(), ri);
    }

    // NOTE: should be synced if the cache will be managed by a specific threads
    public int dischargeOldRequests() {
        ArrayList riToRemove = new ArrayList<RequestInfo>();
        
        Collection c = requestCache.values();
        Iterator itr = c.iterator();
        while (itr.hasNext()) {
            RequestInfo ri = (RequestInfo) itr.next();
            long currentTime = System.currentTimeMillis();
            if ((currentTime - ri.getLastUpdateTime()) > maxTimeToLive) {
                if (ri.getCounter() < minNumberOfCopies) {
                    riToRemove.add(ri);
                }
            }
        }
        
        itr = riToRemove.iterator();
        while (itr.hasNext()) {
            RequestInfo ri = (RequestInfo) itr.next();
            requestCache.remove(ri.getRequest().getUniqueId());
        }
        
        int i = riToRemove.size();
        
        riToRemove.clear();
        
        logger.incrementSpecificCounter("NREQSDISCHARGED", i);
        
        //eventCache.dischargeOldEvents();
        logger.incrementSpecificCounter("NEVENTSDISCHARGED", eventCache.dischargeOldEvents());
        
        return i;
    }

    // NOTE: should be synced if the cache will be managed by a specific threads
    public Request getRequest(String requestId, int numberOfEvents) {
        
        RequestInfo ri = (RequestInfo) requestCache.get(requestId);
        
        Log.logDebug(this, "GET REQUEST " + requestId, Log.getLineNumber());
        
        if (ri != null) {
            
            Request response = ri.getRequest();
            
            if (ri.getCounter() < minNumberOfCopies) {
                Log.logWarning(this, "request " + response.getUniqueId() + " NOT RELIABLE (RECEIVED ONLY " + ri.getCounter() + " COPIES)", Log.getLineNumber());
            }
            
            Log.logDebug(this, "REMOVING REQUEST " + requestId, Log.getLineNumber());
            
            requestCache.remove(ri.getRequest().getUniqueId());
            
            logger.incrementSpecificCounter("NREQSREMOVED", 1);
            
            if (numberOfEvents > 0) {
                ArrayList loe = eventCache.getListOfEvents(numberOfEvents, response.getChannelTag());
                
                logger.incrementSpecificCounter("NEVENTSREMOVED", loe.size());
                
                response.setArrayOfEvents(loe);
            }
            
            return response;
        } else {
            Log.logWarning(this, "request info for id " + requestId + " IS NULL", Log.getLineNumber());
        }
        
        Log.logWarning(this, "NO REQUEST FOUND FOR " + requestId + "!", Log.getLineNumber());
        
        return null;
    }
    
    public void currentStats() {
        Log.logInfo(this, "CACHE STATS: NUMBER OF REQS ADDED: " + logger.getSpecificCounterValue("NREQSADDED") + " NUMBER OF REQS REMOVED: " + logger.getSpecificCounterValue("NREQSREMOVED") + " NUMBER OF REQS DISCHARGED: " + logger.getSpecificCounterValue("NREQSDISCHARGED"), Log.getLineNumber());
        Log.logInfo(this, "CACHE STATS: NUMBER OF EVENTS ADDED: " + logger.getSpecificCounterValue("NEVENTSADDED") + " NUMBER OF EVENTS REMOVED: " + logger.getSpecificCounterValue("NEVENTSREMOVED") + " NUMBER OF EVENTS DISCHARGED: " + logger.getSpecificCounterValue("NEVENTSDISCHARGED"), Log.getLineNumber());
    }

}

class RequestInfo {
    private Request request;
    private int counter;
    private long lastUpdateTime;
    
    public RequestInfo(Request req) {
        request = req;
        counter = 0;
        lastUpdateTime = System.currentTimeMillis();
    }
    
    public void incCounterAndUpdateTimestamp() {
        counter++;
        lastUpdateTime = System.currentTimeMillis();
    }
    
    public Request getRequest() {
        return request;
    }
    
    public int getCounter() {
        return counter;
    }
    
    public long getLastUpdateTime() {
        return lastUpdateTime;
    }
}