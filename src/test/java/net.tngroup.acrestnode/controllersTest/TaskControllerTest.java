package net.tngroup.acrestnode.controllersTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.tngroup.acrestnode.databases.cassandra.models.Client;
import net.tngroup.acrestnode.databases.cassandra.services.ClientService;
import net.tngroup.acrestnode.databases.cassandra.services.TaskConditionService;
import net.tngroup.acrestnode.databases.cassandra.services.TaskResultService;
import net.tngroup.acrestnode.web.components.JsonComponent;
import net.tngroup.acrestnode.web.components.KafkaComponent;
import net.tngroup.acrestnode.web.controllers.TaskController;
import net.tngroup.acrestnode.web.controllers.requestmodels.TaskRequest;
import net.tngroup.acrestnode.web.security.components.SecurityComponent;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;
import java.util.function.Function;

import static net.tngroup.acrestnode.web.controllers.Responses.kafkaNotAvailableResponse;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

public class TaskControllerTest {

    private final static UUID MOCK_CLIENT_ID = UUID.randomUUID();

    @InjectMocks
    private TaskController taskController;

    @Mock
    private KafkaComponent kafkaComponent;
    @Mock
    private ClientService clientService;
    @Mock
    private TaskConditionService taskConditionService;
    @Mock
    private TaskResultService taskResultService;
    @Mock
    private HttpServletRequest httpServletRequest;

    @Spy
    private JsonComponent jsonComponent = new JsonComponent();

    @Spy
    private SecurityComponent securityComponent = new ValidSecurityComponent();

    @Test
    public void deserializationTest() throws IOException {

        final ObjectMapper objectMapper = new ObjectMapper();

        final TaskRequest taskRequest = objectMapper.readValue("{\n" +
                "    \"topic\" : \"string\",\n" +
                "    \"message\" : \"string\" \n" +
                "}", TaskRequest.class);

        Assert.assertEquals(taskRequest.getTopic(), "string");
        Assert.assertEquals(taskRequest.getMessage(), "{\"message\":\"string\"}");

    }

    @Test
    public void givenProblemWithKafka_whenCallSend_thenShouldBeKafkaNotAvailable() throws Exception {

        MockitoAnnotations.initMocks(this);
        doThrow(Exception.class).when(kafkaComponent).testSocket();

        Assert.assertEquals(
                kafkaNotAvailableResponse(),
                taskController.send(httpServletRequest, new TaskRequest()));
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
