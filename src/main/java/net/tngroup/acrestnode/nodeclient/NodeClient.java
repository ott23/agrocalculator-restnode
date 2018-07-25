package net.tngroup.acrestnode.nodeclient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.tngroup.acrestnode.nodeclient.components.InputMessageComponent;
import net.tngroup.acrestnode.nodeclient.components.StatusComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

@Component
@PropertySource("classpath:node.properties")
public class NodeClient {

    @Value("${node.server.host}")
    private String host;

    @Value("${node.server.port}")
    private int port;

    @Value("${node.server.local-port}")
    private int localPort;

    private StatusComponent statusComponent;
    private InputMessageComponent inputMessageComponent;

    @Autowired
    public NodeClient(StatusComponent statusComponent,
                      InputMessageComponent inputMessageComponent) {
        this.statusComponent = statusComponent;
        this.inputMessageComponent = inputMessageComponent;
    }

    public Bootstrap createBootstrap(Bootstrap bootstrap, EventLoopGroup eventLoopGroup) {

        if (bootstrap != null) {

            SocketAddress remoteAddress = new InetSocketAddress(host, port);

            bootstrap.group(eventLoopGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new NodeClientSocketInitializer(statusComponent, inputMessageComponent, this));
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);

            bootstrap.remoteAddress(remoteAddress);
            bootstrap.localAddress(localPort);
            bootstrap.connect().addListener(new NodeClientConnectionListener(this));
        }
        return bootstrap;
    }
}
