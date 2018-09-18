package net.tngroup.acrestnode.controllersTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.tngroup.acrestnode.web.components.JsonComponent;
import net.tngroup.acrestnode.web.security.components.SecurityComponent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public abstract class BaseReadDeleteTest<T> {

    private final static UUID MOCK_CLIENT_ID = UUID.randomUUID();

    protected EntityService<T> service;

    protected EntityController<T> controller;

    @Mock
    private JsonComponent jsonComponent;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Spy
    private SecurityComponent securityComponent;


    public interface EntityService<T> {

        T save(T entity);

        T getById(UUID id);

        List<T> getAllByClient(UUID client);

        boolean deleteById(UUID id);
    }

    public interface EntityController<T> {

        ResponseEntity getList(HttpServletRequest httpServletRequest);

        ResponseEntity save(HttpServletRequest httpServletRequest, T entity);

        ResponseEntity getById(HttpServletRequest httpServletRequest, UUID uuid);

        ResponseEntity deleteById(HttpServletRequest httpServletRequest, UUID uuid);
    }

    interface Entity<T>{

        void setClient(UUID clientId);

        T get();
    }

    protected abstract EntityController<T> provideEntityController();

    protected abstract Entity<T> newEntity();

    @Before
    public void initValidSecurityComponent() {
        securityComponent = Mockito.spy(new ValidSecurityComponent(MOCK_CLIENT_ID));
        controller = provideEntityController();
    }

    //region ==================== GetList ====================

    @Test
    public void givenNullClient_whenCallGetList_thenShouldNotBeCallEntityService() {

        securityComponent = Mockito.spy(new WrongSecurityComponent());
        MockitoAnnotations.initMocks(this);

        HttpStatus resultStatus = controller.getList(httpServletRequest).getStatusCode();

        assertEquals(resultStatus, HttpStatus.FAILED_DEPENDENCY);

        verifyZeroInteractions(service);
    }

    @Test
    public void givenClient_whenCallGetList_thenShouldBeCallGeozoneServiceAndReturnResponse() {

        MockitoAnnotations.initMocks(this);

        List<T> result = new ArrayList<>();

        when(service.getAllByClient(any())).thenReturn(result);

        ResponseEntity actual = controller.getList(httpServletRequest);

        verify(service, times(1)).getAllByClient(any());

        assertEquals(actual.getStatusCode(), HttpStatus.OK);

        assertEquals(actual.getBody(), result);
    }

    //endregion

    //region ==================== Get ====================

    @Test
    public void givenValidClientIdAndEntityId_whenCallGet_thenShouldBeReturnEntity() throws JsonProcessingException {

        MockitoAnnotations.initMocks(this);

        final Entity<T> mockEntity = newEntity();
        final ObjectMapper objectMapper = new ObjectMapper();
        mockEntity.setClient(MOCK_CLIENT_ID);

        when(service.getById(any())).thenReturn(mockEntity.get());
        when(jsonComponent.getObjectMapper()).thenReturn(objectMapper);

        assertEquals(
                controller.getById(httpServletRequest, UUID.randomUUID()).getBody(),
                objectMapper.writeValueAsString(mockEntity.get())
        );
    }

    //endregion


}
