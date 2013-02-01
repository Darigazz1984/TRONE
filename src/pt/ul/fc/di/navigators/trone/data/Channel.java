/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import pt.ul.fc.di.navigators.trone.utils.CurrentTime;
import pt.ul.fc.di.navigators.trone.utils.Define.QoP;
import pt.ul.fc.di.navigators.trone.utils.Define.QoSchannel;
import pt.ul.fc.di.navigators.trone.utils.Log;

/**
 * @author kreutz
 */
public class Channel implements Serializable{

    private String myTag;
    private HashMap<String, Publisher> publisherHashMap;
    private HashMap<String, Subscriber> subscriberHashMap;
    private long nextEventIdWithinTheChannel;
    private Log logger;
    private QoP faultLevel;
    private QoSchannel channelOrdering;
    private long clientTimeToLive;
    private long eventTimeToLive;
    private long maxEvent;
    private int maxPublishers;
    private int maxSubscribers;
    private String dischargeOrder;
    
    public Channel(){
        logger = new Log(100);
    }
    
    public Channel(String tag, int replicaId){
        super();
        myTag = tag;
        subscriberHashMap = new HashMap<String, Subscriber>();
        publisherHashMap = new HashMap<String, Publisher>();
        nextEventIdWithinTheChannel = 0;
        logger = new Log(100);
    }
    
    public void setNextIdWithinTheChannel(long id){
        nextEventIdWithinTheChannel = id;
    }
    
    public Channel(String tag, int replicaId, QoP flt, QoSchannel order, long clientTimeToLive, long eventTimeToLive, long maxEvent, int maxPub, int maxSub, String eventDischargeOrder){
         switch(flt){
            case CFT:
                Log.logInfo(this, "CREATING CHANNEL WITH TAG: "+tag+ " AND QoP: CFT", Log.getLineNumber());
                break;
            case BFT:
                Log.logInfo(this, "CREATING CHANNEL WITH TAG: "+tag+ " AND QoP: BFT", Log.getLineNumber());
                break;
            default:
                Log.logWarning(this, "PROBS", Log.getLineNumber());
                break;
        }
        myTag = tag;
        faultLevel = flt;
        channelOrdering = order;
        subscriberHashMap = new HashMap<String, Subscriber>();
        publisherHashMap = new HashMap<String, Publisher>();
        nextEventIdWithinTheChannel = 0;
        logger = new Log(100);
        this.clientTimeToLive = clientTimeToLive;
        this.eventTimeToLive = eventTimeToLive;
        this.maxEvent = maxEvent;
        maxPublishers = maxPub;
        maxSubscribers = maxSub;
        dischargeOrder = eventDischargeOrder;
    }
    
    
    public Channel(String tag, int replicaId, QoP flt, QoSchannel order) {
        switch(flt){
            case CFT:
                Log.logInfo(this, "CREATING CHANNEL WITH TAG: "+tag+ " AND QoP: CFT", Log.getLineNumber());
                break;
            case BFT:
                Log.logInfo(this, "CREATING CHANNEL WITH TAG: "+tag+ " AND QoP: BFT", Log.getLineNumber());
                break;
            default:
                Log.logWarning(this, "PROBS", Log.getLineNumber());
                break;
        }
        myTag = tag;
        faultLevel = flt;
        channelOrdering = order;
        subscriberHashMap = new HashMap<String, Subscriber>();
        publisherHashMap = new HashMap<String, Publisher>();
        nextEventIdWithinTheChannel = 0;
        logger = new Log(100);
    }
    
    public String getTag() {
        return myTag;
    }
    
    public QoP getQoP(){
        return faultLevel;
    }
    
    public QoSchannel getQoS(){
        return channelOrdering;
    }

    synchronized public boolean addPublisher(Publisher pub) {
        if (publisherHashMap.containsKey(pub.getId())) {
            return false;
        }
        if(publisherHashMap.size() < maxPublishers){
            Log.logInfo(this, "ADDING NEW PUBLISHER: " + pub.getId() + " TO CHANNEL: " + myTag + " AT TIME: " + CurrentTime.getTimeInSeconds(), Log.getLineNumber());
            publisherHashMap.put(pub.getId(), pub);
            return true;
        }else{
            Log.logInfo(this, "Max publishers reached",Log.getLineNumber());
            return false;
        }
    }

