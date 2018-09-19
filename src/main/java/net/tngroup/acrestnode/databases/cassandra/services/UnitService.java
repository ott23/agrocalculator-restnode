package net.tngroup.acrestnode.databases.cassandra.services;

import net.tngroup.acrestnode.databases.cassandra.models.Unit;
import net.tngroup.acrestnode.databases.cassandra.services.base.ClientEntityService;

import java.util.List;

public interface UnitService extends ClientEntityService<Unit> {

    List<Unit> getAllByImei(String imei);

    Unit save(Unit unit);

}
