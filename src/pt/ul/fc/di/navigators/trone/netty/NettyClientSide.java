/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.netty;

import java.util.List;
import java.util.concurrent.Semaphore;

/**
 *
 * @author igor
 */
public class NettyClientSide {
    private Semaphore sendSem;
    private int myID;
    private List<Server> theServers;
    
    
    
    public NettyClientSide(List<Server> ts){
        this.sendSem = new Semaphore(1);
        this.theServers = ts;
    }
    
    public void connect(){
        
    }
}
