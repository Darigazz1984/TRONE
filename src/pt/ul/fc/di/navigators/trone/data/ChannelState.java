/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.data;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;
import pt.ul.fc.di.navigators.trone.utils.Define.QoP;
import pt.ul.fc.di.navigators.trone.utils.Define.QoSchannel;

/**
 *
 * @author igor
 * This object represents the state of a given channel
 * Contains all relevant information
 */
public class ChannelState implements Serializable{  
    private String tag;
    private Set<String> subscribers;
    private Set<String> publishers;
    private long nextEventIdWithinTheChannel;
    private QoP faultLevel;
    private QoSchannel channelOrdering;
    
    /**
     * Class constructor
     * @param t the channel tag
     */
    public ChannelState(String t){
        tag = t;
    }
    
    /**
     * Sets the subscribers
     * @param sub the subscribers
     */
    public void setSubscribers(Set<String> sub){
        this.subscribers = sub;
    }
    /**
     * Add a subscriber
     * @param sub the subscriber
     */
    public void addSubscriber(String sub){
        if(subscribers != null)
            this.subscribers.add(sub);
        else{
            subscribers = new TreeSet<String>();
            subscribers.add(sub);
        }
    }
    
    /**
     * 
     * @return the subscribers for this channel
     */
    public Set<String> getSubscribers(){
        return this.subscribers;
    }
    
    /**
     * Sets the publishers
     * @param pub the publishers
     */
    public void setPublishers(Set<String> pub){
        this.publishers = pub;
    }
    
    
    /**
     * Add a publisher
     * @param tag the subscriber
     */
    public void addPublishers(String tag){
        this.publishers.add(tag);
    }
    
    /**
     * 
     * @return the publishers for this channel
     */
    public Set<String> getPublishers(){
        return this.publishers;
    }
    
    public void setNextEventNumber(long num){
        this.nextEventIdWithinTheChannel = num;
    }
    
    public long getNextEventNumber(){
        return this.nextEventIdWithinTheChannel;
    }
    
    public void setQoP(QoP ftl){
        this.faultLevel = ftl;
    }
    
    public QoP getQoP(){
        return this.faultLevel;
    }
    
    public void setQoS(QoSchannel or){
        this.channelOrdering = or;
    }
    
    public QoSchannel getQoS(){
        return this.channelOrdering;
    }
    
    
    
    public void readObject(ObjectInput objectInput) throws ClassNotFoundException, IOException {
          tag = (String)objectInput.readObject();
          subscribers = (Set<String>)objectInput.readObject();
          publishers = (Set<String>)objectInput.readObject();
          nextEventIdWithinTheChannel = objectInput.readLong();
          faultLevel = (pt.ul.fc.di.navigators.trone.utils.Define.QoP)objectInput.readObject();
          channelOrdering = (pt.ul.fc.di.navigators.trone.utils.Define.QoSchannel)objectInput.readObject();
    }

    public void writeObject(ObjectOutput objectOutput) throws IOException {
        objectOutput.writeObject(tag);
        objectOutput.writeObject(subscribers);
        objectOutput.writeObject(publishers);
        objectOutput.writeLong(nextEventIdWithinTheChannel);
        objectOutput.writeObject(faultLevel);
        objectOutput.writeObject(channelOrdering);
    }
}
