package pt.ul.fc.di.navigators.trone.xsimul;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import pt.ul.fc.di.navigators.trone.comm.ServerProxy;

/**
 *
 * @author kreutz
 */
public class CmdServerWithPoolOfThreads1 {
    
    public static void main(String[] args) {
        
        int replicaId = 1;
        
        try {
            ServerProxy serverProxy = new ServerProxy(replicaId);
            serverProxy.startWorkerThreadPools();
        } catch (Exception ex) {
            Logger.getLogger(CmdServerWithPoolOfThreads1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}