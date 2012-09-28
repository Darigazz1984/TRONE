package pt.ul.fc.di.navigators.trone.comm;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
/**
 *
 * @author kreutz
 */
import bftsmart.tom.ServiceProxy;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import pt.ul.fc.di.navigators.trone.data.Request;
import pt.ul.fc.di.navigators.trone.mgt.ConfigClientManager;
import pt.ul.fc.di.navigators.trone.mgt.ConfigNetManager;
import pt.ul.fc.di.navigators.trone.utils.Define.METHOD;
import pt.ul.fc.di.navigators.trone.utils.IdGenerator;
import pt.ul.fc.di.navigators.trone.utils.Log;
import pt.ul.fc.di.navigators.trone.utils.ServerInfo;

public class ClientProxy {
    
    
   
    private ServiceProxy longTermServiceProxy;
    private boolean order;
    private int clientID;
    static ConfigNetManager netConfig;
    static ConfigClientManager clientConfig;
    static RequestCache requestCache;
    private ServerInfo globalServerInfo;
    private int numberOfEventsToCachePerRequest;
    private int maxNumberOfEventsToFetchPerRequest;
    private boolean useSBFT;
    private boolean useCFT;
    private boolean useRequestCache;
    private int cacheCleanUpPeriodInNumberOfRequests;
    private int sbftRequestsCounter;
    private Log logger;
    

    public ClientProxy() throws FileNotFoundException, IOException {
        Random rand = new Random(System.currentTimeMillis());
        Log.logDebugFlush(this, "CLIENT PROXY STARTING ... ", Log.getLineNumber());
        netConfig = new ConfigNetManager("netConfig.props");
        clientID = (rand.nextInt(100)%9);
        //clientID = IdGenerator.getUniqueIdInt();
        
        clientConfig = new ConfigClientManager("clientConfig.props");
        int minNumberOfCopies = ((netConfig.getNumberOfServers() * clientConfig.getMajorityInPercentage()) / 100); // minimal number of copies for voting
        if (minNumberOfCopies >= netConfig.getNumberOfServers()) {
            minNumberOfCopies = netConfig.getNumberOfServers();
            Log.logWarning(this, "number of copies equal number of server (NO FAULT is being tolerated)", Log.getLineNumber());
        }

        requestCache = new RequestCache(minNumberOfCopies, clientConfig.getEventTimeToLiveInMilliseconds(), clientConfig.getTimeoutForReplicaCounterReset());
        useSBFT = clientConfig.useSBFT();
        useCFT = clientConfig.useCFT();
        order = clientConfig.useOrdered();
        if(order){
            Log.logDebug(this, "USING TOTAL ORDER", Log.getLineNumber());
        }else
            Log.logDebug(this, "NOT USING TOTAL ORDER", Log.getLineNumber());
        if (useCFT) {
            Log.logDebug(this, "USING CFT", Log.getLineNumber());
            if(clientConfig.useLongTermConnections()){
                setupConnection();
            }
        }else
            if(useSBFT){
                Log.logDebug(this, "USING SBFT", Log.getLineNumber());
                setupSBFTConnection();
            }else{
                Log.logError(this, "MODO DE OPERAÇÃO NÂO DEFINIDO. DEVE OPTAR POR BFT OU CFT.", Log.getLineNumber());
                System.exit(-1);
            }

        logger = new Log(100);

        globalServerInfo = null;

        cacheCleanUpPeriodInNumberOfRequests = clientConfig.getCacheCleanUpPeriodInNumberOfRequests();
        sbftRequestsCounter = 0;

        
        useRequestCache = true;
        
        numberOfEventsToCachePerRequest = clientConfig.getNumberOfEventsToCachePerRequest();
        maxNumberOfEventsToFetchPerRequest = clientConfig.getMaxNumberOfEventsToFetchPerRequest();
        if (numberOfEventsToCachePerRequest > maxNumberOfEventsToFetchPerRequest) {
            numberOfEventsToCachePerRequest = maxNumberOfEventsToFetchPerRequest;
        }

        Log.logDebugFlush(this, "CLIENT PROXY UP AND RUNNING ...", Log.getLineNumber());

    }

    private void setupConnection() throws UnknownHostException, IOException {
        netConfig.setupConnections();
    }
    
    private void setupSBFTConnection(){
        Log.logDebug(this, "STARTING SBFT CLIENT WITH ID: "+this.clientID+" AND CONFIGURATION PATH: "+clientConfig.getConfigPath(), Log.getLineNumber());
        longTermServiceProxy = new ServiceProxy (this.clientID, clientConfig.getConfigPath());
    }
    
    public void closeConnection() throws IOException{
        if(useCFT){
            netConfig.closeConnection();
        }else
            longTermServiceProxy.close();
    }
    
