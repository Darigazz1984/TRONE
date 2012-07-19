/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

/**
 *
 * @author kreutz
 */
public class ConfigHandler {
    
    private String fileName;
    private Properties defaultProps;
    private Object nextElement;
    private Enumeration enumElements;

    public ConfigHandler(String fileName) throws FileNotFoundException, IOException {
        Log.logDebugFlush(this, "STARTING ...", Log.getLineNumber());
        
        this.fileName = fileName;
        defaultProps = new Properties();
        
        Log.logDebugFlush(this, "UP AND RUNNING ...", Log.getLineNumber());
    }
    
    public void readConfig() throws FileNotFoundException, IOException {  
        FileInputStream in = new FileInputStream(fileName);
        defaultProps.load(in);
        enumElements = defaultProps.propertyNames();
        in.close();
    }

    public void writeConfig() throws FileNotFoundException, IOException {
        FileOutputStream out = new FileOutputStream(fileName);
        defaultProps.store(out, "--- Key and Value file (ip = port) ---");
        out.close();
    }
    
    public Enumeration getProperties() {
        if (defaultProps.isEmpty()) {
            Log.logWarning(this, "There are NO properties! You have to use the readConfig() method first.", Log.getLineNumber());
        }
        return defaultProps.propertyNames();
    }
       
    public void setProperty(String key, String value) {
        defaultProps.setProperty(key, value);
        enumElements = defaultProps.propertyNames();
    }
    
    public String getProperty(String key) {
        if (defaultProps.containsKey(key)) {
            return defaultProps.getProperty(key);
        } 
        Log.logWarning(this, "property for key " + key + " NOT found", Log.getLineNumber());
        return null;
    }
       
    public String getFirstElement() {
        Enumeration e = getProperties();
        if (e.hasMoreElements()) {
            return e.nextElement().toString();
        }
        Log.logWarning(this, "NO element could be found", Log.getLineNumber());
        return null;
    }
    
    public String getFirstElementValue () {
        return getProperty(getFirstElement());
    }

    public String getNextElement() {
        if (enumElements != null) {
            if (enumElements.hasMoreElements()) {
                nextElement = enumElements.nextElement();
                return nextElement.toString();
            }
        }
        Log.logWarning(this, "there are NO more elements", Log.getLineNumber());
        return null;
    }
    
    public String getNextElementValue () {
        if (nextElement != null) {
            return getProperty(nextElement.toString());
        }
        Log.logWarning(this, "there are NO next elements", Log.getLineNumber());
        return null;
    }
    
    public String getElementValue (String key) {
       return defaultProps.getProperty(key);
    }
    
    public boolean hasMoreElements() {
        if (enumElements != null) {
            if (enumElements.hasMoreElements())
                return true;
        }
        return false;
    }
    
    public ArrayList getChannelTags() {
        ArrayList a = new ArrayList<String>();
        Enumeration e = getProperties();
        while (e.hasMoreElements()) {
            String key = e.nextElement().toString();
            String value = getElementValue(key);
            if (value.endsWith("-1")) {
                a.add(key);
            }
        }
        return a;
    }
    
    public void store() throws FileNotFoundException, IOException {
        writeConfig();
    }
    
    public void print() {
        Enumeration e = getProperties();
        while (e.hasMoreElements()) {
            String nextKey = (String) e.nextElement();
            System.out.println("Key: " + nextKey + " Value: " + getProperty(nextKey));
        }
    }
    
    public void printElements() {
        while (hasMoreElements()) {
            String nextKey = getNextElement();
            System.out.println("Key: " + nextKey + " Value: " + getProperty(nextKey));
        }
    }      
    
    public String getStringValue(String key) {
        String value;
        
        value = getElementValue(key);
        
        if (value == null) {
            Log.logWarning(this, "value for key " + key + " is NULL", Log.getLineNumber());
        }
        
        return value;
    }
    
    public int getIntValue(String key) {
        int value;
        
        try {
            value = Integer.parseInt(getElementValue(key));
        } catch (NumberFormatException ex) {
            Log.logWarning(this, "NumberFormatException for key " + key, Log.getLineNumber());
            value = 0;
        }
        
        return value;
    }
    
    public long getLongValue(String key) {
        long value;
        
        try {
            value = Long.parseLong(getElementValue(key));
        } catch (NumberFormatException ex) {
            Log.logWarning(this, "NumberFormatException for key " + key, Log.getLineNumber());
            value = 0;
        }
        
        return value;
    }
    
}
