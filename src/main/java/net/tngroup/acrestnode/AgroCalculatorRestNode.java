package net.tngroup.acrestnode;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {CassandraDataAutoConfiguration.class})
public class AgroCalculatorRestNode {

    public static void main(String[] args) {
        new SpringApplicationBuilder(AgroCalculatorRestNode.class).run(args);
    }

}
