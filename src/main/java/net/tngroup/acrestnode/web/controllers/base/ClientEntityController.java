package net.tngroup.acrestnode.web.controllers.base;

import net.tngroup.acrestnode.databases.cassandra.models.base.ClientEntity;
import net.tngroup.acrestnode.databases.cassandra.services.base.ClientEntityService;
import net.tngroup.acrestnode.web.security.components.SecurityComponent;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

import static net.tngroup.acrestnode.web.controllers.Responses.*;

public abstract class ClientEntityController<T extends ClientEntity> {

    private ClientEntityService<T> clientEntityService;
    protected SecurityComponent securityComponent;

    public ClientEntityController(ClientEntityService<T> clientEntityService,
                                  SecurityComponent securityComponent) {
        this.clientEntityService = clientEntityService;
        this.securityComponent = securityComponent;
    }

    public ResponseEntity getList(HttpServletRequest request) {

        return securityComponent.doIfUser(client -> {

            List<T> unitList = clientEntityService.getAllByClient(client.getId());
            return okResponse(unitList);
        });
    }

    public ResponseEntity getById(HttpServletRequest request, UUID id) {

        return securityComponent.doIfUser(client -> {

            final T clientEntity = clientEntityService.getById(id);

            if (clientEntity == null) nonFoundResponse();

            if (!clientEntity.getClient().equals(client.getId())) return failedDependencyResponse();

            return okResponse(clientEntity);
        });
    }

    public ResponseEntity deleteById(HttpServletRequest request, UUID id) {

        return securityComponent.doIfUser(client -> {

            final T unit = clientEntityService.getById(id);

            if (unit == null) return nonFoundResponse();

            if (!unit.getClient().equals(client.getId())) return failedDependencyResponse();

            clientEntityService.deleteById(id);
            return successResponse();
        });
    }


}
