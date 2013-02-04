/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.comm;

/**
 *
 * @author kreutz
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import pt.ul.fc.di.navigators.trone.data.Command;
import pt.ul.fc.di.navigators.trone.data.Storage;
import pt.ul.fc.di.navigators.trone.mgt.ConfigChannelManager;
import pt.ul.fc.di.navigators.trone.mgt.ConfigServerManager;
import pt.ul.fc.di.navigators.trone.serverThreads.BftServer;
import pt.ul.fc.di.navigators.trone.serverThreads.ServerProxyThreadLongTermConn;
import pt.ul.fc.di.navigators.trone.serverThreads.ServerProxyThreadShortTermConn;
import pt.ul.fc.di.navigators.trone.utils.Define;
import pt.ul.fc.di.navigators.trone.utils.Log;
import pt.ul.fc.di.navigators.trone.utils.ServerInfo;

public class ServerProxy {

    static int serverIndex;
    static Storage sharedStorage; // Em principio este sera para separar
    static Storage cftStorage, bftStorage;//STORAGE SEPARADOS -> VERIFICAR SE DA PARA TER TOTAL E NORMAL SEPARADO
    static ServerSocket sharedServerSocketForShortTerm;
    static ServerSocket sharedServerSocketForLongTerm;
    static ConfigServerManager sharedServerConfig;
    static boolean sharedUseLongTermConn;
    static long sharedMessageTimeToLive;
    static int sharedReplicaId;
    static long sleepTime; //tempo que a replica vai esperar para responder a uma mensagem
    
    //FLAGS DE CONTROLO DA REPLCIA
    static boolean lie;
    static boolean slow;

    public ServerProxy(int replicaId) throws FileNotFoundException, IOException {
        lie = false;
        slow = false;
        sleepTime = 0;
        
        Log.logDebugFlush(this, "SERVER PROXY STARTING ...", Log.getLineNumber());
        sharedStorage = new Storage(replicaId); //OLHAR BEM PARA ISTO PARA DIVIDIR O STORAGE, CRIAR STORAGE BFT E CTF EM SEPARADO
        cftStorage = new Storage(replicaId); //Por agora estao
        bftStorage = new Storage(replicaId); //Por agora estao
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
        if(path != null){
            File folder = new File(path);
            File f[] = folder.listFiles();
            for(File fileEntry: f){
                Log.logInfo(this, "Starting creation of channel from file: "+fileEntry.getPath(), Log.getLineNumber());
                String tag = ((fileEntry.getName()).split("[.]"))[0];
                ConfigChannelManager ccm = new ConfigChannelManager(path+fileEntry.getName());
                if(ccm.isBFT()){
                    this.bftStorage.insertChannel(ccm.generateChannel(tag, serverIndex));
                    Log.logInfo(this, "channel with TAG: " + tag + " inserted in the BFT Storage", Log.getLineNumber());
                }else{
                    this.cftStorage.insertChannel(ccm.generateChannel(tag, serverIndex));
                    Log.logInfo(this, "channel with TAG: " + tag + " inserted in the CFT Storage", Log.getLineNumber());
                }
                //ESTE EM PRINCIPIO SERA PARA TIRAR 
                //sharedStorage.insertChannel(ccm.generateChannel(tag, serverIndex));
                //Log.logInfo(this, "channel with TAG: " + tag + " CREATED", Log.getLineNumber());
            }
        }else
            Log.logError(this.getClass().getCanonicalName(), "ERROR in channel config path", Log.getLineNumber());
    }
    
    public void lie(){
        if(lie){
            Log.logInfo(ServerProxy.class.getCanonicalName(), "STOPING BYZANTINE", Log.getLineNumber());
            lie = false;
        }
        else{
            Log.logInfo(ServerProxy.class.getCanonicalName(), "STARTING BYZANTINE", Log.getLineNumber());
            lie = true;
        }
        
    }
    
    public void slow(long st){
        if(slow){
            Log.logInfo(ServerProxy.class.getCanonicalName(), "STOPING SLOW", Log.getLineNumber());
            sleepTime = 0;
            slow = false;
        }else{
            Log.logInfo(ServerProxy.class.getCanonicalName(), "STARTING SLOW", Log.getLineNumber());
            sleepTime = st;
            slow = true;
        }
    }
    //ATENCAO QUE ESTE BLOCO DE LIE E SLOW SO ESTA PARA O BFTServer
    public boolean getLie(){
        return lie;
    }
    
    public boolean getSlow(){
        return slow;
    }

    public long getSleepTime(){
        return sleepTime;
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
            BftServer bftS = new BftServer( this.bftStorage, sharedServerConfig.getConfigPath(), serverIndex, this);
            
        }
        
        if(si != null && sharedServerConfig.controlled()){
            Log.logOut(this.getClass().getCanonicalName(), "Starting controller thread on port: "+(sharedServerConfig.getControllerPort()+serverIndex), Log.getLineNumber());
            ReplicaController rc = new ReplicaController((sharedServerConfig.getControllerPort()+serverIndex), this); 
            rc.start();
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
                //number = thStorage.removeAllOldPublishers(publisherTimeToLive, currentTime);
                  number = thStorage.removeAllOldPublishers(currentTime);
                Log.logInfo(this, "REMOVING: " + number + " old PUBLISHERS have been REMOVED", Log.getLineNumber());

                publisherLastCheck = currentTime;
            }
            if ((subscriberLastCheck + subscriberCleanerRoundPeriod) < currentTime) {
                //number = thStorage.removeAllOldSubscribers(subscriberTimeToLive, currentTime);
                  number = thStorage.removeAllOldSubscribers(currentTime);
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
        ServerProxy serverProxy;
        
        ReplicaController(int p, ServerProxy sp) throws IOException{
            port = p;
            theServerSocket = new ServerSocket(port);
            serverProxy = sp;
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
                        //Log.logInfo(this.getClass().getCanonicalName(), "Cliente com o ip: "+client.getInetAddress().getHostAddress()+" acabou de se connectar.", Log.getLineNumber());
                        Thread.sleep(50); // por alguma razao e necessario
                        cIn = new ObjectInputStream(client.getInputStream());
                        cOut = new ObjectOutputStream(client.getOutputStream());
                        while (inCommand == null || !(inCommand instanceof Command)){
                            inCommand = (Command) cIn.readObject();    
                        }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ReplicaController.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Log.logError(this.getClass().getCanonicalName(), "Erro ao aceitar com do cliente\n" + ex.toString(), Log.getLineNumber());
                    } catch (ClassNotFoundException ex) {
                        Log.logError(this.getClass().getCanonicalName(), "Erro ao ler pedido do cliente\n" + ex.toString(), Log.getLineNumber());
                    }
                    
                    
                    if(inCommand != null){
                        switch(inCommand.getCommand()){
                            case KILL:
                                Log.logInfo(this.getClass().getCanonicalName(), "KILLING REPLICA", Log.getLineNumber());
                                System.exit(0);
                                break;
                            case SLOW:
                               // Log.logInfo(this.getClass().getCanonicalName(), "SLOWING REPLICA", Log.getLineNumber());
                                serverProxy.slow(inCommand.getSleepTime());
                                break;
                            case LIE:
                               // Log.logInfo(this.getClass().getCanonicalName(), "BYZANTINE BEHAVIOUR", Log.getLineNumber());
                                serverProxy.lie();
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
                        cIn.close();
                        cOut.close();
                    } catch (IOException ex) {
                        Log.logError(this.getClass().getCanonicalName(), "Erro ao fechar ligacao com o cliente", Log.getLineNumber());
                    }
                }//fim do while
            }else{
                Log.logError(this.getClass().getCanonicalName(), "Erro ao tentar iniciar o run do controlador da replica", Log.getLineNumber());
            }
        }
        
    
}