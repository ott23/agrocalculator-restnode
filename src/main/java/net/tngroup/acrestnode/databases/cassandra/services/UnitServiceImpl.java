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
    public List<Unit> getAllByClient(UUID id) {
        return unitRepository.findAllByClient(id);
    }

    @Override
    public List<Unit> getAllByImei(String imei) {
        return unitRepository.findAllByImei(imei);
    }

    @Override
    public Unit getById(UUID id) {
        return unitRepository.findById(id).orElse(null);
    }

    @Override
    public Unit save(Unit unit) {
        return unitRepository.save(unit);
    }

    @Override
    public boolean deleteById(UUID id) {
        return unitRepository.deleteUnitById(id).isPresent();
    }
}
