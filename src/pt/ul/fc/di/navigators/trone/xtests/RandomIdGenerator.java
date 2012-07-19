/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.xtests;

import java.util.logging.Level;
import java.util.logging.Logger;
import pt.ul.fc.di.navigators.trone.utils.IdGenerator;

/**
 *
 * @author kreutz
 */
public class RandomIdGenerator {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        for (long i = 0; i < 1000000000; i++) {
            try {
                System.out.println("LONG: " + IdGenerator.getUniqueIdLong());
                System.out.println("DOUBLE: " + IdGenerator.getUniqueIdString());
                System.out.println("MD5: " + IdGenerator.getUniqueIdMD5());
                System.out.println("SHA1: " + IdGenerator.getUniqueIdSHA1());
            } catch (Exception ex) {
                Logger.getLogger(RandomIdGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
