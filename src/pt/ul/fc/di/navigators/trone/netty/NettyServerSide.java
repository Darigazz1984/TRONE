/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ul.fc.di.navigators.trone.netty;

import java.net.InetSocketAddress;
import static org.jboss.netty.channel.Channels.pipeline;
import java.util.concurrent.Executors;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import pt.ul.fc.di.navigators.trone.utils.Log;

/**
 *
 * @author igor
 */
public class NettyServerSide {
    private int port;
    private int myID;
    private Log log;
    private MessageHandler messageHandler;
    
    public NettyServerSide(int p, int mid, MessageHandler mh){
        this.port = p;
        this.myID = mid;
        this.messageHandler = mh;
        this.log = new Log(100);
        Log.logInfo(this, "Creating server on port: "+this.port+" with ID: "+this.myID, Log.getLineNumber());
    }
    
    public void run(){
        
        ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),Executors.newCachedThreadPool()));
        bootstrap.setOption("tcpNoDelay", true);
        bootstrap.setOption("keepAlive", true);
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
              public ChannelPipeline getPipeline() throws Exception {
                  ChannelPipeline p = pipeline();
                  NettyServerSideHandler nssh = new NettyServerSideHandler(myID, messageHandler);
                  p.addLast("decoder",new NettyObjectDecoder());
                  p.addLast("encoder", new NettyObjectEncoder());
                  p.addLast("handler", nssh);
                  return p;
                  //return Channels.pipeline(new ObjectEncoder(), new ObjectDecoder(), new ServerSideHandler(mh, myID));
              }
        });
        bootstrap.bind(new InetSocketAddress(port));
    }
}
