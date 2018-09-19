package net.tngroup.acrestnode.databases.cassandra.services;

import net.tngroup.acrestnode.databases.cassandra.models.Geozone;
import net.tngroup.acrestnode.databases.cassandra.services.base.ClientEntityService;

import java.util.List;

public interface GeozoneService extends ClientEntityService<Geozone> {

    List<Geozone> getAllByName(String name);

    Geozone save(Geozone Geozone);

}
