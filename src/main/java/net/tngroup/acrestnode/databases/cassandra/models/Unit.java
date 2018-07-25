package net.tngroup.acrestnode.databases.cassandra.models;

import lombok.Data;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;

import java.util.UUID;

@Data
public class Unit {

    @PrimaryKey
    private UUID id;

    private String name;

    private String imei;

    private UUID client;

}
