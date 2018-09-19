package net.tngroup.acrestnode.controllersTest;

import net.tngroup.acrestnode.databases.cassandra.models.Unit;
import net.tngroup.acrestnode.databases.cassandra.services.UnitService;
import net.tngroup.acrestnode.databases.cassandra.services.base.ClientEntityService;
import net.tngroup.acrestnode.web.components.JsonComponent;
import net.tngroup.acrestnode.web.controllers.UnitController;
import net.tngroup.acrestnode.web.controllers.base.ClientEntityController;
import net.tngroup.acrestnode.web.security.components.SecurityComponent;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import javax.servlet.http.HttpServletRequest;

public class UnitControllerTest extends ClientEntityControllerTest<Unit> {

    @InjectMocks
    private UnitController unitController;
    @Mock
    private JsonComponent jsonComponent;
    @Mock
    private UnitService unitService;
    @Spy
    private SecurityComponent securityComponent;
    @Mock
    private HttpServletRequest httpServletRequest;


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
}
