/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.clientSideCom;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import pt.ul.fc.di.navigators.trone.data.Request;

/**
 *
 * @author igor
 */
public class RequestSerializer {
    
    public static Request convertByteToRequest(byte[] bytes){
       
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
       
        ObjectInputStream is;
        try {
            is = new ObjectInputStream(in);
            return (Request)is.readObject();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(RequestSerializer.class.getCanonicalName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RequestSerializer.class.getCanonicalName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static byte[] convertRequestToByte(Request req){
        ByteArrayOutputStream out = new ByteArrayOutputStream();    
        ObjectOutputStream os = null;
        try {
            os = new ObjectOutputStream(out);
            os.writeObject(req);
            //logger.incrementSpecificCounter("NRETEVENTS", req.getAllEvents().size());
            return out.toByteArray();
        } catch (IOException ex) {
            Logger.getLogger(RequestSerializer.class.getCanonicalName()).log(Level.SEVERE, null, ex);
        }
         
         return null;
    }
}
