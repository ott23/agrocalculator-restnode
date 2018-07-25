package net.tngroup.acrestnode.web.components;

import net.tngroup.acrestnode.nodeclient.databases.h2.services.SettingService;
import net.tngroup.acrestnode.nodeclient.components.SettingComponent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Lazy
public class KafkaComponent {

    // Loggers
    private Logger logger = LogManager.getFormatterLogger("CommonLogger");

    private SettingComponent settingComponent;
    private SettingService settingService;

    // Settings
    private String kafkaBootstrapServers;
    private Long kafkaSendTimeout;
    private Long kafkaPollTimeout;
    private Integer kafkaTestTimeout;
    private String kafkaGroupId;
    private String kafkaResultsTopic;


    @Autowired
    public KafkaComponent(SettingComponent settingComponent,
                          SettingService settingService) {
        this.settingComponent = settingComponent;
        this.settingService = settingService;
    }

    public void init() {
        while (!settingComponent.isSettingReady()) Thread.yield();

        kafkaBootstrapServers = settingService.getByName("kafka.bootstrap-servers").getValue();
        kafkaPollTimeout = Long.parseLong(settingService.getByName("kafka.poll-timeout").getValue());
        kafkaSendTimeout = Long.parseLong(settingService.getByName("kafka.send-timeout").getValue());
        kafkaTestTimeout = Integer.parseInt(settingService.getByName("kafka.test-timeout").getValue());
        kafkaResultsTopic = settingService.getByName("kafka.results-topic").getValue();
        kafkaGroupId = settingService.getByName("restnode.kafka.group-id").getValue();
    }

    private Properties kafkaProducerProperties() {
        Properties configProps = new Properties();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, kafkaSendTimeout);
        configProps.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 1024 * 1024 * 128);
        return configProps;
    }

    private Properties kafkaConsumerProperties() {
        Properties configProps = new Properties();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaGroupId);
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1");
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return configProps;
    }


    public String send(String topic, String key, String message) {

        long startTime = System.currentTimeMillis();

        // Инициализация продюссера Kafka
        Properties properties = kafkaProducerProperties();
        KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(properties);
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, message);

        Future<RecordMetadata> future = kafkaProducer.send(record, (recordMetadata, e) -> {
            if (recordMetadata != null) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                logger.info("Task `%s`: message delivered to Kafka in %d ms", key, elapsedTime);
            }
        });

        // Закрытие продюссера
        kafkaProducer.close();

        // Throwing an exception if no response from Kafka
        try {
            future.get();
            return "Success";
        } catch (Exception e) {
            return "Failure";
        }
    }

    /*
    Чтение из Kafka
     */
    public Map<String, String> read(boolean fromBeginning) {

        // Соединение и чтение из Kafka
        Properties properties = kafkaConsumerProperties();
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, kafkaGroupId);

        if (fromBeginning) {
            properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
        }

        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(properties);
        kafkaConsumer.subscribe(Collections.singletonList(kafkaResultsTopic));

        if (fromBeginning) {
            kafkaConsumer.poll(0);
            kafkaConsumer.seekToBeginning(kafkaConsumer.assignment());
        }

        Map<String, String> taskMap = new TreeMap<>();
        ConsumerRecords<String, String> records = kafkaConsumer.poll(kafkaPollTimeout);

        for (ConsumerRecord<String, String> record : records) {
            taskMap.put(record.key(), record.value());
        }

        kafkaConsumer.close();

        return taskMap;
    }



    public void testSocket() throws Exception {

        String serversList = kafkaBootstrapServers;
        String[] sockets = serversList.replace(" ", "").split(",");

        // Active Kafka handlers counter
        AtomicInteger active = new AtomicInteger(0);

        // Loop iterating while no one responses
        for (String socketString : sockets) {
            String[] socketArray = socketString.split(":");
            new Thread(() -> {
                try {
                    SocketAddress isa = new InetSocketAddress(socketArray[0], Integer.valueOf(socketArray[1]));
                    Socket socket = new Socket();
                    socket.connect(isa, kafkaTestTimeout);
                    socket.close();
                    active.set(active.get() + 1);
                } catch (IOException e) {
                    logger.debug("Kafka broker `%s` is not available", socketString);
                }
            }).start();
        }

        long time = System.currentTimeMillis();
        while (active.get() == 0 && (System.currentTimeMillis() - time) < kafkaTestTimeout) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (active.get() == 0) throw new Exception("Kafka brokers are unavailable!");
    }

}