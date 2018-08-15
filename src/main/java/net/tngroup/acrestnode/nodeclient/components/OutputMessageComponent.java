package net.tngroup.acrestnode.nodeclient.components;

import net.tngroup.acrestnode.nodeclient.models.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class OutputMessageComponent {

    @Value("${node.type}")
    private String nodeType;

    private ChannelComponent channelComponent;
    private CipherComponent cipherComponent;

    @Autowired
    public OutputMessageComponent(ChannelComponent channelComponent,
                                  CipherComponent cipherComponent) {
        this.channelComponent = channelComponent;
        this.cipherComponent = cipherComponent;
    }

    void sendMessageConfirm(Message inputMessage) {
        Message outputMessage = new Message(channelComponent.getCode(), "confirm", inputMessage.getType(), inputMessage.getId());
        try {
            sendMessage(outputMessage);
        } catch (Exception e) {
            // Exception during message sending
        }

    }

    void sendMessageStatus(boolean status) {
        Message outputMessage = new Message(channelComponent.getCode(), "status", nodeType, status ? 1 : 0);
        try {
            sendMessage(outputMessage);
        } catch (Exception e) {
            // Exception during message sending
        }
    }

    void sendMessageKeyRequest() {
        Message outputMessage = new Message(channelComponent.getCode(), "key request", nodeType, null);
        try {
            sendMessage(outputMessage);
        } catch (Exception e) {
            // Exception during message sending
        }
    }

    void sendMessageSettingsRequest() {
        Message outputMessage = new Message(channelComponent.getCode(), "settings request", nodeType, null);
        try {
            sendMessage(outputMessage);
        } catch (Exception e) {
            // Exception during message sending
        }
    }

    /*
    Handler of message sending
     */
    private void sendMessage(Message message) throws Exception {
        String msg = message.formJson();

        if (channelComponent.getKey() != null) msg = cipherComponent.encodeDes(msg, channelComponent.getKey());
        else msg = Base64.getEncoder().encodeToString(msg.getBytes());
        String result_msg = "-" + msg.length() + "-" + msg;

        while (channelComponent.getChannel() == null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

        }
        channelComponent.getChannel().writeAndFlush(result_msg);
    }

}