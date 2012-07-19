package pt.ul.fc.di.navigators.trone.apps;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.logging.Level;
import java.util.logging.Logger;
import pt.ul.fc.di.navigators.trone.comm.ServerProxy;

/**
 *
 * @author kreutz
 */
public class CmdServerWithPoolOfThreads {
    
    public static void main(String[] args) {
        
        int replicaId = 0;
        
        if (args.length > 0) {
            replicaId = Integer.parseInt(args[0]);
        }
        
        try {
            ServerProxy serverProxy = new ServerProxy(replicaId);
            serverProxy.startWorkerThreadPools();
        } catch (Exception ex) {
            Logger.getLogger(CmdServerWithPoolOfThreads.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}