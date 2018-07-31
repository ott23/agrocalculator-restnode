package net.tngroup.acrestnode.databases.cassandra.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;

import java.util.Date;

@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"key", "time", "hashCode"})
public class TaskCondition {

    @PrimaryKey
    private TaskKey key;

    private String topic;

    private String value;

    private int hashCode;

    private Date time;

    public TaskCondition(String topic, String value) {
        this.topic = topic;
        this.value = value;
        this.hashCode = hashCode();
    }

    public TaskCondition(TaskKey key, String topic, String value) {
        this.key = key;
        this.topic = topic;
        this.value = value;
        this.hashCode = hashCode();
    }

}
