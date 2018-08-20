package net.tngroup.acrestnode.node;

import net.tngroup.acrestnode.databases.cassandra.CassandraConnector;
import net.tngroup.acrestnode.databases.h2.services.SettingService;
import net.tngroup.acrestnode.nodeclient.components.ChannelComponent;
import net.tngroup.acrestnode.nodeclient.components.OutputMessageComponent;
import net.tngroup.acrestnode.nodeclient.components.SettingComponent;
import net.tngroup.acrestnode.web.components.KafkaComponent;
import net.tngroup.acrestnode.web.components.TaskResultComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Processor {

    private ChannelComponent channelComponent;
    private SettingComponent settingComponent;
    private SettingService settingService;
    private CassandraConnector cassandraConnector;
    private KafkaComponent kafkaComponent;
    private TaskResultComponent taskResultComponent;
    private OutputMessageComponent outputMessageComponent;

    @Autowired
    public Processor(ChannelComponent channelComponent,
                     SettingComponent settingComponent,
                     SettingService settingService,
                     CassandraConnector cassandraConnector,
                     KafkaComponent kafkaComponent,
                     TaskResultComponent taskResultComponent,
                     OutputMessageComponent outputMessageComponent) {
        this.channelComponent = channelComponent;
        this.settingComponent = settingComponent;
        this.settingService = settingService;
        this.cassandraConnector = cassandraConnector;
        this.kafkaComponent = kafkaComponent;
        this.taskResultComponent = taskResultComponent;
        this.outputMessageComponent = outputMessageComponent;
    }

    public void doCommand(String command) throws Exception {
        switch (command) {
            case "start":
                start();
                break;
            case "stop":
                stop();
                break;
            case "restart":
                restart();
                break;
            case "destroy":
                destroy();
                break;
            case "shutdown":
                close();
                break;
            default:
                break;
        }
    }

    private void start() throws Exception {
        try {
            if (channelComponent.isChannelReady()
                    && !settingComponent.updateSettings()) {
                throw new Exception("Settings are not loaded");
            }

            cassandraConnector.init();
            cassandraConnector.connect();

            kafkaComponent.init();
            kafkaComponent.testSocket();

            taskResultComponent.start();

            changeStatus("true");
            if (channelComponent.isChannelReady()) outputMessageComponent.sendMessageStatus(true);
        } catch (Exception e) {
            changeStatus("false");
            if (channelComponent.isChannelReady()) outputMessageComponent.sendMessageStatus(false);
            throw e;
        }
    }

    private void stop() {
        cassandraConnector.close();
        taskResultComponent.stop();

        changeStatus("false");
        if (channelComponent.isChannelReady()) outputMessageComponent.sendMessageStatus(false);
    }

    private void restart() throws Exception {
        stop();
        start();
    }

    private void destroy() {
        stop();
        settingService.deleteAll();
        close();
    }

    private void close() {
        System.exit(0);
    }

    private void changeStatus(String status) {
        settingService.updateByName("node.service.status", status);
        settingComponent.checkStatus();
    }
}