    synchronized public boolean removePublisher(String id) {
        if (publisherHashMap.containsKey(id)) {
            publisherHashMap.remove(id);
            Log.logInfo(this, "REMOVIND PUBLISHER: " + id + " FROM CHANNEL: " + myTag + " AT TIME: " + CurrentTime.getTimeInSeconds(), Log.getLineNumber());
            return true;
        }
        return false;
    }

    synchronized public boolean addSubscriber(Subscriber sub) {
        
        if (subscriberHashMap.containsKey(sub.getId())) {
            return false;
        }
        if(subscriberHashMap.size() < maxSubscribers){
            sub.setMaxEvents(this.maxEvent);
            Log.logInfo(this, "ADDING NEW SUBSCRIBER: " + sub.getId() + " TO CHANNEL: " + myTag + " AT TIME: " + CurrentTime.getTimeInSeconds(), Log.getLineNumber());
            subscriberHashMap.put(sub.getId(), sub);
            return true;
        }
        Log.logInfo(this, "Max subscribers reached",Log.getLineNumber());
        return false;
    }

    synchronized public boolean removeSubscriber(String id) {
        if (subscriberHashMap.containsKey(id)) {
            subscriberHashMap.remove(id);
            Log.logInfo(this, "REMOVING SUBSCRIBER: " + id + " FROM CHANNEL: " + myTag + " AT TIME: " + CurrentTime.getTimeInSeconds(), Log.getLineNumber());
            return true;
        }
        return false;
    }

    synchronized public boolean addEventToSubscribers(Event e) {
        if (publisherHashMap.containsKey(e.getClientId())) { // verificação se quem envia o evento pode publicar no canal
            //long localTime = System.currentTimeMillis(); //Deixa de ser necessário
            e.setUniqueIdWithinTheChannel(nextEventIdWithinTheChannel);
            //e.setLocalTimestamp(localTime);
            nextEventIdWithinTheChannel++;

            if (!subscriberHashMap.isEmpty()) {
                Collection c = subscriberHashMap.values();
                Iterator it = c.iterator();
                while (it.hasNext()) {
                    Subscriber s = (Subscriber) it.next();
                    if (s.queueIsNotFull()) { // ISTO PODE FALHAR SE O SUBSCRIBER JA MORREU OU  NÃO COLHE OS EVENTOS A TEMPO, ARRANJAR FORMA DE VERIFICAR SE O SUBSCRIBER ESTA VIVO. ISTO OCORRE QUANDO UM SUBSCRIBER FALHA SEM SER REGISTADO PELO SISTEMA, ELE VAI CONTINUAR SUBSCRITO NO CANAL
                        s.insertNewEvent(e);
                    } else {
                        if(dischargeOrder.equals("older")){
                            s.removeOlder();
                            logger.logWarningIfCounterReached(this, "QUEUE for subscriber ID " + s.getId() + " is full. The oldest event was discharged", Log.getLineNumber());
                        }else{
                            s.removeNewer();
                            logger.logWarningIfCounterReached(this, "QUEUE for subscriber ID " + s.getId() + " is full. The newer event was discharged", Log.getLineNumber());
                        }
                        s.insertNewEvent(e);
                    }
                }
            }

            return true;
        } else {
            Log.logWarning(this, "PUBLISHER ID " + e.getClientId() + " in NOT a registered publisher for channel " + myTag, Log.getLineNumber());
        }
        return false;
    }

    synchronized public boolean removeEvent(String subscriberId, Event e) {
        Subscriber s = subscriberHashMap.get(subscriberId);
        if (s != null) {
            if (s.removeEvent(e)) {
                return true;
            }
        } 
        return false;
    }

    synchronized public int getNumberOfPublishers() {
        return publisherHashMap.size();
    }

    synchronized public int getNumberOfSubscribers() {
        return subscriberHashMap.size();
    }

    /*
    synchronized public long removeAllOldEvents(long messageTimeToLive, long currentTime) {
        long sum = 0;
        Iterator it = subscriberHashMap.keySet().iterator();
        while (it.hasNext()) {
            String str = (String) it.next();
            Subscriber s = subscriberHashMap.get(str);
            if (s != null) {
                sum += s.removeAllOldEvents(messageTimeToLive, currentTime);
            }
        }

        return sum;
    }*/
    
