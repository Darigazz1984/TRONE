/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.xtests;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import pt.ul.fc.di.navigators.trone.data.Event;
import pt.ul.fc.di.navigators.trone.data.Request;
import pt.ul.fc.di.navigators.trone.utils.IdGenerator;
import pt.ul.fc.di.navigators.trone.utils.Size;

/**
 *
 * @author kreutz
 */
public class TestObjectSize {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        int limit = 1000;
        Request req = new Request();
        Event event = new Event();
            
        for (int i = 0; i < limit; i++) {
            try {
                req.addEvent(event);
                event = new Event();
                event.setContent(Integer.toString(i)+IdGenerator.getUniqueIdMD5());
            } catch (UnknownHostException ex) {
                Logger.getLogger(TestObjectSize.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(TestObjectSize.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        try {
            System.out.println("REQUEST SIZE: " + Size.serializableObjectSizeOf(req) + " EVENT SIZE: " + Size.serializableObjectSizeOf(event) + " TOTAL OF EVENTS: " + Size.serializableObjectSizeOf(event) * limit);
        } catch (IOException ex) {
            Logger.getLogger(TestObjectSize.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
