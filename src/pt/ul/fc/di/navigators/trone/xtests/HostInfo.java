/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.xtests;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kreutz
 */
public class HostInfo {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        InetAddress ia;
        String hostname;
        try {
            ia = InetAddress.getLocalHost();
            hostname = ia.getHostName();
            int pos;

            System.out.println("My name is: " + ia.getHostName());
            System.out.println("My FQDN is: " + ia.getCanonicalHostName());
        } catch (Exception ex) {
            Logger.getLogger(HostInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
