package net.tngroup.acrestnode.databases.cassandra.models;

import lombok.Data;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;

import java.util.UUID;

@Data
public class ClientEntity {

    @PrimaryKey
    private UUID id;

    private UUID client;
}
