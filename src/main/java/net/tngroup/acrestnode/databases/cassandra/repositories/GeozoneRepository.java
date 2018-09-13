package net.tngroup.acrestnode.databases.cassandra.repositories;

import net.tngroup.acrestnode.databases.cassandra.models.Geozone;
import org.springframework.data.cassandra.repository.AllowFiltering;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GeozoneRepository extends CassandraRepository<Geozone, UUID> {

    @AllowFiltering
    List<Geozone> findAllByName(String name);

    @AllowFiltering
    List<Geozone> findAllByClient(UUID id);

    Optional<Geozone> deleteGeozoneById(UUID id);
}
