package net.tngroup.acrestnode.databases.cassandra.services;

import net.tngroup.acrestnode.databases.cassandra.models.TaskCondition;
import net.tngroup.acrestnode.databases.cassandra.models.TaskKey;
import net.tngroup.acrestnode.databases.cassandra.repositories.TaskConditionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class TaskConditionServiceImpl implements TaskConditionService {

    private TaskConditionRepository taskConditionRepository;

    @Autowired
    public TaskConditionServiceImpl(@Lazy TaskConditionRepository taskConditionRepository) {
        this.taskConditionRepository = taskConditionRepository;
    }

    @Override
    public TaskCondition getByHashCode(int hashCode) {
        return taskConditionRepository.findByHashCode(hashCode).orElse(null);
    }

    @Override
    public Optional<TaskCondition> getByTaskKey(TaskKey taskKey) {
        return taskConditionRepository.findById(taskKey);
    }

    @Override
    public TaskCondition save(TaskCondition taskCondition) {
        taskCondition.setTime(new Date());
        return taskConditionRepository.save(taskCondition);
    }
}
