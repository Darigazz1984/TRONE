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
import pt.ul.fc.di.navigators.trone.data.Request;
import pt.ul.fc.di.navigators.trone.mgt.MessageBrokerClient;

/**
 *
 * @author igor
 */
public class CmdSubscriberTeste {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException, UnknownHostException, NoSuchAlgorithmException, Exception {
        String channelTag = "apache";
        Request req = null;
        MessageBrokerClient cchm;
        
        cchm = new MessageBrokerClient(1, "subclientConfig.props");
        try {
            req = cchm.subscribe(channelTag);
        } catch (Exception ex) {
            Logger.getLogger(CmdPublishersTeste.class.getName()).log(Level.SEVERE, "ERRO AO SUBSCREVER", ex);
        }
        if(req != null && req.isOpSuccess()){
            System.out.println("A INICIAR");
            while(true){
                Request r = cchm.pollEventsFromChannel(channelTag, 0);
                System.out.println("NUMERO DE EVENTOS COLHIDOS:  "+r.getNumberOfEvents());
                Thread.sleep(1000);
            }
        }else
         cchm.closeConnection();   
    }
}
