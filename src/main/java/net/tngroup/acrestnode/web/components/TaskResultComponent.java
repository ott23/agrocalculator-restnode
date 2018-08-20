package net.tngroup.acrestnode.web.components;

import net.tngroup.acrestnode.databases.cassandra.models.TaskKey;
import net.tngroup.acrestnode.databases.cassandra.models.TaskResult;
import net.tngroup.acrestnode.databases.cassandra.services.TaskResultService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@Lazy
public class TaskResultComponent {

    private KafkaComponent kafkaComponent;
    private TaskResultService taskResultService;

    private Thread taskResultThread;

    // Loggers
    private Logger logger = LogManager.getFormatterLogger("CommonLogger");

    @Autowired
    public TaskResultComponent(@Lazy KafkaComponent kafkaComponent,
                               @Lazy TaskResultService taskResultService) {
        this.kafkaComponent = kafkaComponent;
        this.taskResultService = taskResultService;

    }

    public void start() {
        if (taskResultThread != null) taskResultThread.interrupt();
        taskResultThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    handleTaskResults();
                    Thread.sleep(200);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        taskResultThread.start();
    }

    public void stop() {
        if (taskResultThread != null) taskResultThread.interrupt();
    }

    private void handleTaskResults() {
        Map<String, String> taskMap = kafkaComponent.read(false);
        taskMap.forEach((k, v) -> {
            String[] keyStringArray = k.split("/");
            TaskKey taskKey = new TaskKey(UUID.fromString(keyStringArray[0]), UUID.fromString(keyStringArray[1]));
            TaskResult taskResult = new TaskResult(taskKey, v);
            taskResultService.save(taskResult);
            logger.info("Task `%s`: message read from Kafka", k);
        });
    }
}
