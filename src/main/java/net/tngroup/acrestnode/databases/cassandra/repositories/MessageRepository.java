package net.tngroup.acrestnode.databases.cassandra.repositories;

import net.tngroup.acrestnode.databases.cassandra.models.Message;
import net.tngroup.acrestnode.databases.cassandra.models.MessageKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface MessageRepository extends CassandraRepository<Message, MessageKey> {
    @Query("SELECT * FROM Message m WHERE m.unit = :unit AND time >= :startTime AND time <= :endTime")
    List<Message> findAllByKey(@Param("unit") String unit, @Param("startTime") Date startTime, @Param("endTime") Date endTime);
}