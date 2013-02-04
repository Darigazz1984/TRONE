 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.mgt;

import java.io.IOException;
import java.util.ArrayList;
import pt.ul.fc.di.navigators.trone.data.*;
import pt.ul.fc.di.navigators.trone.utils.Log;

/**
 *
 * @author kreutz
 */
public class MessageBrokerServer {

    private Log logger;
    private static ConfigServerManager thServerConfigManager;
    /*
    public MessageBrokerServer(ConfigServerManager scm) {
        Log.logDebugFlush(this, "MBS: STARTING IN THREAD " + Thread.currentThread().getId(), Log.getLineNumber());
        
        thServerConfigManager = scm;
        
        logger = new Log(100);
        logger.initSpecificCounter("NPUBLISHEDEVENTS", 0);
        logger.initSpecificCounter("NPOLLEDEVENTS", 0);
        
        Log.logDebugFlush(this, "MBS: UP AND RUNNING IN THREAD " + Thread.currentThread().getId(), Log.getLineNumber());
    }*/
    
    
    public MessageBrokerServer() {
        Log.logDebugFlush(this, "MBS: STARTING IN THREAD " + Thread.currentThread().getId(), Log.getLineNumber());
        
        //thServerConfigManager = scm;
        
        logger = new Log(100);
        logger.initSpecificCounter("NPUBLISHEDEVENTS", 0);
        logger.initSpecificCounter("NPOLLEDEVENTS", 0);
        
        Log.logDebugFlush(this, "MBS: UP AND RUNNING IN THREAD " + Thread.currentThread().getId(), Log.getLineNumber());
    }

    public boolean register(Request r, Storage rStorage) throws IOException, ClassNotFoundException {
        String tag = r.getChannelTag();
        if (rStorage.hasChannel(tag)) {
            synchronized (this) {
                /*if (rStorage.getNumberOfPublishersForChannel(tag) < thServerConfigManager.getMaxNumberOfPublishersPerChannel()) {
                    Publisher p = new Publisher(r.getClientId());
                    return rStorage.insertNewPublisher(p, r.getChannelTag());
                } else {
                    Log.logWarning(this, "Number of PUBLISHERS for channel " + tag + " exceeded", Log.getLineNumber());
                }*/ 
                Publisher p = new Publisher(r.getClientId());
                if( rStorage.insertNewPublisher(p, tag)){
                    return true;
                }else{
                    return false;
                }
            } // FIM DO SYNC
        } else {
            Log.logWarning(this, "Channel TAG " + tag + " NOT found", Log.getLineNumber());
        }
        return false;
    }

    public boolean publish(Request r, Storage rStorage) throws ClassNotFoundException, IOException {
        logger.incrementSpecificCounter("NPUBLISHEDEVENTS", 1);
        //Log.logOut(this, "EVENTOS PUBLICADOS:                       "+logger.getSpecificCounterValue("NPUBLISHEDEVENTS"), Log.getLineNumber());
        return rStorage.insertNewEvent(r.getEvent(), r.getChannelTag());
    }

    public boolean publishWithCaching(Request r, Storage rStorage) throws ClassNotFoundException, IOException {
        logger.incrementSpecificCounter("NPUBLISHEDEVENTS", r.getAllEvents().size());
        //Log.logOut(this, "EVENTOS PUBLICADOS:                       "+logger.getSpecificCounterValue("NPUBLISHEDEVENTS"), Log.getLineNumber());
        Log.logDebug(this, "REQ ID: " + r.getUniqueId() + " N EVENTS TO ADD: " + r.getAllEvents().size(), Log.getLineNumber());
        Log.logDebug(this, "STORAGE AMOUNT OF EVENTS: " + rStorage.getNumberOfEvents(), Log.getLineNumber());
        
        return rStorage.insertListOfEvents(r.getAllEvents(), r.getChannelTag());
    }
    
    public ArrayList<Event> pollEventsFromChannel(Request r, Storage rStorage) {
        String tag = r.getChannelTag();
        if (rStorage.hasChannel(tag.toLowerCase())) {
            int nEvents = r.getNumberOfEventsToFetch();
            logger.incrementSpecificCounter("NPOLLEDEVENTS", nEvents);
            Log.logDebug(this, "REQ ID: " + r.getUniqueId() + " N EVENTS TO FETCH: " + r.getNumberOfEventsToFetch(), Log.getLineNumber());
            return rStorage.getEventsFromChannel(r.getClientId(), r.getChannelTag(), nEvents);
        } else {
            Log.logWarning(this, " channel TAG " + tag + " NOT FOUND", Log.getLineNumber());
        }
        return null;
    }
    
    public ArrayList<Event> poll(Request r, Storage rStorage) {
        return pollEventsFromChannel(r, rStorage);
    }

    public boolean subscribe(Request r, Storage rStorage) throws IOException, ClassNotFoundException {
        String tag = r.getChannelTag();
        if (rStorage.hasChannel(tag)) {
            synchronized (this) {
                /*if (rStorage.getNumberOfSubscribersForChannel(tag) < thServerConfigManager.getMaxNumberOfSubscribersPerChannel()) {
                    Subscriber s = new Subscriber(r.getClientId(), thServerConfigManager.getMaxNumberOfEventsPerQueue());
                    return rStorage.insertNewSubscriber(s, r.getChannelTag());
                } else {
                    Log.logWarning(this, "Number of Subscribers for channel " + tag + " exceeded.", Log.getLineNumber());
                }
                Subscriber s = new Subscriber(r.getClientId(), thServerConfigManager.getMaxNumberOfEventsPerQueue());*/
                Subscriber s = new Subscriber (r.getClientId());
                if(rStorage.insertNewSubscriber(s, tag)){
                    return true;
                }else{
                    return false;
                }
            }
        } else {
            Log.logWarning(this, "Channel TAG " + tag + " NOT found", Log.getLineNumber());
        }
        return false;
    }

    public boolean unSubscribe(Request r, Storage rStorage) throws IOException, ClassNotFoundException {
        String tag = r.getChannelTag();
        if (rStorage.hasChannel(tag)) {
            return rStorage.removeSubscriber(r.getClientId(), r.getChannelTag());
        } else {
            Log.logWarning(this, "Channel TAG " + tag + " NOT found", Log.getLineNumber());
        }
        return false;
    }

    public boolean unSubscribeFromAllChannels(Request r, Storage rStorage) throws IOException, ClassNotFoundException {
        return rStorage.unSubscribeFromAllChannels(r.getClientId());
    }
    
    public boolean unRegisterFromAllChannels(Request r, Storage rStorage) throws IOException, ClassNotFoundException {
        return rStorage.unRegisterFromAllChannels(r.getClientId());
    }

    public boolean unRegister(Request r, Storage rStorage) throws IOException, ClassNotFoundException {
        String tag = r.getChannelTag();
        if (rStorage.hasChannel(tag)) {
            return rStorage.removePublisher(r.getClientId(), r.getChannelTag());
        } else {
            Log.logWarning(this, "Channel TAG " + tag + " NOT found", Log.getLineNumber());
        }
        return false;
    }
    
    public void currentStats() {
        Log.logInfo(this, "NUMBER OF PUBLISHED EVENTS: " + logger.getSpecificCounterValue("NPUBLISHEDEVENTS") + " NUMBER OF POLLED EVENTS: " + logger.getSpecificCounterValue("NPOLLEDEVENTS"), Log.getLineNumber());
    }

}
