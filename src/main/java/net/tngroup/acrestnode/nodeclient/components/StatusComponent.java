package net.tngroup.acrestnode.nodeclient.components;

import io.netty.channel.Channel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StatusComponent {

    private Logger logger = LogManager.getFormatterLogger("CommonLogger");

    private ChannelComponent channelComponent;
    private SettingComponent settingComponent;
    private OutputMessageComponent outputMessageComponent;

    @Autowired
    public StatusComponent(ChannelComponent channelComponent,
                           SettingComponent settingComponent,
                           OutputMessageComponent outputMessageComponent) {
        this.channelComponent = channelComponent;
        this.settingComponent = settingComponent;
        this.outputMessageComponent = outputMessageComponent;
    }

    /*
    Event of connection
    */
    public void connected(Channel channel) {
        logger.info("Server with address '%s': connected", channel.remoteAddress().toString());
        channelComponent.setChannel(channel);
        settingComponent.checkChannel();

        if (channelComponent.getKey() == null) outputMessageComponent.sendMessageKeyRequest();
        else outputMessageComponent.sendMessageStatus(channelComponent.isStatus());
    }

    /*
    Event of disconnection
     */
    public void disconnected(Channel channel) {
        logger.info("Server with address '%s': disconnected", channel.remoteAddress().toString());
        channelComponent.setChannel(null);
        settingComponent.checkChannel();
    }
}
