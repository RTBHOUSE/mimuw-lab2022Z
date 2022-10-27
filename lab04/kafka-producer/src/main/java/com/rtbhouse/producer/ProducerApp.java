package com.rtbhouse.producer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jeasy.random.EasyRandom;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ProducerApp {

    private static final String TOPIC = "my_third_topic";

    private static final Map<String, Object> kafkaProperties = new HashMap<>();

    static {
        kafkaProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        kafkaProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        kafkaProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    }

    private static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    public static void main(String[] args) throws InterruptedException {
        try (Producer<String, String> producer = new KafkaProducer<>(kafkaProperties)) {
            ExecutorService fixedThreadPool = Executors.newFixedThreadPool(20);

            IntStream.range(0, 20).forEach(i -> fixedThreadPool.submit(() -> {
                EasyRandom generator = new EasyRandom();
                for (int j = 0; j < 20000; j++) {
                    try {
                        producer.send(new ProducerRecord<>(TOPIC, RandomStringUtils.randomAlphabetic(20), GSON.toJson(generator.nextObject(Data.class)))).get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }
            }));

            fixedThreadPool.shutdown();
            fixedThreadPool.awaitTermination(100, TimeUnit.SECONDS);
        }
    }
}
