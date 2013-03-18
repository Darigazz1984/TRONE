package pt.ul.fc.di.navigators.trone.netty;


import java.net.InetSocketAddress;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * This class represents a server in the client side
 * @author igor
 */
public class Server {
    private String address;
    private int port;
    private int serverId;
    
    /**
     * 
     * @param a address
     * @param p port
     * @param sID serverID
     */
    public Server(String a, int p, int sID){
        address = a;
        port = p;
        this.serverId = sID;
    }
    
    public InetSocketAddress getInetAddress(){
        return new InetSocketAddress(address, port);
    }
    
    public String getAddrees(){
        return this.address;
    }
    
    public int getPort(){
        return this.port;
    }
    
    public int getServerId(){
        return this.serverId;
    }
}
