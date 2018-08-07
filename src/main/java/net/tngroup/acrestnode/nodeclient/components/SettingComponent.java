package net.tngroup.acrestnode.nodeclient.components;

import lombok.Getter;
import lombok.Setter;
import net.tngroup.acrestnode.nodeclient.databases.h2.models.Setting;
import net.tngroup.acrestnode.nodeclient.databases.h2.services.SettingService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@PropertySource("classpath:settings.properties")
public class SettingComponent {

    @Value("${settings.list}")
    private String settingsString;

    private Logger logger = LogManager.getFormatterLogger("CommonLogger");

    private ChannelComponent channelComponent;
    private SettingService settingService;
    private OutputMessageComponent outputMessageComponent;

    @Getter
    @Setter
    private boolean isSettingReady = false;

    @Autowired
    public SettingComponent(ChannelComponent channelComponent,
                            SettingService settingService,
                            OutputMessageComponent outputMessageComponent) {
        this.channelComponent = channelComponent;
        this.settingService = settingService;
        this.outputMessageComponent = outputMessageComponent;
    }

    public void checkCode() {
        channelComponent.setCode(null);
        Setting nodeSetting = settingService.getByName("node.service.code");
        if (nodeSetting != null) channelComponent.setCode(nodeSetting.getValue());
        else {
            logger.info("Name did not exist, code was generated");
            channelComponent.setCode(UUID.randomUUID().toString());
            settingService.add(new Setting("node.service.code", channelComponent.getCode()));
        }
    }

    public void checkKey() {
        channelComponent.setKey(null);
        Setting keySetting = settingService.getByName("node.service.key");
        if (keySetting != null && !keySetting.getValue().equals("")) channelComponent.setKey(keySetting.getValue());
        else logger.info("Key did not exist");
    }

    public void checkStatus() {
        channelComponent.setStatus(false);
        Setting statusSetting = settingService.getByName("node.service.status");
        if (statusSetting != null) channelComponent.setStatus(Boolean.parseBoolean(statusSetting.getValue()));
    }

    void checkChannel() {
        channelComponent.setChannelReady(channelComponent.getCode() != null
                && channelComponent.getKey() != null
                && channelComponent.getChannel() != null);
    }

    public void checkSettings() {
        long nullSettingsCount = Arrays.stream(settingsString.split(","))
                .map(s -> settingService.getByName(s.trim()))
                .filter(s -> s == null || s.getValue() == null)
                .count();

        setSettingReady(true);
        if (nullSettingsCount > 0) {
            setSettingReady(false);
            logger.info("Settings are not valid");
        }

    }

    private void resetSettings() {
        setSettingReady(false);
        Arrays.stream(settingsString.split(",")).forEach(s -> settingService.updateByName(s.trim(), null));
    }

    void setSettings(List<Setting> settingList) {
        settingList.forEach(setting -> {
            if (Arrays.stream(settingsString.split(","))
                    .map(String::trim)
                    .collect(Collectors.toSet())
                    .contains(setting.getName())) settingService.updateByName(setting.getName(), setting.getValue());
        });
    }

    public boolean updateSettings() {
        try {
            resetSettings();
            outputMessageComponent.sendMessageSettingsRequest();

            int[] steps = {1, 2, 4};
            for (int i = 0; i < 3; i++) {
                Thread.sleep(steps[i] * 1000);
                checkSettings();
                if (isSettingReady) break;
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        checkSettings();
        return isSettingReady;
    }

}
