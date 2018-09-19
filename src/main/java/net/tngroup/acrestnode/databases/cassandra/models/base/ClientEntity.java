package net.tngroup.acrestnode.databases.cassandra.models.base;

import lombok.Data;

import java.util.UUID;

@Data
public class ClientEntity extends Entity{

   private UUID client;
}
