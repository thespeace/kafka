# Tunning Broker

<br>

## Broker Monitoring
* Bytes In/Out
* Partitions
  * Count 및 Leader Count : 일반적으로 동일해야 한다. Broker가 down되면 잠시 동안 불일치가 발생할 수 있다.
  * Under Replicated 및 Offline
* Threads
  * Network Pool, Request Pool
  * Max. Dirty Percent
* Requests
  * Rates 및 Times - Total, Queue, Local 및 Send

<br>

## Topic Monitoring
* Bytes In/Out
* Messages In, Produce Rate, Produce Failure Rate
* Fetch Rate, Fetch Failure Rate
* Partition Bytes
* Quota Throttling 

<br>

## 일반적인 가이드라인(1/5)
클러스터의 Broker 간에 Partition Leadership을 분배해야 한다.

리더십에는 많은 Network I/O 리소스가 필요하다.  
예를 들어, Replication Factor로 3 을 사용하여 실행하는 경우, Leader는 Partition 데이터를 수신하고, 두 개의 Copy를 두 개의 Replicas로 각각 전송한 다음, 해당 데이터를 Consume하려는 많은 Consumer에게 전송해야 한다.  
따라서, 아래 그림의 예에서 Leader가 되는 것은, 사용되는 Network I/O 측면에서, Follower가 되는 것보다 최소 4배 더 부하가 발생한다.  
Leader는 Disk 에서 읽어야 할 수도 있다. Followers는 Fetch해서 Write만 한다.  
아래 그림의 예에서 Leadership이 Broker들 사이에 균등하게 분산되지 않음을 볼 수 있는데, 이 경우 맨 위의 Broker는 맨 아래의 Broker보다 수행할 작업량이 약 12배 더 많다.

![Leadership is not evenly distributed among brokers](../images/14.Leadership%20is%20not%20evenly%20distributed%20among%20brokers.PNG)

<br>

## 일반적인 가이드라인(2/5)
처리량이 지속적으로 높아야 하는 Broker의 경우, Disk Subsystem에서 읽지 않도록 충분한 메모리를 확보해야 한다.

Partition 데이터는 가능하다면 운영 체제(OS)의 File System Cache에서 직접 제공되어야 한다.  
이는 Consumer가 잘 따라잡을 수 있도록 보장해야 함을 의미한다.  
* 충분한 OS Page Cache가 필요

<br>

Lagging Consumer(지연되는 컨슈머)는 Broker가 디스크에서 읽도록 강제해야 한다.  
또한, Confluent 문서에서 권장하는 대로 충분한 Java Heap을 구성하는 것을 잊으면 않된다.  
* 충분한 Java Heap Memory가 필요

<br>

## 일반적인 가이드라인(3/5)
최신 Topic Message Format으로 오래된 버전의 클라이언트를 사용하거나 그 반대의 경우, 클라이언트를 대신하여 Format을 변환해야 하기 때문에 Broker에 추가적인 부하가 발생할 확률이 있다.  
가능할 때마다 이것을 피해야 한다.

필요에 따라 Apache Log4j 속성을 수정해야 한다.  
Kafka Broker Logging은 과도한 양의 디스크 공간을 사용할 수 있다.  
그러나, Logging을 꺼버리면 디버깅이 불가하기 때문에 포기할 순 없다.  
Broker Log는 사고 후 이벤트 순서를 재구성하는 가장 좋은 방법일 수 있으며 때로는 유일한 방법일 수 있다.

<br>

## 일반적인 가이드라인(4/5)
Compacted Topic을 위해 Broker에 추가적인 Memory 및 CPU 리소스가 필요하다.

Log Compaction(로그 압축)을 성공적으로 완료하려면 Broker의 Heap(Memory)과 CPU 자원이 모두 필요하며, 로그 압축에 실패하면 무제한으로 커지는 Partition으로 인해 Broker가 위험에 처할 수도 있다.

Broker에서 `log.cleaner.dedupe.buffer.size` 및 `log.cleaner.threads`를 조정할 수 있지만 이러한 값은 Broker의 Heap 사용량에 영향을 미친다는 점에 유의해야 한다.

