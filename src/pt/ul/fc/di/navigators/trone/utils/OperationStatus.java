/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.utils;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

/**
 *
 * @author kreutz
 */
public class OperationStatus implements Serializable {
    private String myValue;
    private boolean myStatus;
    
    public OperationStatus () {
        super();
        myValue = null;
        myStatus = false;
    }
    
    public OperationStatus (String value, boolean status) {
        myValue = value;
        myStatus = status;
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
        StringBuilder str = new StringBuilder();
        if (myStatus) {
            str.append("[INFO]" + myValue);
        } else {
            str.append("[WARNING]" + myValue);
        }
        return str.toString();
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
            return "No ERROR message provided inside the OperationStatus";
        }
        return myValue;
    }
    
    public void setMessage(String value) {
        setValueToMessage(value);
    }
    
     public void readObject(ObjectInput objectInput) throws ClassNotFoundException, IOException {
        this.myValue = (String) objectInput.readObject();
        this.myStatus = objectInput.readBoolean();
     }

    public void writeObject(ObjectOutput objectOutput) throws IOException {
        objectOutput.writeObject(myValue);
        objectOutput.writeBoolean(myStatus);
    }
 
}
