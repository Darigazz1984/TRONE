/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.mgt;

import java.io.FileNotFoundException;
import java.io.IOException;
import pt.ul.fc.di.navigators.trone.utils.ConfigHandler;
import pt.ul.fc.di.navigators.trone.utils.Log;

/**
 *
 * @author kreutz
 */
public class ConfigClientManager {

    private ConfigHandler configData;
    
    public ConfigClientManager (String clientConfigFile) throws FileNotFoundException, IOException {
        Log.logDebugFlush(this, "CONFIG CLIENT MANAGER: STARTING ...", Log.getLineNumber());
        
        configData = new ConfigHandler(clientConfigFile);
        configData.readConfig();
        
        Log.logDebugFlush(this, "CONFIG CLIENT MANAGER: UP AND RUNNING ...", Log.getLineNumber());
        
    }
          
    public boolean useLongTermConnections() {
        
        int value = configData.getIntValue("useLongTermConnections");
        
        if (value == 1) {
            return true;
        }
        
        return false;
    }

    public boolean useSBFT() {
        
        int value = configData.getIntValue("useSBFT");
        
        if (value == 1) {
            return true;
        }
        
        return false;
    }

    public boolean useAllReplicasOnCFT() {
        
        int value = configData.getIntValue("useAllReplicasOnCFT");
        
        if (value == 1) {
            return true;
        }
        
        return false;
    }
    
    public long getEventTimeToLiveInMilliseconds() {
        return configData.getLongValue("eventTimeToLiveInMilliseconds");
    }

    public int getNumberOfEventsToCachePerRequest() {
        return configData.getIntValue("numberOfEventsToCachePerRequest");
    }

    public int getMaxNumberOfEventsToFetchPerRequest() {
        return configData.getIntValue("maxNumberOfEventsToFetchPerRequest");
    }

    public int getMajorityInPercentage() {
        return configData.getIntValue("majorityInPercentage");
    }
    
    public int getTimeoutForReplicaCounterReset() {
        return configData.getIntValue("timeoutForReplicaCounterReset");
    }
    
    public int getCacheCleanUpPeriodInNumberOfRequests () {
        return configData.getIntValue("cacheCleanUpPeriodInNumberOfRequests");
    }
       
    public int getNumberOfConnectionRetries () {
        return configData.getIntValue("numberOfConnectionRetries");
    }
    
    public int getMaxTimeToWaitWithCaching() {
        return configData.getIntValue("maxTimeToWaitWithCaching");
    }
}
