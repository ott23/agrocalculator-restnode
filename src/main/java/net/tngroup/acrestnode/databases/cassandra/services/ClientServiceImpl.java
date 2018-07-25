package net.tngroup.acrestnode.databases.cassandra.services;


import net.tngroup.acrestnode.databases.cassandra.models.Client;
import net.tngroup.acrestnode.databases.cassandra.repositories.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ClientServiceImpl implements ClientService {

    private ClientRepository clientRepository;

    @Autowired
    public ClientServiceImpl(@Lazy ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }


    @Override
    public Client getById(UUID id) {
        return clientRepository.findById(id).orElse(null);
    }

    @Override
    public Client getByName(String name) {
        return clientRepository.findByName(name).orElse(null);
    }
}
