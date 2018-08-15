package net.tngroup.acrestnode.nodeclient.components;

import io.netty.channel.Channel;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class ChannelComponent {

    private Channel channel = null;

    private String code = null;

    private String key = null;

    private boolean status = false;

    private boolean isChannelReady = false;

}
