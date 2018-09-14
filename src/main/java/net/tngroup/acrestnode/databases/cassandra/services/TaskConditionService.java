package net.tngroup.acrestnode.databases.cassandra.services;

import net.tngroup.acrestnode.databases.cassandra.models.TaskCondition;
import net.tngroup.acrestnode.databases.cassandra.models.TaskKey;

import java.util.Optional;

public interface TaskConditionService {

    TaskCondition getByHashCode(int hashCode);

    Optional<TaskCondition> getByTaskKey(TaskKey taskKey);

    TaskCondition save(TaskCondition taskCondition);

}
