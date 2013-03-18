/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.serverThreads;

import pt.ul.fc.di.navigators.trone.data.Request;
import pt.ul.fc.di.navigators.trone.netty.unorderedLayer.SingleRecoverable;

/**
 *
 * @author igor
 */
public class CftNoOrderServer implements SingleRecoverable {

    @Override
    public Request executeUnordered(Request r) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] getState() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setState(byte[] s) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
