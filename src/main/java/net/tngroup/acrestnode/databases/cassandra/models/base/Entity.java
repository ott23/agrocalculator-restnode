package net.tngroup.acrestnode.databases.cassandra.models.base;

import lombok.Data;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;

import java.util.UUID;

@Data
public class Entity {

    @PrimaryKey
    private UUID id;
}
