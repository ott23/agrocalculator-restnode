package net.tngroup.acrestnode.databases.cassandra.models;

import lombok.Data;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;

import java.util.UUID;

@Data
public class Geozone {

    @PrimaryKey
    private UUID id;

    private UUID client;

    private String name;

    private String geometry;

}
