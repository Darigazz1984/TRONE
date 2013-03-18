/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.netty;

import java.net.ConnectException;
import java.nio.channels.ClosedChannelException;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import pt.ul.fc.di.navigators.trone.data.Request;
import pt.ul.fc.di.navigators.trone.utils.Log;

/**
 *
 * @author igor
 */
public class NettyServerSideHandler extends SimpleChannelHandler {
    private Log log;
    private int myID;
    private MessageHandler messagHandler;
    public NettyServerSideHandler(int mid, MessageHandler mh){
        this.log = new Log(100);
        this.myID = mid;
        this.messagHandler = mh;
    }
    
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
         Log.logDebug(this, "I AM: "+this.myID+" and I have received a message from address: "+e.getChannel().getRemoteAddress(), Log.getLineNumber());
         Request r = (Request)e.getMessage();
         if( r!= null){
            ctx.getChannel().write(this.messagHandler.handleMessage(r)); // we want to wait for a response from the above layer
         }else
             Log.logWarning(this, "Just received a null message", Log.getLineNumber());
    }
    
    @Override
    public void exceptionCaught( ChannelHandlerContext ctx, ExceptionEvent e) {
        //e.getCause().printStackTrace();
        if (!(e.getCause() instanceof ClosedChannelException) && !(e.getCause() instanceof ConnectException)) {
            Log.logWarning(this,"Connection closed", Log.getLineNumber());
        } else {
            e.getCause().printStackTrace(System.err);
            Log.logWarning(this,"ExceptionCaught "+e.getCause().toString(), Log.getLineNumber());
        }
    }
    
    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
        Log.logInfo(this, "Client with ip address: "+e.getChannel().getRemoteAddress()+" just connected", Log.getLineNumber());
    }
    
    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
        Log.logInfo(this, "Client with ip address: "+e.getChannel().getRemoteAddress()+" just disconneted", Log.getLineNumber());
    }
    
    @Override
    public void channelClosed( ChannelHandlerContext ctx, ChannelStateEvent e) {     
            Log.logInfo(this, "Channel with ip address: "+e.getChannel().getRemoteAddress()+" just closed", Log.getLineNumber());
    }
    
}
