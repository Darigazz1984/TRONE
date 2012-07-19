/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.utils;

import java.util.ArrayList;
import pt.ul.fc.di.navigators.trone.data.Event;

/**
 *
 * @author kreutz
 */
public class ServerDataToReturn {
    private ArrayList listOfEvents;
    private String myValue;
    private boolean myStatus;
    
    public ServerDataToReturn() {
        listOfEvents = new ArrayList<Event>();
        myValue = null;
        myStatus = false;
    }
    
    public void setListOfEvents(ArrayList eventList) {
        if (!listOfEvents.isEmpty()) 
            listOfEvents.clear();
        listOfEvents.addAll(eventList);
    }
    
    public ArrayList getListOfEvents() {
        return listOfEvents;
    }
    
    public void appendValueToMessage(String value) {
        myValue = myValue.concat(value);
    }
     
    public void setValueToMessage(String value) {
        myValue = value;
    }
    
    public void setStatus(boolean status) {
        myStatus = status;
    }
    
    public String getValueOfMessage() {
        String str = new String();
        if (myStatus) {
            str = str.concat("[INFO]" + myValue);
        } else {
            str = str.concat("[WARNING]" + myValue);
        }
        return str;
    }
    
    public boolean getStatus() {
        return myStatus;
    }
    
    public boolean wasSuccessful() {
        return myStatus;
    }

    public boolean hasFailed() {
        return myStatus;
    }

    public boolean hasMessage() {
        if (myValue != null && hasFailed()) {
            return true;
        }
        return false;
    }

    public String getMessage() {
        if(myValue == null) {
            return "No error message provided inside the OperationStatus";
        }
        return myValue;
    }
    
    public void setMessage(String value) {
        setValueToMessage(value);
    }

    public void setEvent(Event globalEvent) {
        if (!listOfEvents.isEmpty()) 
            listOfEvents.clear();
        listOfEvents.add(globalEvent);
    }
}
