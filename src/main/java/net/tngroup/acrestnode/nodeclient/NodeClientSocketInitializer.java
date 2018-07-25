package net.tngroup.acrestnode.nodeclient;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import net.tngroup.acrestnode.nodeclient.components.InputMessageComponent;
import net.tngroup.acrestnode.nodeclient.components.StatusComponent;
import org.springframework.stereotype.Service;

@Service
public class NodeClientSocketInitializer extends ChannelInitializer<SocketChannel> {

    private StatusComponent statusComponent;
    private InputMessageComponent inputMessageComponent;
    private NodeClient nodeClient;

    public NodeClientSocketInitializer(StatusComponent statusComponent,
                                       InputMessageComponent inputMessageComponent,
                                       NodeClient nodeClient) {
        this.statusComponent = statusComponent;
        this.inputMessageComponent = inputMessageComponent;
        this.nodeClient = nodeClient;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        ChannelPipeline pipeline = socketChannel.pipeline();

        //pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
        pipeline.addLast(new StringDecoder());
        pipeline.addLast(new StringEncoder());

        pipeline.addLast(new NodeClientHandler(statusComponent, inputMessageComponent, nodeClient));
    }
}
