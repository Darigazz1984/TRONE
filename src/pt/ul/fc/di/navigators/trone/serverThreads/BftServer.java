/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.serverThreads;

import bftsmart.tom.MessageContext;
import bftsmart.tom.ReplicaContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import pt.ul.fc.di.navigators.trone.comm.ServerProxy;
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
public class BftServer extends DefaultSingleRecoverable implements Runnable{
    private Storage storage;
    private int replicaId;
    private ServiceReplica serviceReplica;
    private Log logger;
    private MessageBrokerServer thMessageBroker;
    //private ConfigServerManager configServer;
    private String configPath; 
    private ReplicaContext rctx;
    private ServerProxy serverProxy;
    
    /**
     * 
     * @param sto Storage used by this thread
     * @param scm path to the configuration of BFT-SMaRt
     * @param replicaId the id of this replica
     * @param sp The controller of this server
     */
    public BftServer( Storage sto, /*ConfigServerManager*/ String scm, int replicaId, ServerProxy sp){
        this.storage = sto;
        this.replicaId = replicaId;
        this.configPath = scm;
        //this.configServer = scm;
        //this.thMessageBroker = new MessageBrokerServer(scm);
        this.thMessageBroker = new MessageBrokerServer();
        this.logger = new Log(100);
        this.serverProxy = sp;
        Log.logInfo(this, "LAUNCHING SERVICE REPLICA WITH ID: " +  this.replicaId + " CONFIGURATION PATH: "+configPath , Log.getLineNumber());
        //this.serviceReplica = new ServiceReplica(replicaId, scm.getConfigPath(), this, this);
        this.serviceReplica = new ServiceReplica(replicaId, configPath, this, this);
    }
    
    
    @Override
    public void run(){
        Log.logInfo(this, "RUNNING SERVICE REPLICA THREAD "+configPath, Log.getLineNumber());
        logger.initSpecificCounter("NREQS", 0);
        logger.initSpecificCounter("NREQSEVENTS", 0);
        logger.initSpecificCounter("NRETEVENTS", 0);
        logger.initSpecificCounter("NNULLINVOKES", 0);
        logger.initSpecificCounter("WORNGCONFIGS", 0);
        
    }
    
    
    private Request convertByteToRequest(byte[] bytes){
       
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        Request aux = null;
        ObjectInputStream is;
        try {
            is = new ObjectInputStream(in);
            aux = (Request) is.readObject();
            in.close();
            is.close();
            //return (Request)is.readObject();
        } catch (ClassNotFoundException ex) {
            Log.logError(this.getClass().getCanonicalName(), "Error converting from bytes to request", Log.getLineNumber());
        } catch (IOException ex) {
            Log.logError(this.getClass().getCanonicalName(), "Error converting from bytes to request", Log.getLineNumber());
        }
        return aux;
    }
    
    
    @SuppressWarnings("UnusedAssignment")
    private byte[] convertRequestToByte(Request req){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = null;
        byte [] result = null;
        try {
            os = new ObjectOutputStream(out);
            os.writeObject(req);
            logger.incrementSpecificCounter("NRETEVENTS", req.getAllEvents().size());
            result = out.toByteArray();
            out.close();
            os.close();
        } catch (IOException ex) {
            Log.logError(this.getClass().getCanonicalName(), "Error converting from request to bytes", Log.getLineNumber());
        } 
        return result;
    }
    
    @SuppressWarnings("UnusedAssignment")
    private Request resolveRequest(Request req) throws IOException, ClassNotFoundException, InterruptedException{
            Request response = new Request();
            response.setChannelTag(req.getChannelTag());
            Long sTime, time;
            ArrayList<Event> events = null;
            Thread.sleep(10);
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
            if(serverProxy.getLie()){
               response.setClientId("LIE");
            }
            
            if(serverProxy.getSlow()){
               Thread.sleep(serverProxy.getSleepTime()); 
            }
            
            return response;
    }
 
