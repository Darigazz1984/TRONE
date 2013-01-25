/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.mgt;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import pt.ul.fc.di.navigators.trone.data.Channel;
import pt.ul.fc.di.navigators.trone.utils.ConfigHandler;
import pt.ul.fc.di.navigators.trone.utils.Define.QoP;
import pt.ul.fc.di.navigators.trone.utils.Define.QoSchannel;


/**
 *
 * @author igor
 */
public class ConfigChannelManager {
    ConfigHandler handler;
    private QoP type;
    private QoSchannel order;
    private long maxEvent;
    private long clientTimeToLive;
    private String eventDischargOrder;
    private long eventTimeToLive;
    private int maxPublishers;
    private int maxSubscribers;
   
    
    public ConfigChannelManager(String path){
            handler = null;
        try {
            handler = new ConfigHandler(path);
            handler.readConfig();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ConfigChannelManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ConfigChannelManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        maxPublishers = 0;
        maxSubscribers = 0;
        initiate();
    }
    
    private void initiate(){
        if(handler == null){
            //DO NOTHING
        }else{
            if(handler.getStringValue("type").equals("BFT"))
                type = QoP.BFT;
            else
                type = QoP.CFT;
            
            if(handler.getIntValue("totalOrder") == 1)
                order = QoSchannel.TOTAL_ORDER;
            else
                order = QoSchannel.NO_ORDER;
            
            //read max pub and max sub
            maxPublishers = handler.getIntValue("maxPublishers");
            maxSubscribers = handler.getIntValue("maxSubscribers");
            maxEvent = handler.getLongValue("maxEventsPerQueue");
            clientTimeToLive = handler.getLongValue("clientsTimeToLive");
            eventTimeToLive = handler.getLongValue("eventsTimeToLive");
            eventDischargOrder = handler.getStringValue("eventsDischargingOrder");
        }
    }
    
    public boolean isBFT(){
        return (type.equals(QoP.BFT));
    }
    
    public Channel generateChannel(String t, int id){
        //public Channel(String tag, int replicaId, QoP flt, QoSchannel order, long clientTimeToLive, long eventTimeToLive, long maxEvent, int maxPub, int maxSub, String eventDischargeOrder)
        //return (new Channel(t.toLowerCase(), id, type, order));
        //new Channel(t.toLowerCase(), id, type, order, clientTimeToLive, eventTimeToLive, maxEvent, maxPublishers, maxSubscribers, eventDischargOrder);
        return (new Channel(t.toLowerCase(), id, type, order, clientTimeToLive, eventTimeToLive, maxEvent, maxPublishers, maxSubscribers, eventDischargOrder));
    }
    
    public int getMaxPublishers(){
        return maxPublishers;
    }
    
    public int getMaxSubscribers(){
        return maxSubscribers;
    }
    
    public long getMaxEvent(){
        return maxEvent;
    }
    
    public long getClientTimeToLive(){
        return clientTimeToLive;
    }
    
    public long getEventTimeToLive(){
        return eventTimeToLive;
    }
    
    public boolean dischargeOlder(){
        return eventDischargOrder.equals("older");
    }
            
}
