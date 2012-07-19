package pt.ul.fc.di.navigators.trone.apps;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import pt.ul.fc.di.navigators.trone.utils.ConfigHandler;

/**
 *
 * @author kreutz
 */
public class ConfigsWriter {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ConfigHandler confHandler;

        try {
            InetAddress ia = InetAddress.getLocalHost();

            // write net config file
            confHandler = new ConfigHandler("netConfig.props");

            //if (ia.getHostName().equals("xiru.navigators.tche")) {
                confHandler.setProperty("127.0.0.1", "4010");
                confHandler.setProperty("127.0.0.2", "4020");
                confHandler.setProperty("127.0.0.3", "4030");
                confHandler.setProperty("127.0.0.4", "4040");
            //} else {
            //    confHandler.setProperty("192.168.2.35", "4010"); //s15
            //    confHandler.setProperty("192.168.2.88", "4020"); //s14
            //    confHandler.setProperty("192.168.2.33", "4010"); //s13
            //    confHandler.setProperty("192.168.2.89", "4020"); //212
            //}

            confHandler.store();

            // write client config file
            confHandler = new ConfigHandler("clientConfig.props");

            confHandler.setProperty("configLookUpIP", "127.0.0.5");
            confHandler.setProperty("configLookUpPort", "4009");
            confHandler.setProperty("useLongTermConnections", "1");
            confHandler.setProperty("useSBFT", "1");
            confHandler.setProperty("useAllReplicasOnCFT", "1"); // for publishers on CFT
            confHandler.setProperty("majorityInPercentage", "75");
            confHandler.setProperty("eventTimeToLiveInMilliseconds", "5000");
            confHandler.setProperty("numberOfEventsToCachePerRequest", "10");
            confHandler.setProperty("maxNumberOfEventsToFetchPerRequest", "10");
            confHandler.setProperty("timeoutForReplicaCounterReset", "10000");
            confHandler.setProperty("cacheCleanUpPeriodInNumberOfRequests", "10");
            confHandler.setProperty("numberOfConnectionRetries", "3");
            confHandler.setProperty("maxTimeToWaitWithCaching", "1000");

            confHandler.store();

            // write server config file
            confHandler = new ConfigHandler("serverConfig.props");

            confHandler.setProperty("enableShortTermConnections", "1");
            confHandler.setProperty("enableLongTermConnections", "1");
            confHandler.setProperty("numberOfThreadsForShortTermConnections", "10");
            confHandler.setProperty("numberOfThreadsForLongTermConnections", "50");
            confHandler.setProperty("enableGarbageCollector", "1");
            confHandler.setProperty("publishersPerChannel", "2000");
            confHandler.setProperty("subscribersPerChannel", "2000");
            confHandler.setProperty("messageTimeToLive", "60000"); // in milliseconds
            confHandler.setProperty("publisherTimeToLive", "200000"); // in milliseconds
            confHandler.setProperty("subscriberTimeToLive", "300000"); // in milliseconds
            confHandler.setProperty("messageCleanerRoundPeriod", "30000"); // in milliseconds
            confHandler.setProperty("publisherCleanerRoundPeriod", "100000"); // in milliseconds
            confHandler.setProperty("subscriberCleanerRoundPeriod", "100000"); // in milliseconds
            confHandler.setProperty("useLongTermConnections", "1");
            confHandler.setProperty("maxNumberOfEventsPerQueue", "100000");
            confHandler.setProperty("network", "-1");
            confHandler.setProperty("cpu", "-1");
            confHandler.setProperty("storage", "-1");
            confHandler.setProperty("security", "-1");
            confHandler.setProperty("apache", "-1");
            confHandler.setProperty("mysql", "-1");
            confHandler.setProperty("opennebula", "-1");
            confHandler.setProperty("openstack", "-1");
            confHandler.setProperty("logs", "-1");
            confHandler.setProperty("iaas", "-1");
            confHandler.setProperty("smtp", "-1");
            confHandler.setProperty("dns", "-1");
            confHandler.setProperty("dhcp", "-1");
            /*
            for (int i = 30; i <= 70; i++) {
                confHandler.setProperty("c"+i, "-1");
            }
            for (int i = 0; i <= 100; i++) {
                confHandler.setProperty("d"+i, "-1");
            }
            for (int i = 0; i <= 200; i++) {
                confHandler.setProperty("k"+i, "-1");
            }
            */
            confHandler.store();

        } catch (Exception ex) {
            Logger.getLogger(ConfigsWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
