/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.xtests;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * @author kreutz
 */

public class MessageExternalizable implements Externalizable {

    private String str;

    public MessageExternalizable() {
        super();
    }

    public MessageExternalizable(String value) {
        str = value;
    }

    public String getMessage() {
        return str;
    }

    public void setMessage(String value) {
        str = value;
    }

    @Override
    public void readExternal(ObjectInput objectInput) throws IOException, ClassNotFoundException {
        this.str = (String) objectInput.readObject();
    }

    @Override
    public void writeExternal(ObjectOutput objectOutput) throws IOException {
        objectOutput.writeObject(str);
    }
}

