package net.tngroup.acrestnode.web.controllers;

import net.tngroup.acrestnode.databases.cassandra.models.Unit;
import net.tngroup.acrestnode.databases.cassandra.services.UnitService;
import net.tngroup.acrestnode.web.controllers.base.ClientEntityController;
import net.tngroup.acrestnode.web.security.components.SecurityComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

import static net.tngroup.acrestnode.web.controllers.Responses.*;

@RestController
@RequestMapping("/unit")
public class UnitController extends ClientEntityController<Unit> {

    private UnitService unitService;
    private SecurityComponent securityComponent;

    @Autowired
    public UnitController(@Lazy UnitService unitService,
                          SecurityComponent securityComponent) {
        super(unitService, securityComponent);
        this.unitService = unitService;
        this.securityComponent = securityComponent;
    }

    @Override
    @RequestMapping
    public ResponseEntity getList(HttpServletRequest request) {
        return super.getList(request);
    }

    @PostMapping("/save")
    public ResponseEntity save(HttpServletRequest request, @RequestBody final Unit unit) {

        return securityComponent.doIfUser(client -> {

            if (unit.getId() != null) {
                Unit dbUnit = unitService.getById(unit.getId());
                if (dbUnit != null && !dbUnit.getClient().equals(client.getId())) {
                    return failedDependencyResponse();
                }
            }

            List<Unit> unitList = unitService.getAllByImei(unit.getImei());


            if (unitList.size() == 1 && !unitList.get(0).getId().equals(unit.getId()) || unitList.size() > 1)
                return conflictResponse("imei");

            if (unit.getId() == null) unit.setId(UUID.randomUUID());
            if (unit.getClient() == null) unit.setClient(client.getId());

            final Unit savedUnit = unitService.save(unit);

            return okResponse(savedUnit);
        });
    }

    @Override
    @RequestMapping("/get/{id}")
    public ResponseEntity getById(HttpServletRequest request, @PathVariable UUID id) {
        return super.getById(request, id);
    }

    @Override
    @RequestMapping("/delete/{id}")
    public ResponseEntity deleteById(HttpServletRequest request, @PathVariable UUID id) {
        return super.deleteById(request, id);
    }
}
