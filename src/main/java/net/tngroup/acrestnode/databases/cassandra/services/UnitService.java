package net.tngroup.acrestnode.databases.cassandra.services;

import net.tngroup.acrestnode.databases.cassandra.models.Unit;

import java.util.List;
import java.util.UUID;

public interface UnitService {

    List<Unit> getAll();

    List<Unit> getAllByClient(UUID id);

    List<Unit> getAllByNameOrImei(String name, String imei);

    Unit getById(UUID id);

    void save(Unit unit);

    void deleteById(UUID id);

}
