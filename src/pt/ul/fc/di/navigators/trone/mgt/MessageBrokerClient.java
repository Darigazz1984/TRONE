/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.mgt;

/**
 *
 * @author smruti and kreutz
 */

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import pt.ul.fc.di.navigators.trone.comm.ClientProxy;
import pt.ul.fc.di.navigators.trone.data.Event;
import pt.ul.fc.di.navigators.trone.data.Request;
import pt.ul.fc.di.navigators.trone.utils.CurrentTime;
import pt.ul.fc.di.navigators.trone.utils.Define.METHOD;
import pt.ul.fc.di.navigators.trone.utils.IdGenerator;
import pt.ul.fc.di.navigators.trone.utils.Log;

public class MessageBrokerClient {
 
    private ClientProxy clientProxy;
    private String localClientId;
    private static long eventIdSequenceNumber;
    private static long requestIdSequenceNumber;
    // FIXME: these global variable/objects are because of Linux JVM's performance issues (on Mac those variable can be local)
    private Request globalRequest;
    private static Request requestWithCaching;
    private ArrayList<Event> eventCachingArray;
    //private long firstCachedEventTime;
    
    public MessageBrokerClient() throws FileNotFoundException, IOException, ClassNotFoundException, UnknownHostException, NoSuchAlgorithmException {
        Log.logDebugFlush(this, "MBC: STARTING ...", Log.getLineNumber());
        
        
        clientProxy = new ClientProxy();
        
        eventIdSequenceNumber = 0;
        requestIdSequenceNumber = 0;
        
        localClientId = IdGenerator.getUniqueIdMD5();
        
        
        globalRequest = new Request();
        globalRequest.setClientId(getLocalClientId());
        
        requestWithCaching = new Request();
        requestWithCaching.setClientId(getLocalClientId());
        
        eventCachingArray = new ArrayList<Event>();
        
        //firstCachedEventTime = 0;
        
        Log.logDebugFlush(this, "MBC: UP AND RUNNING ...", Log.getLineNumber());
    }

    private long requestNextId() {
        long nextId = requestIdSequenceNumber;
        requestIdSequenceNumber++;
        return nextId;
    }

    private String getLocalClientId() {
        return localClientId;
    }
    
    public Request register(String tag) throws IOException, ClassNotFoundException, UnknownHostException, NoSuchAlgorithmException, Exception {
        globalRequest.prepare(requestNextId());
        globalRequest.setMethod(METHOD.REGISTER);
        globalRequest.setChannelTag(tag);
        return clientProxy.invoke(globalRequest);
    }

    public Request subscribe(String tag) throws IOException, ClassNotFoundException, UnknownHostException, NoSuchAlgorithmException, Exception {
        globalRequest.prepare(requestNextId());
        //r.setClientId(getLocalClientId());
        globalRequest.setMethod(METHOD.SUBSCRIBE);
        globalRequest.setChannelTag(tag);
        return clientProxy.invoke(globalRequest);
    }

    public Request publish(Event e, String tag) throws ClassNotFoundException, IOException, UnknownHostException, NoSuchAlgorithmException, Exception {
        globalRequest.prepare(requestNextId());
        globalRequest.setMethod(METHOD.PUBLISH);
        //globalRequest.setChannelTag(tag);
        e.setClientId(getLocalClientId());
        e.setContent(e.getContent());
        e.setId(eventIdSequenceNumber);
        eventIdSequenceNumber++;
        globalRequest.setEvent(e);
        globalRequest.setNumberOfEventsToFetch(1);
        return clientProxy.invoke(globalRequest);
    }

