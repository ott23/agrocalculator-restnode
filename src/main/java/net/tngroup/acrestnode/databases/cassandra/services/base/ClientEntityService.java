package net.tngroup.acrestnode.databases.cassandra.services.base;

import net.tngroup.acrestnode.databases.cassandra.models.base.ClientEntity;

import java.util.List;
import java.util.UUID;

public interface ClientEntityService<T extends ClientEntity>{

    List<T> getAllByClient(UUID client);

    T getById(UUID id);

    boolean deleteById(UUID id);
}
