/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author kreutz
 */
public class Log implements Serializable{

    static boolean enableOut = true;
    static boolean enableInfo = true;
    static boolean enableWarning = true;
    static boolean enableError = true;
    static boolean enableDebug = false;

    long myOutCounter;
    long myInfoCounter;
    long myWarningCounter;
    long myErrorCounter;
    long myOutCounterLimit;
    long myInfoCounterLimit;
    long myErrorCounterLimit;
    long myWarningCounterLimit;
    ArrayList<AtomicLong> myAtomicCounter;
    HashMap<String, AtomicLong> mySpecificCounter;

    public Log() {
        init(0, 0, 0, 0);
    }

    public Log(long equalCounterLimit) {
        init(equalCounterLimit, equalCounterLimit, equalCounterLimit, equalCounterLimit);
    }

    public Log(long outCounterLimit, long infoCounterLimit, long warningCounterLimit, long errorCounterLimit) {
        init(outCounterLimit, infoCounterLimit, warningCounterLimit, errorCounterLimit);
    }

    private void init(long outCounterLimit, long infoCounterLimit, long warningCounterLimit, long errorCounterLimit) {
        myAtomicCounter = new ArrayList<AtomicLong>();
        for (int i = 0; i < 5; i++) {
            myAtomicCounter.add(new AtomicLong(0));
        }
        myOutCounter = 0;
        myInfoCounter = 0;
        myWarningCounter = 0;
        myErrorCounter = 0;
        myOutCounterLimit = outCounterLimit;
        myInfoCounterLimit = infoCounterLimit;
        myWarningCounterLimit = warningCounterLimit;
        myErrorCounterLimit = errorCounterLimit;
        mySpecificCounter = new HashMap<String, AtomicLong>();
    }
    
    public void initSpecificCounter(String counterName, long counterInitValue) {
        mySpecificCounter.put(counterName, new AtomicLong(counterInitValue));
    }

    public void incrementSpecificCounter(String counterName, long incrementValue) {
        if (mySpecificCounter.containsKey(counterName)) {
            mySpecificCounter.get(counterName).addAndGet(incrementValue);
        } else {
            Log.logDebug(this, "COUNTER ID " + counterName + " NOT FOUND", Log.getLineNumber());
        }
    }
    
    public long getSpecificCounterValue(String counterName) {
        if (mySpecificCounter.containsKey(counterName)) {
            return mySpecificCounter.get(counterName).longValue();
        } else {
            return -1;
        }
    }

    public void setCounterLimit(long equalCounterLimit) {
        setOutCounterLimit(equalCounterLimit);
        setInfoCounterLimit(equalCounterLimit);
        setWarningCounterLimit(equalCounterLimit);
        setErrorCounterLimit(equalCounterLimit);
    }

    public void setOutCounterLimit(long limit) {
        myOutCounterLimit = limit;
    }

    public void setInfoCounterLimit(long limit) {
        myInfoCounterLimit = limit;
    }

    public void setWarningCounterLimit(long limit) {
        myWarningCounterLimit = limit;
    }

    public void setErrorCounterLimit(long limit) {
        myErrorCounterLimit = limit;
    }

    public long getOutCounter() {
        return myOutCounter;
    }

    public long getInfoCounter() {
        return myInfoCounter;
    }

    public long getWarningCounter() {
        return myWarningCounter;
    }

    public long getErrorCounter() {
        return myErrorCounter;
    }

    private long incrementAndGetOutCounter(long increment) {
        myOutCounter += increment;
        return myOutCounter;
    }

    private long incrementAndGetInfoCounter(long increment) {
        myInfoCounter += increment;
        return myInfoCounter;
    }

    private long incrementAndGetWarningCounter(long increment) {
        myWarningCounter += increment;
        return myWarningCounter;
    }

    private long incrementAndGetErrorCounter(long increment) {
        myErrorCounter += increment;
        return myErrorCounter;
    }

    public static int getLineNumber() {
        return Thread.currentThread().getStackTrace()[2].getLineNumber();
    }

    private static String getClassName(Object o) {
        if (o != null) {
            if (o instanceof String) {
                return (String) o;
            } else {
                return o.getClass().getSimpleName();
            }
        } else {
            return getTheNameOfTheRunnigApp();
        }
    }

    public static void logDebug(Object o, String s, int lineNumber) {
        if (enableDebug)
            System.out.println("[DEBUG] " + s + " [" + Log.getClassName(o) + ":" + lineNumber + "]");
    }

    public static void logOut(Object o, String s, int lineNumber) {
        if (enableOut)
            System.out.println("[OUT] " + s + " [" + Log.getClassName(o) + ":" + lineNumber + "]");
    }

