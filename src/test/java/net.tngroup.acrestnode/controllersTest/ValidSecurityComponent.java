package net.tngroup.acrestnode.controllersTest;

import net.tngroup.acrestnode.databases.cassandra.models.Client;
import net.tngroup.acrestnode.web.security.components.SecurityComponent;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.util.UUID;
import java.util.function.Function;

import static org.mockito.Mockito.when;

public class ValidSecurityComponent implements SecurityComponent {

    private final UUID mockClientId;

    public ValidSecurityComponent(final UUID mockClientId) {
        this.mockClientId = mockClientId;
    }

    @Override
    public ResponseEntity doIfUser(Function<Client, ResponseEntity> next) {

        final Client mockClient = Mockito.mock(Client.class);
        when(mockClient.getId()).thenReturn(mockClientId);
        return next.apply(mockClient);
    }
}