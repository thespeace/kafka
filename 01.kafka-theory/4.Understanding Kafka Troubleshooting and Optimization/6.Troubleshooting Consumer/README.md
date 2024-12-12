# Troubleshooting Consumer

<br>

## Troubleshooting - Consumer Lag
### A. 환경의 구조를 이해
Kafka로부터 Consume할 때 때때로 Consumer Lag이 발생하고 Timeout도 발생할 수 있다.  
여기서는 이러한 문제의 근본 원인을 해결하고 확인하는 방법을 간략하게 설명하고자 한다.

<br>

먼저 Kafka 환경의 구조를 이해해야 한다. 다음 사항을 식별하는 것이 중요하다.
1. Kafka 클러스터에는 몇 개의 Broker가 있는지?  
   주어진 hostname과 연관된 hostnames 및 BrokerID 목록을 가져와야 올 수 있어야 한다.   
   예) host1.foo.com:BrokerID=1
2. Consumer로부터 Consume되고 있는 Topic을 확인하고 메모해야 한다.
3. 그런 다음 Topic의 세부 사항을 확인해야 한다.  
    ```
    kafka-topics --bootstrap-server <hostName>:9092 --describe --topic <topicName> 
    ```
   
<br>

### B. 클러스터 상태 확인(1/3)
클러스터의 구조를 알아낸 후, Consumption에 영향을 미칠 수 있는 일반적인 상태 문제를 검토해야 한다.
1. 클러스터에서 Under-Replicated Partitions(URP)을 확인해야 한다.  
   ```kafka-topics --describe --under-replicated-partitions --bootstrap-server <hostName>:9092```  
   URP가 보이면 원인을 조사해야 한다.
2. 영향을 받은 Consumer Group 이름을 확인해야 한다.  
   ```kafka-consumer-groups --list --bootstrap-server <hostName>:9092 ```

<br>

### B. 클러스터 상태 확인(2/3)
3. Consumer Group에 얼마나 많은 Consumer가 있는지 와 현재 Consume 상태를 확인해야 한다.  
   ```kafka-consumer-groups --bootstrap-server <hostName>:9092 --describe --group <my-group>```  

<br>

   Output은 아래와 유사하다.
   ```
   TOPIC PARTITION CURRENT-OFFSET LOG-END-OFFSET LAG
   CONSUMER-ID HOST CLIENT-ID
   my-topic 0 2 7 5
   consumer-1-129af99c-973c-9751-a920-cefd91a779d6 /127.0.0.1 consumer-1
   my-topic 1 5 6 1
   consumer-1-129af99c-973c-9751-a920-cefd91a779d6 /127.0.0.1 consumer-1
   my-topic 2 5 6 1
   consumer-2-32c1abd7-e7b2-725d-a7bb-e1ea77b27b77 /127.0.0.1 consumer-1
   ```
   위의 Output 예에서 세 개의 Consumer가 있고 첫 번째 Consumer의 LAG 수준이 더 높은 것을 볼 수 있다.

<br>

### B. 클러스터 상태 확인(3/3)
4. LAG이 하나의 Partition에서 증가하는지 아니면 여러 Partition에서 증가하는지 확인하고 어떤 Partition이 영향을 받는지 확인한다.
5. 특히 Thread 수와 관련된 현재 Broker 구성을 확인이 필요하다. Default는 다음과 같다.  
   ```
   num.network.threads = 3
   num.io.threads = 8
   num.replica.fetchers = 1
   ```
6. Broker의 CPU 부하를 확인하여 지정된 Thread 수에 대해 CPU가 충분한지 확인해야 한다.  
   예) `top` 명령
7. JMX Metrics를 활성화했는지 여부를 확인한다.  
   그런 다음, Network Thread와 IO Thread가 과부하(Overload) 되지 않았는지 확인할 수 있다.

<br>

### C. Test Consumption
어떤 Partition이 지연되는지 확인하고 나면 Console Consumer를 사용하여 Consumption을 테스트할 수 있다.

