# Tuning Producer

<br>

## Data Formats
* String
* Short, Integer, Long
* Float, Double
* UUID
* Binary (`byte[]`, `ByteBuffer`)
* JSON
* AVRO
* ProtoBuf

https://kafka.apache.org/36/javadoc/org/apache/kafka/common/serialization/Serializer.html  
https://docs.confluent.io/cloud/current/sr/fundamentals/serdes-develop/index.html  

<br>

## Serialization and Deserialization
![Serialization and Deserialization](../images/23.Serialization%20and%20Deserialization.PNG)

* Serialization는 객체를 Bytes Stream으로 변환하는 프로세스
* Deserialization는 위와 반대의 프로세스
* Serialization/Deserialization 프로세스는 일반적으로 CPU에 바인딩
* Throughput(처리량)을 증가시키기 위해 Record Payload를 최대한 단순하게 유지

<br>

## Custom SerDes
Kafka와 함께 사용하려는, 지원되지 않는 Data Format이 있는 경우, Custom SerDes를 구현할 수 있다.  
Confluent Example Repository는 템플릿 기반 serdes를 구현하는 방법을 보여준다.

### 구현 과정
1. `org.apache.kafka.common.serialization.Serializer` 를 구현하여 Data Type `T`에 대한 `Serializer`를 작성
2. `org.apache.kafka.common.serialization.Deserializer` 를 구현하여 Data Type `T`에 대한 `Deserializer`를 작성
3. `org.apache.kafka.common.serialization.Serde` 를 구현하여 `T`용 `Serde`를 작성  
   `Serdes.serdeFrom(Serializer<T>, Deserializer<T>`)과 같은 Helper Functions를 활용할 수 있다.

https://github.com/confluentinc/kafka-streams-examples/tree/5.1.2-post/src/main/java/io/confluent/examples/streams/utils

<br>

## AVRO 권장

Apache Avro는 Kafka와 함께 사용되는 통신 프로토콜에 사용될 뿐만 아니라, HDFS에 Persistent Data를 저장하기 위해 널리 사용되는 컴팩트 바이너리 형식에 대한 Data Serialization Standard이다.  
Avro를 사용하는 장점 중 하나는 매우 우수한 수집(Ingestion) 성능을 제공할 수 있는 가볍고 빠른 Data Serialization 및 Deserialization이 가능하기 때문이다.

* Avro는 Kafka 에코 시스템 뿐만 아니라 Hadoop 등에서도 널리 사용된다. 따라서, 이 Data Format을 지원하는 도구가 많이 있다.
* JSON과 직접 맵핑된다. 따라서 사용하기 편하다.
* 매우 콤팩트한 Format을 가지고 있으며, 매우 빠르게 사용할 수 있다.
* 다양한 프로그래밍 언어에 대한 뛰어난 바인딩을 갖추고 있어, 이벤트 데이터 작업을 더 쉽게 만드는 Java 객체를 생성할 수 있다.
* 데이터 파일을 읽거나 쓰거나 RPC 프로토콜을 사용하거나 구현하는 데 코드 생성이 필요하지 않다.  
  코드 생성은 선택적 최적화 방안 중 하나이다.
* 순수 JSON으로 정의된 풍부하고 확장 가능한 스키마 언어가 있다.
* 시간이 지남에 따라 데이터가 발전할 수 있도록 최고의 호환성 개념을 갖추고 있다.
* 더 자세한 내용 참고 : https://www.confluent.io/blog/avro-kafka-data/

<br>

## Partition 개수
* Topic의 Partition 개수는 Consumer의 확장성에 정비례하다.  
  필요한 컴퓨팅 리소스가 충분히 있으며, 최대 확장성이 필요한 경우, 해당 Topic 생성시에 Partition 개수를 높게 지정하는 것을 권장하고 있다.
