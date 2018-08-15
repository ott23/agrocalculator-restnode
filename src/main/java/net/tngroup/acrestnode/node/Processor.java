package net.tngroup.acrestnode.node;

import net.tngroup.acrestnode.databases.cassandra.CassandraConnector;
import net.tngroup.acrestnode.nodeclient.components.ChannelComponent;
import net.tngroup.acrestnode.nodeclient.components.SettingComponent;
import net.tngroup.acrestnode.databases.h2.services.SettingService;
import net.tngroup.acrestnode.web.components.KafkaComponent;
import net.tngroup.acrestnode.web.components.TaskResultComponent;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Processor {

    private org.apache.logging.log4j.Logger logger = LogManager.getFormatterLogger("CommonLogger");

    private ChannelComponent channelComponent;
    private SettingComponent settingComponent;
    private SettingService settingService;
    private CassandraConnector cassandraConnector;
    private KafkaComponent kafkaComponent;
    private TaskResultComponent taskResultComponent;

    @Autowired
    public Processor(ChannelComponent channelComponent,
                     SettingComponent settingComponent,
                     SettingService settingService,
                     CassandraConnector cassandraConnector,
                     KafkaComponent kafkaComponent,
                     TaskResultComponent taskResultComponent) {
        this.channelComponent = channelComponent;
        this.settingComponent = settingComponent;
        this.settingService = settingService;
        this.cassandraConnector = cassandraConnector;
        this.kafkaComponent = kafkaComponent;
        this.taskResultComponent = taskResultComponent;
    }

    public boolean doCommand(String command) {
        boolean result = false;
        switch (command) {
            case "start":
                result = start();
                break;
            case "stop":
                result = stop();
                break;
            case "restart":
                result = restart();
                break;
            case "destroy":
                result = destroy();
                break;
            case "shutdown":
                result = close();
                break;
            default:
                break;
        }
        return result;
    }

    private boolean start() {
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
            return true;
        } catch (Exception e) {
            changeStatus("false");
            return false;
        }
    }

    private boolean stop() {
        cassandraConnector.close();
        taskResultComponent.stop();

        changeStatus("false");
        return true;

    }

    private boolean restart() {
        stop();
        start();
        return true;
    }

    private boolean destroy() {
        stop();
        settingService.deleteAll();
        close();
        return true;
    }

    private boolean close() {
        System.exit(0);
        return true;
    }

    private void changeStatus(String status) {
        settingService.updateByName("node.service.status", status);
        settingComponent.checkStatus();
    }
}

