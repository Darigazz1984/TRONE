/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.apps;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import pt.ul.fc.di.navigators.trone.data.Event;
import pt.ul.fc.di.navigators.trone.data.Request;
import pt.ul.fc.di.navigators.trone.mgt.MessageBrokerClient;
import pt.ul.fc.di.navigators.trone.utils.Define;

/**
 *
 * @author igor
 */
public class CmdPublishersTeste {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException, UnknownHostException, NoSuchAlgorithmException, Exception {
        String channelTag = "apache";
        Request req = null;
        MessageBrokerClient cchm;
        
        cchm = new MessageBrokerClient(0, "pubclientConfig.props");
        try {
            req = cchm.register(channelTag);
        } catch (Exception ex) {
            Logger.getLogger(CmdPublishersTeste.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(req != null && req.isOpSuccess()){
            System.out.println("A INICIAR");
            while(true){
                Event e = new Event();
                e.setContent(System.currentTimeMillis()+"");
                cchm.publishWithCaching(e, channelTag);
                Thread.sleep(10);
            }
        }else
         cchm.closeConnection();   
        
    }
}
