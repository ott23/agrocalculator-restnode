package net.tngroup.acrestnode.web.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.tngroup.acrestnode.databases.cassandra.models.Client;
import net.tngroup.acrestnode.databases.cassandra.models.Unit;
import net.tngroup.acrestnode.databases.cassandra.services.ClientService;
import net.tngroup.acrestnode.databases.cassandra.services.UnitService;
import net.tngroup.acrestnode.nodeclient.components.ChannelComponent;
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

    private ChannelComponent channelComponent;
    private UnitService unitService;
    private ClientService clientService;

    @Autowired
    public UnitController(ChannelComponent channelComponent,
                          @Lazy ClientService clientService,
                          @Lazy UnitService unitService) {
        this.channelComponent = channelComponent;
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

        // Error if channel is closed
        if (!channelComponent.isStatus()) return badResponse(new Exception("Server is not started"));

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

        // Error if channel is closed
        if (!channelComponent.isStatus()) return badResponse(new Exception("Server is not started"));

        try {
            Unit unit = new ObjectMapper().readValue(jsonRequest, Unit.class);

            if (unit.getId() != null) {
                Unit dbUnit = unitService.getById(unit.getId());
                if (dbUnit != null && !dbUnit.getClient().equals(client.getId())) return failedDependencyResponse();
            }

            List<Unit> unitList = unitService.getAllByNameOrImei(unit.getName(), unit.getImei());
            if (unitList.size() == 1 && !unitList.get(0).getId().equals(unit.getId()) || unitList.size() > 1)
                return conflictResponse();

            if (unit.getId() == null) unit.setId(UUID.randomUUID());
            if (unit.getClient() == null) unit.setClient(client.getId());

            unitService.save(unit);
            return successResponse();
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

        // Error if channel is closed
        if (!channelComponent.isStatus()) return badResponse(new Exception("Server is not started"));

        try {
            Unit unit = unitService.getById(id);
            if (!unit.getClient().equals(client.getId())) return failedDependencyResponse();

            unitService.deleteById(id);
            return successResponse();
        } catch (Exception e) {
            return badResponse(e);
        }
    }

}
