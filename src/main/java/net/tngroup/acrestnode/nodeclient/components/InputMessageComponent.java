package net.tngroup.acrestnode.nodeclient.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.tngroup.acrestnode.nodeclient.databases.h2.models.Setting;
import net.tngroup.acrestnode.nodeclient.databases.h2.services.SettingService;
import net.tngroup.acrestnode.nodeclient.models.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class InputMessageComponent {

    private Logger logger = LogManager.getFormatterLogger("CommonLogger");

    private ChannelComponent channelComponent;
    private SettingService settingService;
    private SettingComponent settingComponent;
    private OutputMessageComponent outputMessageComponent;
    private TaskComponent taskComponent;
    private CipherComponent cipherComponent;

    @Autowired
    public InputMessageComponent(ChannelComponent channelComponent,
                                 SettingService settingService,
                                 SettingComponent settingComponent,
                                 OutputMessageComponent outputMessageComponent,
                                 TaskComponent taskComponent,
                                 CipherComponent cipherComponent) {
        this.channelComponent = channelComponent;
        this.outputMessageComponent = outputMessageComponent;
        this.settingComponent = settingComponent;
        this.settingService = settingService;
        this.taskComponent = taskComponent;
        this.cipherComponent = cipherComponent;
    }

    /*
    Event of new message
    */
    private String messageCache = null;

    public void readMessage(String msg) {

        try {
            if (messageCache != null) {
                msg = messageCache + msg;
                messageCache = null;
            }

            Pattern p = Pattern.compile("-[0-9]+-");
            Matcher m = p.matcher(msg);

            while (m.find()) {
                String result_msg;
                String lengthString = msg.substring(m.start() + 1, m.end() - 1);
                int length = Integer.parseInt(lengthString);
                if (msg.length() == m.end() + length) {

                    result_msg = msg.substring(m.end(), m.end() + length);

                    Message message = base64Message(result_msg);
                    if (message == null) {
                        message = decMessage(result_msg);
                        message.setEncoded(true);
                    }

                    messageHandler(message);
                } else messageCache = msg.substring(m.start(), msg.length());
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error during message reading: %s", e.getMessage());
        }
    }

    /*
    If encodedKey is needed handler
     */
    private Message base64Message(String msg) {
        try {
            msg = new String(Base64.getDecoder().decode(msg));
            return new Message(msg);
        } catch (Exception e) {
            return null;
        }
    }

    /*
    Message decoder
     */
    private Message decMessage(String msg) throws Exception {
        try {
            msg = cipherComponent.decodeDes(msg, channelComponent.getKey());
            return new Message(msg);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new Exception("Unexpected error: decoding error");
        } catch (IOException e) {
            throw new Exception("Unexpected error: json parsing error");
        } catch (Exception e) {
            throw new Exception("Unexpected error: wrong message");
        }
    }

    /*
    Message handler
     */
    private void messageHandler(Message message) {
        logger.info("Message from server: %s", message.getType());

        // Key
        if (message.getType().equals("key") && !message.isEncoded()) {
            keyEvent(message);
            return;
        }
        // Wrong message
        if (message.getType().equals("wrong message") && !message.isEncoded()) {
            wrongMessageEvent();
            return;
        }
        // Command
        if (message.getType().equals("command") && message.isEncoded()) {
            commandEvent(message);
            return;
        }
        // Settings
        if (message.getType().equals("settings") && message.isEncoded()) {
            settingsEvent(message);
            return;
        }

        logger.info("Unknown type of message");
    }

    private void keyEvent(Message message) {
        settingService.updateByName("node.service.key", message.getValue());
        settingComponent.checkKey();
        settingComponent.checkChannel();
        outputMessageComponent.sendMessageConfirm(message);
    }

    private void wrongMessageEvent() {
        channelComponent.setKey(null);
        settingService.updateByName("node.service.key", "");
        outputMessageComponent.sendMessageKeyRequest();
    }

    private void commandEvent(Message message) {
        taskComponent.getTaskList().add(message.getValue());
        outputMessageComponent.sendMessageConfirm(message);
    }

    private void settingsEvent(Message message) {
        try {
            List<Setting> settingList = Arrays.asList(new ObjectMapper().readValue(message.getValue(), Setting[].class));
            settingComponent.setSettings(settingList);
            outputMessageComponent.sendMessageConfirm(message);
        } catch (IOException e) {
            logger.error("Error during settings reading");
        }
    }

}
