/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.xtests;

import java.io.Serializable;

/**
 *
 * @author kreutz
 */
public class MessageS implements Serializable {

    private String str;

    public MessageS() {
        super();
    }

    public MessageS(String value) {
        str = value;
    }

    public String getMessage() {
        return str;
    }

    public void setMessage(String value) {
        str = value;
    }
}