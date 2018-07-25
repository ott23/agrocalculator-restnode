package net.tngroup.acrestnode.databases.cassandra.repositories;

import net.tngroup.acrestnode.databases.cassandra.models.TaskCondition;
import net.tngroup.acrestnode.databases.cassandra.models.TaskKey;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface TaskConditionRepository extends CassandraRepository<TaskCondition, TaskKey> {

}