    public Request publishWithCaching(Event e, String tag) throws ClassNotFoundException, IOException, UnknownHostException, NoSuchAlgorithmException, Exception {
        e.setClientId(getLocalClientId());
        e.setId(eventIdSequenceNumber);
        eventIdSequenceNumber++;
        /*if (eventCachingArray.size() == 0) {
            firstCachedEventTime = CurrentTime.getTimeInMilliseconds();
        }*/
        eventCachingArray.add(e);
        if (eventCachingArray.size() >= clientProxy.getNumberOfEventsToCachePerRequest()) {
            Log.logDebug(this, "CACHING COUNTER: " + eventCachingArray.size() + " MAX PER REQUEST: " + clientProxy.getNumberOfEventsToCachePerRequest(), Log.getLineNumber());
            globalRequest.prepare(requestNextId());
            //globalRequest.setChannelTag(tag);
            globalRequest.setMethod(METHOD.PUBLISH_WITH_CACHING);
            globalRequest.setArrayOfEvents(eventCachingArray);
            Log.logDebug(this, "REQ ID BEFORE INVOKE: " + globalRequest, Log.getLineNumber());
            Request res = clientProxy.invoke(globalRequest);
            //eventCachingArray = new ArrayList<Event>();
            eventCachingArray.clear();
            return res;
        }
        return null;
    }

    public Request pollEventsFromChannel(String tag, int numberOfEvents) throws IOException, ClassNotFoundException, UnknownHostException, NoSuchAlgorithmException, Exception {
        globalRequest.prepare(requestNextId());
        //globalRequest.setChannelTag(tag);
        globalRequest.setMethod(METHOD.POLL_EVENTS_FROM_CHANNEL);
        if (numberOfEvents > clientProxy.getMaxNumberOfEventsToFetchPerRequest()) 
            numberOfEvents = clientProxy.getMaxNumberOfEventsToFetchPerRequest();
        globalRequest.setNumberOfEventsToFetch(numberOfEvents);
        return clientProxy.invoke(globalRequest);
    }

    public Request pollEventsFromChannelWithCaching(String tag) throws IOException, ClassNotFoundException, UnknownHostException, NoSuchAlgorithmException, Exception {
        return pollEventsFromChannel(tag, clientProxy.getNumberOfEventsToCachePerRequest());
    }

    public Request pollAllEventsFromChannel(String tag) throws IOException, ClassNotFoundException, UnknownHostException, NoSuchAlgorithmException, Exception {
        return pollEventsFromChannel(tag, clientProxy.getMaxNumberOfEventsToFetchPerRequest());
    }

    public Request unSubscribe(String tag) throws IOException, ClassNotFoundException, UnknownHostException, NoSuchAlgorithmException, Exception {
        globalRequest.prepare(requestNextId());
        globalRequest.setMethod(METHOD.UNREGISTER);
        globalRequest.setChannelTag(tag);
        return clientProxy.invoke(globalRequest);
    }

    public Request unSubscribeFromAllChannels() throws IOException, ClassNotFoundException, UnknownHostException, NoSuchAlgorithmException, Exception {
        globalRequest.prepare(requestNextId());
        globalRequest.setMethod(METHOD.UNSUBSCRIBE_FROM_ALL_CHANNELS);
        return clientProxy.invoke(globalRequest);
    }

    public Request unRegisterFromAllChannels() throws IOException, ClassNotFoundException, UnknownHostException, NoSuchAlgorithmException, Exception {
        globalRequest.prepare(requestNextId());
        globalRequest.setClientId(getLocalClientId());
        globalRequest.setMethod(METHOD.UNREGISTER_FROM_ALL_CHANNELS);
        return clientProxy.invoke(globalRequest);
    }

    public Request unRegister(String tag) throws IOException, ClassNotFoundException, UnknownHostException, NoSuchAlgorithmException, Exception {
        globalRequest.prepare(requestNextId());
        //globalRequest.setChannelTag(tag);
        globalRequest.setMethod(METHOD.UNREGISTER);
        return clientProxy.invoke(globalRequest);
    }

    public int getNumberOfEventsPerPoll() {
        return clientProxy.getMaxNumberOfEventsToFetchPerRequest();
    }
    
    public int getNumberOfEventsPerCachedRequest() {
        return clientProxy.getNumberOfEventsToCachePerRequest();
    }
    
    public void currentStats() {
        clientProxy.currentStats();
    }
    
    public String getClientId() {
        return getLocalClientId();
    }
    
    public void closeConnection() throws IOException{
        clientProxy.closeConnection();
    }
}
