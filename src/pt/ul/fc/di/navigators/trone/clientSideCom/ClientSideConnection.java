/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.clientSideCom;

import pt.ul.fc.di.navigators.trone.data.Request;

/**
 *
 * @author igor
 */
public interface ClientSideConnection {
    public void start();
    public Request invoke(Request r);
    public void close();   
}
