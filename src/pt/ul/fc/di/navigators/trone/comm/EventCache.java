/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.comm;

import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import pt.ul.fc.di.navigators.trone.data.Event;
import pt.ul.fc.di.navigators.trone.utils.CurrentTime;
import pt.ul.fc.di.navigators.trone.utils.HashFunctions;
import pt.ul.fc.di.navigators.trone.utils.Log;


/**
 * @author kreutz
 */
public class EventCache {
    
    private HashMap hashEventCache;
    private int minNumberOfCopies;
    private long maxTimeToLive;
    private HashMap replicaEventCounter;
    private long timeoutForReplicaCounterReset;
    
    
    public EventCache(int minimunNumberOfCopies, long eventTimeToLiveInMilliseconds, long timeoutForReplicaCounter) {
        hashEventCache = new HashMap<String, EventInfo>();
        replicaEventCounter = new HashMap<Integer, Long>();
        minNumberOfCopies = minimunNumberOfCopies;
        maxTimeToLive = eventTimeToLiveInMilliseconds;
        timeoutForReplicaCounterReset = timeoutForReplicaCounter;
    }

    // NOTE: should be synced if the cache will be managed by a specific threads
    public void addEvents(ArrayList events, int replicaId, String tag) {
        if (events != null) {
            long currentTime = CurrentTime.getTimeInMilliseconds();
            
            Integer repKey = Integer.valueOf(replicaId);
            if (!replicaEventCounter.containsKey(repKey)) {
                replicaEventCounter.put(repKey, Long.valueOf(0));
            } else {
                Long l = (Long) replicaEventCounter.get(repKey);
                if (l.longValue() > timeoutForReplicaCounterReset) {
                    Iterator it = replicaEventCounter.keySet().iterator();
                    while (it.hasNext()) {
                        Integer i = (Integer) it.next();
                        replicaEventCounter.put(i, Long.valueOf(0)); //reset all replicas counters
                    }
                } else {
                    replicaEventCounter.put(repKey, Long.valueOf(l.longValue()+events.size()));
                }
            }
        
            Iterator itE = events.iterator();
            while (itE.hasNext()) {
                EventInfo ei;
                Event e = (Event) itE.next();
                
                if (hashEventCache.containsKey(e.getUniqueId())) {
                    ei = (EventInfo) hashEventCache.get(e.getUniqueId());
                } else {
                    ei = new EventInfo(e, replicaId, tag);
                }
                
                ei.incCounterAndUpdateTimestamp(currentTime);
                
                //System.out.println("[EventCache] adding EVENT ID: " + ei.getEvent().getUniqueId() + " with counter: " + ei.getCounter());
                
                hashEventCache.put(e.getUniqueId(), ei);
            }
        } else {
            Log.logWarning(this, "REQUEST IS NULL (NOT INCLUDING IT IN LOCAL CACHE)", Log.getLineNumber());
        }
           
    }

    // NOTE: should be synced if the cache will be managed by a specific threads
    public int dischargeOldEvents() {
        ArrayList eiToRemove = new ArrayList<EventInfo>();
        
        Collection c = hashEventCache.values();
        Iterator itr = c.iterator();
        while (itr.hasNext()) {
            EventInfo ei = (EventInfo) itr.next();
            long currentTime = System.currentTimeMillis();
            if ((currentTime - ei.getLastUpdateTime()) > maxTimeToLive) {
                if (ei.getCounter() < minNumberOfCopies) {
                    eiToRemove.add(ei);
                }
            }
        }
        
        itr = eiToRemove.iterator();
        while (itr.hasNext()) {
            EventInfo ei = (EventInfo) itr.next();
            hashEventCache.remove(ei.getEvent().getUniqueId());
        }
        
        int i = eiToRemove.size();
        
        eiToRemove.clear();
        
        return i;
    }

    // NOTE: should be synced if the cache will be managed by a specific threads
    public ArrayList<Event> getListOfEvents(int numberOfEvents, String tag) {
        
        ArrayList eventsToRemove = new ArrayList<Event>();
        
        Collection c = hashEventCache.values();
        Iterator itr = c.iterator();
        while (itr.hasNext() && eventsToRemove.size() < numberOfEvents) {
            
            EventInfo ei = (EventInfo) itr.next();
            
            if (ei.getCounter() >= minNumberOfCopies && tag.equalsIgnoreCase(ei.getChannelTag())) {
                Event e = ei.getEvent();
                eventsToRemove.add(e);
            }
        }
        
        itr = eventsToRemove.iterator();
        while (itr.hasNext()) {
            Event e = (Event) itr.next();
            hashEventCache.remove(e.getUniqueId());
        }

        return eventsToRemove;
    }
    
