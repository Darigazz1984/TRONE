/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.comm;

/**
 *
 * @author kreutz
 */
import bftsmart.statemanagment.ApplicationState;
import bftsmart.tom.MessageContext;
import bftsmart.tom.ReplicaContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.Recoverable;
import bftsmart.tom.server.SingleExecutable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import pt.ul.fc.di.navigators.trone.data.Command;
import pt.ul.fc.di.navigators.trone.data.Event;
import pt.ul.fc.di.navigators.trone.data.Request;
import pt.ul.fc.di.navigators.trone.data.Storage;
import pt.ul.fc.di.navigators.trone.mgt.ConfigChannelManager;
import pt.ul.fc.di.navigators.trone.mgt.ConfigServerManager;
import pt.ul.fc.di.navigators.trone.mgt.MessageBrokerServer;
import pt.ul.fc.di.navigators.trone.utils.Define;
import pt.ul.fc.di.navigators.trone.utils.Define.METHOD;
import pt.ul.fc.di.navigators.trone.utils.Define.QoP;
import pt.ul.fc.di.navigators.trone.utils.Define.QoSchannel;
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
    
    //FLAGS DE CONTROLO DA REPLCIA
    static boolean lie;
    static boolean slow;

    public ServerProxy(int replicaId) throws FileNotFoundException, IOException {
        lie = false;
        slow = false;
        
        
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
        
        String path = sharedServerConfig.getChannelPath();
        File folder = new File(path);
        File f[] = folder.listFiles();
        for(File fileEntry: f){
            String tag = ((fileEntry.getName()).split("[.]"))[0];
            ConfigChannelManager ccm = new ConfigChannelManager(path+fileEntry.getName());
            sharedStorage.insertChannel(ccm.generateChannel(tag, serverIndex));
            Log.logInfo(this, "channel with TAG: " + tag + " CREATED", Log.getLineNumber());
        }
    }

    public void startWorkerThreadPools() throws IOException, UnknownHostException, NoSuchAlgorithmException {

        ServerInfo si = sharedServerConfig.getLocalServerInfo(serverIndex);

        if (si != null && sharedServerConfig.useCFT()) {
            
            sharedServerSocketForShortTerm = new ServerSocket(si.getPortForShortTerm());
            // start SHORT term connection threads
            if (sharedServerConfig.enableShortTermConnections()) {
                Log.logOut(this, "Starting " + sharedServerConfig.getNumberOfThreadsForShortTermConnections() + " threads for SHORT term connections on PORT: " + si.getPortForShortTerm(), Log.getLineNumber());
                for (int i = 0; i < sharedServerConfig.getNumberOfThreadsForShortTermConnections(); i++) {
                    ServerProxyThreadShortTermConn newThread = new ServerProxyThreadShortTermConn(sharedStorage, sharedServerSocketForShortTerm, sharedServerConfig, sharedReplicaId, slow, lie);
                    newThread.start();
                }
            }

            sharedServerSocketForLongTerm = new ServerSocket(si.getPortForLongTerm());
            // start LONG term connection threads
            if (sharedServerConfig.enableLongTermConnections()) {
                Log.logOut(this, "Starting " + sharedServerConfig.getNumberOfThreadsForLongTermConnections() + " threads for LONG term connections on PORT: " + si.getPortForLongTerm(), Log.getLineNumber());
                for (int i = 0; i < sharedServerConfig.getNumberOfThreadsForLongTermConnections(); i++) {
                    ServerProxyThreadLongTermConn newThread = new ServerProxyThreadLongTermConn(sharedStorage, sharedServerSocketForLongTerm, sharedServerConfig, sharedReplicaId, slow, lie);
                    newThread.start();
                }
            }
            
         } else {
            Log.logWarning(this, "no server info for index " + serverIndex, Log.getLineNumber());
        }

        if (si != null && sharedServerConfig.enableGarbageCollector()) {
            Log.logOut(this, "Starting 1 threads for GARBAGE COLLECTION", Log.getLineNumber());
            ServerStorageGarbageCollectorThread newT = new ServerStorageGarbageCollectorThread(sharedStorage, sharedServerConfig);
            newT.start();
        }
        
        if(si != null && sharedServerConfig.useBFT()){
            Log.logOut(this, "Starting BFT-SMaRt Server with id: "+serverIndex, Log.getLineNumber());
            BftServer bftS = new BftServer( sharedStorage, sharedServerConfig, serverIndex, slow, lie);
            
        }
        
        if(si != null && sharedServerConfig.controlled()){
            Log.logOut(this.getClass().getCanonicalName(), "Starting controller thread on port: "+(sharedServerConfig.getControllerPort()+serverIndex), Log.getLineNumber());
            ReplicaController rc = new ReplicaController((sharedServerConfig.getControllerPort()+serverIndex),slow, lie); 
            rc.start();
        }
       
    }
}

