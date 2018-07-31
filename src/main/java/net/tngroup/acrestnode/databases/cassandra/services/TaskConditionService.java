package net.tngroup.acrestnode.databases.cassandra.services;

import net.tngroup.acrestnode.databases.cassandra.models.TaskCondition;

public interface TaskConditionService {

    TaskCondition getByHashCode(int hashCode);

    TaskCondition save(TaskCondition taskCondition);

}
