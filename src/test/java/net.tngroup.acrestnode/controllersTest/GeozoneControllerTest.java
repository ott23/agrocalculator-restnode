package net.tngroup.acrestnode.controllersTest;


import com.fasterxml.jackson.databind.ObjectMapper;
import net.tngroup.acrestnode.databases.cassandra.models.Geozone;
import net.tngroup.acrestnode.databases.cassandra.services.GeozoneService;
import net.tngroup.acrestnode.databases.cassandra.services.base.ClientEntityService;
import net.tngroup.acrestnode.web.components.JsonComponent;
import net.tngroup.acrestnode.web.controllers.GeozoneController;
import net.tngroup.acrestnode.web.controllers.Responses;
import net.tngroup.acrestnode.web.controllers.base.ClientEntityController;
import net.tngroup.acrestnode.web.security.components.SecurityComponent;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


public class GeozoneControllerTest extends ClientEntityControllerTest<Geozone> {


    private final static UUID MOCK_CLIENT_ID = UUID.randomUUID();

    @InjectMocks
    private GeozoneController geozoneController;

    @Mock
    private JsonComponent jsonComponent;
    @Mock
    private GeozoneService geozoneService;
    @Spy
    private SecurityComponent securityComponent = new ValidSecurityComponent(MOCK_CLIENT_ID);
    @Mock
    private HttpServletRequest httpServletRequest;

    //region ==================== ClientEntityControllerTest ====================


    @Override
    protected ClientEntityController<Geozone> getEntityController() {
        return geozoneController;
    }

    @Override
    protected void setSecurityComponent(SecurityComponent securityComponent) {
        this.securityComponent = securityComponent;
    }

    @Override
    protected HttpServletRequest getHttpServletMock() {
        return httpServletRequest;
    }

    @Override
    protected ClientEntityService<Geozone> getClientEntityService() {
        return geozoneService;
    }

    @Override
    protected JsonComponent getJsonComponent() {
        return jsonComponent;
    }

    @Override
    protected Geozone newEntity() {
        return new Geozone();
    }

    @Override
    protected void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    //endregion

    //region ==================== Save ====================

    @Test
    public void givenGeozoneWithoutIdAndClient_whenCallSave_thenShouldBeReturnRandomIdAndCurrentClientId() {

        MockitoAnnotations.initMocks(this);

        final Geozone mockGeozone = new Geozone();

        when(geozoneService.save(any())).thenReturn(mockGeozone);
        when(jsonComponent.getObjectMapper()).thenReturn(new ObjectMapper());

        geozoneController.save(httpServletRequest, mockGeozone);

        verify(geozoneService, times(1)).save(mockGeozone);
        assertNotNull(mockGeozone.getId());
        assertEquals(mockGeozone.getClient(), MOCK_CLIENT_ID);

    }

    @Test
    public void givenExistGeozoneWithOtherClient_whenCallSave_thenShouldBeReturnFailedDependency() {

        MockitoAnnotations.initMocks(this);

        final ResponseEntity errorResponse = Responses.failedDependencyResponse();
        final Geozone mockGeozone = spy(new Geozone());
        final Geozone dbGeozone = mock(Geozone.class);

        when(mockGeozone.getId()).thenReturn(UUID.randomUUID());
        when(geozoneService.getById(any())).thenReturn(dbGeozone);
        when(dbGeozone.getClient()).thenReturn(UUID.randomUUID());

        assertEquals(
                geozoneController.save(httpServletRequest, mockGeozone).getStatusCode(),
                errorResponse.getStatusCode()
        );
    }

    //endregion
}
