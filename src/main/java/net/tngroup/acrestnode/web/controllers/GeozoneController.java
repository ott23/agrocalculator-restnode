package net.tngroup.acrestnode.web.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.tngroup.acrestnode.databases.cassandra.models.Client;
import net.tngroup.acrestnode.databases.cassandra.models.Geozone;
import net.tngroup.acrestnode.databases.cassandra.services.ClientService;
import net.tngroup.acrestnode.databases.cassandra.services.GeozoneService;
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
@RequestMapping("/geozone")
public class GeozoneController {

    private ClientService clientService;
    private GeozoneService geozoneService;

    @Autowired
    public GeozoneController(@Lazy ClientService clientService,
                             @Lazy GeozoneService geozoneService) {
        this.clientService = clientService;
        this.geozoneService = geozoneService;
    }

    @RequestMapping
    public ResponseEntity getList(HttpServletRequest request) {
        // Get client, error if null
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Client client = clientService.getByName(name);
        if (client == null) return failedDependencyResponse();

        List<Geozone> geozoneList = geozoneService.getAllByClient(client.getId());
        return okResponse(geozoneList);
    }

    @RequestMapping("/save")
    public ResponseEntity save(@RequestBody String jsonRequest, HttpServletRequest request) {
        // Get client, error if null
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Client client = clientService.getByName(name);
        if (client == null) return failedDependencyResponse();

        try {
            Geozone geozone = new ObjectMapper().readValue(jsonRequest, Geozone.class);

            if (geozone.getId() != null) {
                Geozone dbGeozone = geozoneService.getById(geozone.getId());
                if (dbGeozone != null && !dbGeozone.getClient().equals(client.getId())) {
                    return failedDependencyResponse();
                }
            }

            if (geozone.getId() == null) geozone.setId(UUID.randomUUID());
            if (geozone.getClient() == null) geozone.setClient(client.getId());

            geozone = geozoneService.save(geozone);

            return okResponse(new ObjectMapper().writeValueAsString(geozone));
        } catch (Exception e) {
            return badResponse(e);
        }
    }

    @RequestMapping("/get/{id}")
    public ResponseEntity getById(@PathVariable UUID id, HttpServletRequest request) {

        // Get client, error if null
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Client client = clientService.getByName(name);
        if (client == null) return failedDependencyResponse();

        try {
            Geozone geozone = geozoneService.getById(id);

            if (geozone == null) nonFoundResponse();

            assert geozone != null;
            if (!geozone.getClient().equals(client.getId())) return failedDependencyResponse();

            return okResponse(new ObjectMapper().writeValueAsString(geozone));
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

        try {
            Geozone geozone = geozoneService.getById(id);

            if (geozone == null) return nonFoundResponse();

            if (!geozone.getClient().equals(client.getId())) return failedDependencyResponse();

            geozoneService.deleteById(id);
            return successResponse();
        } catch (Exception e) {
            e.printStackTrace();
            return badResponse(e);
        }
    }
    

}
