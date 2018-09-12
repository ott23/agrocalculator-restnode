package net.tngroup.acrestnode.web.security.components;


import net.tngroup.acrestnode.databases.cassandra.models.Client;
import org.springframework.http.ResponseEntity;

import java.util.function.Function;

public interface SecurityComponent {

    ResponseEntity doIfUser(Function<Client, ResponseEntity> next);

}
