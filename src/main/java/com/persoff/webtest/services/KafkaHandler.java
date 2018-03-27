package com.persoff.webtest.services;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Service
public class KafkaHandler {

    Logger LOG = LoggerFactory.getLogger(KafkaHandler.class);

    @Autowired
    Properties kafkaProducerProperties;

    @Autowired
    Properties kafkaConsumerProperties;

    private String response;


    public String send(String topic, String key, String message) {

        long startTime = System.currentTimeMillis();

        Properties properties = kafkaProducerProperties;
        KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(properties);

        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, message);

        kafkaProducer.send(record, new Callback() {
            @Override
            public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                if (recordMetadata != null) {
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    response = "SUCCESS";
                    LOG.info("Message was successfully delivered in " + elapsedTime + " ms");
                } else {
                    response = "FAILURE";
                    LOG.info("Error during message delivery");
                }

            }
        });

        kafkaProducer.close();

        return response;
    }

    public List<String> read(String topic, String key) {

        Properties properties = kafkaConsumerProperties;
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, key);
        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(properties);
        kafkaConsumer.subscribe(Arrays.asList(topic));
        ConsumerRecords<String, String> records = kafkaConsumer.poll(5000);
        kafkaConsumer.close();

        List<String> recordsList = new ArrayList<>();
        for (ConsumerRecord<String, String> record : records) {
            if (key.equals(record.key())) {
                recordsList.add(record.value());
                LOG.info("Received message: (" + record.key() + ", " + record.value() + ") at offset " + record.offset());
            }
        }

        return recordsList;

    }

}