    synchronized public long removeAllOldEvents(long currentTime) {
        long sum = 0;
        Iterator it = subscriberHashMap.keySet().iterator();
        while (it.hasNext()) {
            String str = (String) it.next();
            Subscriber s = subscriberHashMap.get(str);
            if (s != null) {
                sum += s.removeAllOldEvents(eventTimeToLive, currentTime);
            }
        }

        return sum;
    }
    
/*
    synchronized public long removeAllOldPublishers(long publisherTimeToLive, long currentTime) {
        ArrayList pubToRemove = new ArrayList<String>();
        Iterator it = publisherHashMap.keySet().iterator();
        while (it.hasNext()) {
            Publisher pub = publisherHashMap.get(it.next().toString());
            if ((pub.getLocalTimestamp() + publisherTimeToLive) < currentTime) {
                pubToRemove.add(pub.getId());
            }
        }

        for (int i = 0; i < pubToRemove.size(); i++) {
            publisherHashMap.remove((String) pubToRemove.get(i));
        }

        return pubToRemove.size();
    }*/
    
      synchronized public long removeAllOldPublishers(long currentTime) {
        ArrayList pubToRemove = new ArrayList<String>();
        Iterator it = publisherHashMap.keySet().iterator();
        while (it.hasNext()) {
            Publisher pub = publisherHashMap.get(it.next().toString());
            if ((pub.getLocalTimestamp() + clientTimeToLive) < currentTime) {
                pubToRemove.add(pub.getId());
            }
        }

        for (int i = 0; i < pubToRemove.size(); i++) {
            publisherHashMap.remove((String) pubToRemove.get(i));
        }

        return pubToRemove.size();
    }
    
    
    
    
/*
    synchronized public long removeAllOldSubscribers(long subscriberTimeToLive, long currentTime) {
        ArrayList subToRemove = new ArrayList<String>();
        Iterator it = subscriberHashMap.keySet().iterator();
        while (it.hasNext()) {
            Subscriber sub = subscriberHashMap.get(it.next().toString());
            if ((sub.getLocalTimestamp() + subscriberTimeToLive) < currentTime) {
                subToRemove.add(sub.getId());
            }
        }

        for (int i = 0; i < subToRemove.size(); i++) {
            subscriberHashMap.remove((String) subToRemove.get(i));
        }

        return subToRemove.size();
    }*/
    
    synchronized public long removeAllOldSubscribers(/*long subscriberTimeToLive,*/ long currentTime) {
        ArrayList subToRemove = new ArrayList<String>();
        Iterator it = subscriberHashMap.keySet().iterator();
        while (it.hasNext()) {
            Subscriber sub = subscriberHashMap.get(it.next().toString());
            if ((sub.getLocalTimestamp() + clientTimeToLive) < currentTime) {
                subToRemove.add(sub.getId());
            }
        }

        for (int i = 0; i < subToRemove.size(); i++) {
            subscriberHashMap.remove((String) subToRemove.get(i));
        }

        return subToRemove.size();
    }

    synchronized public long getNumberOfEvents() {
        long sum = 0;
        Iterator it = subscriberHashMap.keySet().iterator();
        while (it.hasNext()) {
            Subscriber sub = subscriberHashMap.get(it.next().toString());
            if (sub != null) {
                sum += (long) sub.getNumberOfEvents();
            }
        }
        return sum;
    }

    synchronized public boolean addListOfEventsToSubscribers(ArrayList eventsToStore) {
        if (!eventsToStore.isEmpty()) {
            Event e = (Event)eventsToStore.get(0);
            if (e != null && publisherHashMap.containsKey(e.getClientId())) {

                //long localTime;
                //localTime = System.currentTimeMillis();
                //publisherHashMap.get(e.getClientId()).setLocalTimestamp(localTime);

                //ID DO CANAL
                Iterator itX = eventsToStore.iterator();
                while (itX.hasNext()) {
                    e = (Event) itX.next();
                    e.setUniqueIdWithinTheChannel(nextEventIdWithinTheChannel);
                    //e.setLocalTimestamp(localTime);
                    nextEventIdWithinTheChannel++;
                }

                if (!subscriberHashMap.isEmpty()) {
                    Collection c = subscriberHashMap.values();
                    Iterator it = c.iterator();
                    while (it.hasNext()) {
                        Subscriber s = (Subscriber) it.next();
                        if (s.queueIsNotFull()) {
                            s.insertNewListOfEvents(eventsToStore);
                        } else {
                            logger.logWarningIfCounterReachedAndIncrement(this, "queue for subscriber ID " + s.getId() + ", on channel " + myTag + ", is FULL (" + s.getNumberOfEvents() + " events) [event " + e.getUniqueId() + " will be discharged] (total so far discharged: " + logger.getWarningCounter() + " )", Log.getLineNumber());
                        }
                    }
                } else {
                    Log.logWarning(this, "List of SUBSCRIBERS is EMPTY - discharging events", Log.getLineNumber());
                }

                return true;
            } else {
                Log.logWarning(this, "PUBLISHER ID " + e.getClientId() + " in NOT a registered publisher for channel " + myTag, Log.getLineNumber());
            }
        } else {
            Log.logWarning(this, "ARRAY LIST OF EVENTS IS EMPTY for TAG " + myTag, Log.getLineNumber());
        }
        return false;
    }

