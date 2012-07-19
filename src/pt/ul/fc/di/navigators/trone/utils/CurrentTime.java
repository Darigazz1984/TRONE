/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.utils;

/**
 *
 * @author kreutz
 */
public class CurrentTime {

    public CurrentTime() {
    }

    public static String getTimeInNanosecondsWithoutEndingZeros() {
        Long lObj = new Long(getTimeInNanoseconds());
        String nano = lObj.toString();
        int nZeros = 0;
        for (int i = nano.length()-1; i >= 0 && nano.charAt(i) == '0'; i--) {
            nZeros++;
        }
        int newSize = nano.length() - nZeros;
        StringBuilder r = new StringBuilder( newSize );
        r.setLength( newSize );
        for (int i = nano.length()-1; i >= 0; i--) {
            char cur = nano.charAt(i);
            if (cur != '0')
                r.setCharAt( --newSize, cur );
        }
        return r.toString();
    }
    
    public static long getTimeInNanoseconds() {
        return System.nanoTime();
    }
    
    public static long getTimeInMilliseconds() {
        return System.currentTimeMillis();
    }
    
    public static long getTimeInSeconds() {
        return (getTimeInMilliseconds() / 1000);
    }
    
    public static long getTimeInMinutes() {
        return (getTimeInSeconds() / 60);
    }
    
    public static long getTimeInHours() {
        return (getTimeInMinutes() / 60);
    }
    
    public static long getTimeInDays() {
        return (getTimeInHours() / 24);
    }
    
    public static void showTimeInNanosecondsWithoutEndingZeros() {
        System.out.println("Current time in nanoseconds (without ending zeros) is " + getTimeInNanosecondsWithoutEndingZeros());
    }

    public static void showTimeInNanoseconds() {
        System.out.println("Current time in nanoseconds is " + getTimeInNanoseconds());
    }
    
    public static void showTimeInMilliseconds() {
        System.out.println("Current time in milliseconds is " + getTimeInMilliseconds());
    }
    
    public static void showTimeInSeconds() {
        System.out.println("Current time in seconds is " + getTimeInSeconds());
    }

    public static void showTimeInMinutes() {
        System.out.println("Current time in minutes is " + getTimeInMinutes());
    }
    
    public static void showTimeInHours() {
        System.out.println("Current time in hours is " + getTimeInHours());
    }
    
    public static void showTimeInDays() {
        System.out.println("Current time in days is " + getTimeInDays());
    }
}
