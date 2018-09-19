package net.tngroup.acrestnode.databases.cassandra.models;

import lombok.Data;
import net.tngroup.acrestnode.databases.cassandra.models.base.ClientEntity;

import java.util.Map;

@Data
public class Geozone extends ClientEntity {

    private String name;

    private Map<String, Object> geometry;

}
