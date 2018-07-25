package net.tngroup.acrestnode.databases.cassandra.repositories;

import net.tngroup.acrestnode.databases.cassandra.CassandraConnector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.cassandra.repository.support.CassandraRepositoryFactory;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

@Configuration
public class RepositoryConfig {

    @Bean
    @Lazy
    public ClientRepository clientRepository(CassandraConnector cassandraConnector)
    {
        final RepositoryFactorySupport support = new CassandraRepositoryFactory(cassandraConnector.cassandraTemplate());
        return support.getRepository(ClientRepository.class);
    }

    @Bean
    @Lazy
    public MessageRepository messageRepository(CassandraConnector cassandraConnector)
    {
        final RepositoryFactorySupport support = new CassandraRepositoryFactory(cassandraConnector.cassandraTemplate());
        return support.getRepository(MessageRepository.class);
    }

    @Bean
    @Lazy
    public TaskConditionRepository taskConditionRepository(CassandraConnector cassandraConnector)
    {
        final RepositoryFactorySupport support = new CassandraRepositoryFactory(cassandraConnector.cassandraTemplate());
        return support.getRepository(TaskConditionRepository.class);
    }

    @Bean
    @Lazy
    public TaskResultRepository taskResultRepository(CassandraConnector cassandraConnector)
    {
        final RepositoryFactorySupport support = new CassandraRepositoryFactory(cassandraConnector.cassandraTemplate());
        return support.getRepository(TaskResultRepository.class);
    }

    @Bean
    @Lazy
    public UnitRepository unitRepository(CassandraConnector cassandraConnector)
    {
        final RepositoryFactorySupport support = new CassandraRepositoryFactory(cassandraConnector.cassandraTemplate());
        return support.getRepository(UnitRepository.class);
    }

}