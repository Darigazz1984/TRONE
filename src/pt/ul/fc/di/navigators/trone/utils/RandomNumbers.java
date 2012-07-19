/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.utils;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

/**
 *
 * @author kreutz
 */
public class RandomNumbers {

    public RandomNumbers () {
    }
    
    public static String getUniqueStringRandomValue() {
        StringBuilder sb = new StringBuilder();
        
        sb.append(getLongRandomString());
        sb.append(getDoubleRandomString());
        
        return sb.toString();
    }
    
    public static long getUniqueLongRandomNumber() {
        return (getLongRandomNumber() + getLongRandomNumber());
    }
    
    public static long getUniqueLongPositiveRandomNumber() {
        long randomNumber = (getLongRandomNumber() + getLongRandomNumber());
        if (randomNumber < 0)
            randomNumber *= -1;
        return randomNumber;
    }
    
    public static double getUniqueDoublePositiveRandomNumber() {
        double randomNumber = (getDoubleRandomNumber() + (double)getLongRandomNumber());
        if (randomNumber < 0.0)
            randomNumber *= -1;
        return randomNumber;
    }
    
    public static double getUniqueDoubleRandomNumber() {
        return (getDoubleRandomNumber() + (double)getLongRandomNumber());
    }
    
    public static long getLongRandomNumber () {
        long randomNumber;
        long longMod = 1000000000;
        Random rand = new Random();
        SecureRandom secRandom = new SecureRandom();
        BigInteger bigInt = new BigInteger(29, secRandom);
        
        rand.setSeed(CurrentTime.getTimeInMilliseconds() % longMod);
        
        randomNumber = (rand.nextLong() % longMod);
        randomNumber += (long) (Math.random() * longMod);
        randomNumber += (long) (rand.nextGaussian() * longMod);
        randomNumber += (long) (bigInt.longValue() % longMod);
        
        return randomNumber;
    }
    
    public static double getDoubleRandomNumber () {
        double randomNumber;
        double longMod = 1000000000000.0;
        Random rand = new Random();
        SecureRandom secRandom = new SecureRandom();
        BigInteger bigInt = new BigInteger(29, secRandom);
        
        rand.setSeed(CurrentTime.getTimeInMilliseconds() % (long)longMod);
        
        randomNumber = ((double)rand.nextLong() % longMod);
        randomNumber += (double) (Math.random() * longMod);
        randomNumber += (double) (rand.nextGaussian() * longMod);
        randomNumber += (double) (bigInt.longValue() % longMod);
        randomNumber += (Double.parseDouble(CurrentTime.getTimeInNanosecondsWithoutEndingZeros()) % longMod);
        
        return randomNumber;
    }
    
    public static String getLongRandomString () {
        long longMod = 1000000000;
        Random rand = new Random();
        SecureRandom secRandom = new SecureRandom();
        BigInteger bigInt = new BigInteger(29, secRandom);
        StringBuilder sb = new StringBuilder();
        
        rand.setSeed(CurrentTime.getTimeInMilliseconds() % longMod);
        
        sb.append((rand.nextLong() % longMod));
        sb.append((long) (Math.random() * longMod));
        sb.append((long) (rand.nextGaussian() * longMod));
        sb.append((long) (bigInt.longValue() % longMod));
        
        return sb.toString();
    }
    
    public static String getDoubleRandomString () {
        double longMod = 1000000000000.0;
        Random rand = new Random();
        SecureRandom secRandom = new SecureRandom();
        BigInteger bigInt = new BigInteger(29, secRandom);
        StringBuilder sb = new StringBuilder();
        
        rand.setSeed(CurrentTime.getTimeInMilliseconds() % (long)longMod);
        
        sb.append(((double)rand.nextLong() % longMod));
        sb.append((double) (Math.random() * longMod));
        sb.append((double) (rand.nextGaussian() * longMod));
        sb.append((double) (bigInt.longValue() % longMod));
        sb.append((Double.parseDouble(CurrentTime.getTimeInNanosecondsWithoutEndingZeros()) % longMod));
        
        return sb.toString();
    }
    
}
