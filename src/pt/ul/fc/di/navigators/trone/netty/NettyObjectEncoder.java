/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.netty;

import org.jboss.netty.buffer.ChannelBuffer;
import static org.jboss.netty.buffer.ChannelBuffers.buffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import pt.ul.fc.di.navigators.trone.clientSideCom.RequestSerializer;
import pt.ul.fc.di.navigators.trone.data.Request;
import pt.ul.fc.di.navigators.trone.utils.Log;

/**
 *
 * @author igor
 */
public class NettyObjectEncoder extends SimpleChannelHandler {
     @Override
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        //Logger log = Logger.getLogger(this.getClass().getCanonicalName());
        Request r = (Request) e.getMessage();
        byte[] data = RequestSerializer.convertRequestToByte(r);
        ChannelBuffer buf = buffer(4+data.length);
        buf.writeInt((data.length));
        buf.writeBytes(data);
        Channel c = ctx.getChannel();
        if(c.isOpen() && c.isBound() && c.isConnected()  && c.isWritable()){
            Channels.write(ctx, e.getFuture(), buf);
        }else// This will not solve the problem but it will minimize it... in turn there should be not problem because the disconnection order is generated in the client so if it fails because the pipe is broken there should be no problem at all...
            Log.logError(this,"Channel for ip: "+c.getRemoteAddress().toString()+" is closed and I tried to write to it.", Log.getLineNumber() );
    }      
}
