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
public class BftUnorderedConnection implements ClientSideConnection{
    private String configPath;
    private int clientID;
    
    public BftUnorderedConnection(String cp, int cid){
        this.configPath = cp;
        this.clientID = cid;
    }
    
    public void start(){
        Log.logDebug(this.getClass().getCanonicalName(), "STARTING UNORDERED S-BFT CLIENT WITH ID: "+this.clientID+" AND CONFIGURATION PATH: "+this.configPath, Log.getLineNumber());
        //this.serverConnection = new ServiceProxy(this.clientID, this.configPath);
        
    }
    
    
    @Override
    public Request invoke(Request req) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
