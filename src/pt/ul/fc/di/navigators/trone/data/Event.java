/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.data;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Arrays;

/**
 *
 * @author kreutz
 */
public class Event implements Serializable {
   
    private long eventId;
    private String myClientId;
    private String myContent; // legacy purposes
    private byte[] payload; // Event payload
    private long myIdInTheChannel; // FIXME: to be used, maybe not
    private long myLocalTimeStamp;
    
    public Event() {
        super();
        eventId = 0;
        myClientId = null;
        myContent = null;
        myIdInTheChannel = 0;
        myLocalTimeStamp = 0;
        payload = new byte[0];
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
    
    public byte[] getPayload(){
        return this.payload;
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
    
    public void setPayload(byte[] p){
        int s = p.length;
        this.payload = new byte[s];
        System.arraycopy(p, 0, this.payload, 0, s);
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
        this.payload = new byte[objectInput.readInt()];
        objectInput.read(this.payload);
        
    }

    private void writeObject(ObjectOutput objectOutput) throws IOException {
        objectOutput.writeLong(eventId);
        objectOutput.writeObject(myClientId);
        objectOutput.writeObject(myContent);
        objectOutput.writeLong(myIdInTheChannel);
        objectOutput.writeLong(myLocalTimeStamp);
        objectOutput.writeInt(payload.length);
        objectOutput.write(payload);
    }

}