/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.comm;

/**
 *
 * @author kreutz
 */
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import pt.ul.fc.di.navigators.trone.data.Event;
import pt.ul.fc.di.navigators.trone.data.Request;
import pt.ul.fc.di.navigators.trone.data.Storage;
import pt.ul.fc.di.navigators.trone.mgt.ConfigServerManager;
import pt.ul.fc.di.navigators.trone.mgt.MessageBrokerServer;
import pt.ul.fc.di.navigators.trone.utils.Define.METHOD;
import pt.ul.fc.di.navigators.trone.utils.Log;
import pt.ul.fc.di.navigators.trone.utils.ServerInfo;

public class ServerProxy {

    static int serverIndex;
    static Storage sharedStorage;
    static ServerSocket sharedServerSocketForShortTerm;
    static ServerSocket sharedServerSocketForLongTerm;
    static ConfigServerManager sharedServerConfig;
    static boolean sharedUseLongTermConn;
    static long sharedMessageTimeToLive;
    static int sharedReplicaId;

    public ServerProxy(int replicaId) throws FileNotFoundException, IOException {

        Log.logDebugFlush(this, "SERVER PROXY STARTING ...", Log.getLineNumber());

        sharedStorage = new Storage(replicaId);
        sharedServerConfig = new ConfigServerManager("netConfig.props", "serverConfig.props");
        createChannelsFromConfig();
        serverIndex = replicaId;
        sharedServerSocketForShortTerm = null;
        sharedServerSocketForLongTerm = null;
        sharedReplicaId = replicaId;

        Log.logDebugFlush(this, "SERVER PROXY IS UP AND RUNNING...", Log.getLineNumber());
    }

    private void createChannelsFromConfig() {
        ArrayList channelTags = sharedServerConfig.getChannelTags();
        Iterator itr = channelTags.iterator();
        while (itr.hasNext()) {
            String tag = (String) itr.next();
            sharedStorage.insertNewChannel(tag);
            Log.logOut(this, "channel with TAG: " + tag + " CREATED: " + sharedStorage.hasChannel(tag), Log.getLineNumber());
        }
    }

    public void startWorkerThreadPools() throws IOException, UnknownHostException, NoSuchAlgorithmException {

        ServerInfo si = sharedServerConfig.getLocalServerInfo(serverIndex);

        if (si != null) {

            sharedServerSocketForShortTerm = new ServerSocket(si.getPortForShortTerm());
            // start SHORT term connection threads
            if (sharedServerConfig.enableShortTermConnections()) {
                Log.logOut(this, "starting " + sharedServerConfig.getNumberOfThreadsForShortTermConnections() + " threads for SHORT term connections on PORT: " + si.getPortForShortTerm(), Log.getLineNumber());
                for (int i = 0; i < sharedServerConfig.getNumberOfThreadsForShortTermConnections(); i++) {
                    ServerProxyThreadShortTermConn newThread = new ServerProxyThreadShortTermConn(sharedStorage, sharedServerSocketForShortTerm, sharedServerConfig, sharedReplicaId);
                    newThread.start();
                }
            }

            sharedServerSocketForLongTerm = new ServerSocket(si.getPortForLongTerm());
            // start LONG term connection threads
            if (sharedServerConfig.enableLongTermConnections()) {
                Log.logOut(this, "starting " + sharedServerConfig.getNumberOfThreadsForLongTermConnections() + " threads for LONG term connections on PORT: " + si.getPortForLongTerm(), Log.getLineNumber());
                for (int i = 0; i < sharedServerConfig.getNumberOfThreadsForLongTermConnections(); i++) {
                    ServerProxyThreadLongTermConn newThread = new ServerProxyThreadLongTermConn(sharedStorage, sharedServerSocketForLongTerm, sharedServerConfig, sharedReplicaId);
                    newThread.start();
                }
            }

            if (sharedServerConfig.enableGarbageCollector()) {
                Log.logOut(this, "starting 1 threads for GARBAGE COLLECTION", Log.getLineNumber());
                ServerStorageGarbageCollectorThread newT = new ServerStorageGarbageCollectorThread(sharedStorage, sharedServerConfig);
                newT.start();
            }
        } else {
            Log.logWarning(this, "no server info for index " + serverIndex, Log.getLineNumber());
        }
    }
}

class ServerProxyThreadShortTermConn extends Thread {

