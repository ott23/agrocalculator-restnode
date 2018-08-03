package net.tngroup.acrestnode.web.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.tngroup.acrestnode.databases.cassandra.models.Client;
import net.tngroup.acrestnode.databases.cassandra.models.Unit;
import net.tngroup.acrestnode.databases.cassandra.services.ClientService;
import net.tngroup.acrestnode.databases.cassandra.services.UnitService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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

    // Loggers
    private Logger logger = LogManager.getFormatterLogger("CommonLogger");

    private UnitService unitService;
    private ClientService clientService;

    @Autowired
    public UnitController(@Lazy ClientService clientService,
                          @Lazy UnitService unitService) {
        this.clientService = clientService;
        this.unitService = unitService;
    }

    @RequestMapping
    public ResponseEntity getList(HttpServletRequest request) {
        // Get client, error if null
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Client client = clientService.getByName(name);
        if (client == null) return failedDependencyResponse();

        // Logging
        logger.info("Request to `/unit` (get list) from " + request.getRemoteAddr() + " by `" + client.getName() + "`");

        List<Unit> unitList = unitService.getAllByClient(client.getId());
        return okResponse(unitList);
    }

    @RequestMapping("/save")
    public ResponseEntity save(@RequestBody String jsonRequest, HttpServletRequest request) {
        // Get client, error if null
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Client client = clientService.getByName(name);
        if (client == null) return failedDependencyResponse();

        // Logging
        logger.info("Request to `/unit/save` (add or update) from " + request.getRemoteAddr() + " by `" + client.getName() + "`");

        try {
            Unit unit = new ObjectMapper().readValue(jsonRequest, Unit.class);

            if (unit.getId() != null) {
                Unit dbUnit = unitService.getById(unit.getId());
                if (dbUnit != null && !dbUnit.getClient().equals(client.getId())) {
                    return failedDependencyResponse();
                }
            }

            List<Unit> unitList = unitService.getAllByImei(unit.getImei());
            if (unitList.size() == 1 && !unitList.get(0).getId().equals(unit.getId()) || unitList.size() > 1) return conflictResponse("imei");

            if (unit.getId() == null) unit.setId(UUID.randomUUID());
            if (unit.getClient() == null) unit.setClient(client.getId());

            unit = unitService.save(unit);

            return okResponse(formUnit(unit));
        } catch (Exception e) {
            return badResponse(e);
        }
    }

    @RequestMapping("/get/{id}")
    public ResponseEntity getById(@PathVariable UUID id, HttpServletRequest request) {

        System.out.println(id.toString());

        // Get client, error if null
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Client client = clientService.getByName(name);
        if (client == null) return failedDependencyResponse();

        // Logging
        logger.info("Request to `/unit/get/{id}` (getn) from " + request.getRemoteAddr() + " by `" + client.getName() + "`");

        try {
            Unit unit = unitService.getById(id);

            if (unit == null) nonFoundResponse();

            if (!unit.getClient().equals(client.getId())) return failedDependencyResponse();

            return okResponse(formUnit(unit));
        } catch (Exception e) {
            return badResponse(e);
        }
    }

    @RequestMapping("/delete/{id}")
    public ResponseEntity deleteById(@PathVariable UUID id, HttpServletRequest request) {
        // Get client, error if null
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Client client = clientService.getByName(name);
        if (client == null) return failedDependencyResponse();

        // Logging
        logger.info("Request to `/unit/delete/{id}` (delete) from " + request.getRemoteAddr() + " by `" + client.getName() + "`");

        try {
            Unit unit = unitService.getById(id);

            if (unit == null) return nonFoundResponse();

            if (!unit.getClient().equals(client.getId())) return failedDependencyResponse();

            unitService.deleteById(id);
            return successResponse();
        } catch (Exception e) {
            e.printStackTrace();
            return badResponse(e);
        }
    }

    private String formUnit(Unit unit) throws Exception {
        return new ObjectMapper().writeValueAsString(unit);
    }

}
