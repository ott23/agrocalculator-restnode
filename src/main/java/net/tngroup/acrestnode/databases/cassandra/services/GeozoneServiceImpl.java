package net.tngroup.acrestnode.databases.cassandra.services;

import net.tngroup.acrestnode.databases.cassandra.models.Geozone;
import net.tngroup.acrestnode.databases.cassandra.repositories.GeozoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GeozoneServiceImpl implements GeozoneService {

    private GeozoneRepository geozoneRepository;

    @Autowired
    public GeozoneServiceImpl(@Lazy GeozoneRepository geozoneRepository) {
        this.geozoneRepository = geozoneRepository;
    }

    @Override
    public List<Geozone> getAllByClient(UUID id) {
        return geozoneRepository.findAllByClient(id);
    }

    @Override
    public List<Geozone> getAllByName(String name) {
        return geozoneRepository.findAllByName(name);
    }

    @Override
    public Geozone getById(UUID id) {
        return geozoneRepository.findById(id).orElse(null);
    }

    @Override
    public Geozone save(Geozone Geozone) {
        return geozoneRepository.save(Geozone);
    }

    @Override
    public boolean deleteById(UUID id) {
        return geozoneRepository.deleteGeozoneById(id).isPresent();
    }
}
