package net.tngroup.acrestnode.databases.cassandra.models;

import lombok.Data;

import java.util.Map;

@Data
public class Geozone extends ClientEntity{

    private String name;

    private Map<String, Object> geometry;

}
