/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.mgt;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import pt.ul.fc.di.navigators.trone.utils.ConfigHandler;
import pt.ul.fc.di.navigators.trone.utils.Log;
import pt.ul.fc.di.navigators.trone.utils.ServerInfo;

/**
 * @author kreutz
 */
public class ConfigServerManager {
    
    private ConfigHandler serverConfig;
    private ConfigNetManager netConfigManager;
    
    public ConfigServerManager (String netConfigFile, String serverConfigFile) throws FileNotFoundException, IOException {
        
        Log.logDebugFlush(this, "CONFIG SERVER MANAGER: STARTING ...", Log.getLineNumber());
        
        netConfigManager = new ConfigNetManager(netConfigFile);
        serverConfig = new ConfigHandler(serverConfigFile);
        serverConfig.readConfig();
        
        Log.logDebugFlush(this, "CONFIG SERVER MANAGER: UP AND RUNNING ...", Log.getLineNumber());
        
    }
    
    public boolean enableShortTermConnections() {
        int value = serverConfig.getIntValue("enableShortTermConnections");
        if (value == 1) {
            return true;
        }
        return false;
    }
    
    public boolean enableLongTermConnections() {
        int value = serverConfig.getIntValue("enableLongTermConnections");
        if (value == 1) {
            return true;
        }
        return false;
    }
    
    public int getNumberOfThreadsForShortTermConnections() {
        return serverConfig.getIntValue("numberOfThreadsForShortTermConnections");
    }
    
    public int getNumberOfThreadsForLongTermConnections() {
        return serverConfig.getIntValue("numberOfThreadsForLongTermConnections");
    }
    
    public int getMaxNumberOfPublishersPerChannel() {
        return serverConfig.getIntValue("publishersPerChannel");
    }
    
    public int getMaxNumberOfSubscribersPerChannel() {
        return serverConfig.getIntValue("subscribersPerChannel"); 
    }
    
    public ServerInfo getLocalServerInfo(int myIndex) {
        return netConfigManager.getServerInfoAtIndex(myIndex);
    }
    
    public ArrayList getChannelTags() {
        return serverConfig.getChannelTags();
    }
    
    public long getMessageTimeToLive() {
        return serverConfig.getLongValue("messageTimeToLive");
    }
    
    public long getPublisherTimeToLive() {
        return serverConfig.getLongValue("publisherTimeToLive");
    }
    
    public long getSubscriberTimeToLive() {
        return serverConfig.getLongValue("subscriberTimeToLive");
    }
    
    public long getMessageCleanerRoundPeriod() {
        return serverConfig.getLongValue("messageCleanerRoundPeriod");
    }

    public long getSubscriberCleanerRoundPeriod() {
        return serverConfig.getLongValue("subscriberCleanerRoundPeriod");
    }

    public long getPublisherCleanerRoundPeriod() {
        return serverConfig.getLongValue("publisherCleanerRoundPeriod");
    }
    
    public boolean enableGarbageCollector() {
         if (serverConfig.getIntValue("enableGarbageCollector") == 1) {
                 return true;
         }
         return false;
    }

    public int getMaxNumberOfEventsPerQueue() {
        return serverConfig.getIntValue("maxNumberOfEventsPerQueue");
    }
    
    public String getConfigPath(){
        return serverConfig.getStringValue("configPath");
    }
    
    public boolean useBFT(){
        if(serverConfig.getIntValue("useBFT") == 1)
            return true;
        else
            return false;
    }
    
    public boolean useCFT(){
        if(serverConfig.getIntValue("useCFT") == 1)
            return true;
        else
            return false;
    }
    
    public String getChannelPath(){
        return serverConfig.getStringValue("channelConfigPath");
    }
}