* Consume할 수 있는 Partition보다 더 많은 Consumer가 있는 Consumer Group이 있는 경우, Extra Consumer는 Idle(유휴) 상태가 된다.  
  많은 경우에서 이것은 낭비인 경우가 많다.
* 하지만, Extra Consumer가 적합한 시나리오가 있다.  
  이는 Kafka Streams Application, Consumer와 같이 Consumer가 Stateful(상태를 유지)인 경우  
  Extra Consumer를 Standby(대기)로 지정하고 로컬 상태를 복제하여, Source Consumer가 Fail(실패)할 경우 실패한 Consumer가 중단된 위치를 신속하게 이어받을 수 있다.

<br>

## Partitioning 전략
* Default : Compute `Hash` of Record `Key`  
  Default Partition 전략은 많은 경우에 좋은 Partition 전략이다.
* 그러나, 이로 인해 Unbalanced Partition이 발생하는 시나리오가 있을 수 있다.
* 목표는 주어진 Key에 대해 Partition 간에 데이터를 균등하게 분배하는 최상의 Partition 전략을 선택하는 것이다.

<br>

## Throttle Mechanism
![Throttle Mechanism](../images/24.Throttle%20Mechanism.PNG)

1. 단일 Broker의 Replication에 대한 최대 Bandwidth를 결정하고, Admin이 리밸런스 혹은 다른 관리 작업을 시작한다.
2. Throttle Quota 및 Throttle Replicas 정보가 Broker로 전송된다.
3. FollowerQuotaRate가 초과되지 않은 경우에만 Follower가 Fetch Request에 Throttled Partition을 포함을 시킨다.  
   이제 Request는 고정된 크기이며, Fetcher는 공정성을 보장하기 위해 Partition이 전달되는 순서를 무작위로 지정해 전달한다.  
4. Leader는 Request에 정의된 순서대로 Partition을 처리한다.  
   LeaderQuotaRate가 초과되면 Throttled Partition이 Response에서 제외되고 전달한다.
5. Follower는 Response를 읽고 수신된 Bytes 만큼 Quota를 증가시킨다. (다시 3번으로 프로세스가 반복된다.)

https://cwiki.apache.org/confluence/display/KAFKA/KIP-73+Replication+Quotas


<br>

## Replication Quotas
* Replication Quotas는 기존 데이터를 Broker 간에 이동해야 할 때 Network Bandwidth(네트워크 대역폭)을 보호하는 데 매우 유용하다.
* 이는 일반적으로 필요하며, Confluent Auto Data Rebalancer 및 Kafka reassignpartition 도구 사용시 또는 Broker를 새 시스템으로 교체시에 대해 고려해야 한다.
* Replication Quotas는 Broker 당 할당한다.  
  `leader.replication.throttled.rate`를 `follower.replication.throttled.rate`와 별도로 제어할 수 있다.  
  그러나, 일반적인 경우에는 Network Bandwidth에 대한 제약 조건에 따라 두 값을 모두 동일한 값으로 설정해서 사용하고 있다.
  
<br>

## Compacting Topics
* Kafka는 모든 Key에 대한 마지막 값의 존재를 보장한다.
* 고유한 Key가 몇 개인지 알 수 없다.
* Hard Retention Policy를 정의하여 Disk Space 사용량을 제한할 수 있다.  
  일부 고유한 Key가 손실되어도 문제 없는 경우에만 제한 할 수 있다.

<br>

## 중요한 설정 파라미터
* `batch.size` : 처리될 Batch 크기를 결정
* `linger.ms` : Batch를 충분히 빨리 채우지 못하는 느린 Producer에 대해 중요
* `buffer.memory` : Producer가 서버로 전송되기를 기다리는 Record를 Buffering 하는 데 사용할 수 있는 총 Memory Bytes 수  
  Batch 처리가 얼마나 잘 작동하는지에 영향을 미친다.  
  Memory가 충분하지 않으면 Producer는 차단(`max.block.ms`)하거나 Exception 을 발생시킨다.