class ServerProxyThreadShortTermConn extends Thread {

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
        thMessageBroker = new MessageBrokerServer(scm);
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

                if (thInReq != null &&  (thStorage.getQoP(thInReq.getChannelTag())).equals(QoP.CFT)) {

                    Log.logDebug(this, "RECEIVED REQ: " + logger.getSpecificCounterValue("NREQS") + " ID: " + thInReq.getUniqueId() + " METHOD: " + thInReq.getMethod() + " OBJ ID: " + thInReq, Log.getLineNumber());
                    
                    //Servidor esta lento
                    if(slow){
                        Thread.sleep(100);
                    }
                    
                    
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
                
                if(lie){
                    thOutReq = new Request();
                    thOutReq.setClientId("LIE");
                    thOutReq.setOperationStatus(true);
                    thOutReq.setMethod(METHOD.POLL);
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
    
    boolean slow;
    boolean lie;

    public ServerProxyThreadLongTermConn(Storage sto, ServerSocket s, ConfigServerManager scm, int replicaId, boolean sl, boolean l) throws UnknownHostException, NoSuchAlgorithmException {
        thStorage = sto;
        thServerSocket = s;
        thMessageBroker = new MessageBrokerServer(scm);
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

                        if (thInReq != null &&  thStorage.getQoP(thInReq.getChannelTag()).equals(QoP.CFT)) {

                            Log.logDebug(this, "RECEIVED REQ: " + logger.getSpecificCounterValue("NREQS") + " ID: " + thInReq.getUniqueId() + " METHOD: " + thInReq.getMethod() + " OBJ ID: " + thInReq, Log.getLineNumber());
                            if(slow){
                                Thread.sleep(100);
                            }
                            
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
                        if(lie){
                            thOutReq = new Request();
                            thOutReq.setClientId("LIE");
                            thOutReq.setOperationStatus(true);
                            thOutReq.setMethod(METHOD.POLL);
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




class BftServer extends Thread implements SingleExecutable, Recoverable{
    private Storage storage;
    private int replicaId;
    private ServiceReplica serviceReplica;
    private Log logger;
    private MessageBrokerServer thMessageBroker;
    private ConfigServerManager configServer;
    private ReplicaContext rctx;
    
    private boolean slow;
    private boolean lie;
    
    public BftServer( Storage sto, ConfigServerManager scm, int replicaId, boolean sl, boolean l){
        this.storage = sto;
        this.replicaId = replicaId;
        this.configServer = scm;
        this.thMessageBroker = new MessageBrokerServer(scm);
        this.logger = new Log(100);
        this.slow = sl;
        this.lie = l;
        Log.logInfo(this, "LAUNCHING SERVICEREPLICA WITH ID: " +  this.replicaId + " CONFIGURATION PATH: "+configServer.getConfigPath() , Log.getLineNumber());
        this.serviceReplica = new ServiceReplica(replicaId, scm.getConfigPath(), this, this);
        
        
        
        
    }
    
    
    @Override
    public void run(){
        Log.logInfo(this, "LAUNCHING SERVICEREPLICA THREAD "+configServer.getConfigPath(), Log.getLineNumber());
        logger.initSpecificCounter("NREQS", 0);
        logger.initSpecificCounter("NREQSEVENTS", 0);
        logger.initSpecificCounter("NRETEVENTS", 0);
        logger.initSpecificCounter("NNULLINVOKES", 0);
        logger.initSpecificCounter("WORNGCONFIGS", 0);
        
    }
    
    
    private Request convertByteToRequest(byte[] bytes){
       
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
       
        ObjectInputStream is;
        try {
            is = new ObjectInputStream(in);
            return (Request)is.readObject();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(BftServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BftServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    
    @SuppressWarnings("UnusedAssignment")
    private byte[] convertRequestToByte(Request req){
         ByteArrayOutputStream out = new ByteArrayOutputStream();    
         ObjectOutputStream os = null;
        try {
            os = new ObjectOutputStream(out);
            os.writeObject(req);
            logger.incrementSpecificCounter("NRETEVENTS", req.getAllEvents().size());
            return out.toByteArray();
        } catch (IOException ex) {
            Logger.getLogger(BftServer.class.getName()).log(Level.SEVERE, null, ex);
        }
         
         return null;
    }
    
    @SuppressWarnings("UnusedAssignment")
    private Request resolveRequest(Request req) throws IOException, ClassNotFoundException, InterruptedException{
            Request response = new Request();
            response.setChannelTag(req.getChannelTag());
        
            ArrayList<Event> events = null;
            if(lie){
                Thread.sleep(100);
            }
            
            switch (req.getMethod()) {
                  case REGISTER:
                      if (thMessageBroker.register(req, storage)) {
                        response.setOperationStatus(true);
                      }else {
                        response.setOperationStatus(false);
                      }
                      break;
                  case SUBSCRIBE:
                      if (thMessageBroker.subscribe(req, storage)) {
                        response.setOperationStatus(true);
                      }else {
                        response.setOperationStatus(false);
                      }
                      break;
                  case PUBLISH:
                      if (thMessageBroker.publish(req, storage)) {
                        response.setOperationStatus(true);
                      }else {
                        response.setOperationStatus(false);
                      }
                      break;
                  case PUBLISH_WITH_CACHING:
                      if (thMessageBroker.publishWithCaching(req, storage)) {
                        response.setOperationStatus(true);
                      }else {
                        response.setOperationStatus(false);
                      }
                      break;
                  case POLL:
                      events = thMessageBroker.poll(req, storage);
                      if (events != null) {
                        response.addAllEvents(events);
                        response.setOperationStatus(true);
                      }else {
                        response.setOperationStatus(false);
                      }
                      break;
                  case POLL_EVENTS_FROM_CHANNEL:
                      events = thMessageBroker.pollEventsFromChannel(req, storage);
                      if (events != null) {
                        response.setOperationStatus(true);
                        response.addAllEvents(events);
                      }else {
                        response.setOperationStatus(false);
                      }
                      break;
                  case UNREGISTER:
                      if (thMessageBroker.unRegister(req, storage)) {
                        response.setOperationStatus(true);
                      }else {
                        response.setOperationStatus(false);
                      }
                      break;
                  case UNSUBSCRIBE:
                      if (thMessageBroker.unSubscribe(req, storage)) {
                        response.setOperationStatus(true);
                      }else {
                        response.setOperationStatus(false);
                      }
                      break;
                  case UNSUBSCRIBE_FROM_ALL_CHANNELS:
                      if (thMessageBroker.unSubscribeFromAllChannels(req, storage)) {
                        response.setOperationStatus(true);
                      }else {
                        response.setOperationStatus(false);
                      }
                      break;
                  case UNREGISTER_FROM_ALL_CHANNELS:
                      if (thMessageBroker.unRegisterFromAllChannels(req, storage)) {
                        response.setOperationStatus(true);
                      }else {
                        response.setOperationStatus(false);
                      }
                      break;
                  default:
                      response.setOperationStatus(false);
                      Log.logWarning(this, "METHOD " + req.getMethod() + " NOT SUPPORTED", Log.getLineNumber());
                      break;
            }

            response.setId(req.getId());
            response.setClientId(req.getClientId());
            response.setMethod(req.getMethod());
            if(lie){
               response = new Request();
               response.setClientId("LIE");
               response.setOperationStatus(true);
               response.setMethod(METHOD.POLL);
            }
            
            return response;
    }
    
    
    @Override
    public byte[] executeOrdered(byte[] command, MessageContext msgCtx) {
        
        Request response = new Request();
        Request req = null;
        
        if(command != null )
            req = convertByteToRequest(command);
        else
            Log.logInfo(BftServer.class.getCanonicalName(), "REQUEST VEIO A NULL", Log.getLineNumber());
        
        
        
        if(req == null){
            Log.logInfo(BftServer.class.getCanonicalName(), "ERRO NA CONVERSÃO DOS BYTES PARA REQUEST", Log.getLineNumber());
            logger.incrementSpecificCounter("NNULLREQSRECV", 1);
            response.setOperationStatus(false);
            response.setMethod(METHOD.NOT_DEFINED);
            return convertRequestToByte(response);
        }else{
            if(storage.getQoP(req.getChannelTag()).equals(QoP.BFT) && storage.getQoS(req.getChannelTag()).equals(QoSchannel.TOTAL_ORDER)){
                logger.incrementSpecificCounter("NREQS", 1);
                logger.incrementSpecificCounter("NREQSEVENTS", req.getNumberOfEventsToFetch());
                Log.logDebug(this, "RECEIVED REQ: " + logger.getSpecificCounterValue("NREQS") + " ID: " + req.getUniqueId() + " METHOD: " + req.getMethod() + " OBJ ID: " + req, Log.getLineNumber());
                try {
                        return convertRequestToByte((resolveRequest(req)));
                } catch (IOException ex) {
                    Log.logInfo(BftServer.class.getCanonicalName(), ex.toString(), Log.getLineNumber());
                } catch (ClassNotFoundException ex) {
                    Log.logInfo(BftServer.class.getCanonicalName(), ex.toString(), Log.getLineNumber());
                } catch (InterruptedException ex) {
                        Logger.getLogger(BftServer.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }else{
                Log.logInfo(BftServer.class.getCanonicalName(), "ERRO NAS CONFIGURAÇÕES DO CLIENTE", Log.getLineNumber());
                logger.incrementSpecificCounter("WORNGCONFIGS", 1);
                //response.setClientId(String.valueOf(replicaId));
                //response.setOperationStatus(false);
                response.setMethod(METHOD.WRONG_CONFIGURATIONS);
            }
               
        }
        response.setChannelTag(req.getChannelTag());
        response.setOperationStatus(false);
        response.setId(req.getId());
        response.setClientId(req.getClientId());
        response.setMethod(req.getMethod());
        return convertRequestToByte(response);
    }

    @Override
    public byte[] executeUnordered(byte[] command, MessageContext msgCtx) {
        
        Request response = new Request();
        Request req = null;
        
        if(command != null )
            req = convertByteToRequest(command);
        else
            Logger.getLogger(BftServer.class.getName()).log(Level.WARNING, null, "REQUEST VEIO A NULL");
        
        
        
        if(req == null){
            Log.logError(this.getClass().getCanonicalName(), "ERRO NA CONVERSÃO DOS BYTES PARA REQUEST", Log.getLineNumber());
            logger.incrementSpecificCounter("NNULLREQSRECV", 1);
            //response.setClientId(String.valueOf(replicaId));
            response.setOperationStatus(false);
            response.setMethod(METHOD.NOT_DEFINED);
            return convertRequestToByte(response);
        }else{
           // if(storage.getQoP(req.getChannelTag()).equals(QoP.BFT) && storage.getQoS(req.getChannelTag()).equals(QoSchannel.NO_ORDER)){
                logger.incrementSpecificCounter("NREQS", 1);
                logger.incrementSpecificCounter("NREQSEVENTS", req.getNumberOfEventsToFetch());
                Log.logDebug(this, "RECEIVED REQ: " + logger.getSpecificCounterValue("NREQS") + " ID: " + req.getUniqueId() + " METHOD: " + req.getMethod() + " OBJ ID: " + req, Log.getLineNumber());
                try {
                    return convertRequestToByte((resolveRequest(req)));
                } catch (IOException ex) {
                    Log.logOut(this, "INSIDE PRIMEIRO CATCH", replicaId);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(BftServer.class.getName()).log(Level.SEVERE, null, ex);   
                } catch (InterruptedException ex) {
                        Logger.getLogger(BftServer.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            /*}else{
                Logger.getLogger(BftServer.class.getName()).log(Level.SEVERE, null, "ERRO NAS CONFIGURAÇÕES DO CLIENTE");
                logger.incrementSpecificCounter("WORNGCONFIGS", 1);
                //response.setClientId(String.valueOf(replicaId));
                response.setOperationStatus(false);
                response.setMethod(METHOD.WRONG_CONFIGURATIONS);
            }*/
               
        }
        
        //response.setReplicaId(replicaId);
        response.setChannelTag(req.getChannelTag());
        response.setOperationStatus(false);
        response.setId(req.getId());
        response.setClientId(req.getClientId());
        response.setMethod(req.getMethod());
        return convertRequestToByte(response);
    }

    @Override
    public void setReplicaContext(ReplicaContext replicaContext) {
        System.out.println("REPLICA_CONTEXT");
        this.rctx = replicaContext;
    }

    @Override
    public ApplicationState getState(int eid, boolean sendState) {
        System.out.println("GET_STATE");
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int setState(ApplicationState state) {
        System.out.println("SET_STATE");
        throw new UnsupportedOperationException("Not supported yet.");
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
    /**
     * Esta class vai servir para controlar esta réplica
     */
    class ReplicaController extends Thread{
        ServerSocket theServerSocket; // socket para receber com
        int port; // port do servidir
        boolean slow;
        boolean lie;
        
        ReplicaController(int p, boolean s, boolean l) throws IOException{
            port = p;
            theServerSocket = new ServerSocket(port);
            slow = s;
            lie = l;
        }
        
        
        @Override
        public void run(){
            Socket client;
            ObjectOutputStream cOut = null;
            ObjectInputStream cIn = null;
            Command inCommand;
            Command outCommand;
            
            if(theServerSocket != null){
                while(true){
                    
                    client = null;
                    inCommand = null;
                    outCommand = null;
                    
                    try {
                        client = theServerSocket.accept();
                        Log.logInfo(this.getClass().getCanonicalName(), "Cliente com o ip: "+client.getInetAddress().getHostAddress()+" acabou de se connectar.", Log.getLineNumber());
                        
                        cIn = new ObjectInputStream(client.getInputStream());
                        cOut = new ObjectOutputStream(client.getOutputStream());
                      
                        while (inCommand == null && !(inCommand instanceof Command)) {
                               inCommand = (Command) cIn.readObject();
                        }
                        
                        
                    } catch (IOException ex) {
                        Log.logError(this.getClass().getCanonicalName(), "Erro ao aceitar com do cliente\n" + ex.toString(), Log.getLineNumber());
                    } catch (ClassNotFoundException ex) {
                        Log.logError(this.getClass().getCanonicalName(), "Erro ao ler pedido do cliente\n" + ex.toString(), Log.getLineNumber());
                      }
                    
                     
                    
                    
                    
                    if(inCommand != null){
                        switch(inCommand.getCommand()){
                            case KILL:
                                Log.logInfo(this.getClass().getCanonicalName(), "Killing replica", Log.getLineNumber());
                                
                                System.exit(0);
                                
                                break;
                            case SLOW:
                                if(slow == false)
                                    slow = true;
                                else
                                    slow = false;
                                break;
                            case LIE:
                                if(lie == false)
                                    lie = true;
                                else
                                    lie = false;
                                break;
                            case PING:
                                outCommand = new Command();
                                outCommand.setCommand(Define.ReplicaCommand.PONG);
                                try {
                                    cOut.writeObject(outCommand);
                                } catch (IOException ex) {
                                    Log.logError(this.getClass().getCanonicalName(), "ERRO AO ENVIAR PONG", Log.getLineNumber());
                                }
                                break;
                            default:
                                
                                break;
                        }
                        
                        
                        
                        
                    }else{
                        Log.logError(this.getClass().getCanonicalName(), "Erro ao aceitar com do cliente", Log.getLineNumber());
                    }
                    
                    
                    try {
                        client.close();
                    } catch (IOException ex) {
                        Log.logError(this.getClass().getCanonicalName(), "Erro ao fechar ligacao com o cliente", Log.getLineNumber());
                    }
                }//fim do while
            }else{
                Log.logError(this.getClass().getCanonicalName(), "Erro ao tentar iniciar o run do controlador da replica", Log.getLineNumber());
            }
        }
        
    
}