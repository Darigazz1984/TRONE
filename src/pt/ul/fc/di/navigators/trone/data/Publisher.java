/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.data;

/**
 *
 * @author kreutz
 */
public class Publisher {

    private String myId;
    private long myLocalTimestamp;

    public Publisher(String id) {
        myId = id;
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
}
