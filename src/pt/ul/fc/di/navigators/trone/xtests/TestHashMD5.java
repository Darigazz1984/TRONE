/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.xtests;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import pt.ul.fc.di.navigators.trone.utils.HashFunctions;
import pt.ul.fc.di.navigators.trone.utils.Log;

/**
 *
 * @author kreutz
 */
public class TestHashMD5 {

    
    public TestHashMD5() {
        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            // TODO code application logic here
            System.out.println("HASH SHA1 for string XUXU: " + HashFunctions.getHashSHA1("XUXU"));
            System.out.println("HASH MD5 for string XUXU: " + HashFunctions.getHashMD5("XUXU"));
            System.out.println("HASH SHA1 for string XUXU: " + HashFunctions.getHashSHA1("XUXU"));
            System.out.println("HASH MD5 for string XUXU: " + HashFunctions.getHashMD5("XUXU"));
            System.out.println("HASH SHA1 for string XUXU: " + HashFunctions.getHashSHA1("XUXU"));
            System.out.println("HASH MD5 for string XUXU: " + HashFunctions.getHashMD5("XUXU"));
            System.out.println("HASH SHA1 for string XUXU: " + HashFunctions.getHashSHA1("XUXU"));
            System.out.println("HASH MD5 for string XUXU: " + HashFunctions.getHashMD5("XUXU"));
            Log.logOut(TestHashMD5.class.getSimpleName(), "HASH MD5 for string XUXU: " + HashFunctions.getHashMD5("XUXU"), Log.getLineNumber());
            Log.logError(null, "test", Log.getLineNumber());
            Log.logOutFlush(null, "HASH MD5 for string XUXU: " + HashFunctions.getHashMD5("XUXU"), Log.getLineNumber());
            Log.logErrorFlush(null, "test", Log.getLineNumber());
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(TestHashMD5.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(TestHashMD5.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
