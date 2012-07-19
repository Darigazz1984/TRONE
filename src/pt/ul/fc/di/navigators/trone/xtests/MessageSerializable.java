/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.xtests;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

/**
 *
 * @author kreutz
 */
public class MessageSerializable implements Serializable {

    private String str;

    public MessageSerializable() {
        super();
    }

    public void MessageSerializable(String value) {
        str = value;
    }

    public String getMessage() {
        return str;
    }

    public void setMessage(String value) {
        str = value;
    }

    public void readObject(ObjectInput objectInput) throws ClassNotFoundException, IOException {
        this.str = (String) objectInput.readObject();
    }

    public void writeObject(ObjectOutput objectOutput) throws IOException {
        objectOutput.writeObject(str);
    }
}

