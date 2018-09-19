package net.tngroup.acrestnode.web.controllers;

import com.datastax.driver.core.utils.UUIDs;
import net.tngroup.acrestnode.databases.cassandra.models.Geozone;
import net.tngroup.acrestnode.databases.cassandra.services.GeozoneService;
import net.tngroup.acrestnode.web.controllers.base.ClientEntityController;
import net.tngroup.acrestnode.web.security.components.SecurityComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

import static net.tngroup.acrestnode.web.controllers.Responses.failedDependencyResponse;
import static net.tngroup.acrestnode.web.controllers.Responses.okResponse;

@RestController
@RequestMapping("/geozone")
public class GeozoneController extends ClientEntityController<Geozone> {

    private GeozoneService geozoneService;

    @Autowired
    public GeozoneController(@Lazy GeozoneService geozoneService,
                             SecurityComponent securityComponent) {
        super(geozoneService, securityComponent);
        this.geozoneService = geozoneService;
    }


    @Override
    @RequestMapping
    public ResponseEntity getList(HttpServletRequest request) {
        return super.getList(request);
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
            final Geozone savedGeozone = geozoneService.save(geozone);
            return okResponse(savedGeozone);
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
