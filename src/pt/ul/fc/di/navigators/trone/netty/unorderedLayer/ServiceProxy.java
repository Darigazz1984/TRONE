/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.netty.unorderedLayer;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;
import pt.ul.fc.di.navigators.trone.data.Event;
import pt.ul.fc.di.navigators.trone.data.Request;
import pt.ul.fc.di.navigators.trone.netty.MessageHandler;
import pt.ul.fc.di.navigators.trone.netty.NettyServerSide;
import pt.ul.fc.di.navigators.trone.utils.Define.METHOD;
import pt.ul.fc.di.navigators.trone.utils.Log;

/**
 *
 * @author igor
 */
public class ServiceProxy implements MessageHandler {
    
    private NettyServerSide nss; // The netty server
    private int myID; // the id of this replica
    private int port; // the port in wich connections will be received
    private SingleRecoverable singleRecoverable; // the object that will be called to handle messages
    private AtomicLong executionID; // the number of executions so far
    private Request[] theRequests; //TODO: THINK ABOUT THIS
    
    public ServiceProxy(int mid, int p, SingleRecoverable sr){
        
        this.executionID = new AtomicLong(0);
        this.myID = mid;
        this.port = p;
        if(sr != null){
            this.singleRecoverable = sr;
            nss = new NettyServerSide(this.myID, this.port, this);
        }else
            Log.logError(this, "Error, the singleRecoverable object is null", Log.getLineNumber());
    }
    
    public void run(){
        this.nss.run();
    }
    
    @Override
    synchronized public Request handleMessage(Request e) {
        //TODO: ALL ANALYZE THE REQUEST AND CHECK FOR WATH THIS MESSAGE IS AND CALL THE APROPRIATE METHOD
        // CHECK THE executionID
        Request response = null;
        byte[] state = null;
        switch(e.getMethod()){
            case GET_STATE:
                Event ev = new Event();
                ev.setClientId(""+this.myID);
                ev.setPayload(this.singleRecoverable.getState());
                response = new Request();
                response.setMethod(METHOD.APPLICATION_STATE);
                response.addEvent(ev);
                response.setOperationStatus(true);
                break;
            case GET_CURRENT_EID:
                break;
            case APPLICATION_STATE:
                ArrayList<Event> t = e.getAllEvents();
                this.singleRecoverable.setState(t.get(0).getPayload());
                break;
                
                
            default: response = this.singleRecoverable.executeUnordered(e);
        }
        this.executionID.incrementAndGet(); //maybe not in the reight place
        return response;
    }
    
}
