/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.serverThreads;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import pt.ul.fc.di.navigators.trone.data.Event;
import pt.ul.fc.di.navigators.trone.data.Request;
import pt.ul.fc.di.navigators.trone.data.Storage;
import pt.ul.fc.di.navigators.trone.mgt.ConfigServerManager;
import pt.ul.fc.di.navigators.trone.mgt.MessageBrokerServer;
import pt.ul.fc.di.navigators.trone.utils.Define;
import pt.ul.fc.di.navigators.trone.utils.Log;

/**
 *
 * @author igor
 */
public class ServerProxyThreadLongTermConn extends Thread {

    static Storage thStorage;
    static ServerSocket thServerSocket;
    private MessageBrokerServer thMessageBroker;
    private int thReplicaId;
    private Log logger;
    
    boolean slow;
    boolean lie;

    public ServerProxyThreadLongTermConn(Storage sto, ServerSocket s, ConfigServerManager scm, int replicaId, boolean sl, boolean l) throws UnknownHostException, NoSuchAlgorithmException {
        thStorage = sto;
        thServerSocket = s;
        //thMessageBroker = new MessageBrokerServer(scm);
        thMessageBroker = new MessageBrokerServer();
        thReplicaId = replicaId;
        logger = new Log(100);
        
        slow = sl;
        lie = l;
    }

    @Override
    public void run() {
        

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

                        if (thInReq != null &&  thStorage.getQoP(thInReq.getChannelTag()).equals(Define.QoP.CFT)) {

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
                            thOutReq.setMethod(Define.METHOD.NOT_DEFINED);
                        }

                        thOutReq.setChannelTag(thInReq.getChannelTag());

                        logger.incrementSpecificCounter("NRETEVENTS", thOutReq.getAllEvents().size());

                        if (logger.getSpecificCounterValue("NREQS") % 1000 == 0) {
                            Log.logDebug(this, "STATS: NUMBER OF RECEIVED REQUESTS: " + logger.getSpecificCounterValue("NREQS") + " NUMBER OF REQUESTED EVENTS: " + logger.getSpecificCounterValue("NREQSEVENTS") + " NUMBER OF EVENTS SENT: " + logger.getSpecificCounterValue("NRETEVENTS"), Log.getLineNumber());
                            Log.logDebug(this, "STATS: NUMBER OF NULL RESULTED INVOKES: " + logger.getSpecificCounterValue("NNULLINVOKES") + " NUMBER OF REQUESTS EQUAL NULL: " + logger.getSpecificCounterValue("NNULLREQSRECV"), Log.getLineNumber());
                            thMessageBroker.currentStats();
                        }
                        if(lie){
                            thOutReq = new Request();
                            thOutReq.setClientId("LIE");
                            thOutReq.setOperationStatus(true);
                            thOutReq.setMethod(Define.METHOD.POLL);
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
                Log.logError(this.getClass().getCanonicalName(), "ERROR running next cyckle", Log.getLineNumber());
            }
        }
    }
}
