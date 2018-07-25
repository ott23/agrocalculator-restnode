package net.tngroup.acrestnode.databases.cassandra.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;

@Data
@AllArgsConstructor
public class TaskCondition {

    @PrimaryKey
    private TaskKey key;

    private String topic;

    private String value;

}
