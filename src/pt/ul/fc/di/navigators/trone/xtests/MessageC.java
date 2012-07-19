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
public class MessageC implements Serializable  {
    
    private String str;
    
    public MessageC() {
        super();
    }
    
    public MessageC(String value) {
        str = value;
    }
    
    public String getMessage() {
        return str;
    }
    
    public void setMessage(String value) {
        str = value;
    }
}
