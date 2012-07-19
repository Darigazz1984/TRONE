/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import pt.ul.fc.di.navigators.trone.utils.CurrentTime;
import pt.ul.fc.di.navigators.trone.utils.Log;

/**
 * @author kreutz
 */
public class Channel {

    private String myTag;
    private HashMap<String, Publisher> publisherHashMap;
    private HashMap<String, Subscriber> subscriberHashMap;
    private long nextEventIdWithinTheChannel;
    private Log logger;
    
    public Channel(String tag, int replicaId) {
        myTag = tag;
        subscriberHashMap = new HashMap<String, Subscriber>();
        publisherHashMap = new HashMap<String, Publisher>();
        nextEventIdWithinTheChannel = 0;
        logger = new Log(100);
    }

    public String getTag() {
        return myTag;
    }

    synchronized public boolean addPublisher(Publisher pub) {
        if (publisherHashMap.containsKey(pub.getId())) {
            return false;
        }
        Log.logInfo(this, "ADDING NEW PUBLISHER: " + pub.getId() + " TO CHANNEL: " + myTag + " AT TIME: " + CurrentTime.getTimeInSeconds(), Log.getLineNumber());
        publisherHashMap.put(pub.getId(), pub);
        return true;
    }

    synchronized public boolean removePublisher(String id) {
        if (publisherHashMap.containsKey(id)) {
            publisherHashMap.remove(id);
            Log.logInfo(this, "REMOVIND PUBLISHER: " + id + " FROM CHANNEL: " + myTag + " AT TIME: " + CurrentTime.getTimeInSeconds(), Log.getLineNumber());
            return true;
        }
        return false;
    }

    synchronized public boolean addSubscriber(Subscriber sub) {
        if (subscriberHashMap.containsKey(sub.getId())) {
            return false;
        } 
        Log.logInfo(this, "ADDING NEW SUBSCRIBER: " + sub.getId() + " TO CHANNEL: " + myTag + " AT TIME: " + CurrentTime.getTimeInSeconds(), Log.getLineNumber());
        subscriberHashMap.put(sub.getId(), sub);
        return true;
    }

    synchronized public boolean removeSubscriber(String id) {
        if (subscriberHashMap.containsKey(id)) {
            subscriberHashMap.remove(id);
            Log.logInfo(this, "REMOVIND SUBSCRIBER: " + id + " FROM CHANNEL: " + myTag + " AT TIME: " + CurrentTime.getTimeInSeconds(), Log.getLineNumber());
            return true;
        }
        return false;
    }

    synchronized public boolean addEventToSubscribers(Event e) {
        if (publisherHashMap.containsKey(e.getClientId())) {
            long localTime = System.currentTimeMillis();
            e.setUniqueIdWithinTheChannel(nextEventIdWithinTheChannel);
            e.setLocalTimestamp(localTime);
            nextEventIdWithinTheChannel++;

            if (!subscriberHashMap.isEmpty()) {
                Collection c = subscriberHashMap.values();
                Iterator it = c.iterator();
                while (it.hasNext()) {
                    Subscriber s = (Subscriber) it.next();
                    if (s.queueIsNotFull()) {
                        s.insertNewEvent(e);
                    } else {
                        logger.logWarningIfCounterReachedAndIncrement(this, "QUEUE for subscriber ID " + s.getId() + ", on channel " + myTag + ", is FULL (" + s.getNumberOfEvents() + " events) [event " + e.getUniqueId() + " will be discharged] (total so far discharged: " + logger.getWarningCounter() + " )", Log.getLineNumber());
                    }
                }
            }

            publisherHashMap.get(e.getClientId()).setLocalTimestamp(localTime);

            return true;
        } else {
            Log.logWarning(this, "PUBLISHER ID " + e.getClientId() + " in NOT a registered publisher for channel " + myTag, Log.getLineNumber());
        }
        return false;
    }

    synchronized public boolean removeEvent(String subscriberId, Event e) {
        Subscriber s = subscriberHashMap.get(subscriberId);
        if (s != null) {
            if (s.removeEvent(e)) {
                return true;
            }
        } 
        return false;
    }

    synchronized public int getNumberOfPublishers() {
        return publisherHashMap.size();
    }

    synchronized public int getNumberOfSubscribers() {
        return subscriberHashMap.size();
    }

    synchronized public long removeAllOldEvents(long messageTimeToLive, long currentTime) {
        long sum = 0;
        Iterator it = subscriberHashMap.keySet().iterator();
        while (it.hasNext()) {
            String str = (String) it.next();
            Subscriber s = subscriberHashMap.get(str);
            if (s != null) {
                sum += s.removeAllOldEvents(messageTimeToLive, currentTime);
            }
        }

        return sum;
    }

