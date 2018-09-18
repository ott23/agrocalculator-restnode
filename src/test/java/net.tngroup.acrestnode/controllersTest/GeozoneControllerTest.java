package net.tngroup.acrestnode.controllersTest;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.tngroup.acrestnode.databases.cassandra.models.Geozone;
import net.tngroup.acrestnode.databases.cassandra.services.ClientService;
import net.tngroup.acrestnode.databases.cassandra.services.GeozoneService;
import net.tngroup.acrestnode.web.components.JsonComponent;
import net.tngroup.acrestnode.web.controllers.GeozoneController;
import net.tngroup.acrestnode.web.controllers.Responses;
import net.tngroup.acrestnode.web.security.components.SecurityComponent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


public class GeozoneControllerTest {


    private final static UUID MOCK_CLIENT_ID = UUID.randomUUID();

    @InjectMocks
    private GeozoneController geozoneController;

    @Mock
    private JsonComponent jsonComponent;
    @Mock
    private ClientService clientService;
    @Mock
    private GeozoneService geozoneService;
    @Spy
    private SecurityComponent securityComponent;
    @Mock
    private HttpServletRequest httpServletRequest;

    @Before
    public void initValidSecurityComponent() {
        securityComponent = Mockito.spy(new ValidSecurityComponent(MOCK_CLIENT_ID));
    }

    //region ==================== GetList ====================

    @Test
    public void givenNullClient_whenCallGetList_thenShouldBeCallGeozoneService() {

        securityComponent = Mockito.spy(new WrongSecurityComponent());
        MockitoAnnotations.initMocks(this);

        HttpStatus resultStatus = geozoneController.getList(httpServletRequest).getStatusCode();

        assertEquals(resultStatus, HttpStatus.FAILED_DEPENDENCY);

        verifyZeroInteractions(geozoneService);
    }

    @Test
    public void givenClient_whenCallGetList_thenShouldBeCallGeozoneServiceAndReturnResponse() {

        MockitoAnnotations.initMocks(this);

        List<Geozone> result = new ArrayList<>();

        when(geozoneService.getAllByClient(any())).thenReturn(result);

        ResponseEntity actual = geozoneController.getList(httpServletRequest);

        verify(geozoneService, times(1)).getAllByClient(any());

        assertEquals(actual.getStatusCode(), HttpStatus.OK);

        assertEquals(actual.getBody(), result);
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

    //region ==================== Get ====================

    @Test
    public void givenValidClientIdAndGeozoneId_whenCallGet_thenShouldBeReturnGeozone() throws JsonProcessingException {

        MockitoAnnotations.initMocks(this);

        final Geozone mockGeozone = new Geozone();
        final ObjectMapper objectMapper = new ObjectMapper();
        mockGeozone.setClient(MOCK_CLIENT_ID);

        when(geozoneService.getById(any())).thenReturn(mockGeozone);
        when(jsonComponent.getObjectMapper()).thenReturn(objectMapper);

        assertEquals(
                geozoneController.getById(httpServletRequest, UUID.randomUUID()).getBody(),
                objectMapper.writeValueAsString(mockGeozone));


    }

    @Test
    public void givenNonExistedID_whenCallGet_thenShouldBeReturnNotFoundResponse() {

        MockitoAnnotations.initMocks(this);

        when(geozoneService.getById(any())).thenReturn(null);

        assertEquals(
                Responses.nonFoundResponse().getStatusCode(),
                geozoneController.getById(httpServletRequest, UUID.randomUUID()).getStatusCode()
        );

    }

    @Test
    public void givenGeozoneIdFromOtherClient_whenCallGet_thenShouldBeReturnFailedDependency() {

        MockitoAnnotations.initMocks(this);

        final Geozone mockGeozone = new Geozone();
        mockGeozone.setClient(UUID.randomUUID());

        when(geozoneService.getById(any())).thenReturn(mockGeozone);
        assertEquals(
                geozoneController.getById(httpServletRequest, UUID.randomUUID()).getStatusCode(),
                Responses.failedDependencyResponse().getStatusCode()
        );

    }

    //endregion

    //region ==================== Delete ====================

    @Test
    public void givenValidGeozoneIdAndClientId_whenCallDelete_thenShouldBeReturnSuccessResponse() {

        MockitoAnnotations.initMocks(this);

        final Geozone geozone = new Geozone();
        geozone.setClient(MOCK_CLIENT_ID);

        when(geozoneService.getById(any())).thenReturn(geozone);
        when(geozoneService.deleteById(any())).thenReturn(true);

        assertEquals(
                geozoneController.deleteById(httpServletRequest, UUID.randomUUID()),
                Responses.successResponse()
        );

    }

    @Test
    public void givenInvalidGeozoneId_whenCallDelete_thenShouldBeReturnNotFound() {

        MockitoAnnotations.initMocks(this);
        when(geozoneService.getById(any())).thenReturn(null);

        assertEquals(
                geozoneController.deleteById(httpServletRequest, UUID.randomUUID()),
                Responses.nonFoundResponse()
        );
    }

    @Test
    public void givenInvalidClientId_whenCallDelete_thenShouldBeReturnFailedDependency() {

        MockitoAnnotations.initMocks(this);

        final Geozone geozone = new Geozone();
        geozone.setClient(UUID.randomUUID());

        when(geozoneService.getById(any())).thenReturn(geozone);

        assertEquals(
                geozoneController.deleteById(httpServletRequest, UUID.randomUUID()),
                Responses.failedDependencyResponse()
        );
    }

    @Test
    public void givenValidClientIdAndGeozoneId_whenTwoClientDoDeletePermanent_thenReturnNonFound() {

        MockitoAnnotations.initMocks(this);

        final Geozone geozone = new Geozone();
        geozone.setClient(MOCK_CLIENT_ID);

        when(geozoneService.getById(any())).thenReturn(geozone);
        when(geozoneService.deleteById(any())).thenReturn(false);

        assertEquals(
                geozoneController.deleteById(httpServletRequest, UUID.randomUUID()),
                Responses.nonFoundResponse()
        );

    }

    //endregion
}
