/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;
import pt.ul.fc.di.navigators.trone.utils.Log;

/**
 *
 * @author kreutz
 */
public class Storage extends HashMap {

    //private static Map<String, Channel> syncChannelHashMap;
    private static HashMap<String, Channel> syncChannelHashMap;
    private static int myReplicaId;
    private Log logger;
    private static AtomicLong eventsPub;
    private static AtomicLong eventsSub;
    private static AtomicLong eventsPubTimes;
    private static AtomicLong eventsSubTimes;
    
    
    public Storage(int replicaId) {
        //syncChannelHashMap = Collections.synchronizedMap(new HashMap<String, Channel>());
        syncChannelHashMap = new HashMap<String, Channel>();
        myReplicaId = replicaId;
        logger = new Log(100);
        eventsPub = new AtomicLong(0);
        eventsSub = new AtomicLong(0);
        eventsPubTimes = new AtomicLong(0);
        eventsSubTimes = new AtomicLong(0);
    }

    public void insertNewChannel(String tag) {
        syncChannelHashMap.put(tag.toLowerCase(), new Channel(tag.toLowerCase(), myReplicaId));
    }

    public boolean insertNewPublisher(Publisher pub, String tag) {
        Channel c = syncChannelHashMap.get(tag.toLowerCase());
        if (c != null) {
            return c.addPublisher(pub);
        }
        return false;
    }

    public boolean insertNewSubscriber(Subscriber sub, String tag) {
        Channel c = syncChannelHashMap.get(tag.toLowerCase());
        if (c != null) {
            return c.addSubscriber(sub);
        }
        return false;
    }

    public boolean insertNewEvent(Event e, String tag) {
        if (e != null) {
            Channel c = syncChannelHashMap.get(tag.toLowerCase());
            if (c != null) {
                if (c.addEventToSubscribers(e)) {
                    //logger.logIfAtomicCounterReachedAndIncrement(0, "INFO", this, "THREAD: " + Thread.currentThread().getId() + " CHANNEL: " + tag + " NEW EVENT: " + e.getUniqueId() + " EVENT COUNT: " + c.getNumberOfEvents(), Log.getLineNumber());
                    return true;
                }
            }
        }
        return false;
    }

    public boolean removePublisher(String id, String tag) {
        Channel c = syncChannelHashMap.get(tag.toLowerCase());
        if (c != null) {
            return c.removePublisher(id);
        }
        return false;
    }

    public boolean removeSubscriber(String id, String tag) {
        Channel c = syncChannelHashMap.get(tag.toLowerCase());
        if (c != null) {
            return c.removeSubscriber(id);
        }
        return false;
    }

    public boolean removeEventX(String id, Event e, String tag) {
        Channel c = syncChannelHashMap.get(tag.toLowerCase());
        if (c != null) {
            return c.removeEvent(id, e);
        } 
        return false;
    }

    public Event getNextEvent(String tag, String id) {
        Channel c = syncChannelHashMap.get(tag.toLowerCase());
        if (c != null) {
            return c.getNextEvent(id);
        }
        return null;
    }

    public boolean hasChannel(String tag) {
        Channel c = syncChannelHashMap.get(tag.toLowerCase());
        if (c != null) {
            return true;
        }
        return false;
    }

    public int getNumberOfPublishers() {
        // FIXME: it does not takes under account repeated publishers (more than one channel)
        int sum = 0;
        
        synchronized (this) {
            Iterator iterator = syncChannelHashMap.keySet().iterator();

            while (iterator.hasNext()) {
                String tag = (String) iterator.next();
                sum += getNumberOfPublishersForChannel(tag);
            }
        }
        
        return sum;
    }

    public int getNumberOfSubscribers() {
        // FIXME: it does not takes under account repeated subscribers (more than one channel)
        int sum = 0;
        synchronized (this) {
            Iterator iterator = syncChannelHashMap.keySet().iterator();

            while (iterator.hasNext()) {
                String tag = (String) iterator.next();
                sum += getNumberOfSubscribersForChannel(tag);
            }
        }

        return sum;
    }