    public ArrayList<Event> getListOfEventsWithOrdering(int numberOfEvents, String tag) {
        
        ArrayList events = new ArrayList<Event>();

        if (!replicaEventCounter.isEmpty()) {
            int leaderReplica = 0;
            long leaderReplicaCounter = -1;

            Iterator it = replicaEventCounter.keySet().iterator();
            while (it.hasNext()) {
                Integer key = (Integer) it.next();
                Long l = (Long) replicaEventCounter.get(key);
                if (l.longValue() > leaderReplicaCounter) {
                    leaderReplicaCounter = l.longValue();
                    leaderReplica = key.intValue();
                }
            }
            
            HashMap eventsId = new HashMap<Long, Event>();

            Collection c = hashEventCache.values();
            Iterator itr = c.iterator();
            while (itr.hasNext()) {
                EventInfo ei = (EventInfo) itr.next();
                if (ei.getCounter() >= minNumberOfCopies && tag.equalsIgnoreCase(ei.getChannelTag()) && ei.getReplicaId() == leaderReplica) {
                    Event e = ei.getEvent();
                    eventsId.put(Long.valueOf(e.getUniqueIdWithinTheChannel()), e);
                }
            }

            Set eset = eventsId.keySet();
            Integer[] ekeys = new Integer[eset.size()];
            eset.toArray(ekeys);
            List etmpkeyList = Arrays.asList(ekeys);
            Collections.sort(etmpkeyList);
            Iterator eit = etmpkeyList.iterator();
            while (eit.hasNext()) {
                Long ekey = (Long) eit.next();
                Event e = (Event) eventsId.get(ekey);
                events.add(e);
            }

            itr = events.iterator();
            while (itr.hasNext()) {
                Event e = (Event) itr.next();
                hashEventCache.remove(e.getUniqueId());
            }
            
        } else {
            Log.logWarning(this, "REPLICA SET IS EMPTY", Log.getLineNumber());
        }

        return events;
    }

}

class EventInfo {
    private Event myEvent;
    private int myReplicaId;
    private String myChannelTag;
    private int myCounter;
    private long myCreationTime;
    private long myLastUpdateTime;
    
    public EventInfo(Event e, int replicaId, String channelTag) {
        myEvent = e;
        myCounter = 1;
        myCreationTime = System.currentTimeMillis();
        myLastUpdateTime = myCreationTime;
        myReplicaId = replicaId;
        myChannelTag = channelTag;
    }
    
    public void incCounterAndUpdateTimestamp(long currentTime) {
        myCounter++;
        myLastUpdateTime = currentTime;
    }
    
    public int getReplicaId() {
        return myReplicaId;
    }
    
    public String getChannelTag() {
        return myChannelTag;
    }
    
    public Event getEvent() {
        return myEvent;
    }
    
    public int getCounter() {
        return myCounter;
    }
    
    public long getCreationTime() {
        return myCreationTime;
    }
    
    public long getLastUpdateTime() {
        return myLastUpdateTime;
    }
}

class EventInfoMajority {
    private HashMap<String, Event> myEvents;
    private HashMap<String, AtomicInteger> myEventsHashCounter;
    private int myReplicaId;
    private String myChannelTag;
    private String myId;
    private int myNumberOfCopies;
    private long myCreationTime;
    private long myLastUpdateTime;
    private String lastHash;
    
    public EventInfoMajority(Event event, int replicaId, String channelTag) throws NoSuchAlgorithmException {
        myEvents = new HashMap<String, Event>();
        String hash = HashFunctions.getHashMD5(event.getContent());
        lastHash = hash;
        myEvents.put(hash, event);
        myEventsHashCounter.put(hash, new AtomicInteger(1));
        myId = event.getUniqueId();
        myCreationTime = System.currentTimeMillis();
        myLastUpdateTime = myCreationTime;
        myReplicaId = replicaId;
        myChannelTag = channelTag;
        myNumberOfCopies = 1;
    }
    
    public void addEvent(Event e, long currentTime) throws NoSuchAlgorithmException {
            myNumberOfCopies++;
            String hash = HashFunctions.getHashMD5(e.getContent());
            lastHash = hash;
            if (myEventsHashCounter.containsKey(hash)) {
                myEventsHashCounter.get(hash).addAndGet(1);
            } else {
                myEvents.put(hash, e);
                myEventsHashCounter.put(hash, new AtomicInteger(1));
            }
            myLastUpdateTime = currentTime;
    }
    
    public int getReplicaId() {
        return myReplicaId;
    }
    
    public String getChannelTag() {
        return myChannelTag;
    }
    
    public Event getLastEvent() {
        return myEvents.get(lastHash);
    }
    
    public Event getEventWithMajority() {
        String key = getMajorityKey();
        return myEvents.get(key);
    }
    
    public int getNumberOfCopies() {
        return myNumberOfCopies;
    }
    
    public int getMajority() {
        int majority = 0;
        Iterator it = myEventsHashCounter.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            if (myEventsHashCounter.get(key).intValue() > majority) {
                majority = myEventsHashCounter.get(key).intValue();
            }
        }
        return majority;
    }
    
    private String getMajorityKey() {
        int majority = 0;
        String majorityKey = null;
        Iterator it = myEventsHashCounter.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            if (myEventsHashCounter.get(key).intValue() > majority) {
                majority = myEventsHashCounter.get(key).intValue();
                majorityKey = key;
            }
        }
        return majorityKey;
    }
    
    public long getCreationTime() {
        return myCreationTime;
    }
    
    public long getLastUpdateTime() {
        return myLastUpdateTime;
    }

}

/*
class ReplicaInfo {
    //FIXME: this is in order to improve the get of events with ordering
    private HashMap myChannels;
    private int myId;
    
    public ReplicaInfo(int replicaId) {
        myChannels = new HashMap<String, HashMap<String, EventInfo>>();
        myId = replicaId;
    }
    
    public void addEventsToChannel(ArrayList events, String tag) {
        if (!myChannels.containsKey(tag)) {
            HashMap hm = new HashMap<String, EventInfo>();
            Iterator it = events.iterator();
            while (it.hasNext()) {
                // FIXME: add EventInfo
            }
            myChannels.put(tag, hm);
        } else {
            ArrayList al = (ArrayList) myChannels.get(tag);
            al.addAll(events);
        }
    }

}
*/