    static Storage thStorage;
    static ServerSocket thServerSocket;
    private MessageBrokerServer thMessageBroker;
    private Log logger;
    private int thReplicaId;

    // FIXME
    public ServerProxyThreadShortTermConn(Storage sto, ServerSocket s, ConfigServerManager scm, int replicaId) throws UnknownHostException, NoSuchAlgorithmException {
        thStorage = sto;
        thServerSocket = s;
        thMessageBroker = new MessageBrokerServer(scm);
        thReplicaId = replicaId;
        logger = new Log(1000);
    }

    @Override
    public void run() {

        Socket thSocket;
        ObjectOutputStream cOut;
        ObjectInputStream cIn;

        InetAddress inetAddr = null;

        Request thOutReq = new Request();
        ArrayList<Event> events = null;

        while (true) {
            
            Request thInReq = null;
    
            try {
                thSocket = thServerSocket.accept();

                inetAddr = thSocket.getInetAddress();

                Log.logInfo(this, "SHORT TERM CONN: CLIENT WITH IP " + inetAddr.getHostAddress() + " JUST CONNECTED", Log.getLineNumber());
                
                cOut = new ObjectOutputStream(thSocket.getOutputStream());
                cIn = new ObjectInputStream(thSocket.getInputStream());

                while (thInReq == null && !(thInReq instanceof Request)) {
                    // receive a client request
                    thInReq = (Request) cIn.readObject();
                }

                if (thInReq != null) {

                    Log.logDebug(this, "RECEIVED REQ: " + logger.getSpecificCounterValue("NREQS") + " ID: " + thInReq.getUniqueId() + " METHOD: " + thInReq.getMethod() + " OBJ ID: " + thInReq, Log.getLineNumber());

                    switch (thInReq.getMethod()) {
                        case REGISTER:
                            if (thMessageBroker.register(thInReq, thStorage)) {
                                thOutReq.setOperationStatus(true);
                            } else {
                                thOutReq.setOperationStatus(false);
                            }
                            break;
                        case SUBSCRIBE:
                            if (thMessageBroker.subscribe(thInReq, thStorage)) {
                                thOutReq.setOperationStatus(true);
                            } else {
                                thOutReq.setOperationStatus(false);
                            }
                            break;
                        case PUBLISH:
                            if (thMessageBroker.publish(thInReq, thStorage)) {
                                thOutReq.setOperationStatus(true);
                            } else {
                                thOutReq.setOperationStatus(false);
                            }
                            break;
                        case PUBLISH_WITH_CACHING:
                            if (thMessageBroker.publishWithCaching(thInReq, thStorage)) {
                                thOutReq.setOperationStatus(true);
                            } else {
                                thOutReq.setOperationStatus(false);
                            }
                            break;
                        case POLL:
                            events = thMessageBroker.poll(thInReq, thStorage);
                            if (events != null) {
                                thOutReq.addAllEvents(events);
                                thOutReq.setOperationStatus(true);
                            } else {
                                thOutReq.setOperationStatus(false);
                            }
                            break;
                        case POLL_EVENTS_FROM_CHANNEL:
                            events = thMessageBroker.pollEventsFromChannel(thInReq, thStorage);
                            if (events != null) {
                                thOutReq.setOperationStatus(true);
                                thOutReq.addAllEvents(events);
                            } else {
                                thOutReq.setOperationStatus(false);
                            }
                            break;
                        case UNREGISTER:
                            if (thMessageBroker.unRegister(thInReq, thStorage)) {
                                thOutReq.setOperationStatus(true);
                            } else {
                                thOutReq.setOperationStatus(false);
                            }
                            break;
                        case UNSUBSCRIBE:
                            if (thMessageBroker.unSubscribe(thInReq, thStorage)) {
                                thOutReq.setOperationStatus(true);
                            } else {
                                thOutReq.setOperationStatus(false);
                            }
                            break;
                        case UNSUBSCRIBE_FROM_ALL_CHANNELS:
                            if (thMessageBroker.unSubscribeFromAllChannels(thInReq, thStorage)) {
                                thOutReq.setOperationStatus(true);
                            } else {
                                thOutReq.setOperationStatus(false);
                            }
                            break;
                        case UNREGISTER_FROM_ALL_CHANNELS:
                            if (thMessageBroker.unRegisterFromAllChannels(thInReq, thStorage)) {
                                thOutReq.setOperationStatus(true);
                            } else {
                                thOutReq.setOperationStatus(false);
                            }
                            break;
                        default:
                            thOutReq.setOperationStatus(false);
                            Log.logWarning(this, "METHOD " + thInReq.getMethod() + " NOT SUPPORTED", Log.getLineNumber());
                            break;
                    }

                    thOutReq.setId(thInReq.getId());
                    thOutReq.setClientId(thInReq.getClientId());
                    thOutReq.setMethod(thInReq.getMethod());

                } else {
                    logger.incrementSpecificCounter("NNULLREQSRECV", 1);
                    thOutReq.setClientId(String.valueOf(thReplicaId));
                    thOutReq.setOperationStatus(false);
                    thOutReq.setMethod(METHOD.NOT_DEFINED);
                }

                thOutReq.setChannelTag(thInReq.getChannelTag());

                logger.incrementSpecificCounter("NRETEVENTS", thOutReq.getAllEvents().size());

                if (logger.getSpecificCounterValue("NREQS") % 1000 == 0) {
                    Log.logDebug(this, "STATS: NUMBER OF RECEIVED REQUESTS: " + logger.getSpecificCounterValue("NREQS") + " NUMBER OF REQUESTED EVENTS: " + logger.getSpecificCounterValue("NREQSEVENTS") + " NUMBER OF EVENTS SENT: " + logger.getSpecificCounterValue("NRETEVENTS"), Log.getLineNumber());
                    Log.logDebug(this, "STATS: NUMBER OF NULL RESULTED INVOKES: " + logger.getSpecificCounterValue("NNULLINVOKES") + " NUMBER OF REQUESTS EQUAL NULL: " + logger.getSpecificCounterValue("NNULLREQSRECV"), Log.getLineNumber());
                    thMessageBroker.currentStats();
                }

                // send a responde to the client
                cOut.writeObject(thOutReq);
                cOut.flush();

                thOutReq.cleanArrayOfEvents();
                
                cOut.close();
                cIn.close();
                thSocket.close();

            } catch (Exception ex) {
                Log.logInfo(this, "CLIENT " + inetAddr.getHostAddress() + " DISCONECTED", Log.getLineNumber());
            }
        }
    }
}

