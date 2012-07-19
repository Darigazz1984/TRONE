/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.utils;

import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 *
 * @author kreutz
 */
public class IdGenerator {

    public IdGenerator() throws UnknownHostException, NoSuchAlgorithmException {
    }

    public static String getUniqueIdLong() {
        return Long.toString(RandomNumbers.getLongRandomNumber());
    }
    
    public static String getUniqueIdString() {
        NumberFormat formatter = new DecimalFormat("###.####");  
        String f = formatter.format(RandomNumbers.getDoubleRandomNumber());  
        return f;
    }
    
    public static String getUniqueIdMD5() throws UnknownHostException, NoSuchAlgorithmException {
        return HashFunctions.getHashMD5(SystemInfo.getUniqueId() + RandomNumbers.getUniqueStringRandomValue());
    }

    public static String getUniqueIdSHA1() throws UnknownHostException, NoSuchAlgorithmException, UnsupportedEncodingException {
        return HashFunctions.getHashSHA1(SystemInfo.getUniqueId() + RandomNumbers.getUniqueStringRandomValue());
    }
    
}
