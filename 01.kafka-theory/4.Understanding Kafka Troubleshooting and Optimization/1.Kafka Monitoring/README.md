# Monitoring

<br>

## Monitoring JMX Metrics
Kafka는 JMX Metrics을 노출하지만, 기본적으로 원격으로 노출되지는 않는다.  
JMX Metrics을 원격으로 노출하려면, JMX 환경 변수 세트를 사용하여 Broker를 시작해야 한다.  
(JMX 환경 변수는 뒤에 설명)  
* Metrics(측정 항목)은 다양한 방법으로 수집하고 쿼리할 수 있습니다.
* 모니터링 도구: 이상적으로 JMX Metrics는 Grafana, Graphite, CloudWatch, Datadog 등과 같은 모니터링 도구에서 그래프로 표시됩니다
* GUI 도구: jconsole 등
* CLI 도구: jmxterm 등
* 중요: jconsole 및 jmxterm은 올바른 JVM 설정이 설정된 경우에만 원격 호스트에 연결할 수 있다.

<br>

## Monitoring ZooKeeper
### Four Letter Words
ZooKeeper는 ”Four Letter Words"라고 알려진 제한된 명령 세트에 응답하여 작동 데이터를 내보냅니다.
* 가장 많이 사용되는 명령은 `stat`, `srvr`, `cons` 및 `mntr`
* 예) ZooKeeper 노드에 있는 경우 “`echo mntr | nc localhost 2181`” 을 사용하여 `zk_pending_syncs` 및 `zk_followers`를 포함하여 슬라이드에 언급된 모든 ZooKeeper Metrics(측정항목)을 볼 수 있다.

![Monitoring ZooKeeper](../images/01.Monitoring%20ZooKeeper.PNG)

<br>

## Monitoring ZooKeeper - Example
### Four Letter Words

![Monitoring ZooKeeper - Example](../images/02.Monitoring%20ZooKeeper%20-%20Example.PNG)
https://zookeeper.apache.org/doc/current/zookeeperAdmin.html#sc_zkCommands

<br>

## Monitoring ZooKeeper - JMX Metrics

* Enable Monitoring   
  (https://docs.confluent.io/platform/current/kafka/monitoring.html)
    ```
    KAFKA_JMX_OPTS= -Djava.rmi.server.hostname=[IP] \
    -Dcom.sun.management.jmxremote.port=[PORT] \
    -Dcom.sun.management.jmxremote=true \
    -Dcom.sun.management.jmxremote.authenticate=false \
    -Dcom.sun.management.jmxremote.ssl=false
    
    bin/zookeeper-server-start.sh
    ```
* Enable Monitoring(Docker)  
  (https://docs.confluent.io/platform/current/installation/docker/operations/monitoring.html)
    ```
    docker run -d \
    --name=zk-jmx \
    --net=host \
    -e ZOOKEEPER_TICK_TIME=2000 \
    -e ZOOKEEPER_CLIENT_PORT=32181 \
    -e KAFKA_JMX_PORT=9101 \
    confluentinc/cp-zookeeper:7.5.2
    ```

![Monitoring ZooKeeper - JMX Metrics](../images/03.Monitoring%20ZooKeeper%20-%20JMX%20Metrics.PNG)

<br>

## Monitoring Broker - System Metrics

* 관찰 대상 Metrics
  * CPU Usage
  * Memory Usage
  * Available Disk Space
  * Disk IO
  * Network IO
  * Open File Handles

* Alerts
  * 60% Disk Usage for Disks
  * 60% Disk IO Usage
  * 60% Network IO Usage
  * 60% File Handle Usage

<br>

## Monitoring Broker - JMX Metrics

* Enable Monitoring  
  (https://docs.confluent.io/platform/current/kafka/monitoring.html)
    ```
    KAFKA_JMX_OPTS= -Djava.rmi.server.hostname=[IP] \
    -Dcom.sun.management.jmxremote.port=[PORT] \
    -Dcom.sun.management.jmxremote=true \
    -Dcom.sun.management.jmxremote.authenticate=false \
    -Dcom.sun.management.jmxremote.ssl=false
    
    # Start Kafka
    bin/kafka-server-start.sh
    ```
  
* Enable Monitoring(Docker)  
  (https://docs.confluent.io/platform/current/installation/docker/operations/monitoring.html)
    ```
    docker run -d \
    --name=kafka-jmx \
    --net=host \
    -e KAFKA_BROKER_ID=1 \
    -e KAFKA_ZOOKEEPER_CONNECT=localhost:32181/jmx \
    -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:39092 \
    -e KAFKA_JMX_PORT=9101 \
    -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
    confluentinc/cp-kafka:7.5.2
    ```
  
* Enable Monitoring
    ```
    kafka.server:type=BrokerTopicMetrics,name=MessagesInPerSec
    kafka.server:type=BrokerTopicMetrics,name=BytesInPerSec
    kafka.network:type=RequestMetrics,name=RequestsPerSec,request=<type>
    kafka.server:type=BrokerTopicMetrics,name=BytesOutPerSec
    ```
  
<br>

## Monitoring Kafka Client per Broker
Common per-Broker Metrics for: Consumer, Producer, Connect, Kafka Streams, KSQL 
```
kafka.producer:type=producer-node-metrics, client-id=<client-id>,node-id=<node-id>
kafka.consumer:type=consumer-node-metrics, client-id=<client-id>,node-id=<node-id>
kafka.connect:type=connect-node-metrics, client-id=<client-id>,node-id=<node-id> 
```
Details: http://kafka.apache.org/documentation/#common_node_monitoring

<br>

## Monitoring Producer
```
kafka.producer:type=producer-metrics, client-id=<client-id>
```
![Monitoring Producer](../images/04.Monitoring%20Producer.PNG)

Details: http://kafka.apache.org/documentation/#common_node_monitoring  
Details: https://kafka.apache.org/documentation/#producer_monitoring

<br>

## Monitoring Consumer
```
kafka.consumer:type=consumer-fetch-manager-metrics, client-id=<client-id>
```
![Monitoring Consumer](../images/05.Monitoring%20Consumer1.PNG)

<br>

```
kafka.consumer:type=consumer-fetch-manager-metrics,partition=<partition>,topic=<topic>,client-id=<client-id>
```
![Monitoring Consumer](../images/06.Monitoring%20Consumer2.PNG)

Details: https://kafka.apache.org/documentation/#consumer_monitoring

<br>

## Summary
* Monitoring JMX Metrics
* Monitoring ZooKeeper
* Monitoring Broker
* Monitoring Producer/Consumer