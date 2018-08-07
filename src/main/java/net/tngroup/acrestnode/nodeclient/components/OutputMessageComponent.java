package net.tngroup.acrestnode.nodeclient.components;

import net.tngroup.acrestnode.nodeclient.models.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class OutputMessageComponent {

    @Value("${node.type}")
    private String nodeType;

    private Logger logger = LogManager.getFormatterLogger("CommonLogger");

    private ChannelComponent channelComponent;
    private CipherComponent cipherComponent;

    @Autowired
    public OutputMessageComponent(ChannelComponent channelComponent,
                                  CipherComponent cipherComponent) {
        this.channelComponent = channelComponent;
        this.cipherComponent = cipherComponent;
    }

    /*
    Handler of message sending
     */
    private void sendMessage(Message message) {
        try {
            logger.info("Sending message to server: %s", message.getType());

            String msg = message.formJson();
            if (channelComponent.getKey() != null) msg = cipherComponent.encodeDes(msg, channelComponent.getKey());
            else msg = Base64.getEncoder().encodeToString(msg.getBytes());
            String result_msg = "-" + msg.length() + "-" + msg;

            while (channelComponent.getChannel() == null) Thread.sleep(1000);
            channelComponent.getChannel().writeAndFlush(result_msg);
        } catch (Exception e) {
            logger.error("Error during message sending: %s", e.getMessage());
        }
    }

    void sendMessageConfirm(Message inputMessage) {
        Message message = new Message(channelComponent.getCode(), "confirm", inputMessage.getType(), inputMessage.getId());
        sendMessage(message);
    }

    void sendMessageStatus(boolean status) {
        Message message = new Message(channelComponent.getCode(), "status", nodeType, status ? 1 : 0);
        sendMessage(message);
    }

    void sendMessageKeyRequest() {
        Message message = new Message(channelComponent.getCode(), "key request", nodeType, null);
        sendMessage(message);
    }

    void sendMessageSettingsRequest() {
        Message message = new Message(channelComponent.getCode(), "settings request", nodeType, null);
        sendMessage(message);
    }

}