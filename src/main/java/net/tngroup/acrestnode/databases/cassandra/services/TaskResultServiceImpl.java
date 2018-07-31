package net.tngroup.acrestnode.databases.cassandra.services;

import net.tngroup.acrestnode.databases.cassandra.models.TaskKey;
import net.tngroup.acrestnode.databases.cassandra.models.TaskResult;
import net.tngroup.acrestnode.databases.cassandra.repositories.TaskResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class TaskResultServiceImpl implements TaskResultService {

    private TaskResultRepository taskResultRepository;

    @Autowired
    public TaskResultServiceImpl(@Lazy TaskResultRepository taskResultRepository) {
        this.taskResultRepository = taskResultRepository;
    }

    @Override
    public TaskResult getByKey(TaskKey key) {
        return taskResultRepository.findByClientAndTask(key.getClient(), key.getTask()).orElse(null);
    }

    @Override
    public void save(TaskResult taskResult) {
        taskResult.setTime(new Date());
        taskResultRepository.save(taskResult);
    }
}
