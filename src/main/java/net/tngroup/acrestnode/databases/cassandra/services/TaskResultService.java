package net.tngroup.acrestnode.databases.cassandra.services;

import net.tngroup.acrestnode.databases.cassandra.models.TaskKey;
import net.tngroup.acrestnode.databases.cassandra.models.TaskResult;

public interface TaskResultService {

    TaskResult getByKey(TaskKey key);

    void save(TaskResult taskResult);

}
