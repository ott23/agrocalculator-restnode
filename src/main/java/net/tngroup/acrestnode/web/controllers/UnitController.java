package net.tngroup.acrestnode.web.controllers;

import net.tngroup.acrestnode.databases.cassandra.models.Unit;
import net.tngroup.acrestnode.databases.cassandra.services.UnitService;
import net.tngroup.acrestnode.web.components.JsonComponent;
import net.tngroup.acrestnode.web.security.components.SecurityComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

import static net.tngroup.acrestnode.web.controllers.Responses.*;

@RestController
@RequestMapping("/unit")
public class UnitController {

    private UnitService unitService;
    private JsonComponent jsonComponent;
    private SecurityComponent securityComponent;

    @Autowired
    public UnitController(@Lazy UnitService unitService,
                          JsonComponent jsonComponent,
                          SecurityComponent securityComponent) {
        this.unitService = unitService;
        this.jsonComponent = jsonComponent;
        this.securityComponent = securityComponent;
    }

    @RequestMapping
    public ResponseEntity getList(HttpServletRequest request) {

        return securityComponent.doIfUser(client -> {

            List<Unit> unitList = unitService.getAllByClient(client.getId());
            return okResponse(unitList);
        });
    }

    @RequestMapping("/save")
    public ResponseEntity save(HttpServletRequest request, @RequestBody final Unit unit) {

        return securityComponent.doIfUser(client -> {
            try {

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

                return okResponse(jsonComponent.getObjectMapper().writeValueAsString(savedUnit));
            } catch (Exception e) {
                return badResponse(e);
            }
        });
    }

    @RequestMapping("/get/{id}")
    public ResponseEntity getById(HttpServletRequest request, @PathVariable UUID id) {

        return securityComponent.doIfUser(client -> {
            try {
                Unit unit = unitService.getById(id);

                if (unit == null) nonFoundResponse();

                if (!unit.getClient().equals(client.getId())) return failedDependencyResponse();

                return okResponse(jsonComponent.getObjectMapper().writeValueAsString(unit));
            } catch (Exception e) {
                return badResponse(e);
            }
        });
    }

    @RequestMapping("/delete/{id}")
    public ResponseEntity deleteById(HttpServletRequest request, @PathVariable UUID id) {

        return securityComponent.doIfUser(client -> {
            try {
                final Unit unit = unitService.getById(id);

                if (unit == null) return nonFoundResponse();

                if (!unit.getClient().equals(client.getId())) return failedDependencyResponse();

                unitService.deleteById(id);
                return successResponse();
            } catch (Exception e) {
                e.printStackTrace();
                return badResponse(e);
            }
        });
    }

}
