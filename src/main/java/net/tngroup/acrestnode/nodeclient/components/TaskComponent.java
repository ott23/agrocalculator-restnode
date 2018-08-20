package net.tngroup.acrestnode.nodeclient.components;

import lombok.Getter;
import net.tngroup.acrestnode.node.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TaskComponent extends Thread {

    @Getter
    private List<String> taskList = new ArrayList<>();

    private Processor processor;

    @Autowired
    public TaskComponent(Processor processor) {
        this.processor = processor;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                handleTasks();
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void handleTasks() {
        List<String> bufferTaskList = new ArrayList<>(taskList);
        bufferTaskList.forEach((t) -> {
            taskList.remove(t);
            try {
                processor.doCommand(t);
            }catch (Exception e) {
                // handled in aspect
            }
        });
    }
}