package net.tngroup.acrestnode.controllersTest;


import net.tngroup.acrestnode.databases.cassandra.services.ClientService;
import net.tngroup.acrestnode.databases.cassandra.services.GeozoneService;
import net.tngroup.acrestnode.web.components.JsonComponent;
import net.tngroup.acrestnode.web.controllers.GeozoneController;
import net.tngroup.acrestnode.web.controllers.Responses;
import net.tngroup.acrestnode.web.security.components.SecurityComponent;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;



public class GeozoneControllerTest {


    private GeozoneController geozoneController;

    private JsonComponent jsonComponent;
    private ClientService clientService;
    private GeozoneService geozoneService;
    private SecurityComponent wrongSecurityComponent;
    private HttpServletRequest httpServletRequest;

    @Before
    public void initMocks(){

        jsonComponent = mock(JsonComponent.class);
        clientService = mock(ClientService.class);
        geozoneService = mock(GeozoneService.class);
        wrongSecurityComponent  = next -> Responses.failedDependencyResponse();
        httpServletRequest = mock(HttpServletRequest.class);
        geozoneController = new GeozoneController(clientService, geozoneService, jsonComponent, wrongSecurityComponent);
    }

    @Test
    public void givenNullClientName_whenCallGetList_thenShouldBeFailedDependencyResponse(){

        assertEquals(
                geozoneController.getList(httpServletRequest).getStatusCode(),
                HttpStatus.FAILED_DEPENDENCY
        );

        verifyZeroInteractions(geozoneService);
    }


}
