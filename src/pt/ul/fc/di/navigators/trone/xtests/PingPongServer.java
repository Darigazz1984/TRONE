/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.xtests;

/**
 *
 * @author smruti and kreutz
 */
import java.util.logging.Level;
import java.util.logging.Logger;
import pt.ul.fc.di.navigators.trone.data.Event;
import pt.ul.fc.di.navigators.trone.data.Request;
import pt.ul.fc.di.navigators.trone.utils.IdGenerator;
import pt.ul.fc.di.navigators.trone.utils.PingPong;


public class PingPongServer {

     /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String ip = "127.0.0.1";
        int port = 5000;
        int numberOfMessages = 10000;
        int sizeOfEachMessage = 200;
        int oneConnectionPerMessage = 0;

        if (args.length >= 4) {
            ip = args[0];
            port = Integer.parseInt(args[1]);
            numberOfMessages = Integer.parseInt(args[2]);
            sizeOfEachMessage = Integer.parseInt(args[3]);
            oneConnectionPerMessage = Integer.parseInt(args[4]);
        } else {
            System.err.println("WARNING: using default values IP: " + ip + " port: " + port + " nMessages: " + numberOfMessages + " sizeOfMessages: " + sizeOfEachMessage);
        }

        try {
            Request req = new Request();
            
            req.setClientId(IdGenerator.getUniqueIdMD5());
            req.setEvent(new Event());
            
            PingPong pp = new PingPong(ip, port, numberOfMessages, req);
            
            //PingPong pp = new PingPong(ip, port, numberOfMessages, sizeOfEachMessage);

            pp.server(true);
            
        } catch (Exception ex) {
            Logger.getLogger(PingPongClient.class.getName()).log(Level.SEVERE, null, ex);
        }


    }
}
