package net.tngroup.acrestnode.controllersTest;


import net.tngroup.acrestnode.databases.cassandra.services.ClientService;
import net.tngroup.acrestnode.databases.cassandra.services.GeozoneService;
import net.tngroup.acrestnode.web.components.JsonComponent;
import net.tngroup.acrestnode.web.controllers.GeozoneController;
import net.tngroup.acrestnode.web.controllers.Responses;
import net.tngroup.acrestnode.web.security.components.SecurityComponent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verifyZeroInteractions;



public class GeozoneControllerTest {

    @InjectMocks
    private GeozoneController geozoneController;

    @Mock
    private JsonComponent jsonComponent;
    @Mock
    private ClientService clientService;
    @Mock
    private GeozoneService geozoneService;
    @Mock
    private SecurityComponent securityComponent = next -> Responses.failedDependencyResponse();

    @Mock
    private HttpServletRequest httpServletRequest;

    @Before
    public void initMocks(){

        MockitoAnnotations.initMocks(this);
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