Console Consumer를 사용하여 영향을 받는 각 Partition에서 Consume하여 Partition 자체에 문제가 없는지 확인한다.
```
kaka-console-consumer --bootstrap-server <hostname>:9092 --from-beginning --topic test1
--new -consumer --partition <partition number> > partition_test.txt
```
위 명령은 지정된 Partition의 내용을 partition_test.txt 파일로 파이프 아웃 해서 저장한다.  
이 작업이 성공적으로 완료되면 Partition이 정상임을 확인 할 수 있다.

위 내용을 분석하고 문제를 찾지 `못한` 후에는 문제가 `Consumer Application` 측에 있다고 안전하게 말할 수 있다.

<br>

## Consumer Group Offset 재설정(Reset)
* 이유 :
  * Application 외부에서 Offset을 Reset하는 옵션을 사용하면 더 깔끔하고 일관된 방식으로 재처리(Reprocess)를 수행할 수 있다.
  * Consumer Code를 변경할 필요가 없다.(예: `KafkaConsumer#seek()`).
* `Record를 재처리(Reprocess)하려는 데에는 여러 가지 이유가 있다.`
  * Consumer Code에서 Bug를 발견하고 이를 Fix한 후, 데이터를 다시 처리하는 경우
  * 결과를 보다 정확하게 계산하는 새롭고 향상된 Consumer Logic 개발 후, 데이터를 다시 처리해야 하는 경우

<br>

## Code :
* `--reset-offsets` 파라미터를 사용하면 특정 Consumer Group이 Consume하는 모든 Topic(모든 Partition 포함)을 특정 Timestamp가 나타내는 가장 빠른 Offset으로 재설정할 수 있다.
* 다른 보다 정교한 Reset 시나리오도 가능  
  예) Topic의 명시적으로 나열된 일부 Partition만 선택한 Offset으로 재설정

예시)  
Reset to first offset since 01 January 2019, 00:00:00 hrs UTC 
```
kafka-consumer-groups --reset-offsets \
--group <Group ID> \
--bootstrap-server <hostname>:9092 \
--to-datetime 2019-01-01T00:00:00.000 
```

<br>

## Read Consumer Group Offset
```
kafka-consumer-groups --describe \
--group <Group ID> \
--bootstrap-server <hostname>:9092 
```

Output Example:
```
TOPIC PARTITION CURRENT-OFFSET LOG-END-OFFSET LAG CONSUMER-ID HOST CLIENT-ID
my-topic 0 2 5 3 consumer-1-11… /127.0.0.1 consumer-1
my-topic 1 2 3 1 consumer-1-22… /127.0.0.1 consumer-2
my-topic 2 2 4 2 consumer-1-31… /127.0.0.1 consumer-1
```

<br>

## Read __consumer_offsets Topic(1/3)
__consumer_offsets Topic에는 Consumer Offset 데이터와 Consumer Group Metadata가 포함되어 있다.

이 Topic을 읽고 싶은 이유:
* Consumer가 마지막으로 Offset을 Commit한 시기와 그 Offset이 무엇인지 확인하는 경우
* Consumer Group의 Group Coordinator인 Broker를 확인하는 경우

<br>

## Read __consumer_offsets Topic(2/3)
### Broker is Online
Offset Records : 여기에는 Group에 의해 커밋된 Offset이 포함
```
kafka-console-consumer \
--consumer.config consumer.properties \
--from-beginning \
--topic __consumer_offsets \
--bootstrap-server <hostname>:9092 \
--formatter\
'kafka.coordinator.group.GroupMetadataManager$OffsetsMessageFormatter'
```

GroupMetadata Records: 여기에는 Group의 Metadata(State 및 Members)가 포함
```
…
--formatter\
'kafka.coordinator.group.GroupMetadataManager$GroupMetadataMessageFormatter'
```
Consumer Group Partition Assignment 및 Status에 대한 추가 정보를 제공

<br>