class ServerProxyThreadLongTermConn extends Thread {

    static Storage thStorage;
    static ServerSocket thServerSocket;
    private MessageBrokerServer thMessageBroker;
    private int thReplicaId;
    private Log logger;

    public ServerProxyThreadLongTermConn(Storage sto, ServerSocket s, ConfigServerManager scm, int replicaId) throws UnknownHostException, NoSuchAlgorithmException {
        thStorage = sto;
        thServerSocket = s;
        thMessageBroker = new MessageBrokerServer(scm);
        thReplicaId = replicaId;
        logger = new Log(100);
    }

    @Override
    public void run() {
        //System.out.println("Thread " + this.getId() + " waiting connections at IP: " + thServerSocket.getInetAddress() + " and PORT: "+ thServerSocket.getLocalPort());

        Socket thSocket;
        ObjectOutputStream cOut;
        ObjectInputStream cIn;

        logger.initSpecificCounter("NREQS", 0);
        logger.initSpecificCounter("NREQSEVENTS", 0);
        logger.initSpecificCounter("NRETEVENTS", 0);
        logger.initSpecificCounter("NNULLINVOKES", 0);

        while (true) {

            try {

                thSocket = thServerSocket.accept();
                InetAddress ip = thSocket.getInetAddress();

                Log.logInfo(this, "LONG TERM CONN: CLIENT WITH IP " + ip.getHostAddress() + " JUST CONNECTED", Log.getLineNumber());
                
                cOut = new ObjectOutputStream(thSocket.getOutputStream());
                cIn = new ObjectInputStream(thSocket.getInputStream());

                Request thOutReq = new Request();
                Request thInReq = null;
                ArrayList<Event> events = null;

                thOutReq.setReplicaId(thReplicaId);

                while (true) {
                    try {

                        thInReq = null;
                        while (thInReq == null || !(thInReq instanceof Request)) {
                            thInReq = (Request) cIn.readObject();
                        }

                        logger.incrementSpecificCounter("NREQS", 1);
                        logger.incrementSpecificCounter("NREQSEVENTS", thInReq.getNumberOfEventsToFetch());

                        if (thInReq != null) {

                            Log.logDebug(this, "RECEIVED REQ: " + logger.getSpecificCounterValue("NREQS") + " ID: " + thInReq.getUniqueId() + " METHOD: " + thInReq.getMethod() + " OBJ ID: " + thInReq, Log.getLineNumber());

                            switch (thInReq.getMethod()) {
                                case REGISTER:
                                    if (thMessageBroker.register(thInReq, thStorage)) {
                                        thOutReq.setOperationStatus(true);
                                    } else {
                                        thOutReq.setOperationStatus(false);
                                    }
                                    break;
                                case SUBSCRIBE:
                                    if (thMessageBroker.subscribe(thInReq, thStorage)) {
                                        thOutReq.setOperationStatus(true);
                                    } else {
                                        thOutReq.setOperationStatus(false);
                                    }
                                    break;
                                case PUBLISH:
                                    if (thMessageBroker.publish(thInReq, thStorage)) {
                                        thOutReq.setOperationStatus(true);
                                    } else {
                                        thOutReq.setOperationStatus(false);
                                    }
                                    break;
                                case PUBLISH_WITH_CACHING:
                                    if (thMessageBroker.publishWithCaching(thInReq, thStorage)) {
                                        thOutReq.setOperationStatus(true);
                                    } else {
                                        thOutReq.setOperationStatus(false);
                                    }
                                    break;
                                case POLL:
                                    events = thMessageBroker.poll(thInReq, thStorage);
                                    if (events != null) {
                                        thOutReq.addAllEvents(events);
                                        thOutReq.setOperationStatus(true);
                                    } else {
                                        thOutReq.setOperationStatus(false);
                                    }
                                    break;
                                case POLL_EVENTS_FROM_CHANNEL:
                                    events = thMessageBroker.pollEventsFromChannel(thInReq, thStorage);
                                    if (events != null) {
                                        thOutReq.setOperationStatus(true);
                                        thOutReq.addAllEvents(events);
                                    } else {
                                        thOutReq.setOperationStatus(false);
                                    }
                                    break;
                                case UNREGISTER:
                                    if (thMessageBroker.unRegister(thInReq, thStorage)) {
                                        thOutReq.setOperationStatus(true);
                                    } else {
                                        thOutReq.setOperationStatus(false);
                                    }
                                    break;
                                case UNSUBSCRIBE:
                                    if (thMessageBroker.unSubscribe(thInReq, thStorage)) {
                                        thOutReq.setOperationStatus(true);
                                    } else {
                                        thOutReq.setOperationStatus(false);
                                    }
                                    break;
                                case UNSUBSCRIBE_FROM_ALL_CHANNELS:
                                    if (thMessageBroker.unSubscribeFromAllChannels(thInReq, thStorage)) {
                                        thOutReq.setOperationStatus(true);
                                    } else {
                                        thOutReq.setOperationStatus(false);
                                    }
                                    break;
                                case UNREGISTER_FROM_ALL_CHANNELS:
                                    if (thMessageBroker.unRegisterFromAllChannels(thInReq, thStorage)) {
                                        thOutReq.setOperationStatus(true);
                                    } else {
                                        thOutReq.setOperationStatus(false);
                                    }
                                    break;
                                default:
                                    thOutReq.setOperationStatus(false);
                                    Log.logWarning(this, "METHOD " + thInReq.getMethod() + " NOT SUPPORTED", Log.getLineNumber());
                                    break;
                            }

                            thOutReq.setId(thInReq.getId());
                            thOutReq.setClientId(thInReq.getClientId());
                            thOutReq.setMethod(thInReq.getMethod());

                        } else {
                            logger.incrementSpecificCounter("NNULLREQSRECV", 1);
                            thOutReq.setClientId(String.valueOf(thReplicaId));
                            thOutReq.setOperationStatus(false);
                            thOutReq.setMethod(METHOD.NOT_DEFINED);
                        }

                        thOutReq.setChannelTag(thInReq.getChannelTag());

                        logger.incrementSpecificCounter("NRETEVENTS", thOutReq.getAllEvents().size());

                        if (logger.getSpecificCounterValue("NREQS") % 1000 == 0) {
                            Log.logDebug(this, "STATS: NUMBER OF RECEIVED REQUESTS: " + logger.getSpecificCounterValue("NREQS") + " NUMBER OF REQUESTED EVENTS: " + logger.getSpecificCounterValue("NREQSEVENTS") + " NUMBER OF EVENTS SENT: " + logger.getSpecificCounterValue("NRETEVENTS"), Log.getLineNumber());
                            Log.logDebug(this, "STATS: NUMBER OF NULL RESULTED INVOKES: " + logger.getSpecificCounterValue("NNULLINVOKES") + " NUMBER OF REQUESTS EQUAL NULL: " + logger.getSpecificCounterValue("NNULLREQSRECV"), Log.getLineNumber());
                            thMessageBroker.currentStats();
                        }

                        // send a responde to the client
                        cOut.writeObject(thOutReq);
                        cOut.flush();

                        //NOTE: reset is important when re-using serializable objects
                        cOut.reset();

                        thOutReq.cleanArrayOfEvents();

                    } catch (Exception ex) {
                        //ex.printStackTrace();
                        Log.logInfo(this, "CLIENT " + ip.getHostAddress() + " DISCONNECTED", Log.getLineNumber());
                        Log.logDebug(this, "STATS: NUMBER OF RECEIVED REQUESTS: " + logger.getSpecificCounterValue("NREQS") + " NUMBER OF REQUESTED EVENTS: " + logger.getSpecificCounterValue("NREQSEVENTS") + " NUMBER OF EVENTS SENT: " + logger.getSpecificCounterValue("NRETEVENTS"), Log.getLineNumber());
                        Log.logDebug(this, "STATS: NUMBER OF NULL RESULTED INVOKES: " + logger.getSpecificCounterValue("NNULLINVOKES") + " NUMBER OF REQUESTS EQUAL NULL: " + logger.getSpecificCounterValue("NNULLREQSRECV"), Log.getLineNumber());
                        thMessageBroker.currentStats();
                        break;
                    }

                }

                // close connections
                cOut.close();
                cIn.close();
                thSocket.close();

            } catch (Exception ex) {
                Logger.getLogger(ServerProxyThreadLongTermConn.class.getName()).log(Level.SEVERE, null, ex);
            }


        }
    }
}

