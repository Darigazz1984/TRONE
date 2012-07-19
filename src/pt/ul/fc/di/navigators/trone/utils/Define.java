/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.utils;

/**
 *
 * @author kreutz
 */
public class Define {
    
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
        NOT_DEFINED;
    }
    
    public static enum LOG {
        OUT,
        INFO,
        WARNING,
        ERROR,
        DEBUG;
    }
    
}
