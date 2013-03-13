/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.clientSideCom;

import bftsmart.tom.ServiceProxy;
import pt.ul.fc.di.navigators.trone.data.Request;
import pt.ul.fc.di.navigators.trone.utils.Log;

/**
 *
 * @author igor
 */
public class CftOrderedConnection implements ClientSideConnection {
    private String configPath;
    private int clientID;
    private ServiceProxy serverConnection;
    
    public CftOrderedConnection(String cp, int cid){
        this.configPath = cp;
        this.clientID = cid;
        this.serverConnection = null;
    }
    
    public void start(){
        Log.logDebug(this.getClass().getCanonicalName(), "STARTING S-CFT CLIENT WITH ID: "+this.clientID+" AND CONFIGURATION PATH: "+this.configPath, Log.getLineNumber());
        this.serverConnection = new ServiceProxy(this.clientID, this.configPath);   
    }
    
    @Override
    public Request invoke(Request req) {
        return RequestSerializer.convertByteToRequest(this.serverConnection.invokeOrdered(RequestSerializer.convertRequestToByte(req)));
    }
    
    public void close(){
        this.serverConnection.close();
    }
    
}