class ServerStorageGarbageCollectorThread extends Thread {

    static Storage thStorage;
    static ConfigServerManager thServerConfig;

    public ServerStorageGarbageCollectorThread(Storage sto, ConfigServerManager serverConfig) {
        thStorage = sto;
        thServerConfig = serverConfig;
    }

    @Override
    public void run() {
        long currentTime;
        long messageTimeToLive = thServerConfig.getMessageTimeToLive();
        long messageCleanerRoundPeriod = thServerConfig.getMessageCleanerRoundPeriod();
        long subscriberCleanerRoundPeriod = thServerConfig.getSubscriberCleanerRoundPeriod();
        long publisherCleanerRoundPeriod = thServerConfig.getPublisherCleanerRoundPeriod();
        long subscriberTimeToLive = thServerConfig.getSubscriberTimeToLive();
        long publisherTimeToLive = thServerConfig.getPublisherTimeToLive();
        long publisherLastCheck = 0;
        long subscriberLastCheck = 0;

        while (true) {
            try {
                Thread.sleep(messageCleanerRoundPeriod);
            } catch (InterruptedException ex) {
                Log.logError(this, "InterruptedException on Thread.sleep", Log.getLineNumber());
            }

            long number = 0;

            currentTime = System.currentTimeMillis();

            if ((publisherLastCheck + publisherCleanerRoundPeriod) < currentTime) {
                number = thStorage.removeAllOldPublishers(publisherTimeToLive, currentTime);

                Log.logInfo(this, "REMOVING: " + number + " old PUBLISHERS have been REMOVED", Log.getLineNumber());

                publisherLastCheck = currentTime;
            }
            if ((subscriberLastCheck + subscriberCleanerRoundPeriod) < currentTime) {
                number = thStorage.removeAllOldSubscribers(subscriberTimeToLive, currentTime);

                Log.logInfo(this, "REMOVING: " + number + " old SUBSCRIBERS have been REMOVED", Log.getLineNumber());

                subscriberLastCheck = currentTime;
            }

            number = thStorage.removeAllOldEvents(messageTimeToLive, currentTime);

            Log.logInfo(this, "REMOVING: " + number + " old EVENTS have been REMOVED", Log.getLineNumber());
        }
    }
}