    public int getNumberOfPublishersForChannel(String tag) {
        Channel c = syncChannelHashMap.get(tag.toLowerCase());
        if (c != null) {
            return c.getNumberOfPublishers();
        }
        return 0;
    }

    public int getNumberOfSubscribersForChannel(String tag) {
        Channel c = syncChannelHashMap.get(tag.toLowerCase());
        if (c != null) {
            return c.getNumberOfSubscribers();
        }
        return 0;
    }

    public boolean unRegisterFromAllChannels(String id) {
        
        synchronized (this) {
            Iterator iterator = syncChannelHashMap.keySet().iterator();

            while (iterator.hasNext()) {
                String tag = (String) iterator.next();
                removePublisher(id, tag);
            }
        }

        return true;
    }

    public boolean unSubscribeFromAllChannels(String id) {
        
        synchronized (this) {
            Iterator iterator = syncChannelHashMap.keySet().iterator();

            while (iterator.hasNext()) {
                String tag = (String) iterator.next();
                removeSubscriber(id, tag);
            }
        }

        return true;
    }

    public long removeAllOldPublishers(long publisherTimeToLive, long currentTime) {

        long sum = 0;
        
        synchronized (this) {
            Iterator iterator = syncChannelHashMap.keySet().iterator();

            while (iterator.hasNext()) {
                String tag = (String) iterator.next();
                Channel c = syncChannelHashMap.get(tag);
                sum += c.removeAllOldPublishers(publisherTimeToLive, currentTime);
            }
        }
        
        return sum;
    }
    
    public long removeAllOldSubscribers(long subscriberTimeToLive, long currentTime) {

        long sum = 0;
        
        synchronized (this) {
            Iterator iterator = syncChannelHashMap.keySet().iterator();

            while (iterator.hasNext()) {
                String tag = (String) iterator.next();
                Channel c = syncChannelHashMap.get(tag);
                sum += c.removeAllOldSubscribers(subscriberTimeToLive, currentTime);
            }
        }
        
        return sum;
    }
    
    public long removeAllOldEvents(long messageTimeToLive, long currentTime) {
        long sum = 0;
        
        synchronized (this) {
            Iterator iterator = syncChannelHashMap.keySet().iterator();

            while (iterator.hasNext()) {
                String tag = (String) iterator.next();
                Channel c = syncChannelHashMap.get(tag);
                sum += c.removeAllOldEvents(messageTimeToLive, currentTime);
            }
        }
        
        return sum;
    }

    public long getNumberOfEvents() {
        long sum = 0;
        
        synchronized (this) {
            Iterator iterator = syncChannelHashMap.keySet().iterator();

            while (iterator.hasNext()) {
                String tag = (String) iterator.next();
                Channel c = syncChannelHashMap.get(tag);
                sum += c.getNumberOfEvents();
            }
        }
        
        return sum;
    }

    public boolean insertListOfEvents(ArrayList eventsToStore, String tag) {
        Channel c = syncChannelHashMap.get(tag.toLowerCase());
        if (c != null) {
            eventsPub.addAndGet(eventsToStore.size());
            if (eventsPubTimes.incrementAndGet() % 100 == 0) {
                Log.logInfo(this, "STORAGE: NUMBER OF PUBLISHED EVENTS: " + eventsPub.longValue(), Log.getLineNumber());
            }
            
            if (c.addListOfEventsToSubscribers(eventsToStore)) {
                return true;
            } 
        }
        return false;
    }

    public ArrayList<Event> getEventsFromChannel(String id, String tag, int numberOfEvents) {
        Channel c = syncChannelHashMap.get(tag.toLowerCase());
        if (c != null) {
            ArrayList<Event> a = c.getEventsForSubscriber(id, numberOfEvents);
            eventsSub.addAndGet(a.size());
            if (eventsSubTimes.incrementAndGet() % 100 == 0) {
                Log.logInfo(this, "STORAGE: NUMBER OF RETRIEVED EVENTS: " + eventsSub.longValue(), Log.getLineNumber());
            }
            return a;
        }
        return null;
    }
        
}
