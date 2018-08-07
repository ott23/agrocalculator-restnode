package net.tngroup.acrestnode;

import net.tngroup.acrestnode.databases.cassandra.CassandraConnector;
import net.tngroup.acrestnode.nodeclient.components.ChannelComponent;
import net.tngroup.acrestnode.nodeclient.components.SettingComponent;
import net.tngroup.acrestnode.nodeclient.databases.h2.services.SettingService;
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

    public void doCommand(String command) {

        logger.info("Command received: `%s`", command);
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

    private void start() {
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

            logger.info("REST started");
        } catch (Exception e) {
            changeStatus("false");

            logger.info("REST didn't started: " + e.getMessage());
        }
    }

    private void stop() {
        cassandraConnector.close();

        changeStatus("false");

        taskResultComponent.stop();

        logger.info("REST stopped");
    }

    private void restart() {
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
