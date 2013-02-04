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
public class ServerProxyThreadShortTermConn extends Thread {

    static Storage thStorage;
    static ServerSocket thServerSocket;
    private MessageBrokerServer thMessageBroker;
    private Log logger;
    private int thReplicaId;
    
    boolean slow;
    boolean lie;

    // FIXME
    public ServerProxyThreadShortTermConn(Storage sto, ServerSocket s, ConfigServerManager scm, int replicaId, boolean sl, boolean l) throws UnknownHostException, NoSuchAlgorithmException {
        thStorage = sto;
        thServerSocket = s;
        //thMessageBroker = new MessageBrokerServer(scm);
        thMessageBroker = new MessageBrokerServer();
        thReplicaId = replicaId;
        logger = new Log(1000);
        slow = sl;
        lie = l;
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

                if (thInReq != null &&  (thStorage.getQoP(thInReq.getChannelTag())).equals(Define.QoP.CFT)) {

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