/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author kreutz
 */
public class HashFunctions {

    public HashFunctions() {
    }

    public static String getHashMD5(String stringToHash) throws NoSuchAlgorithmException {
        byte[] messageDigest;
        StringBuilder hexString = new StringBuilder();

        MessageDigest md5Message = MessageDigest.getInstance("MD5");
        md5Message.update(stringToHash.getBytes());
        messageDigest = md5Message.digest();

        return convertToHexdecimal(messageDigest);
    }

    private static String convertToHexdecimal(byte[] data) {
        StringBuilder hexString = new StringBuilder();

        for (int i = 0; i < data.length; i++) {
            hexString.append(Integer.toHexString(0xFF & data[i]));
        }

        return hexString.toString();
    }

    public static String getHashSHA1(String stringToHash) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        byte[] messageDigest;
        MessageDigest sha1Message;

        sha1Message = MessageDigest.getInstance("SHA-1");
        sha1Message.update(stringToHash.getBytes("iso-8859-1"), 0, stringToHash.length());
        messageDigest = sha1Message.digest();

        return convertToHexdecimal(messageDigest);
    }
}
