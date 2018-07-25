package net.tngroup.acrestnode.nodeclient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;

import java.util.concurrent.TimeUnit;

public class NodeClientConnectionListener implements ChannelFutureListener {

    private NodeClient nodeClient;

    NodeClientConnectionListener(NodeClient nodeClient) {
        this.nodeClient = nodeClient;
    }

    @Override
    public void operationComplete(ChannelFuture channelFuture) {
        if (!channelFuture.isSuccess()) {
            final EventLoop eventLoop = channelFuture.channel().eventLoop();
            eventLoop.schedule(() -> nodeClient.createBootstrap(new Bootstrap(), eventLoop), 5L, TimeUnit.SECONDS);
        }
    }

}