Broker가 OutOfMemoryError 예외를 발생시키면 종료되고 잠재적으로 데이터가 손실할 수 있는 확률이 증가한다. Buffer 크기와 Thread 수는 clean될 Topic Partition 수와 해당 Partition에 있는 메시지의 Data Rate 및 Key 크기에 따라 달라질 수 있다.

`ERROR` 항목에 대한 Log-Cleaner Log File을 모니터링하는 것은 Log Cleaner Thread 관련 문제를 감지하는 가장 확실한 방법이다.

<br>

## 일반적인 가이드라인(5/5)
Kafka를 위한 User Limits 설정해야 한다.
* Kafka는 동시에 많은 File을 Open한다.
* 대부분의 Unix 계열 시스템에서 최대 Open File 수에 대한 기본 설정인 1024는 충분하지 않다.
* 상당한 부하로 인해 Error가 발생하고 `java.io.IOException...(Too many open files)`과 같은 Error 메시지가 Kafka Log File에 기록될 수 있다.
* 다음과 같은 오류가 나타날 수도 있다.
  ```
  ERROR Error in acceptor (kafka.network.Acceptor)
  java.io.IOException: Too many open files
  ```
* 따라서 이러한 오류가 발생하기 전에, 100,000 과 같이 상대적으로 높은 시작점을 권장한다.

<br>

## Broker 성능 체크
* 클러스터의 모든 Broker가 동작하고 있는지 확인하는 것이 중요하다.
* Network Interface 가 포화 상태인지 확인하는 것도 중요하다.
  * Partition Leader 재선출 : Leader는 Follower 작업의 4배를 수행한다.(균등하게 분산되었는지 확인)
  * 클러스터의 Partition Rebalancing : Partition이 모든 Broker에 균등하게 분산되었는지 확인
  * Traffic 분산 : 더 많은 Broker 및 Partition
* CPU 사용률이 높은지 확인 해야 한다.(특히 `iowait`)
  * 다른 프로세스가 자원을 놓고 경쟁하고 있는지 확인
  * 디스크가 불량인지 확인
* 정말 큰 메시지를 보낼 필요가 있는지 확인해야 한다. Kafka는 Big Message에 최적화되거나 설계되지 않았다.
  * 1MB 한도를 초과하면 안된다. 그 크기조차도 이미 큰 메시지이다.
  * 큰 메시지가 꼭 필요한 경우 다른 아키텍처 패턴을 사용할 수 있다.
  * 일부 BLOB 저장소에 메시지 Payload를 저장하고 Kafka로 전송된 실제 Record/Message의 해당 메시지 Payload에 BLOB 저장소의 URI를 전달.

<br>

## Producer Request
1. Network Threads Pool의 사용 가능한 Thread에 의해 Network에서 Request가 선택된다.
2. Network Thread는 Request를 Request Queue에 넣는다.
3. IO Thread Pool의 여유 Thread가 Request Queue 에서 사용 가능한 다음 Request를 선택한다.
4. IO Thread는 Broker의 Local Page Cache에 Record를 Write한다.  
   이 Cache는 결국 Broker의 Local Disk에 유지
5. IO Thread는 Request를 Request Purgatory에 넣는다.
6. acks=all인 경우 Broker는 In-Sync Replicas의 Write 확인(ACK)을 기다린다.
7. 완료된 Producer Request은 Response Queue에 추가된다.
8. Network Thread Pool의 모든 여유 Thread는 Response Queue에서 다음 사용 가능한 Request를 받는다.
9. Network Thread는 Response을 다시 Network로 보낸다.

![Producer Request](../images/15.Producer%20Request.PNG)

Apache Kafka에는 Request Purgatory라는 데이터 구조가 있다.  
이곳은 아직 성공 기준을 충족하지 못했지만 아직 오류가 발생하지 않은 Request를 보유한다.  
acks=all을 사용한 Producer Request는 모든 In-Sync Replicas가 Write를 ACK할때까지 완료된 것으로 간주될 수 없으며, Leader가 실패하더라도 손실되지 않을 것이라고 보장할 수 있다.  
이러한 Request는 (a) 요청한 기준이 완료되거나 (b) 일부 시간 초과가 발생하면 완료된 것으로 간주된다.

