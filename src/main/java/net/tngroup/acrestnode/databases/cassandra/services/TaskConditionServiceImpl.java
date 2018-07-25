package net.tngroup.acrestnode.databases.cassandra.services;

import net.tngroup.acrestnode.databases.cassandra.models.TaskCondition;
import net.tngroup.acrestnode.databases.cassandra.models.TaskKey;
import net.tngroup.acrestnode.databases.cassandra.repositories.TaskConditionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class TaskConditionServiceImpl implements TaskConditionService {

    private TaskConditionRepository taskConditionRepository;

    @Autowired
    public TaskConditionServiceImpl(@Lazy TaskConditionRepository taskConditionRepository) {
        this.taskConditionRepository = taskConditionRepository;
    }


    @Override
    public void save(TaskCondition taskCondition) {
        taskConditionRepository.save(taskCondition);
    }
}
