package net.tngroup.acrestnode.databases.cassandra.repositories;

import net.tngroup.acrestnode.databases.cassandra.models.TaskKey;
import net.tngroup.acrestnode.databases.cassandra.models.TaskResult;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TaskResultRepository extends CassandraRepository<TaskResult, TaskKey> {

    @Query("SELECT * FROM TaskResult WHERE client = :client AND task = :task")
    Optional<TaskResult> findByClientAndTask(@Param("client") UUID client, @Param("task") UUID task);

}