    synchronized public Event getNextEvent(String id) {
        Subscriber s = subscriberHashMap.get(id);
        if (s != null) {
            //s.setLocalTimestamp(System.currentTimeMillis());
            return s.getNextEvent();
        }
        return null;
    }

    synchronized public ArrayList<Event> getEventsForSubscriber(String id, int numberOfEvents) {
        Subscriber s = subscriberHashMap.get(id);
        if (s != null) {
            int counter = 0;
            Event e;
            ArrayList<Event> events = new ArrayList<Event>();
            
            //subscriberHashMap.get(id).updateLocalTimestamp();
            //s.updateLocalTimestamp();
            
            if (s.queueIsNotEmpty()) {
                    /**
                     * If the number of events is 0, then all the events are fetched,
                     * Otherwise only the requested number of events are pulled
                     */
                    if(numberOfEvents == 0){
                        do{
                            e = s.getNextEvent();
                            events.add(e);
                        }while(s.queueIsNotEmpty());
                    }else{
                        do{
                            e = s.getNextEvent();
                            events.add(e);
                            counter++;
                        }while(s.queueIsNotEmpty() && counter < numberOfEvents);
                    }
                    
                   
            }else{
               Log.logInfo(this.getClass().getCanonicalName(), "EMPTY QUEUE", Log.getLineNumber());
            }
            Log.logDebug(this, "SUB ID: " + id + " STORAGE NUMBER OF EVENTS RETRIEVED: " + events.size(), Log.getLineNumber());
            
            return events;
        } else {
                Log.logWarning(this, "SUBSCRIBER " + id + " DOES NOT EXISTS ANYMORE", Log.getLineNumber());
        }
        return null;
    }
    
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        myTag = (String) stream.readUTF();
        publisherHashMap = (HashMap<String, Publisher>)stream.readObject();
        subscriberHashMap = (HashMap<String, Subscriber>)stream.readObject();
        nextEventIdWithinTheChannel = stream.readLong();
        faultLevel = (QoP)stream.readObject();
        channelOrdering = (QoSchannel)stream.readObject();
        clientTimeToLive = (long)stream.readLong();
        eventTimeToLive = stream.readLong();
        maxEvent = stream.readLong();
        maxPublishers = stream.readInt();
        maxSubscribers = stream.readInt();
        dischargeOrder = (String) stream.readUTF();
    }
    
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeUTF(myTag);
        stream.writeObject(publisherHashMap);
        stream.writeObject(subscriberHashMap);
        stream.writeLong(nextEventIdWithinTheChannel);
        stream.writeObject(faultLevel);
        stream.writeObject(channelOrdering);
        stream.writeLong(clientTimeToLive);
        stream.writeLong(eventTimeToLive);
        stream.writeLong(maxEvent);
        stream.writeInt(maxPublishers);
        stream.writeInt(maxSubscribers);
        stream.writeUTF(dischargeOrder);
    }
    
    public Set<String> getListOfPublishers(){
        return publisherHashMap.keySet();
    }
    
    public byte[] getState() throws IOException{
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(b);
        
        o.writeUTF(myTag);
        o.writeLong(nextEventIdWithinTheChannel);
        o.writeObject(faultLevel);
        o.writeObject(channelOrdering);
        o.writeLong(clientTimeToLive);
        o.writeLong(eventTimeToLive);
        o.writeLong(maxEvent);
        o.writeInt(maxPublishers);
        o.writeInt(maxSubscribers);
        o.writeUTF(dischargeOrder);
        o.writeInt(publisherHashMap.size());
            for(String p: publisherHashMap.keySet()){
                o.write(publisherHashMap.get(p).getState());
            }
        o.writeInt(subscriberHashMap.size());
            for(String s: subscriberHashMap.keySet()){
                o.write(subscriberHashMap.get(s).getState());
            }
        
        return b.toByteArray();
    }
    
    public void setState(byte[] state){
        
    }
    
}