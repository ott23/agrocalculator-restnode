package net.tngroup.acrestnode.controllersTest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.tngroup.acrestnode.databases.cassandra.models.Client;
import net.tngroup.acrestnode.databases.cassandra.models.TaskCondition;
import net.tngroup.acrestnode.databases.cassandra.models.TaskKey;
import net.tngroup.acrestnode.databases.cassandra.models.TaskResult;
import net.tngroup.acrestnode.databases.cassandra.services.ClientService;
import net.tngroup.acrestnode.databases.cassandra.services.TaskConditionService;
import net.tngroup.acrestnode.databases.cassandra.services.TaskResultService;
import net.tngroup.acrestnode.web.components.JsonComponent;
import net.tngroup.acrestnode.web.components.KafkaComponent;
import net.tngroup.acrestnode.web.controllers.Responses;
import net.tngroup.acrestnode.web.controllers.TaskController;
import net.tngroup.acrestnode.web.controllers.requestmodels.PollRequest;
import net.tngroup.acrestnode.web.controllers.requestmodels.TaskRequest;
import net.tngroup.acrestnode.web.security.components.SecurityComponent;
import org.junit.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static net.tngroup.acrestnode.web.controllers.Responses.kafkaNotAvailableResponse;
import static net.tngroup.acrestnode.web.controllers.Responses.okResponse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

        assertEquals(taskRequest.getTopic(), "string");
        assertEquals(taskRequest.getMessage(), "{\"message\":\"string\"}");

    }

    @Test
    public void givenProblemWithKafka_whenCallSend_thenShouldBeKafkaNotAvailable() throws Exception {

        MockitoAnnotations.initMocks(this);
        doThrow(Exception.class).when(kafkaComponent).testSocket();

        assertEquals(
                kafkaNotAvailableResponse(),
                taskController.send(httpServletRequest, new TaskRequest()));
    }

    @Test
    public void givenNewTaskRequestAndNoTaskResultAnswer_whenCallSend_thenShouldBeCallServicesAndReturnResponse()
            throws Exception {

        MockitoAnnotations.initMocks(this);

        doNothing().when(kafkaComponent).testSocket();
        when(taskConditionService.getByHashCode(anyInt())).thenReturn(null);
        when(kafkaComponent.send(any(), any(), any())).thenReturn("Success");

        when(taskResultService.getByKey(any())).thenReturn(null, null, null, null, null);

        final ResponseEntity response = taskController.send(httpServletRequest, new TaskRequest());

        final JsonNode responseBody = new ObjectMapper().readTree(response.getBody().toString());

        assertNotNull(responseBody.asText("task"));
        assertEquals(
                response.getStatusCode(),
                okResponse(null).getStatusCode()
        );


        verify(taskConditionService, times(1)).save(any());
        verify(taskResultService, times(TaskController.ATTEMPTS_COUNT)).getByKey(any());
    }

    @Test
    public void givenNewTaskRequestAndNotImmediatelyResult_whenCallSend_thenReturnFormTaskResult()
            throws Exception {

        MockitoAnnotations.initMocks(this);

        doNothing().when(kafkaComponent).testSocket();
        when(taskConditionService.getByHashCode(anyInt())).thenReturn(null);
        when(kafkaComponent.send(any(), any(), any())).thenReturn("Success");

        final TaskResult taskResult = new TaskResult();
        taskResult.setTime(new Date(1));
        taskResult.setKey(new TaskKey(MOCK_CLIENT_ID, UUID.randomUUID()));
        taskResult.setValue("{\"value\" : 1}");
        final TaskCondition taskCondition = new TaskCondition();
        taskCondition.setTime(new Date(0));
        when(taskResultService.getByKey(any())).thenReturn(null, null, taskResult);
        when(taskConditionService.save(any(TaskCondition.class))).thenReturn(taskCondition);

        final ResponseEntity response = taskController.send(httpServletRequest, new TaskRequest());

        final JsonNode responseBody = new ObjectMapper().readTree(response.getBody().toString());

        assertNotNull(responseBody.asText("task"));
        assertNotNull(responseBody.asText("time"));
        assertNotNull(responseBody.asText("result"));
        assertEquals(
                response.getStatusCode(),
                okResponse(null).getStatusCode()
        );

        verify(taskConditionService, times(1)).save(any());
        verify(taskResultService, times(3)).getByKey(any());
    }

    @Test
    public void givenNewTaskRequestAndKafkaReturnError_whenCallSend_thenShouldBeBadResponse() throws Exception {

        MockitoAnnotations.initMocks(this);

        doNothing().when(kafkaComponent).testSocket();
        when(taskConditionService.getByHashCode(anyInt())).thenReturn(null);
        when(kafkaComponent.send(any(), any(), any())).thenReturn("Error");


        assertEquals(
                taskController.send(httpServletRequest, new TaskRequest()).getStatusCode(),
                Responses.badResponse(new Exception()).getStatusCode()
        );

        verify(taskConditionService, times(0)).save(any());
        verify(taskResultService, times(0)).getByKey(any());
    }


    @Test
    public void givenProblemWithKafka_whenCallPoll_thenShouldBeKafkaNotAvailable() throws Exception {

        MockitoAnnotations.initMocks(this);
        doThrow(Exception.class).when(kafkaComponent).testSocket();

        assertEquals(
                kafkaNotAvailableResponse(),
                taskController.poll(httpServletRequest, new PollRequest()));
    }

    @Test
    public void givenTaskResult_whenCallPoll_thenShouldBeOkResponse()
            throws NoSuchFieldException, IllegalAccessException {

        MockitoAnnotations.initMocks(this);

        final PollRequest pollRequest = new PollRequest();
        Field taskField = PollRequest.class.getDeclaredField("task");
        taskField.setAccessible(true);
        taskField.set(pollRequest, UUID.randomUUID());
        taskField.setAccessible(false);

        final TaskResult taskResult = new TaskResult();
        taskResult.setTime(new Date());
        taskResult.setKey(new TaskKey(MOCK_CLIENT_ID, UUID.randomUUID()));
        taskResult.setValue("{\"value\" : 1}");

        when(taskResultService.getByKey(any())).thenReturn(taskResult);
        when(taskConditionService.getByTaskKey(any())).thenReturn(Optional.of(new TaskCondition()));

        final ResponseEntity response = taskController.poll(httpServletRequest, pollRequest);

        assertNotNull(response.getBody());
        assertEquals(response.getStatusCode(), okResponse(null).getStatusCode());
        assertEquals(response.getBody(), TaskController.formTaskResult(new JsonComponent(), taskResult));

    }

    @Test
    public void givenInvalidTask_whenCallPoll_thenShouldBeNotFound()
            throws NoSuchFieldException, IllegalAccessException {

        MockitoAnnotations.initMocks(this);

        final PollRequest pollRequest = new PollRequest();
        Field taskField = PollRequest.class.getDeclaredField("task");
        taskField.setAccessible(true);
        taskField.set(pollRequest, UUID.randomUUID());
        taskField.setAccessible(false);

        when(taskConditionService.getByTaskKey(any())).thenReturn(Optional.empty());

        assertEquals(
                taskController.poll(httpServletRequest, pollRequest).getStatusCode(),
                Responses.nonFoundResponse().getStatusCode()
        );
    }

    @Test
    public void givenValidNotReadyTask_whenCallPoll_thenReturnOkNotReady()
            throws NoSuchFieldException, IllegalAccessException {

        MockitoAnnotations.initMocks(this);
        final PollRequest pollRequest = new PollRequest();
        final UUID taskUuid = UUID.randomUUID();
        Field taskField = PollRequest.class.getDeclaredField("task");
        taskField.setAccessible(true);
        taskField.set(pollRequest, taskUuid);
        taskField.setAccessible(false);

        when(taskConditionService.getByTaskKey(any())).thenReturn(Optional.of(new TaskCondition()));

        final ResponseEntity response = taskController.poll(httpServletRequest, pollRequest);
        assertEquals(
                response.getBody(),
                TaskController.formTaskNotReady(new JsonComponent(), taskUuid)
        );

        assertEquals(
                response.getStatusCode(),
                okResponse(null).getStatusCode()
        );
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
