package net.tngroup.acrestnode.databases.cassandra.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;

import java.util.Date;

@Data
@NoArgsConstructor
public class TaskResult {

    @PrimaryKey
    private TaskKey key;

    private String value;

    private Date time;

    public TaskResult(TaskKey key, String value) {
        this.key = key;
        this.value = value;
    }

}