    synchronized public long removeAllOldPublishers(long publisherTimeToLive, long currentTime) {
        ArrayList pubToRemove = new ArrayList<String>();
        Iterator it = publisherHashMap.keySet().iterator();
        while (it.hasNext()) {
            Publisher pub = publisherHashMap.get(it.next().toString());
            if ((pub.getLocalTimestamp() + publisherTimeToLive) < currentTime) {
                pubToRemove.add(pub.getId());
            }
        }

        for (int i = 0; i < pubToRemove.size(); i++) {
            publisherHashMap.remove((String) pubToRemove.get(i));
        }

        return pubToRemove.size();
    }

    synchronized public long removeAllOldSubscribers(long subscriberTimeToLive, long currentTime) {
        ArrayList subToRemove = new ArrayList<String>();
        Iterator it = subscriberHashMap.keySet().iterator();
        while (it.hasNext()) {
            Subscriber sub = subscriberHashMap.get(it.next().toString());
            if ((sub.getLocalTimestamp() + subscriberTimeToLive) < currentTime) {
                subToRemove.add(sub.getId());
            }
        }

        for (int i = 0; i < subToRemove.size(); i++) {
            subscriberHashMap.remove((String) subToRemove.get(i));
        }

        return subToRemove.size();
    }

    synchronized public long getNumberOfEvents() {
        long sum = 0;
        Iterator it = subscriberHashMap.keySet().iterator();
        while (it.hasNext()) {
            Subscriber sub = subscriberHashMap.get(it.next().toString());
            if (sub != null) {
                sum += (long) sub.getNumberOfEvents();
            }
        }
        return sum;
    }

    synchronized public boolean addListOfEventsToSubscribers(ArrayList eventsToStore) {
        if (!eventsToStore.isEmpty()) {
            Event e = (Event)eventsToStore.get(0);
            if (e != null && publisherHashMap.containsKey(e.getClientId())) {

                long localTime;
                localTime = System.currentTimeMillis();
                publisherHashMap.get(e.getClientId()).setLocalTimestamp(localTime);

                
                Iterator itX = eventsToStore.iterator();
                while (itX.hasNext()) {
                    e = (Event) itX.next();
                    e.setUniqueIdWithinTheChannel(nextEventIdWithinTheChannel);
                    e.setLocalTimestamp(localTime);
                    nextEventIdWithinTheChannel++;
                }

                if (!subscriberHashMap.isEmpty()) {
                    Collection c = subscriberHashMap.values();
                    Iterator it = c.iterator();
                    while (it.hasNext()) {
                        Subscriber s = (Subscriber) it.next();
                        if (s.queueIsNotFull()) {
                            s.insertNewListOfEvents(eventsToStore);
                        } else {
                            logger.logWarningIfCounterReachedAndIncrement(this, "queue for subscriber ID " + s.getId() + ", on channel " + myTag + ", is FULL (" + s.getNumberOfEvents() + " events) [event " + e.getUniqueId() + " will be discharged] (total so far discharged: " + logger.getWarningCounter() + " )", Log.getLineNumber());
                        }
                    }
                } else {
                    Log.logWarning(this, "List of SUBSCRIBERS is EMPTY - discharging events", Log.getLineNumber());
                }

                return true;
            } else {
                Log.logWarning(this, "PUBLISHER ID " + e.getClientId() + " in NOT a registered publisher for channel " + myTag, Log.getLineNumber());
            }
        } else {
            Log.logWarning(this, "ARRAY LIST OF EVENTS IS EMPTY for TAG " + myTag, Log.getLineNumber());
        }
        return false;
    }

    synchronized public Event getNextEvent(String id) {
        Subscriber s = subscriberHashMap.get(id);
        if (s != null) {
            s.setLocalTimestamp(System.currentTimeMillis());
            return s.getNextEvent();
        }
        return null;
    }

    synchronized public ArrayList<Event> getEventsForSubscriber(String id, int numberOfEvents) {
        Subscriber s = subscriberHashMap.get(id);
        if (s != null) {
            int counter = 0;
            Event e;
            ArrayList<Event> events = new ArrayList<Event>();
            
            //subscriberHashMap.get(id).updateLocalTimestamp();
            s.updateLocalTimestamp();
                
            if (s.queueIsNotEmply()) {
                while ((e = s.getNextEvent()) != null && counter < numberOfEvents) {
                    events.add(e);
                    counter++;
                }
            }
            Log.logDebug(this, "SUB ID: " + id + " STORAGE NUMBER OF EVENTS RETRIEVED: " + events.size(), Log.getLineNumber());
            
            return events;
        } else {
                Log.logWarning(this, "SUBSCRIBER " + id + " does not exists anymore", Log.getLineNumber());
        }
        return null;
    }
}