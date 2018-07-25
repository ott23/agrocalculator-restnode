package net.tngroup.acrestnode.nodeclient.components;

import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
public class ChannelComponent {

    @Getter
    @Setter
    private Channel channel = null;

    @Getter
    @Setter
    private String code = null;

    @Getter
    @Setter
    private String key = null;

    @Getter
    @Setter
    private boolean status = false;

    @Getter
    @Setter
    private boolean isChannelReady = false;

}
