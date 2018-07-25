package net.tngroup.acrestnode.databases.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import net.tngroup.acrestnode.nodeclient.databases.h2.services.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Lazy
public class CassandraConnector {

    private SettingService settingService;

    @Autowired
    public CassandraConnector(SettingService settingService) {
        this.settingService = settingService;
    }

    private Cluster cluster;
    private Session session;

    private String contactPoints;
    private Integer port;
    private String keySpace;

    public void init() {
        contactPoints = settingService.getByName("cassandra.datasource.contact-points").getValue();
        port = Integer.valueOf(settingService.getByName("cassandra.datasource.port").getValue());
        keySpace = settingService.getByName("cassandra.datasource.key-space").getValue();
    }

    public void connect() throws Exception {
        if (contactPoints == null || port == null || keySpace == null)
            throw new Exception("Cassandra connection error");
        String[] contactPointsArray = Arrays.stream(contactPoints.split(",")).map(String::trim).toArray(String[]::new);
        cluster = Cluster.builder().addContactPoints(contactPointsArray).withPort(port).build();
        session = cluster.connect(keySpace);
    }

    public void close() {
        if (session != null) session.close();
        if (cluster != null) cluster.close();
    }

    public CassandraTemplate cassandraTemplate() {
        return new CassandraTemplate(session);
    }
}