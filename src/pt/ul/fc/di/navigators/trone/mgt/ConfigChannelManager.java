/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.mgt;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import pt.ul.fc.di.navigators.trone.data.Channel;
import pt.ul.fc.di.navigators.trone.utils.ConfigHandler;
import pt.ul.fc.di.navigators.trone.utils.Define.QoP;
import pt.ul.fc.di.navigators.trone.utils.Define.QoSchannel;
import pt.ul.fc.di.navigators.trone.utils.Log;

/**
 *
 * @author igor
 */
public class ConfigChannelManager {
    ConfigHandler handler;
    private QoP type;
    private QoSchannel order;
    
    public ConfigChannelManager(String path){
            handler = null;
        try {
            handler = new ConfigHandler(path);
            handler.readConfig();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ConfigChannelManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ConfigChannelManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        initiate();
    }
    
    private void initiate(){
        if(handler == null){
            
        }else{
            if(handler.getStringValue("type").equals("BFT"))
                type = QoP.BFT;
            else
                type = QoP.CFT;
            
            if(handler.getIntValue("totalOrder") == 1)
                order = QoSchannel.TOTAL_ORDER;
            else
                order = QoSchannel.NO_ORDER;
        }
    }
    
    
    public Channel generateChannel(String t, int id){
        Log.logOut(this, "GENERATING CHANNEL WITH TAG: "+t, id);
        return (new Channel(t.toLowerCase(), id, type, order));
        
        
    }
}
