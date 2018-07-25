package net.tngroup.acrestnode.databases.cassandra.services;

import net.tngroup.acrestnode.databases.cassandra.models.Client;

import java.util.UUID;

public interface ClientService {

    Client getById(UUID id);

    Client getByName(String name);

}