* `max.in.flight.requests.per.connection` : Message 순서(Order)에 영향을 미치는 옵션
* `acks` : 내구성(Durability)에 영향을 미치는 옵션

<br>

## Batch Size
메시지당 Latency를 활용하여 Throughput(처리량)을 늘리고 싶을 때가 있다.  
Message Batch 처리는 Producer의 RPC 호출이 덜 필요하고 일반적으로 Message Batch 처리가 더 나은 압축 비율을 제공한다는 사실로 인해 더 높은 Throughput(처리량)을 제공한다.

![Batch Size](../images/25.Batch%20Size.PNG)

* `Batch Size` : 메시지 개수 대신 Batch Size는 총 Bytes 단위로 Batch 크기를 측정한다. 
  이는 Kafka Broker에 메시지를 보내기 전에 수집할 Data Bytes 수를 제어한다는 의미이다.  
  따라서, 사용 가능한 메모리를 초과하지 않고 이 값을 최대한 높게 설정하는 것을 권장하고 있다.  
  기본값이 16,384(16kB)인지 확인, 그러나 Buffer Size를 늘리면 절대 가득 차지 않을 수 있다.  
  밀리초 단위의 Linger Time과 같은 다른 트리거를 기반으로 Producer는 최종적으로 Data를 보낸다.
* `Linger Time` : Asynchronous(비동기) 모드에서 데이터를 버퍼링하기 위해 linger.ms는 최대 시간을 설정한다.  
  예를 들어 linger.ms=100으로 설정하면 100 ms 동안 메시지를 모아서 한 번에 보낼 수 있다.  
  여기서,버퍼링으로 인해 메시지 전달 Latency가 추가되지만 이로 인해 처리량이 향상된다.
* `Buffer Memory` : 기본 32 MB; Producer가 Batch 처리를 위해 할당한 전체 Buffer


<br>

## Producer Metrics
![Producer Metrics](../images/26.Producer%20Metrics.PNG)

Producer를 튜닝할 때, 가장 좋은 방법은 Producer의 주요 지표(Metrics)의 동작을 모니터링하는 것이다.

측정항목(Metrics)에는 두 가지 주요 카테고리가 있다:
* Producer가 처리하는 모든 Topic에 대해 집계된 Producer Metrics
* Producer가 처리하는 Topic별 Metrics

#### 1)User processing time : User Code에서 소요된 시간, 특히 Acknowledgment를 위해 User Callbacks을 실행하는 데 소요된 시간

<br>

## Producer Metrics - per Topic
![Producer Metrics - per Topic](../images/27.Producer%20Metrics%20-%20per%20Topic.PNG)

<br>

## Producer Compression(압축)

![Producer Compression](../images/28.Producer%20Compression.PNG)

* Producer Compression은 `compression.type` 속성을 설정하여 활성화된다. 가능한 값은 `gzip`, `lz4`, `snappy`, `zstd` 또는 `none`(기본값)이다.
* `buffer.size > 0` 이면 Send 전에 Send Buffer에 있던 Message의 Batch들이 함께 압축된다. `buffer.size = 0` 이면 메시지가 전송될 때 개별적으로 압축된다.
* `Consumed Batches의 Offset 관리`
  * 둘 이상의 Message Batch가 압축되는 경우 전체 Batch가 단일 단위로 Consumer에게 전달된다.
  * Consumer는 Batch 처리의 메시지를 반복하면서 소비된 오프셋(Consumed Offset) 을 다시 Broker에 Commit을 한다.
  * Offset이 Batch 중간 메시지에 해당하고 Consumer가 Shutdown되면 Broker는 다음에 Consumer Group이 Polling할 때 전체 Batch를 다시 보낸다. 이로 인해 메시지가 중복 처리될 수 있다.

<br>

![Producer Compression](../images/29.Producer%20Compression2.PNG)

