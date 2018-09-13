package net.tngroup.acrestnode.controllersTest;


import com.fasterxml.jackson.databind.ObjectMapper;
import net.tngroup.acrestnode.databases.cassandra.models.Client;
import net.tngroup.acrestnode.databases.cassandra.models.Geozone;
import net.tngroup.acrestnode.databases.cassandra.services.ClientService;
import net.tngroup.acrestnode.databases.cassandra.services.GeozoneService;
import net.tngroup.acrestnode.web.components.JsonComponent;
import net.tngroup.acrestnode.web.controllers.GeozoneController;
import net.tngroup.acrestnode.web.controllers.Responses;
import net.tngroup.acrestnode.web.security.components.SecurityComponent;
import org.junit.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

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

        securityComponent = Mockito.spy(new ValidSecurityComponent());
        MockitoAnnotations.initMocks(this);

        List<Geozone> result = new ArrayList<>();

        when(geozoneService.getAllByClient(any())).thenReturn(result);

        ResponseEntity actual = geozoneController.getList(httpServletRequest);

        verify(geozoneService, times(1)).getAllByClient(any());

        assertEquals(actual.getStatusCode(), HttpStatus.OK);

        assertEquals(actual.getBody(), result);


    }

    @Test
    public void givenGeozoneWithoutIdAndClient_whenCallSave_thenShouldBeReturnRandomIdAndCurrentClientId(){

        securityComponent = Mockito.spy(new ValidSecurityComponent());
        MockitoAnnotations.initMocks(this);

        final Geozone mockGeozone = new Geozone();

        when(geozoneService.save(any())).thenReturn(mockGeozone);
        when(jsonComponent.getObjectMapper()).thenReturn(new ObjectMapper());

        geozoneController.save(httpServletRequest, mockGeozone);

        verify(geozoneService, times(1)).save(mockGeozone);
        assertNotNull(mockGeozone.getId());
        assertEquals(mockGeozone.getClient(), MOCK_CLIENT_ID);

    }

    private class WrongSecurityComponent implements SecurityComponent {

        @Override
        public ResponseEntity doIfUser(Function<Client, ResponseEntity> next) {
            return Responses.failedDependencyResponse();
        }
    }

    private class ValidSecurityComponent implements SecurityComponent {

        @Override
        public ResponseEntity doIfUser(Function<Client, ResponseEntity> next) {

            final Client mockCLient = Mockito.mock(Client.class);
            when(mockCLient.getId()).thenReturn(MOCK_CLIENT_ID);
            return next.apply(mockCLient);
        }
    }


}
