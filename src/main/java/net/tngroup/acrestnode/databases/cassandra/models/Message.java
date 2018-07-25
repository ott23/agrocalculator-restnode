package net.tngroup.acrestnode.databases.cassandra.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;

import java.util.Map;

@Data
@NoArgsConstructor
public class Message {

    @PrimaryKey
    private MessageKey key;

    private Map<String, Double> coordinate;

    private Map<String, Double> geo;

    private Map<String, Double> sensors;

    private Map<String, String> params;

    private Map<String, String> other;
}