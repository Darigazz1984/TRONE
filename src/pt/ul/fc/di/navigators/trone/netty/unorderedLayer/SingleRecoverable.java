/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.netty.unorderedLayer;

import pt.ul.fc.di.navigators.trone.data.Request;

/**
 *
 * @author igor
 */
public interface SingleRecoverable {
    public Request executeUnordered(Request r);
    public byte[] getState();
    public void setState(byte[] s);
}
