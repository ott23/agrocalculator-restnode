package net.tngroup.acrestnode.databases.cassandra.services;

import net.tngroup.acrestnode.databases.cassandra.models.Unit;

import java.util.List;
import java.util.UUID;

public interface UnitService {

    List<Unit> getAllByClient(UUID id);

    List<Unit> getAllByName(String name);

    List<Unit> getAllByImei(String imei);

    Unit getById(UUID id);

    Unit save(Unit unit);

    void deleteById(UUID id);

}
