package net.tngroup.acrestnode.nodeclient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import io.netty.channel.SimpleChannelInboundHandler;
import net.tngroup.acrestnode.nodeclient.components.InputMessageComponent;
import net.tngroup.acrestnode.nodeclient.components.StatusComponent;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Sharable
public class NodeClientHandler extends SimpleChannelInboundHandler<String> {

    private StatusComponent statusComponent;
    private InputMessageComponent inputMessageComponent;
    private NodeClient nodeClient;

    public NodeClientHandler(StatusComponent statusComponent,
                             InputMessageComponent inputMessageComponent,
                             NodeClient nodeClient) {
        this.statusComponent = statusComponent;
        this.inputMessageComponent = inputMessageComponent;
        this.nodeClient = nodeClient;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        inputMessageComponent.readMessage(msg);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        statusComponent.connected(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        statusComponent.disconnected(ctx.channel());
        final EventLoop eventLoop = ctx.channel().eventLoop();
        eventLoop.schedule(() -> nodeClient.createBootstrap(new Bootstrap(), eventLoop), 5L, TimeUnit.SECONDS);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
