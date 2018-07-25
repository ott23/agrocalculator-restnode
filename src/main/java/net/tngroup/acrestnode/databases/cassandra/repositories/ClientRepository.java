package net.tngroup.acrestnode.databases.cassandra.repositories;

import net.tngroup.acrestnode.databases.cassandra.models.Client;
import org.springframework.data.cassandra.repository.AllowFiltering;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends CassandraRepository<Client, UUID> {

    @AllowFiltering
    Optional<Client> findByName(String name);
}