## Read __consumer_offsets Topic(3/3)
### Broker is Offline
```
kafka-dump-log \
--files /var/lib/kafka/data/__consumer_offsets-<X>.log \
--offsets-decoder \
--print-data-log
```

<br>

## Rebalancing Issues(1/2)
### Frequent Rebalancing
빈번한 리밸런싱(Frequent Rebalancing)의 일반적인 원인은 Consumer가 Data(Batch)를 처리하는 데 너무 오랜 시간이 걸린다는 것이다.

Consumer는 별도의 Thread를 사용하여 Group Coordinator인 Broker에 `Alive Signal`을 보낸다.

Group Coordinator가 `session.timeout.ms` 속성에 정의된 것보다 오랫동안 alive signal을 수신하지 않으면 해당 Consumer가 죽은것으로 간주하고 Consumer Group에서 제거한다.  
이는 물론 Rebalancing을 Trigger(촉발)한다.

또한, Consumer가 건강하지 않은 것으로 간주되는 것을 방지하기 위해, 가끔 `Poll` 할 필요가 있다.

Poll 사이에 허용되는 최대 시간 간격은 `max.poll.interval.ms` 파라미터에 정의되며 기본값은 5 분으로 설정된다.

이제 Data(Batch) 처리가 `max.poll.interval.ms`에 정의된 값을 초과하는 경우, Group Coordinator는 Consumer Group에서 해당 Consumer를 제거하고 Rebalancing을 Trigger한다.

`따라서 해결책은 Batch 를 더 작게 만들거나 Max Poll Interval 을 더 길게 만드는 것이다.`

* Metric `join-rate`: 초당 Group Join 수  
  Group Join은 Rebalancing 프로토콜의 첫 번째 단계, 값이 크면 Consumer Group이 불안정하고 Lag이 증가할 가능성이 있다.
* Metric `sync-rate`: 초당 Group Sync 수  
  Group Sync는 Rebalancing 프로토콜의 두 번째이자 마지막 단계, Join-rate와 유사하게, 값이 크면 Group이 불안정함을 나타낸다.

<br>

## Rebalancing Issues(2/2)
### Long Rebalancing Time
Rebalancing은 (Stateful) Consumer의 경우 오랜 시간이 걸릴 수 있다.

Stateful Consumer 의 예: Kafka Streams 또는 KSQL 애플리케이션

Old 버전의 Streams에서는 State Store Recovery(상태 저장소 복구)가 Rebalancing의 일부로 포함되었지만, 이제 상태 저장소 복구는 Main Loop에서 수행되므로 Rebalancing 속도가 느려지지 않는다.

Metric `join-time-avg/join-time-max`: Group Rejoin에 소요된 평균 시간/최대 시간  
이 값은 Consumer에 대해 구성된 `max.poll.interval.ms`만큼 높아질 수 있지만, 일반적으로 낮아야 한다.

<br>

## Other Important Metrics for Troubleshooting
Consumer Group의 문제를 해결하는 데 특히 유용한 몇 가지 JMX 지표가 있다.

Consumer Lag을 측정하려면 `records-lag-max` 를 사용: 모든 Partition에 대한 Record 수 측면에서 최대 Lag  
시간이 지남에 따라 값이 증가한다는 것은 Consumer Group이 Producer를 따라잡지 못하고 있다는 가장 좋은 지표이다.

Consumer의 Throughput(처리량)에 대한 아이디어를 얻으려면 다음을 사용
* `fetch-rate`: 초당 Fetch(가져오기) Request 수
* `fetch-latency-avg`, `fetch-latency-max`: Fetch(가져오기) Request에 소요된 평균 시간, 최대 시간
* `records-per-request-avg`: 각 Request의 평균 Record 수
* `bytes-consumed-rate`: 초당 Consume된 평균 Record 수

<br>

## Summary
* Troubleshooting - Consumer Lag
* Consumer Group Offset 재설정(Reset)
* Read __consumer_offsets Topic
* Rebalancing Issues