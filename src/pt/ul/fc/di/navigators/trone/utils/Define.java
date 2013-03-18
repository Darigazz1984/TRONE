/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.utils;

import java.io.Serializable;

/**
 *
 * @author kreutz
 */
public class Define implements Serializable {
    
    public static int DEFAULTEVENTSIZE = 10;
    
    public static enum METHOD {
        REGISTER,
        SUBSCRIBE,
        PUBLISH,
        POLL,
        PUBLISH_WITH_CACHING,
        POLL_EVENTS_FROM_CHANNEL,
        UNREGISTER,
        UNSUBSCRIBE,
        UNSUBSCRIBE_FROM_ALL_CHANNELS,
        UNREGISTER_FROM_ALL_CHANNELS,
        WRONG_CONFIGURATIONS,
        GET_STATE,
        APPLICATION_STATE,
        GET_CURRENT_EID, //get the current execution id, for the unordered communication
        NOT_DEFINED;
    }
    
    public static enum LOG {
        OUT,
        INFO,
        WARNING,
        ERROR,
        DEBUG;
    }
    
    public static enum QoSmessage {
        URGENT,
        NOT_URGENT,
        PERSISTENT,
        NOT_PERSISTENT;
    }
    
    public static enum QoSchannel {
        TOTAL_ORDER,
        NO_ORDER;
    }
    
    public static enum QoP {
        CFT,
        BFT;
    }
     public static enum ReplicaCommand{
         PING, // o ping pong e para saber se a replica esta viva
         PONG,
         KILL,
         SLOW,
         LIE;
     }
}