<br>

## Producer Request in Confluent Control Center
### B. 클러스터 상태 확인(3/3)

Confluent Control Center는 Producer Request의 내부를 분석하는 이상적인 도구이다.  
Producer Request의 `Total Response Time`을 표시하고 5개의 부분 시간으로 분할된 것을 볼 수 있다.

이 그래프에서는 `Response Remote Time`이 전체 Producer Request Latency에서 가장 큰 부분을 차지한다는 것이 분명하다.

JMX Metrics:  
`kafka.network:type=RequestMetrics,request=Produce,name=<name>`  
여기서 `<name>` : `RequestQueueTimeMs`, `LocalTimeMs`, `RemoteTimeMs`, `ResponseQueueTimeMs`, `ResponseSendTimeMs`, `TotalTimeMs`

![Producer Request in Confluent Control Center](../images/16.Producer%20Request%20in%20Confluent%20Control%20Center.PNG)

<br>

## Producer Request - Latency

![Producer Request - Latency](../images/17.Producer%20Request%20-%20Latency.PNG)

<br>

## Producer Request - Configurations(1/3) 

![Producer Request - Configurations(1/3)](../images/18.Producer%20Request%20-%20Configurations1.PNG)

<br>

## Producer Request - Configurations(2/3)

![Producer Request - Configurations(2/3)](../images/19.Producer%20Request%20-%20Configurations2.PNG)

<br>

## Producer Request - Configurations(3/3)

![Producer Request - Configurations(3/3)](../images/20.Producer%20Request%20-%20Configurations3.PNG)

<br>

## High Latency를 없애기 위한 튜닝 요소
빠르고 안정적인 Network는 분산 시스템의 필수 성능 구성 요소이다.  
Low Latency(낮은 대기 시간)은 Node가 쉽게 통신할 수 있도록 보장하고, High Bandwidth(높은 대역폭)은 Shard 이동 및 복구에 도움이 된다.  
최신 데이터 센터 Networking(10 GbE)은 대부분의 클러스터에 충분하다.

![Tuning elements to eliminate high latency](../images/21.Tuning%20elements%20to%20eliminate%20high%20latency.PNG)

<br>

## Monitoring
* Network Throughput - Transmit (TX) and Receive (RX)
* Disk I/O
* Disk Space
* CPU Usage

<br>

## 서비스 목표에 따른 Optimization(최적화)

![Optimization according to service goals](../images/22.Optimization%20according%20to%20service%20goals.PNG)  
이 4가지 모두를 만족시킬 수 없고 각각의 최적화 방식이 존재한다.  
우선순위에 맞춰 세팅을 해야 한다.

<br>

## Optimization(최적화) for Throughput(처리량)
Broker:  
• Nothing

<br>

## Optimization(최적화) for Latency(지연시간)
Broker:  
* `num.replica.fetchers`: Follower가 Leader를 따라잡지 못하면 증가 (default 1) 

<br>

## Optimization(최적화) for Durability(내구성)
Broker:  
* `default.replication.factor=3` (default 1)
* `auto.create.topics.enable=false` (default true)
* `min.insync.replicas=2` (default 1); Topic override available
* `unclean.leader.election.enable=false` (default false); Topic override available
* `broker.rack`: rack of the broker (default null)
* `log.flush.interval.messages`, `log.flush.interval.ms`: 처리량이 매우 낮은 Topic의 경우  
  필요에 따라 message interval 또는 time interval을 낮게 설정 (Default는 OS가 Flush를 제어하도록 허용함); Topic override available

<br>

## Optimization(최적화) for Availability(가용성)
Broker:  
* `unclean.leader.election.enable=true` (default false); Topic override available
* `min.insync.replicas=1` (default 1); Topic override available
* `num.recovery.threads.per.data.dir`: `log.dirs` 내의 디렉토리 개수 (default 1)

<br>

## Summary
* 일반적인 가이드라인
* Producer Request
* 서비스 목표에 따른 Optimization(최적화)