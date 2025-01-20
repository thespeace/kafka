package com.thespeace.kafkahandson.model;

/**
 * <h1>Topic 관리</h1>
 *
 * TODO 아래 Topic이 존재하지 않는다면 Kafka UI를 통해 생성하기.
 * <ul>
 *     <li>Number of partitions : 3</li>
 *     <li>Replication Factor : 2</li>
 *     <li>Time to retain data : 7 days</li>
 * </ul>
 */
public class Topic {

    public static final String MY_JSON_TOPIC = "my-json-topic";
    public static final String MY_CDC_TOPIC = "my-cdc-topic";
}