    private Request BftSendOrderedRequest(Request req){
        byte [] r = this.longTermServiceProxy.invokeOrdered(convertRequestToByte(req));
        return convertByteToRequest(r);
    }
    
    
    private Request BftSendUnorderedRequest(Request req){
        byte [] r = this.longTermServiceProxy.invokeUnordered(convertRequestToByte(req));
        return convertByteToRequest(r);
    }
    
    
        private Request convertByteToRequest(byte[] bytes){
       
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
       
        ObjectInputStream is;
        try {
            is = new ObjectInputStream(in);
            return (Request)is.readObject();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ClientProxy.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ClientProxy.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(ClientProxy.class.getName()).log(Level.SEVERE, null, ex);
        }
         
         return null;
    }

    private Request sendRequestToReplicaWithLongTerm(Request req) throws UnknownHostException, IOException, ClassNotFoundException, Exception {
      
        Request localReq = null;
        
        try {
            if (globalServerInfo.getSocket() != null) {

                // send a request to the server
                globalServerInfo.getOutputStreamForLongTerm().writeObject(req);
                globalServerInfo.getOutputStreamForLongTerm().flush();

                int tries = 0;
                // receive a responde from the server
                while ((localReq == null || !(localReq instanceof Request)) && tries < clientConfig.getNumberOfConnectionRetries()) {
                    localReq = (Request) globalServerInfo.getInputStreamForLongTerm().readObject();
                    tries++;
                }

                //NOTE: reset is important when re-using serializable objects
                globalServerInfo.getOutputStreamForLongTerm().reset();
                
                //logger.logInfoIfCounterReached(this, "    RECEIVED REQUEST: " + localReq.getUniqueId() + " SUCCESS: " + localReq.isOpSuccess() + " N EVENTS GOT: " + localReq.getAllEvents().size(), Log.getLineNumber());

                Log.logDebug(this, "RECEIVED REQ ID: " + localReq.getUniqueId() + " N_EVENTS: " + localReq.getAllEvents().size(), Log.getLineNumber());
            }
        } catch (Exception ex) {
            Log.logWarning(this, "problem with the connection to server " + globalServerInfo.getIP() + " at port " + globalServerInfo.getPortForLongTerm(), Log.getLineNumber());
        }

        return localReq;
    }

    private Request sendRequestToReplicaWithShortTerm(Request req) throws UnknownHostException, IOException, ClassNotFoundException, Exception {

        
        
        Request localReq = null;

        try {
            Socket localSocket;
            ObjectOutputStream localOut;
            ObjectInputStream localIn;
            localSocket = new Socket(globalServerInfo.getIP(), globalServerInfo.getPortForShortTerm());
            if (localSocket != null) {
                localOut = new ObjectOutputStream(localSocket.getOutputStream());
                localIn = new ObjectInputStream(localSocket.getInputStream());
                // send a request to the server
                localOut.writeObject(req);
                localOut.flush();

                int tries = 0;
                // receive a responde from the server
                while ((localReq == null || !(localReq instanceof Request)) && tries < clientConfig.getNumberOfConnectionRetries()) {
                    localReq = (Request) localIn.readObject();
                    tries++;
                }

                logger.logInfoIfCounterReached(this, "RECEIVED REQUEST: " + localReq.getUniqueId() + " SUCCESS: " + localReq.isOpSuccess() + " N EVENTS GOT: " + localReq.getAllEvents().size(), Log.getLineNumber());

                Log.logDebug(this, "RECEIVED REQ ID: " + localReq.getUniqueId() + " N EVENTS: " + localReq.getAllEvents().size(), Log.getLineNumber());
                
                localOut.close();
                localIn.close();
                localSocket.close();
            } else {
                Log.logWarning(this, "could not connect to SERVER: " + globalServerInfo.getIP() + " and PORT: " + globalServerInfo.getPortForShortTerm(), Log.getLineNumber());
            }
        } catch (Exception ex) {
            Log.logWarning(this, "problem with the connection to server " + globalServerInfo.getIP() + " at port " + globalServerInfo.getPortForLongTerm(), Log.getLineNumber());
        }

        return localReq;
    }

