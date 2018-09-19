package net.tngroup.acrestnode.controllersTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.tngroup.acrestnode.databases.cassandra.models.base.ClientEntity;
import net.tngroup.acrestnode.databases.cassandra.services.base.ClientEntityService;
import net.tngroup.acrestnode.utils.Json;
import net.tngroup.acrestnode.web.components.JsonComponent;
import net.tngroup.acrestnode.web.controllers.Responses;
import net.tngroup.acrestnode.web.controllers.base.ClientEntityController;
import net.tngroup.acrestnode.web.security.components.SecurityComponent;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public abstract class ClientEntityControllerTest<T extends ClientEntity> {

    private final static UUID MOCK_CLIENT_ID = UUID.randomUUID();

    private ClientEntityController<T> controller;

    protected abstract ClientEntityController<T> getEntityController();

    protected abstract void setSecurityComponent(SecurityComponent securityComponent);

    protected abstract HttpServletRequest getHttpServletMock();

    protected abstract ClientEntityService<T> getClientEntityService();

    protected abstract JsonComponent getJsonComponent();

    protected abstract T newEntity();

    protected abstract void initMocks();

    private void defaultInit() {
        setSecurityComponent(new ValidSecurityComponent(MOCK_CLIENT_ID));
        initMocks();
        controller = getEntityController();
    }

    //region ==================== GetList ====================

    @Test
    public void givenNullClient_whenCallGetList_thenShouldNotBeCallEntityService() {

        setSecurityComponent(new WrongSecurityComponent());
        initMocks();
        controller = getEntityController();

        HttpStatus resultStatus = controller.getList(getHttpServletMock()).getStatusCode();

        assertEquals(resultStatus, HttpStatus.FAILED_DEPENDENCY);

        verifyZeroInteractions(getClientEntityService());
    }

    @Test
    public void givenClient_whenCallGetList_thenShouldBeCallGeozoneServiceAndReturnResponse() throws JsonProcessingException {

        defaultInit();

        List<T> result = new ArrayList<>();

        when(getClientEntityService().getAllByClient(any())).thenReturn(result);

        ResponseEntity actual = controller.getList(getHttpServletMock());

        verify(getClientEntityService(), times(1)).getAllByClient(any());

        assertEquals(actual.getStatusCode(), HttpStatus.OK);

        assertEquals(actual.getBody(), Json.objectMapper.writeValueAsString(result));
    }

    //endregion

    //region ==================== Get ====================

    @Test
    public void givenValidClientIdAndEntityId_whenCallGet_thenShouldBeReturnEntity() throws JsonProcessingException {

        defaultInit();

        final T mockEntity = newEntity();
        final ObjectMapper objectMapper = new ObjectMapper();
        mockEntity.setClient(MOCK_CLIENT_ID);

        when(getClientEntityService().getById(any())).thenReturn(mockEntity);
        when(getJsonComponent().getObjectMapper()).thenReturn(objectMapper);

        assertEquals(
                controller.getById(getHttpServletMock(), UUID.randomUUID()).getBody(),
                objectMapper.writeValueAsString(mockEntity)
        );
    }

    @Test
    public void givenNonExistedID_whenCallGet_thenShouldBeReturnNotFoundResponse() {

        defaultInit();

        when(getClientEntityService().getById(any())).thenReturn(null);

        assertEquals(
                Responses.nonFoundResponse().getStatusCode(),
                controller.getById(getHttpServletMock(), UUID.randomUUID()).getStatusCode()
        );

    }

    @Test
    public void givenClientEntityIdFromOtherClient_whenCallGet_thenShouldBeReturnFailedDependency() {

        defaultInit();

        final T mockEntity = newEntity();
        mockEntity.setClient(UUID.randomUUID());

        when(getClientEntityService().getById(any())).thenReturn(mockEntity);
        assertEquals(
                controller.getById(getHttpServletMock(), UUID.randomUUID()).getStatusCode(),
                Responses.failedDependencyResponse().getStatusCode()
        );
    }

    //endregion

    //region ==================== Delete ====================

    @Test
    public void givenValidClientEntityIdAndClientId_whenCallDelete_thenShouldBeReturnSuccessResponse() {

        defaultInit();

        final T entity = newEntity();
        entity.setClient(MOCK_CLIENT_ID);

        when(getClientEntityService().getById(any())).thenReturn(entity);
        when(getClientEntityService().deleteById(any())).thenReturn(true);

        assertEquals(
                controller.deleteById(getHttpServletMock(), UUID.randomUUID()),
                Responses.successResponse()
        );
    }

    @Test
    public void givenInvalidGeozoneId_whenCallDelete_thenShouldBeReturnNotFound() {

        defaultInit();
        when(getClientEntityService().getById(any())).thenReturn(null);

        assertEquals(
                controller.deleteById(getHttpServletMock(), UUID.randomUUID()),
                Responses.nonFoundResponse()
        );
    }

    @Test
    public void givenInvalidClientId_whenCallDelete_thenShouldBeReturnFailedDependency() {

        defaultInit();

        final T mockEntity = newEntity();
        mockEntity.setClient(UUID.randomUUID());

        when(getClientEntityService().getById(any())).thenReturn(mockEntity);

        assertEquals(
                controller.deleteById(getHttpServletMock(), UUID.randomUUID()),
                Responses.failedDependencyResponse()
        );
    }

    @Test
    public void givenValidClientIdAndEntityId_whenTwoClientDoDeletePermanent_thenReturnNonFound() {

        defaultInit();

        final T mockEntity = newEntity();
        mockEntity.setClient(MOCK_CLIENT_ID);

        when(getClientEntityService().getById(any())).thenReturn(mockEntity);
        when(getClientEntityService().deleteById(any())).thenReturn(false);

        assertEquals(
                controller.deleteById(getHttpServletMock(), UUID.randomUUID()),
                Responses.nonFoundResponse()
        );

    }

    //endregion

}
