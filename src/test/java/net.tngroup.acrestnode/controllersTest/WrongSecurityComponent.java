package net.tngroup.acrestnode.controllersTest;

import net.tngroup.acrestnode.databases.cassandra.models.Client;
import net.tngroup.acrestnode.web.controllers.Responses;
import net.tngroup.acrestnode.web.security.components.SecurityComponent;
import org.springframework.http.ResponseEntity;

import java.util.function.Function;

public class WrongSecurityComponent implements SecurityComponent {

    @Override
    public ResponseEntity doIfUser(Function<Client, ResponseEntity> next) {
        return Responses.failedDependencyResponse();
    }
}