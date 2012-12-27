/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.serverThreads;

import bftsmart.statemanagment.ApplicationState;
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
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private ConfigServerManager configServer;
    private ReplicaContext rctx;
    private ServerProxy serverProxy;
    
    public BftServer( Storage sto, ConfigServerManager scm, int replicaId, ServerProxy sp){
        this.storage = sto;
        this.replicaId = replicaId;
        this.configServer = scm;
        this.thMessageBroker = new MessageBrokerServer(scm);
        this.logger = new Log(100);
        this.serverProxy = sp;
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
            Log.logError(this.getClass().getCanonicalName(), "Error converting from bytes to request", Log.getLineNumber());
        } catch (IOException ex) {
            Log.logError(this.getClass().getCanonicalName(), "Error converting from bytes to request", Log.getLineNumber());
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
            Log.logError(this.getClass().getCanonicalName(), "Error converting from request to bytes", Log.getLineNumber());
        }
         
         return null;
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
    
    /*
    @Override
    public byte[] executeOrdered(byte[] command, MessageContext msgCtx) {
        
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
    }*/

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
        System.out.println("REPLICA_CONTEXT");
        this.rctx = replicaContext;
    }

    @Override
    public void installSnapshot(byte[] state) {
      /*  System.out.println("STETING SNAPSHOT");
        ByteArrayInputStream bis = new ByteArrayInputStream(state);
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(bis);
            storage = (Storage)in.readObject();
            in.close();
            bis.close();
        } catch (ClassNotFoundException ex) {
            System.out.println("ERRO CLASS NOT FOUND EXCEPTION");
            Logger.getLogger(BftServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            System.out.println("ERRO IO EXCEPTION");
            Logger.getLogger(BftServer.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }

    @Override
    public byte[] getSnapshot() {
        /*System.out.println("GETING SNAPSHOT");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(storage);
            out.flush();
            out.close();
            bos.close();
            return bos.toByteArray();
        } catch (IOException ex) {
            System.out.println("ERRO AO FAZER GETSNAPSHOT");
            Logger.getLogger(BftServer.class.getName()).log(Level.SEVERE, null, ex);
            return new byte[0];
        }*/
        return null;
    }
}
