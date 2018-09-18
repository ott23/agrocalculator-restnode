package net.tngroup.acrestnode.controllersTest;

import net.tngroup.acrestnode.databases.cassandra.models.Unit;
import net.tngroup.acrestnode.web.controllers.UnitController;
import org.junit.Before;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

public class UnitControllerTest extends BaseReadDeleteTest<Unit> {

    private UnitController unitController;

    @Override
    protected EntityController<Unit> provideEntityController() {

        return new EntityController<Unit>() {

            @Override
            public ResponseEntity getList(HttpServletRequest httpServletRequest) {
                return unitController.getList(httpServletRequest);
            }

            @Override
            public ResponseEntity save(HttpServletRequest httpServletRequest, Unit entity) {
                return unitController.save(httpServletRequest, entity);
            }

            @Override
            public ResponseEntity getById(HttpServletRequest httpServletRequest, UUID uuid) {
                return unitController.getById(httpServletRequest, uuid);
            }

            @Override
            public ResponseEntity deleteById(HttpServletRequest httpServletRequest, UUID uuid) {
                return unitController.deleteById(httpServletRequest, uuid);
            }
        };
    }

    @Override
    protected Entity<Unit> newEntity() {
        return new Entity<Unit>() {

            private Unit unit = new Unit();

            @Override
            public void setClient(UUID clientId) {
                unit.setClient(clientId);
            }

            @Override
            public Unit get() {
                return unit;
            }
        };
    }

    @Before
    public void initController() {

    }

}
