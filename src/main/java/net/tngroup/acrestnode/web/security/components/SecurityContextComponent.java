package net.tngroup.acrestnode.web.security.components;


import net.tngroup.acrestnode.databases.cassandra.models.Client;
import net.tngroup.acrestnode.databases.cassandra.services.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.function.Function;

import static net.tngroup.acrestnode.web.controllers.Responses.failedDependencyResponse;

@Component
public class SecurityContextComponent implements SecurityComponent {

    private ClientService clientService;

    @Autowired
    public SecurityContextComponent(ClientService clientService) {
        this.clientService = clientService;
    }

    @Override
    public ResponseEntity doIfUser(Function<Client, ResponseEntity> next) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Client client = clientService.getByName(name);
        return client == null ? failedDependencyResponse() : next.apply(client);
    }
}
