/*
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.xtests;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kreutz
 */
public class TryToConnectToServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String ip = "127.0.0.1";
        int port = 5000;
        for (int i = 0; i < 5; i++) {
            Socket cSocket = null;
            try {
                cSocket = new Socket(ip, port);

            } catch (Exception ex) {
                Logger.getLogger(TryToConnectToServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (cSocket != null) {
                ObjectOutputStream cOut;
                try {
                    cOut = new ObjectOutputStream(cSocket.getOutputStream());
                    ObjectInputStream cIn = new ObjectInputStream(cSocket.getInputStream());

                    cOut.writeObject("www");
                    cOut.flush();

                    cOut.close();
                    cIn.close();
                    cSocket.close();
                } catch (IOException ex) {
                    Logger.getLogger(TryToConnectToServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                System.err.println("WARNING: could not connect to SERVER: " + ip + " and PORT: " + port);
            }
        }
    }
}
