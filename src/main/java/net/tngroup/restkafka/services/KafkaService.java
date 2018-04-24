package net.tngroup.restkafka.services;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.Socket;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

@Service
public class KafkaService {

    Logger logger = LogManager.getLogger("CommonLogger");

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.response-timeout}")
    private Long responseTimeout;

    private String response;

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
            properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, clientId+"-fromBeginning");
        }

        if (maxCount == 0) {
            maxCount = Integer.MAX_VALUE;
        } else {
            properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxCount.toString());
        }

        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(properties);
        kafkaConsumer.subscribe(Collections.singletonList(topic));

        if (fromBeginning) {
            kafkaConsumer.poll(0);
            kafkaConsumer.seekToBeginning(kafkaConsumer.assignment());
        }

        Map<String, String> taskMap = new TreeMap<>();
        ConsumerRecords<String, String> records;
        long counter = 0;
        do {
            records = kafkaConsumer.poll(responseTimeout);
            for (ConsumerRecord<String, String> record : records) {
                taskMap.put(record.key(), record.value());
            }
            counter += records.count();
        } while (records.count() > 0 && counter < maxCount);

        kafkaConsumer.close();

        return taskMap;
    }

    /*
    Проверка соединения с серверами Kafka
     */
    public boolean isKafkaNotAvailable() {
        String serversList = bootstrapServers;
        String[] sockets = serversList.split(",");

        // Active Kafka handlers counter
        int active = 0;

        // Loop iterating while no one responses
        for (String socket : sockets) {
            try {
                String[] socketArray = socket.split(":");
                (new Socket(socketArray[0], Integer.valueOf(socketArray[1]))).close();
                active++;
            } catch (IOException e) {
                logger.debug("Kafka broker `%s` is not available", socket);
            }
            if (active > 0) break;
        }

        if (active > 0) return false;

        logger.warn("All Kafka brokers are unavailable!");
        return true;
    }

}
