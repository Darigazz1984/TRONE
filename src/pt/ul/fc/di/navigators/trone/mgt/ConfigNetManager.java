/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.mgt;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import pt.ul.fc.di.navigators.trone.utils.ConfigHandler;
import pt.ul.fc.di.navigators.trone.utils.Log;
import pt.ul.fc.di.navigators.trone.utils.ServerInfo;

/**
 *
 * @author kreutz
 */
public class ConfigNetManager {
    
    private ConfigHandler configHandler;
    private ArrayList<ServerInfo> serverList;
    private Iterator itServerList;
    
    public ConfigNetManager (String configFile) throws FileNotFoundException, IOException {
        
        Log.logDebugFlush(this, "CONFIG NET MANAGER: STARTING ...", Log.getLineNumber());
        
        configHandler = new ConfigHandler(configFile);
        configHandler.readConfig();
        serverList = new ArrayList<ServerInfo>();
        Enumeration e = configHandler.getProperties();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            ServerInfo si = new ServerInfo();
            si.setIP(key);
            si.setPorts(configHandler.getIntValue(key));
            serverList.add(si);
        }
        itServerList = serverList.iterator();
        
        Log.logDebugFlush(this, "CONFIG NET MANAGER: UP AND RUNNING ...", Log.getLineNumber());
        
    }
    
    public void resetServerListIterator() {
        itServerList = serverList.iterator();
    }
    
    public ServerInfo getFirstServerInfo() {
        return (ServerInfo) serverList.get(0);
    }
     
    public ServerInfo getNextServerInfo() {
        return (ServerInfo) itServerList.next();
    }
    
    public boolean hasMoreServers() {
        return itServerList.hasNext();
    }
        
    public int getNumberOfServers() {
        return serverList.size();
    }
    
    public void setupConnections() throws UnknownHostException, IOException {
        Iterator it = serverList.iterator();
        while (it.hasNext()) {
            ServerInfo si = (ServerInfo) it.next();
            si.setConnectionForLongTerm();
        }
    }
    
    public ServerInfo getServerInfoAtIndex(int myIndex) {
        if (myIndex < serverList.size()) {
            return (ServerInfo) serverList.get(myIndex);
        } else {
            System.out.println("WARNING: index " + myIndex + " OUT OF BOUND");
        }
        return null;
    }
    
    public void print() {
        configHandler.print();
    }
    
    public void closeConnection() throws IOException{
        for(ServerInfo server : serverList){
            server.closeConnection();
        }
        
    }
}