package net.tngroup.acrestnode.web.controllers;

import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.core.JsonProcessingException;
import net.tngroup.acrestnode.databases.cassandra.models.Geozone;
import net.tngroup.acrestnode.databases.cassandra.services.ClientService;
import net.tngroup.acrestnode.databases.cassandra.services.GeozoneService;
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
@RequestMapping("/geozone")
public class GeozoneController {

    private JsonComponent jsonComponent;
    private ClientService clientService;
    private GeozoneService geozoneService;
    private SecurityComponent securityComponent;

    @Autowired
    public GeozoneController(@Lazy ClientService clientService,
                             @Lazy GeozoneService geozoneService,
                             JsonComponent jsonComponent,
                             SecurityComponent securityComponent) {
        this.clientService = clientService;
        this.geozoneService = geozoneService;
        this.jsonComponent = jsonComponent;
        this.securityComponent = securityComponent;
    }

    @RequestMapping
    public ResponseEntity getList(HttpServletRequest request) {

        return securityComponent.doIfUser(client -> {
            List<Geozone> geozoneList = geozoneService.getAllByClient(client.getId());
            return okResponse(geozoneList);
        });

    }

    @RequestMapping("/save")
    public ResponseEntity save(HttpServletRequest request, @RequestBody final Geozone geozone) {


        return securityComponent.doIfUser(client -> {


            if (geozone.getId() != null) {
                Geozone dbGeozone = geozoneService.getById(geozone.getId());
                if (dbGeozone != null && !dbGeozone.getClient().equals(client.getId())) {
                    return failedDependencyResponse();
                }
            } else {
                geozone.setId(UUIDs.timeBased());
            }

            if (geozone.getClient() == null) geozone.setClient(client.getId());

            final Geozone resultGeozone = geozoneService.save(geozone);

            try {
                return okResponse(jsonComponent.getObjectMapper().writeValueAsString(resultGeozone));
            } catch (JsonProcessingException e) {
                return Responses.failedDependencyResponse();
            }

        });

    }

    @RequestMapping("/get/{id}")
    public ResponseEntity getById(HttpServletRequest request, @PathVariable UUID id) {

        return securityComponent.doIfUser(client -> {

            final Geozone geozone = geozoneService.getById(id);

            if (geozone == null) return nonFoundResponse();

            if (!geozone.getClient().equals(client.getId())) return failedDependencyResponse();

            try {
                return okResponse(jsonComponent.getObjectMapper().writeValueAsString(geozone));
            } catch (JsonProcessingException e) {
                return failedDependencyResponse();
            }
        });

    }

    @RequestMapping("/delete/{id}")
    public ResponseEntity deleteById(HttpServletRequest request, @PathVariable UUID id) {

        return securityComponent.doIfUser(client -> {

            final Geozone geozone = geozoneService.getById(id);

            if (geozone == null) return nonFoundResponse();

            if (!geozone.getClient().equals(client.getId())) return failedDependencyResponse();

            geozoneService.deleteById(id);

            return successResponse();
        });
    }


}