    @Override
    public byte[] appExecuteOrdered(byte[] command, MessageContext msgCtx) {
            
        Request response = new Request();
        Request req = null;
        
        if(command != null )
            req = convertByteToRequest(command);
        else
            Log.logError(this.getClass().getCanonicalName(), "NULL request", Log.getLineNumber());
        
        if(req == null){
            Log.logError(this.getClass().getCanonicalName(), "Error converting from bytes to request", Log.getLineNumber());
            logger.incrementSpecificCounter("NNULLREQSRECV", 1);
            response.setOperationStatus(false);
            response.setMethod(Define.METHOD.NOT_DEFINED);
            return convertRequestToByte(response);
        }else{
            if(storage.getQoP(req.getChannelTag()).equals(Define.QoP.BFT) && storage.getQoS(req.getChannelTag()).equals(Define.QoSchannel.TOTAL_ORDER)){
                logger.incrementSpecificCounter("NREQS", 1);
                logger.incrementSpecificCounter("NREQSEVENTS", req.getNumberOfEventsToFetch());
                Log.logDebug(this, "RECEIVED REQ: " + logger.getSpecificCounterValue("NREQS") + " ID: " + req.getUniqueId() + " METHOD: " + req.getMethod() + " OBJ ID: " + req, Log.getLineNumber());
                try {
                    return convertRequestToByte((resolveRequest(req)));
                } catch (IOException ex) {
                    Log.logError(this.getClass().getCanonicalName(), "Error returning request", Log.getLineNumber());
                } catch (ClassNotFoundException ex) {
                    Log.logError(this.getClass().getCanonicalName(), "Error returning request", Log.getLineNumber());
                } catch (InterruptedException ex) {
                    Log.logError(this.getClass().getCanonicalName(), "Error returning request", Log.getLineNumber());
                }
                
            }else{
                Log.logError(this.getClass().getCanonicalName(), "Client config error", Log.getLineNumber());
                logger.incrementSpecificCounter("WORNGCONFIGS", 1);
                //response.setClientId(String.valueOf(replicaId));
                //response.setOperationStatus(false);
                response.setMethod(Define.METHOD.WRONG_CONFIGURATIONS);
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
            Log.logError(this.getClass().getCanonicalName(), "NULL request", Log.getLineNumber());
        
        
        
        if(req == null){
            Log.logError(this.getClass().getCanonicalName(), "ERRO NA CONVERSÃO DOS BYTES PARA REQUEST", Log.getLineNumber());
            logger.incrementSpecificCounter("NNULLREQSRECV", 1);
            //response.setClientId(String.valueOf(replicaId));
            response.setOperationStatus(false);
            response.setMethod(Define.METHOD.NOT_DEFINED);
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
                    Log.logError(this.getClass().getCanonicalName(), "Error returning request", Log.getLineNumber());  
                } catch (InterruptedException ex) {
                    Log.logError(this.getClass().getCanonicalName(), "Error returning request", Log.getLineNumber());
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
        Log.logOut(this.getClass().getCanonicalName(), "Seting replica context", Log.getLineNumber());
        this.rctx = replicaContext;
    }

    @Override
    public void installSnapshot(byte[] state) {
        System.out.println("INSTALLING SNAPSHOT");
        Log.logOut(this.getClass().getCanonicalName(), "Installing new Snapshot", Log.getLineNumber());
        ByteArrayInputStream in;
        ObjectInputStream is;
        try {
            in = new ByteArrayInputStream(state);
            is = new ObjectInputStream(in);
            storage = (Storage) is.readObject();
            in.close();
            is.close();
        } catch (ClassNotFoundException ex) {
            Log.logError(this.getClass().getCanonicalName(), "Error converting from bytes to storage", Log.getLineNumber());
        } catch (IOException ex) {
            Log.logError(this.getClass().getCanonicalName(), "Error converting from bytes to storage", Log.getLineNumber());
        }      
        storage.listPublishersByChannel();
    }

    @Override
    public byte[] getSnapshot() {
        Log.logOut(this.getClass().getCanonicalName(), "Geting Snapshot", Log.getLineNumber());
        ByteArrayOutputStream out;
        ObjectOutputStream os = null;
        byte [] result = null;
        try {
            out = new ByteArrayOutputStream();
            os = new ObjectOutputStream(out);
            os.writeObject(storage);
            result = out.toByteArray();
            out.close();
            os.close();
        } catch (IOException ex) {
            Log.logError(this.getClass().getCanonicalName(), "Error converting from storage to bytes", Log.getLineNumber());
            
        } 
        return result;
    }
}
