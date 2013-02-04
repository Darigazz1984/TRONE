/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author kreutz
 */
public class Subscriber implements Serializable {

    private String myId;
    private ArrayList<Event> myEvents;
    private long myLocalTimestamp;
    private long maxNumberOfEvents;
    
    public Subscriber(){
        myId = "";
        myEvents = new ArrayList<Event>();
        myLocalTimestamp = System.currentTimeMillis();
        maxNumberOfEvents = 0;
                
    }

    /*public Subscriber(String id, int maxEvents) {
        super();
        myId = id;
        myEvents = new ArrayList<Event>();
        myLocalTimestamp = System.currentTimeMillis();
        maxNumberOfEvents = maxEvents;
    }*/
    
    public Subscriber(String id){
        super();
        myId = id;
        myEvents = new ArrayList<Event>();
        myLocalTimestamp = System.currentTimeMillis();
        maxNumberOfEvents = 0;
    }

    public String getId() {
        return myId;
    }
    
    public void setMaxEvents(long maxE){
        maxNumberOfEvents = maxE;
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
    }

    public boolean queueIsNotEmpty() {
       
        if (myEvents.size() > 0) {
            return true;
        }
        return false;
    }
     /**
      * Removes the oldest event of the subscriber queue
      */
    public void removeOlder(){
        myEvents.remove(0);
    }
    
    /**
     * Inserts urgent event. This event will count has the oldest event.
     * @param e 
     */
    public void insertUrgentEvent(Event e){
        myEvents.add(0, e);
    }
    
    /**
     * Removes the newest event of the subscriber queue
     */
    public void removeNewer(){
        myEvents.remove(myEvents.size()-1);
    }
    
    
    
    private void writeObject(ObjectOutputStream stream) throws IOException {
        System.out.println("SERIALIZING SUBSCRIBER");
        stream.writeUTF(myId);
        stream.writeObject(myEvents);
        stream.writeLong(maxNumberOfEvents);
        
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        System.out.println("DESERIALIZING SUBSCRIBER");
        myId = (String) stream.readUTF();
        myEvents = (ArrayList<Event>) stream.readObject();
        maxNumberOfEvents = (long) stream.readLong();
        this.myLocalTimestamp = System.currentTimeMillis();
    }
    /*
    public byte[] getState() throws IOException{
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(b);
        
        o.writeUTF(myId); //escrever id do subscriber
        o.writeLong(maxNumberOfEvents);
        for(Event e: myEvents){
            o.writeObject(e);
        }
        return b.toByteArray();
    }
    
    public void setState(byte[] state) throws IOException, ClassNotFoundException{
        ByteArrayInputStream b = new ByteArrayInputStream(state);
        ObjectInputStream o = new ObjectInputStream(b);
        this.myId = (String)o.readUTF();
        this.maxNumberOfEvents = (long) o.readLong();
        Event e = (Event) o.readObject();
        while(e != null){
            this.myEvents.add(e);
            e = (Event) o.readObject();
        }
    }*/
}
