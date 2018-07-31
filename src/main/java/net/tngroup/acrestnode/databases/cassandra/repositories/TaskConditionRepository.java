package net.tngroup.acrestnode.databases.cassandra.repositories;

import net.tngroup.acrestnode.databases.cassandra.models.TaskCondition;
import net.tngroup.acrestnode.databases.cassandra.models.TaskKey;
import org.springframework.data.cassandra.repository.AllowFiltering;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.Optional;

public interface TaskConditionRepository extends CassandraRepository<TaskCondition, TaskKey> {

    @AllowFiltering
    Optional<TaskCondition> findByHashCode(int hashCode);

}

