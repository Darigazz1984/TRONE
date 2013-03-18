/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.netty;

import pt.ul.fc.di.navigators.trone.data.Request;

/**
 *
 * @author igor
 */
public interface MessageHandler {
    public Request handleMessage(Request e);
}
