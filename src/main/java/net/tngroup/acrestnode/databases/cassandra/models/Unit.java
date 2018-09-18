package net.tngroup.acrestnode.databases.cassandra.models;

import lombok.Data;

@Data
public class Unit extends ClientEntity{

    private String name;

    private String imei;

}
