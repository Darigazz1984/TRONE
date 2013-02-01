/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.data;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

/**
 *
 * @author kreutz
 */
public class Event implements Serializable {
   
    private long eventId;
    private String myClientId;
    private String myContent;
    private long myIdInTheChannel; // FIXME: to be used
    private long myLocalTimeStamp;

    //private static final long serialVersionUID = 7996973399624796147L;
    
    public Event() {
        super();
        eventId = 0;
        myClientId = null;
        myContent = null;
        myIdInTheChannel = 0;
        myLocalTimeStamp = 0;
    }
    
    public void setClientId(String cId) {
        myClientId = cId;
    }

    public void setLocalTimestamp(long localTime) {
        myLocalTimeStamp = localTime;
    }

    public long getLastUpdateTime() {
        return myLocalTimeStamp;
    }
    
    public void updateLocalTimestamp() {
        myLocalTimeStamp = System.currentTimeMillis();
    }

    public long getLocalTimestamp() {
        return myLocalTimeStamp;
    }

    public void setId(long eId) {
        eventId = eId;
    }

    public long getUniqueIdWithinTheChannel() {
        return myIdInTheChannel;
    }

    public void setUniqueIdWithinTheChannel(long id) {
        myIdInTheChannel = id;
    }

    public long getId() {
        return eventId;
    }

    public String getUniqueId() {
        String s = myClientId;
        s = s.concat(Long.toString(eventId));
        return s;
    }

    public String getClientId() {
        return myClientId;
    }

    public void setContent(String value) {
        myContent = value;
    }

    public String getContent() {
        return myContent;
    }
   
    private void readObject(ObjectInput objectInput) throws ClassNotFoundException, IOException {
        this.eventId = objectInput.readLong();
        this.myClientId = (String) objectInput.readObject();
        this.myContent = (String) objectInput.readObject();
        this.myIdInTheChannel = objectInput.readLong();
        this.myLocalTimeStamp = objectInput.readLong();
    }

    private void writeObject(ObjectOutput objectOutput) throws IOException {
        objectOutput.writeLong(eventId);
        objectOutput.writeObject(myClientId);
        objectOutput.writeObject(myContent);
        objectOutput.writeLong(myIdInTheChannel);
        objectOutput.writeLong(myLocalTimeStamp);
    }

}