    public static void logError(Object o, String s, int lineNumber) {
        if (enableError)
            System.err.println("[ERROR] " + s + " [" + Log.getClassName(o) + ":" + lineNumber + "]");
    }

    public static void logWarning(Object o, String s, int lineNumber) {
        if (enableWarning)
            System.out.println("[WARNING] " + s + " [" + Log.getClassName(o) + ":" + lineNumber + "]");
    }

    public static void logInfo(Object o, String s, int lineNumber) {
        if (enableInfo) {
            System.out.println("[INFO] " + s + " [" + Log.getClassName(o) + ":" + lineNumber + "]");
        }
    }

    public static void logDebugFlush(Object o, String s, int lineNumber) {
        logDebug(o, s, lineNumber);
        System.out.flush();
    }
    
    public static void logOutFlush(Object o, String s, int lineNumber) {
        logOut(o, s, lineNumber);
        System.out.flush();
    }

    public static void logErrorFlush(Object o, String s, int lineNumber) {
        logError(o, s, lineNumber);
        System.err.flush();
    }

    public static void logWarningFlush(Object o, String s, int lineNumber) {
        logWarning(o, s, lineNumber);
        System.out.flush();
    }

    public static void logInfoFlush(Object o, String s, int lineNumber) {
        logInfo(o, s, lineNumber);
        System.out.flush();
    }

    public void logIfAtomicCounterReachedAndIncrement(int counterId, String type, Object o, String s, int lineNumber) {
        myAtomicCounter.get(counterId).incrementAndGet();
        logIfAtomicCounterReached(counterId, type, o, s, lineNumber);
    }
    
    public void logIfAtomicCounterReached(int counterId, String type, Object o, String s, int lineNumber) {
        long value = myAtomicCounter.get(counterId).longValue();
        if (type.equals("INFO")) {
            if (value % myInfoCounterLimit == 0) {
                logInfo(o, s, lineNumber);
            }
        } else if (type.equals("WARNING")) {
            if (value % myWarningCounterLimit == 0) {
                logWarning(o, s, lineNumber);
            }
        } else if (type.equals("ERROR")) {
            if (value % myErrorCounterLimit == 0) {
                logError(o, s, lineNumber);
            }
        } else {
            if (value % myOutCounterLimit == 0) {
                logOut(o, s, lineNumber);
            }
        }
    }

    public void logOutIfCounterReached(Object o, String s, int lineNumber) {
        if (getOutCounter() % myOutCounterLimit == 0) {
            logOut(o, s, lineNumber);
        }
    }

    public void logInfoIfCounterReached(Object o, String s, int lineNumber) {
        if (getInfoCounter() % myInfoCounterLimit == 0) {
            logInfo(o, s, lineNumber);
        }
    }

    public void logWarningIfCounterReached(Object o, String s, int lineNumber) {
        if (getWarningCounter() % myWarningCounterLimit == 0) {
            logWarning(o, s, lineNumber);
        }
    }

    public void logErrorIfCounterReached(Object o, String s, int lineNumber) {
        if (getErrorCounter() % myErrorCounterLimit == 0) {
            logError(o, s, lineNumber);
        }
    }

    public void logOutIfCounterReachedAndIncrement(Object o, String s, int lineNumber) {
        if (incrementAndGetOutCounter(1) % myOutCounterLimit == 0) {
            logOut(o, s, lineNumber);
        }
    }

    public void logInfoIfCounterReachedAndIncrement(Object o, String s, int lineNumber) {
        if (incrementAndGetInfoCounter(1) % myInfoCounterLimit == 0) {
            logInfo(o, s, lineNumber);
        }
    }

    public void logWarningIfCounterReachedAndIncrement(Object o, String s, int lineNumber) {
        if (incrementAndGetWarningCounter(1) % myWarningCounterLimit == 0) {
            logWarning(o, s, lineNumber);
        }
    }

    public void logErrorIfCounterReachedAndIncrement(Object o, String s, int lineNumber) {
        if (incrementAndGetErrorCounter(1) % myErrorCounterLimit == 0) {
            logError(o, s, lineNumber);
        }
    }

    private static String getTheNameOfTheRunnigApp() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        StackTraceElement main = stack[stack.length - 1];
        String mainClass = main.getClassName();
        return mainClass;
    }

    public long getAtomicCounter(int counterId) {
        if (!myAtomicCounter.isEmpty() && myAtomicCounter.get(counterId) != null) {
            return myAtomicCounter.get(counterId).longValue();
        } else {
            return -1;
        }
    }

}
