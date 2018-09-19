package net.tngroup.acrestnode.databases.cassandra.models;

import lombok.Data;
import net.tngroup.acrestnode.databases.cassandra.models.base.ClientEntity;

@Data
public class Unit extends ClientEntity {

    private String name;

    private String imei;

}
