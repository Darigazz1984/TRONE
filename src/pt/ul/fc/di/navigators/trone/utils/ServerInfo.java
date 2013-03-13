/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.utils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 *
 * @author kreutz
 */
public class ServerInfo {
    private String myIP;
    private int myPortForShortTerm;
    private int myPortForLongTerm;
    private Socket mySocket;
    private ObjectOutputStream mySocketOut;
    private ObjectInputStream mySocketIn;
    
    public ServerInfo(String ip, int port) {
        myIP = ip;
        myPortForShortTerm = port;
        myPortForLongTerm = port + 1;
    }

    public ServerInfo() {
        myIP = null;
        myPortForShortTerm = 0;
        myPortForLongTerm = 0;
    }
    
    public void closeConnection() throws IOException{
        this.mySocketOut.close();
        this.mySocketIn.close();
        this.mySocket.close();
    }
    
    public String getIP() {
        return myIP;
    }
    
    public int getPortForShortTerm() {
        return myPortForShortTerm;
    }
    
    public int getPortForLongTerm() {
        return myPortForLongTerm;
    }
    
    public void setPorts(int port) {
        setPortForShortTerm(port);
        setPortForLongTerm(port+1);
    }
    
    public void setPortForShortTerm(int port) {
        myPortForShortTerm = port;
    }
    
     public void setPortForLongTerm(int port) {
        myPortForLongTerm = port;
    }
    
    public void setIP(String ip) {
        myIP = ip;
    }
    
    public void setConnectionForLongTerm() throws UnknownHostException, IOException {
        mySocket = new Socket(getIP(), getPortForLongTerm());
        if (mySocket != null) {
            mySocketOut = new ObjectOutputStream(mySocket.getOutputStream());
            mySocketIn = new ObjectInputStream(mySocket.getInputStream());
        } else {
            System.err.println("WARNING: could not setup connetion to IP: " + getIP() + " and PORT: " + getPortForLongTerm());
        }
    }
    
    public ObjectOutputStream getOutputStreamForLongTerm() { 
        return mySocketOut;
    }
    
    public ObjectInputStream getInputStreamForLongTerm() {
        return mySocketIn;
    }
    
    public Socket getSocket() {
        return mySocket;
    }
}