    public Request invoke(Request req) throws IOException, ClassNotFoundException, UnknownHostException, Exception {
        Request localReq = null;
        boolean useLongTerm = clientConfig.useLongTermConnections();

        logger.logInfoIfCounterReachedAndIncrement(this, "INVOKE Client ID: " + req.getClientId() + " method: " + req.getMethod() + " REQ ID: " + req.getUniqueId(), Log.getLineNumber());
        logger.logInfoIfCounterReached(this, "SENDING " + req.getAllEvents().size() + " EVENTS ", Log.getLineNumber());

        Log.logDebug(this, "REQ TO SEND UNIQUE ID: " + req.getUniqueId() + " REQ OBJ ID: " + req  + " METHOD: " + req.getMethod(), Log.getLineNumber());
        
        netConfig.resetServerListIterator();
        
        
        if (useSBFT) { // SBFT
           if(order){
               localReq = this.BftSendOrderedRequest(req);
            }else{
               localReq = this.BftSendUnorderedRequest(req);
           }
        } else { // CFT
             Request localLoopRequest = null;
             while (netConfig.hasMoreServers()) {
                globalServerInfo = netConfig.getNextServerInfo();

                logger.logInfoIfCounterReached(this, "NUMBER OF EVENTS TO FETCH: " + req.getNumberOfEventsToFetch(), Log.getLineNumber());

                
                if (useLongTerm) {
                    localLoopRequest = sendRequestToReplicaWithLongTerm(req);
                } else {
                    localLoopRequest = sendRequestToReplicaWithShortTerm(req);
                }
             }
             localReq = localLoopRequest;
            
           /* if (req.getMethod() == METHOD.PUBLISH || req.getMethod() == METHOD.PUBLISH_WITH_CACHING) {
                useRequestCache = false;
            }
            while (netConfig.hasMoreServers()) {
                globalServerInfo = netConfig.getNextServerInfo();

                logger.logInfoIfCounterReached(this, "NUMBER OF EVENTS TO FETCH: " + req.getNumberOfEventsToFetch(), Log.getLineNumber());

                if (useLongTerm) {
                    localReq = sendRequestToReplicaWithLongTerm(req);
                } else {
                    localReq = sendRequestToReplicaWithShortTerm(req);
                }
                
                if (localReq != null && useRequestCache) {
                    logger.logInfoIfCounterReached(this, "ADDING REQUEST tO CACHE: " + localReq.getUniqueId() + " SUCCESS: " + localReq.isOpSuccess() + " N_EVENTS GOT: " + localReq.getAllEvents().size(), Log.getLineNumber());

                    Log.logDebug(this, "REQ RECEIVED TO CACHE: " + localReq.getUniqueId() + " METHOD: " + req.getMethod() + " N_EVENTS: " + localReq.getNumberOfEvents(), Log.getLineNumber());

                    requestCache.addRequest(localReq);
                    
                } else {
                    Log.logDebug(this, "REQUEST ID " + localReq + " USE REQUEST CACHE IS " + useRequestCache, Log.getLineNumber());
                }
            }
            
            if (useRequestCache) {
                localReq = requestCache.getRequest(req.getUniqueId(), req.getNumberOfEventsToFetch());
                
                sbftRequestsCounter++;

                if (sbftRequestsCounter % cacheCleanUpPeriodInNumberOfRequests == 0) {
                    requestCache.dischargeOldRequests();
                    requestCache.currentStats();
                }
            }*/
           /* 
            useRequestCache = false;
            
            while (netConfig.hasMoreServers()) {
                globalServerInfo = netConfig.getNextServerInfo();

                logger.logInfoIfCounterReached(this, "NUMBER OF EVENTS TO FETCH: " + req.getNumberOfEventsToFetch(), Log.getLineNumber());

                Request localLoopRequest = null;
                if (useLongTerm) {
                    localLoopRequest = sendRequestToReplicaWithLongTerm(req);
                } else {
                    localLoopRequest = sendRequestToReplicaWithShortTerm(req);
                }
                if (localLoopRequest != null) {
                    logger.logInfoIfCounterReached(this, "RECEIVED REQUEST: " + localLoopRequest.getUniqueId() + " SUCCESS: " + localLoopRequest.isOpSuccess() + " N EVENTS GOT: " + localLoopRequest.getAllEvents().size(), Log.getLineNumber());

                    localReq = localLoopRequest;

                    if (!clientConfig.useAllReplicasOnCFT() || req.getMethod() == METHOD.POLL || req.getMethod() == METHOD.POLL_EVENTS_FROM_CHANNEL) {
                        break;
                    }
                    
                } else {
                    Log.logDebug(this, "NULL RESPONSE RECEIVED", Log.getLineNumber());
                }
            }*/
        }

        if (localReq == null) {
            Log.logWarning(this, "REQUEST FROM INVOKE IS RETURNING AS NULL", Log.getLineNumber());
        } else {
            Log.logDebug(this, "REQ RECEIVED UNIQUE ID: " + localReq.getUniqueId() + " METHOD: " + req.getMethod() + " N_EVENTS: " + localReq.getNumberOfEvents(), Log.getLineNumber());
        }

        return localReq;
    }

    public boolean isSBFT() {
        return useSBFT;
    }

    public int getNumberOfEventsToCachePerRequest() {
        return numberOfEventsToCachePerRequest;
    }

    public int getMaxNumberOfEventsToFetchPerRequest() {
        return maxNumberOfEventsToFetchPerRequest;
    }

    public void currentStats() {
        requestCache.currentStats();
    }

}