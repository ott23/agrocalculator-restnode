package com.persoff.restkafka.services;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;


@Service
public class KafkaService {

    Logger LOG = LogManager.getLogger("KafkaLogger");

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.response-timeout}")
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
                response = "SUCCESS";
                LOG.info("Сообщение успешно доставленно за " + elapsedTime + " ms");
            } else {
                response = "FAILURE";
                LOG.warn("Ошибка доставки сообщения");
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
    public List<String> read(String topic, String key) {

        List<String> recordsList = new ArrayList<>();

        // Соединение и чтение из Kafka
        Properties properties = kafkaConsumerProperties();
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, key);
        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(properties);
        kafkaConsumer.subscribe(Arrays.asList(topic));
        ConsumerRecords<String, String> records = kafkaConsumer.poll(responseTimeout);
        kafkaConsumer.close();

        // Обработка данных из Kafka
        for (ConsumerRecord<String, String> record : records) {
            if (key.equals(record.key())) {
                recordsList.add(record.value());
                LOG.info("Получено сообщение: (" + record.key() + ", " + record.value() + "), оффсет: " + record.offset());
            }
        }

        return recordsList;
    }

    /*
    Проверка соединения с серверами Kafka
     */
    public boolean testSocket() {
        String serversList = bootstrapServers;
        String[] sockets = serversList.split(",");
        int unactive = 0;
        for (int i = 0; i < sockets.length; i++) {
            try {
                String[] socket = sockets[i].split(":");
                (new Socket(socket[0], Integer.valueOf(socket[1]))).close();
            } catch (IOException e) {
                LOG.info("Kafka сервер %s недоступен", sockets[i]);
                unactive++;
            }
        }
        if (unactive < sockets.length) return true;
        LOG.warn("Все Kafka серверы недоступны!");
        return false;
    }

}
