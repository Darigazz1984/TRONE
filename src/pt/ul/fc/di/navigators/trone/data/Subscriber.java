/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 *
 * @author kreutz
 */
public class Subscriber {

    private String myId;
    private ArrayList<Event> myEvents;
    private long myLocalTimestamp;
    private int maxNumberOfEvents;

    public Subscriber(String id, int maxEvents) {
        myId = id;
        myEvents = new ArrayList<Event>();
        myLocalTimestamp = System.currentTimeMillis();
        maxNumberOfEvents = maxEvents;
    }

    public String getId() {
        return myId;
    }
    
    public Event getNextEvent() {
            Iterator it = myEvents.iterator();

            if (it.hasNext()) {
                Event e = (Event) it.next();
                myEvents.remove(e);
                return e;
            }
        return null;
    }

    public void insertNewEvent(Event e) {
        myEvents.add(e);
    }

    public boolean removeEvent(Event e) {
        return myEvents.remove(e);
    }

    public long removeAllOldEvents(long messageTimeToLive, long currentTime) {
        ArrayList eventsToRemove = new ArrayList<Event>();
        
            for (int i = 0; i < myEvents.size(); i++) {
                Event e = (Event) myEvents.get(i);
                if ((e.getLocalTimestamp() + messageTimeToLive) < currentTime) {
                    eventsToRemove.add(e);
                }
            }

            Iterator it = eventsToRemove.iterator();
            while (it.hasNext()) {
                myEvents.remove((Event)it.next());
            }
            
        return eventsToRemove.size();
    }
    
    public long getLocalTimestamp() {
        return myLocalTimestamp;
    }
    
    public void updateLocalTimestamp() {
        myLocalTimestamp = System.currentTimeMillis();
    }

    public void setLocalTimestamp(long localTime) {
        myLocalTimestamp = localTime;
    }

    public int getNumberOfEvents() {
        return myEvents.size();
    }

    public boolean queueIsNotFull() {
        if (myEvents.size() >= maxNumberOfEvents) {
            return false;
        }
        return true;
    }

    public void insertNewListOfEvents(ArrayList eventsToStore) {
        myEvents.addAll(eventsToStore);
        //Log.logInfo(this, "I AM: "+this.myId+" AND I CURRENTLY HAVE: "+myEvents.size()+" EVENTS", maxNumberOfEvents);
    }

    public boolean queueIsNotEmpty() {
       
        if (myEvents.size() > 0) {
            return true;
        }
        return false;
    }

}
