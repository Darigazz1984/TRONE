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

/**
 *
 * @author kreutz
 */
public class Publisher implements Serializable {

    private String myId;
    private long myLocalTimestamp;

    public Publisher(String id) {
        super();
        myId = id;
        myLocalTimestamp = System.currentTimeMillis();
    }
    
    public Publisher(){
        myLocalTimestamp = System.currentTimeMillis();
    }
    
    public String getId() {
        return myId;
    }

    public long getLocalTimestamp() {
        return myLocalTimestamp;
    }
    
    public void updateLocalTimestamp() {
        myLocalTimestamp = System.currentTimeMillis();
    }

    void setLocalTimestamp(long localTime) {
        myLocalTimestamp = localTime;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        System.out.println("SERIALIZING PUBLISHER");
        stream.writeUTF(myId);
    }
    
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        System.out.println("DESERIALIZING PUBLISHER");
        myId = (String)stream.readUTF();
        myLocalTimestamp = System.currentTimeMillis();
    }
    /*
    public byte[] getState() throws IOException{
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(b);
        o.writeUTF(myId);
        return b.toByteArray();
    }
    
    public void setState(byte[] state) throws IOException{
        ByteArrayInputStream b = new ByteArrayInputStream(state);
        ObjectInputStream o = new ObjectInputStream(b);
        this.myId = (String)o.readUTF();
        this.myLocalTimestamp = System.currentTimeMillis();
    }*/
}
