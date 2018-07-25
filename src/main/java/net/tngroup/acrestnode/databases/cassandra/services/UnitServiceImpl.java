package net.tngroup.acrestnode.databases.cassandra.services;

import net.tngroup.acrestnode.databases.cassandra.models.Unit;
import net.tngroup.acrestnode.databases.cassandra.repositories.UnitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UnitServiceImpl implements UnitService {

    private UnitRepository unitRepository;

    @Autowired
    public UnitServiceImpl(@Lazy UnitRepository unitRepository) {
        this.unitRepository = unitRepository;
    }

    @Override
    public List<Unit> getAll() {
        return unitRepository.findAll();
    }

    @Override
    public List<Unit> getAllByClient(UUID id) {
        return unitRepository.findAllByClient(id);
    }

    @Override
    public List<Unit> getAllByNameOrImei(String name, String imei) {
        Set<Unit> unitSet = new HashSet<>();
        unitSet.addAll(unitRepository.findAllByName(name));
        unitSet.addAll(unitRepository.findAllByImei(imei));
        return new ArrayList<>(unitSet);
    }

    @Override
    public Unit getById(UUID id) {
        return unitRepository.findById(id).orElse(null);
    }

    @Override
    public void save(Unit unit) {
        unitRepository.save(unit);
    }

    @Override
    public void deleteById(UUID id) {
        unitRepository.deleteById(id);
    }
}
