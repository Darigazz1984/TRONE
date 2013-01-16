/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.data;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author igor
 * This object represent the state of the storage. Must contain all relevant data
 */
public class StorageState implements Serializable{
    private HashMap<String, ChannelState> channels;
    
    /**
     * Class constructor
     */
    public StorageState(){
        channels = new HashMap<String, ChannelState>();
    }
    
    /**
     * Adds a new channel to the object
     * @param tag The TAG of the channel
     * @param channel ChannelState object representing the state of the channel
     */
    public void addChannel(String tag, ChannelState channel){
        channels.put(tag, channel);
    }
    
    /**
     * Returns a set containing the TAG's of all channels represented in this object
     * @return 
     */
    public Set<String> getChannelsTag(){
        return channels.keySet();
    }
    
    /**
     * Returns the state of a channel 
     * @param tag The TAG of the requested channel 
     * @return Object representing the state of the channel
     */
    public ChannelState getChannelState(String tag){
        return channels.get(tag);
    }
    
     public void readObject(ObjectInput objectInput) throws ClassNotFoundException, IOException {
        channels = (HashMap<String, ChannelState>)objectInput.readObject();
    }

    public void writeObject(ObjectOutput objectOutput) throws IOException {
        objectOutput.writeObject(channels);
    }
   
}
