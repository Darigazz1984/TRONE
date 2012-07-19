/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.data;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import pt.ul.fc.di.navigators.trone.utils.Define.METHOD;

/**
 *
 * @author kreutz
 */
public class Request implements Serializable {
    private String clientId;
    private String channelTag;
    private METHOD method;
    private boolean opStatus;
    private long sequenceNumber;
    private int numberOfEventsToFetch;
    private int myReplicaId;
    private ArrayList<Event> listOfEvents;
    
    //private static final long serialVersionUID = 7525131695624786237L;
    
    public Request() {
        super();
        opStatus = false;
        method = null;
        channelTag = null;
        sequenceNumber = 0;
        numberOfEventsToFetch = 0;
        myReplicaId = 0;
        listOfEvents = new ArrayList<Event>();
    }
    
    public void setChannelTag(String tag) {
        channelTag = tag;
    }
    
    public String getChannelTag() {
        return channelTag;
    }
        
    public void setMethod(METHOD m) {
        method = m;
    }
    
    public METHOD getMethod() {
        return method;
    }
    
    public void setOperationStatus(boolean opStatusObj) {
        opStatus = opStatusObj;
    }
    
    public void prepare(long requestIdSequenceNumber) {
        sequenceNumber = requestIdSequenceNumber;
    }
    
    public String getUniqueId() {
        String value = clientId;
        value = value.concat(Long.toString(sequenceNumber));
        return value;
    }
    
    public void setClientId(String id) {
        clientId = id;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public boolean isOpSuccess() {
        return opStatus;
    }
    
    public Event getEvent() {
        if (!listOfEvents.isEmpty()) {
            return (Event) listOfEvents.remove(0);
        }
        return null;
    }
    
    public boolean hasMoreEvents() {
        if (!listOfEvents.isEmpty()) {
            return true;
        }
        return false;
    }
    
   public ArrayList removeAllEvents() {   
        if (!listOfEvents.isEmpty()) {
            ArrayList a = new ArrayList();
            a.addAll(listOfEvents);
            listOfEvents.clear();
            return a;
        }
        return listOfEvents;
    }
   
   public ArrayList getAllEvents() {   
        return listOfEvents;
    }
    
    public void addEvent(Event eventToSend) {
        listOfEvents.add(eventToSend);
    }

    public void setEvent(Event eventToSend) {
        listOfEvents.clear();
        listOfEvents.add(eventToSend);
    }

    public void addAllEvents(ArrayList eventsToSend) {
        listOfEvents.addAll(eventsToSend);
    }
        
    public void setArrayOfEvents(ArrayList eventsToSend) {
        listOfEvents.clear();
        listOfEvents.addAll(eventsToSend);
    }
        
    public void readObject(ObjectInput objectInput) throws ClassNotFoundException, IOException {
        this.clientId = (String) objectInput.readObject();
        this.channelTag = (String) objectInput.readObject();
        this.method = (METHOD) objectInput.readObject();
        this.opStatus = objectInput.readBoolean();
        this.sequenceNumber = objectInput.readLong();
        this.numberOfEventsToFetch = objectInput.readInt();
        this.myReplicaId = objectInput.readInt();
        Event e = (Event) objectInput.readObject();
        while (e != null) {
            this.listOfEvents.add(e);
            e = (Event) objectInput.readObject();
        }
    }

    public void writeObject(ObjectOutput objectOutput) throws IOException {
        objectOutput.writeObject(clientId);
        objectOutput.writeObject(channelTag);
        objectOutput.writeObject(method);
        objectOutput.writeObject(opStatus);
        objectOutput.writeLong(sequenceNumber);
        objectOutput.writeInt(numberOfEventsToFetch);
        objectOutput.writeInt(myReplicaId);
        Iterator it = listOfEvents.iterator();
        while (it.hasNext()) {
            Event e = (Event) it.next();
            objectOutput.writeObject(e);
        }
    }
    
    public void setNumberOfEventsToFetch(int nEventsToFetch) {
        numberOfEventsToFetch = nEventsToFetch;
    }

    public int getNumberOfEventsToFetch() {
        return numberOfEventsToFetch;
    }

    public void setId(long reqId) {
        sequenceNumber = reqId;
    }

    public long getId() {
        return sequenceNumber;
    }

    public void cleanArrayOfEvents() {
        listOfEvents.clear();
    }

    public boolean hasEvents() {
        if (listOfEvents.size() > 0) {
            return true;
        }
        return false;
    }
    
    public void setReplicaId(int replicaId) {
        myReplicaId = replicaId;
    }

    public int getReplicaId() {
        return myReplicaId;
    }

    public void setNumberOfEventsSentX(int size) {
        numberOfEventsToFetch = size;
    }
    
    public int getNumberOfEventsSentX() {
        return numberOfEventsToFetch;
    }
    
    public int getNumberOfEvents() {
        return listOfEvents.size();
    }
}
