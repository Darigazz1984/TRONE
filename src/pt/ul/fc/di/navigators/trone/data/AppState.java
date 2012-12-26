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
import java.util.List;
import java.util.Set;

/**
 *
 * @author igor
 * This class represents the application state
 * contains the name of all channels, all subscribers and all publishers
 */
public class AppState implements Serializable{
    private HashMap<String, List<String>> publishers;
    private HashMap<String, List<String>> subscribers;
    
    public AppState(){
        publishers = new HashMap<String, List<String>>();
        subscribers = new HashMap<String, List<String>>();
    }
    
    
    public void addPublishers(String channel, List<String> pub){
        if(publishers.containsKey(channel)){
            //DO NOTHING
        }else
            publishers.put(channel, pub);
                   
    }
    
    public void addSubscribers(String channel, List<String> sub){
        if(subscribers.containsKey(channel)){
            //DO NOTHING
        }else
            subscribers.put(channel, sub);
    }
    
    public Set<String> getChannels(){
        Set<String> channels = publishers.keySet();
        for(String s: subscribers.keySet()){
            if(channels.contains(s)){
                //DO NOTHING
            }else
                channels.add(s);
        }
        return channels;
    }
    
    
    public List<String> getPublishers(String channel){
        return publishers.get(channel);
    }
    
    public List<String> getSubscribers(String channel){
        return subscribers.get(channel);
    }
    public void readObject(ObjectInput objectInput) throws ClassNotFoundException, IOException{
        publishers = (HashMap<String, List<String>>)objectInput.readObject();
        subscribers = (HashMap<String, List<String>>)objectInput.readObject();
    }

    public void writeObject(ObjectOutput objectOutput) throws IOException{
        objectOutput.writeObject(publishers);
        objectOutput.writeObject(subscribers);
    }
    
}
