package net.tngroup.acrestnode.controllersTest;


import net.tngroup.acrestnode.databases.cassandra.models.Client;
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

import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verifyZeroInteractions;



public class GeozoneControllerTest {


    @InjectMocks
    private GeozoneController geozoneController;

    @Mock private JsonComponent jsonComponent;
    @Mock private ClientService clientService;
    @Mock private GeozoneService geozoneService;
    @Spy private SecurityComponent wrongSecurityComponent = Mockito.spy(new WrongSecurityComponent());
    @Mock private HttpServletRequest httpServletRequest;

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

    private class WrongSecurityComponent implements SecurityComponent{
        @Override
        public ResponseEntity doIfUser(Function<Client, ResponseEntity> next) {
            return Responses.failedDependencyResponse();
        }
    }

}
