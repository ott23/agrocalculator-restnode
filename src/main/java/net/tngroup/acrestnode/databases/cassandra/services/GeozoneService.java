package net.tngroup.acrestnode.databases.cassandra.services;

import net.tngroup.acrestnode.databases.cassandra.models.Geozone;

import java.util.List;
import java.util.UUID;

public interface GeozoneService {

    List<Geozone> getAllByClient(UUID id);

    List<Geozone> getAllByName(String name);

    Geozone getById(UUID id);

    Geozone save(Geozone Geozone);

    boolean deleteById(UUID id);

}
