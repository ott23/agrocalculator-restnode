package net.tngroup.acrestnode.controllersTest;

import net.tngroup.acrestnode.databases.cassandra.models.Unit;
import net.tngroup.acrestnode.databases.cassandra.services.UnitService;
import net.tngroup.acrestnode.databases.cassandra.services.base.ClientEntityService;
import net.tngroup.acrestnode.web.components.JsonComponent;
import net.tngroup.acrestnode.web.controllers.Responses;
import net.tngroup.acrestnode.web.controllers.UnitController;
import net.tngroup.acrestnode.web.controllers.base.ClientEntityController;
import net.tngroup.acrestnode.web.security.components.SecurityComponent;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UnitControllerTest extends ClientEntityControllerTest<Unit> {

    private final static UUID MOCK_CLIENT_ID = UUID.randomUUID();

    @InjectMocks
    private UnitController unitController;
    @Mock
    private JsonComponent jsonComponent;
    @Mock
    private UnitService unitService;
    @Spy
    private SecurityComponent securityComponent = new ValidSecurityComponent(MOCK_CLIENT_ID);
    @Mock
    private HttpServletRequest httpServletRequest;

    //region ==================== ClientEntityControllerTest ====================

    @Override
    protected ClientEntityController<Unit> getEntityController() {
        return unitController;
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
    protected ClientEntityService<Unit> getClientEntityService() {
        return unitService;
    }

    @Override
    protected JsonComponent getJsonComponent() {
        return jsonComponent;
    }

    @Override
    protected Unit newEntity() {
        return new Unit();
    }

    @Override
    protected void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    //endregion

    //region ==================== Save ====================

    @Test
    public void givenExistUnitWithOtherUserId_whenCallSave_thenShouldBeReturnFailedDependency() {

        MockitoAnnotations.initMocks(this);

        final Unit mockUnitFromDb = new Unit();
        mockUnitFromDb.setClient(UUID.randomUUID());
        mockUnitFromDb.setId(UUID.randomUUID());

        when(unitService.getById(any())).thenReturn(mockUnitFromDb);
        verify(unitService, never()).getAllByImei(any());
        verify(unitService, never()).save(any());

        final Unit request = new Unit();
        request.setId(UUID.randomUUID());

        Assert.assertEquals(
                unitController.save(httpServletRequest, request).getStatusCode(),
                Responses.failedDependencyResponse().getStatusCode()
        );
    }

    @Test
    public void givenExistUnitWithUserId_whenCallSave_thenShouldBeReturnSaveUnit() {

        MockitoAnnotations.initMocks(this);

        final Unit request = new Unit();
        request.setId(UUID.randomUUID());
        request.setClient(MOCK_CLIENT_ID);

        final Unit mockUnitFromDb = new Unit();
        mockUnitFromDb.setClient(MOCK_CLIENT_ID);
        mockUnitFromDb.setId(request.getId());

        when(unitService.getById(any())).thenReturn(mockUnitFromDb);
        when(unitService.getAllByImei(anyString())).thenReturn(List.of(mockUnitFromDb));
        when(unitService.save(any())).thenReturn(request);

        Assert.assertEquals(
                unitController.save(httpServletRequest, request).getBody(),
                Responses.okResponse(request).getBody()
        );

    }


    //endregion
}
