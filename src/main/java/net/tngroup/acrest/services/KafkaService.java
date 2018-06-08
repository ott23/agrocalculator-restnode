package net.tngroup.acrest.services;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class KafkaService {

    private Logger logger = LogManager.getLogger("CommonLogger");

    @Autowired
    ConfigService configService;

    private String bootstrapServers;
    private Long responseTimeout;
    private Integer testTimeout;

    private String response;


    @PostConstruct
    public void init() {
        bootstrapServers = configService.getProperty("tn.kafka.bootstrap-servers");
        responseTimeout = Long.parseLong(configService.getProperty("tn.kafka.timeout"));
        testTimeout = Integer.parseInt(configService.getProperty("tn.kafka.test-timeout"));
    }

    /*
    Параметры записи в Kafka
     */
    private Properties kafkaProducerProperties() {
        Properties configProps = new Properties();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, responseTimeout);
        return configProps;
    }

    /*
    Запись в Kafka
     */
    public String send(String topic, String key, String message) {

        long startTime = System.currentTimeMillis();

        // Инициализация продюссера Kafka
        Properties properties = kafkaProducerProperties();
        KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(properties);
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, message);

        // Отправка сообщения и обработка ответа
        kafkaProducer.send(record, (recordMetadata, e) -> {
            if (recordMetadata != null) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                response = "Success";
                logger.info("Message successfully delivered in " + elapsedTime + " ms");
            } else {
                response = "Failure";
                logger.warn("Message delivery failure");
            }
        });

        // Закрытие продюссера
        kafkaProducer.close();

        return response;
    }

    /*
    Параметры чтения из Kafka
     */
    private Properties kafkaConsumerProperties() {
        Properties configProps = new Properties();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        configProps.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return configProps;
    }

    /*
    Чтение из Kafka
     */
    public Map<String, String> read(String topic, String clientId, boolean fromBeginning, Integer maxCount) {


        // Соединение и чтение из Kafka
        Properties properties = kafkaConsumerProperties();
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, clientId);

        if (fromBeginning) {
            properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, clientId + "-fromBeginning");
        }

        if (maxCount != 0) {
            properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxCount.toString());
        }

        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(properties);
        kafkaConsumer.subscribe(Collections.singletonList(topic));

        if (fromBeginning) {
            kafkaConsumer.poll(0);
            kafkaConsumer.seekToBeginning(kafkaConsumer.assignment());
        }

        Map<String, String> taskMap = new TreeMap<>();
        ConsumerRecords<String, String> records = kafkaConsumer.poll(responseTimeout);
        for (ConsumerRecord<String, String> record : records) {
            taskMap.put(record.key(), record.value());
        }

        kafkaConsumer.close();

        return taskMap;
    }

    /*
    Проверка соединения с серверами Kafka
     */
    public synchronized boolean isKafkaNotAvailable() {
        String serversList = bootstrapServers;
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
                    socket.connect(isa, testTimeout);
                    socket.close();
                    active.set(active.get() + 1);
                } catch (IOException e) {
                    logger.debug("Kafka broker `%s` is not available", socketString);
                }
            }).start();
        }

        long time = System.currentTimeMillis();
        while (active.get() == 0 && (System.currentTimeMillis() - time) < testTimeout) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (active.get() > 0) return false;

        logger.warn("All Kafka brokers are unavailable!");
        return true;
    }

}
