/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.netty;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import pt.ul.fc.di.navigators.trone.clientSideCom.RequestSerializer;
import pt.ul.fc.di.navigators.trone.data.Request;

/**
 *
 * @author igor
 */
public class NettyObjectDecoder extends FrameDecoder {
    //TODO: ALL
    @Override
    protected Object decode(ChannelHandlerContext chc, Channel chnl, ChannelBuffer cb) throws Exception {
        
        if(cb.readableBytes()<4){
            return null;
        }
       
        int dataL = cb.getInt(cb.readerIndex());
       
        if(cb.readableBytes()<(4+dataL)){
            return null;
        }
       
        cb.skipBytes(4);
        
        byte[] data = new byte[dataL];
        
        cb.readBytes(data);
        
        Request r = RequestSerializer.convertByteToRequest(data);
        
        
        return r;
    }
    
}