* Broker Compression은 `compression.type` 속성을 통해 설정된다.  
  이는 동일한 이름의 config override를 통해 Topic 레벨에서 재정의될 수 있다.  
  가능한 값은 `zstd`, `gzip`, `lz4`, `snappy`, `uncompressed` 또는 `producer`(기본값)이다.
* 기본값(`producer`)을 사용하는 경우 Broker는 Payload를 수정/재압축하지 않고 Producer로부터 Message와 Batch를 있는 그대로 가져온다.
* Broker의 압축 설정이 다른 경우, 메시지를 디스크에 쓰기 전에 메시지의 압축을 풀고 명시된 형식(uncompressed 이외의 형식을 사용하는 경우)으로 압축한다.
* 이러한 재압축 오버헤드가 발생하지 않도록 하려면, Broker 압축이 producer로 설정되어 있는지 또는 `Producer`가 일반적으로 사용하는 값으로 설정되어 있는지 확인해야 한다.

<br>

## Concurrent In-Flight Requests
* `max.in.flight.requests.per.connection > 1` : 이면 Pipelining을 의미

<br>

일반적으로 Pipelining은 아래와 같다.
* `더 나은 처리량` 제공
* 재시도 시 `순서가 잘못된 전달`이 발생할 수 있음
* `과도한 파이프라이닝` → 처리량 저하
  * Lock Contention
  * Worse Batching

<br>

관련 링크: Benchmarks on Request Pipelining  
https://cwiki.apache.org/confluence/display/KAFKA/An+analysis+of+the+impact+of+max.in.flight.requests.per.connection+and+acks+on+Producer+performance

<br>

Idempotent Producer를 사용한 재시도에서 파이프라이닝으로 인해 순서가 잘못된 메시지가 발생하지 않는다.

<br>

## 서비스 목표에 따른 Optimization(최적화)

![Optimization according to service goals](../images/30.Optimization%20according%20to%20service%20goals.PNG)

뭐가 가장 중요한지 생각해보고 우선순위를 정한 후, 그것에 맞는 세팅부터 먼저 하고 나머지 것들을 세팅해야 한다.

https://www.confluent.io/ko-kr/resources/white-paper/optimizing-your-apache-kafka-deployment/

<br>

## Optimization(최적화) for Throughput(처리량)
Producer:
* `batch.size`: 100,000 – 200,000 로 증가 (default 16,384)
* `linger.ms`: 10 – 100 로 증가 (default 0)
* `compression.type=lz4` (default `none`, 즉, no compression)
* `acks=1` (default 1)
* `buffer.memory`: Partition이 많다면 증가 (default 33,554,432)

https://www.confluent.io/ko-kr/resources/white-paper/optimizing-your-apache-kafka-deployment/
 
<br>

## Optimization(최적화) for Latency(지연시간)
Producer:
* `linger.ms=0` (default 0)
* `compression.type=none` (default `none`, 즉, no compression)
* `acks=1` (default 1) 

https://www.confluent.io/ko-kr/white-paper/optimizing-your-apache-kafka-deployment/

<br>

## Optimization(최적화) for Durability(내구성)
Producer:
* `replication.factor=3` (Topic override available)
* `acks=all` (default 1)
* `enable.idempotence=true` (default false), Message 중복과 순서 처리
* `max.in.flight.requests.per.connection=1` (default 5)  
  Idempotent Producer를 사용하지 않을 때 순서가 잘못된 메시지를 방지하기 위해

https://www.confluent.io/ko-kr/white-paper/optimizing-your-apache-kafka-deployment/

<br>

## Optimization(최적화) for Availability(가용성)
Producer:
* Nothing

https://www.confluent.io/ko-kr/white-paper/optimizing-your-apache-kafka-deployment/

<br>

## Summary
* Data Formats
* Serialization & Deserialization
* 중요한 설정 파라미터
* Compression
* 서비스 목표에 따른 Optimization(최적화)