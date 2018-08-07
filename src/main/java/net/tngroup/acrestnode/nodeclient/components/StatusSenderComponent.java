package net.tngroup.acrestnode.nodeclient.components;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StatusSenderComponent extends Thread {

    private ChannelComponent channelComponent;
    private OutputMessageComponent outputMessageComponent;

    @Autowired
    public StatusSenderComponent(OutputMessageComponent outputMessageComponent,
                                 ChannelComponent channelComponent) {
        this.channelComponent = channelComponent;
        this.outputMessageComponent = outputMessageComponent;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                if (channelComponent.isChannelReady())
                    outputMessageComponent.sendMessageStatus(channelComponent.isStatus());
                Thread.sleep(60 * 1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

