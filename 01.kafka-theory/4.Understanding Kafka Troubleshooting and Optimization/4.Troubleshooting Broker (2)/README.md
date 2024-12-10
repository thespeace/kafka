# Troubleshooting Broker (2)

<br>

## Controller Issues
### Controller is the "brain" of cluster
* 클러스터의 하나의 Broker가 Controller 역할을 한다.
* Broker의 Liveness를 모니터링
* Broker Fail 시 새로운 Leader 선출
* 새로운 Leader를 Broker들에게 전달

`ZK Mode`에서의 Controller 선택은 ZK를 기반으로 한다. ZK에서 /controller 경로를 생성하여 승리한 Broker가 Controller가 된다.  
모든 Partition의 상태는 Controller에 캐시된다. 장애 조치로 인해 이 상태는 ZK에 저장이 된다.  

`KRaft Mode`에서는 어떻게 될까?

<br>

## Where is Controller?
### ZK Mode vs KRaft Mode

![Where is Controller](../images/08.Where%20is%20Controller.PNG)

<br>

### ZK Mode

![Where is Controller](../images/09.Where%20is%20Controller%20-%20ZK%20Mode.PNG)

<br>

### KRaft Mode

![Where is Controller](../images/10.Where%20is%20Controller%20-%20KRaft%20Mode.PNG)

<br>

## Isolation mode vs Combined mode
### Dedicated mode vs Shared mode

![Isolation mode vs Combined mode](../images/11.Isolation%20mode%20vs%20Combined%20mode.PNG)

<br>

## Controller의 가장 일반적인 Task
### Broker Fail 시 조치
* 하나의 Broker를 정상적으로 Shutdown 했을 때
  * Controller는 Local 및 ZK(Leader)에서 영향을 받는 Partition의 상태를 업데이트
  * 상태 변경을 나머지 Broker에게 전달
* Controller에 장애가 발생했을 때
  * ZK 내의 `/controller` path는 임시적이므로, Fail된 Controller와 함께 사라진다.
  * 나머지 Broker들 사이에서 새로운 Controller Election(선택)이 발생
* 새 Controller는 ZK에서 상태를 업로드해야 한다.

<br>

## Controller에서 손실된 ZooKeeper 세션 복구
* KAFKA-2729 가 발생하는 Broker가 있는지 확인을 해야 한다.
* 임시조치: `zookeeper.session.timeout`을 늘린다.
* 근본 원인 이해:
  * 오래 걸리는 GC(Garbage Collection)
  * 성능이 낮은 ZK
  * 일시적인 네트워크 및 하드웨어 문제

<br>

## Broker에 더 많은 스토리지 추가
Kafka에서는 Topic/Partition의 데이터가 Data Directory에 저장된다.
* 이러한 디렉터리 위치는 "`log.dirs`" 파라미터를 사용하여 구성된다.
* 하나 이상의 디렉토리 위치를 구성할 수 있다.

Kafka는 지정된 디렉토리 위치 전체에서 Partition Data Directory의 균형을 유지한다.
* 일반적으로 하나의 디렉토리 위치로 시작
* 운영 중 `데이터 크기가 증가`하면 `더 많은 디스크`를 추가해야 할 수도 있다.
* 기존 "`log.dirs`" 파라미터에 새 디렉토리 위치를 추가할 수 있다.

Kafka Broker를 restart 한 후, 새 Partition에 대해서 새 디렉토리를 사용한다.
* Kafka는 기존 저장된 Partition Data를 새로 추가된 디렉토리로 자동으로 이동하지 않는다.
* `디렉토리 전체에 걸쳐, Partition의 균형을 자동으로 조정하지 않는다.`

### `일부 Partition Data를 다른 디렉토리`로 이동하고 싶을 때!

<br>

## Partition Data를 다른 디렉토리로 이동하는 방법
### 3가지 방법
* Manual Partition Movement
* Using script `kafka-reassign-partitions.sh`
* Using the Confluent Auto Data Balancer

<br>

## Manual Partition Movement
1. 새 Disk 또는 SSD를 Broker에 추가하고, 사용할 `new directory`에 마운트한다.  
   아래의 7번째 단계에서 최종적으로 `log.dirs` 파라미터의 설정값을 `new directory`로 교체 또는 추가
2. Broker 프로세스를 정상적으로 Shutdown한다.
   `SIGKILL` 이 아닌 `SIGTERM`
3. 원본 `log.dirs` 를 백업해 둔다.
4. 기존 `log.dirs` 에서 `new directory`로 Partition Folder(`<Topic-name>-<Partition-number>`로 명명된)를 수동으로 전체 또는 부분 이동한다.
5. `new directory`에 `replication-offset-checkpoint`, `recovery-point-offset-checkpoint` 및 `cleaneroffset-checkpoint` 파일을 생성한다.  
   기존 파일들을 사용하여 `new directory`에 복사/생성한 후 이 파일을 업데이트해야 한다.  
   두 디렉토리(이전 `log.dirs`의 디렉토리와 `new directory`)의 항목을 조정해야 한다.
6. 기존 `log.dirs`의 `meta.properties` 를 모든 `new directory` 에 복사한다.
7. `server.properties` 파일의 `log.dirs` 파라미터의 설정값을 `new directory`로 교체 또는 추가하여 수정한다.
8. Broker를 Start한다.

<br>

## Using script kafka-reassign-partitions.sh
자세한 내용은 아래 KIP-113 참고  
https://cwiki.apache.org/confluence/display/KAFKA/KIP-113%3A+Support+replicas+movement+between+log+directories

1. 만약에 아래와 같은 `sample.json` 파일이 있다면
    ```json
    {"partitions":
        [
            {
            "topic": "foo",
            "partition": 1,
            "replicas": [101],
            "log_dirs": ["/var/lib/kafka/data2"]
            }
        ],
        "version":1
    }
    ```

2. log.dirs 를 `/var/lib/kafka/data,/var/lib/kafka/data2` 로 수정한 다음 Broker를 restart 한다.
3. 아래의 명령을 수행한다.
    ```
    $ kafka-reassign-partitions --zookeeper zookeeper:2181 \
    --bootstrap-server kafka:9092 \
    --reassignment-json-file sample.json \
    --execute
    ```
4. 위 명령은 Topic “foo” 의 Partition “1” 을 새 Data directory로 이동한다.

<br>

## Using the Confluent Auto Data Balancer
1. Confluent Auto Data Balancer를 실행하여 더 많은 디스크가 추가될 Broker의 모든 Partition을 다른 Broker로 이동할 수 있다.
2. 새 Disk 또는 SSD를 Broker에 추가하고 `new directory`에 마운트 한다.
3. Broker 프로세스를 정상적으로 Shutdown한다.  
   `SIGKILL` 이 아닌 `SIGTERM`
4. Broker의 `server.properties` 내의 `log.dirs` 설정 파라미터에 `new directory`를 추가한다.
5. Broker를 Start 합니다.
6. Confluent Auto Data Balancer를 다시 실행하여, 원래 Partition을 이 Broker로 다시 추가한다.

<br>

## Gracefully Shutdown Kafka Cluster
시스템 유지 관리를 위해 일부 상황에서는 Kafka 클러스터를 완전히 종료하는 것이 중요할 수 있다.  
Kafka 클러스터를 다시 시작할 때 발생하는 문제를 방지하기 위해, Kafka Broker들을 정상적으로 종료하는 방법이 중요하다.

* 전제 조건
  * `controlled.shutdown.enable`은 모든 Broker에서 `true`로 설정
  * `controlled.shutdown.max.retries` 및 `controlled.shutdown.retry.backoff.ms`는 대부분 기본값 사용


* 실행 순서
  1. Kafka Cluster를 사용하는 모든 Producer 와 Consumer 프로세스를 stop
  2. 한번에 하나의 Broker를 Shutdown
  3. 로그 메시지를 확인하여 완전히 shut down 되었음을 확인  
     * `INFO [Kafka Server 0], Starting controlled shutdown (kafka.server.KafkaServer)` - Controlled shutdown 요청이 수신되었음을 나타낸다.
     * `INFO [Kafka Server 0], Controlled shutdown succeeded (kafka.server.KafkaServer)` - Controlled shutdown이 처리되었으며 Broker가 클러스터에서 삭제되었지만 프로세스가 여전히 실행 중임을 나타낸다.
     * `INFO [Kafka Server 0], shut down completed (kafka.server.KafkaServer)` - Broker가 완전히 종료되었음을 나타낸다.
    
     
* 각 Broker를 한 번에 하나씩 종료함으로써 클러스터 내의 다른 Live Broker는 다른 Broker와의 통신을 완전히 중지할 수 있다.
* out of sync replicas과 Broker가, 살아있는 것으로 인식되는 종료 중인 Broker와의 통신을 기다리는 동안에, 정지될 수 있는 잠재적인 문제를 방지할 수 있다.

<br>

## Java GC 및 Heap Dump
`OutOfMemoryError` 발생시 자동으로 Heap Dump 생성하는 옵션
```
-XX:+HeapDumpOnOutOfMemoryError -XXHeapDumpPath=<file_name>
```

Kafka Broker의 디스크 구성에 따라 JVM GC 로그가 다른 방식으로 기록되도록 구성해야 할 수도 있다.
1. GC 로그를 기록할 디렉토리 위치를 결정
2. 특정 기간 동안 유지하려는 GC 로그 수를 결정
3. 각 GC 로그 파일의 크기를 결정해야 한다. 일반적으로 약 20 MB 이하 권장
4. 아래의 옵션을 지정
    ```
    export KAFKA_OPTS="-Xloggc:<location>`date +%F_%H-%M-%S`-gc.log -XX:+PrintGCDetails
    -XX:+PrintGCDateStamps -XX:+PrintTenuringDistribution -XX:+PrintGCCause
    -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=<num> -XX:GCLogFileSize=<size>M"
    ```
5. 변경 사항을 적용하려면 Broker를 다시 시작

<br>

## ISR 개수가 너무 작으면?
Single Replica에 기록된 메시지가 손실되어 나중에 사용할 수 없게 되는 것을 방지하기 위해, ISR 크기가 특정 최소값보다 큰 경우에만 Partition에서 Write를 허용한다.  
이 설정은 Producer가 `acks=all`을 사용하고 `min.insync.replicas` 파라미터(보통 2 로 설정)를 설정한 경우에만 적용된다.

`Min ISR 크기를 더 높게 설정하면` 메시지가 더 많은 Replica에 기록되어 메시지가 손실될 가능성이 줄어듦으로 `더 나은 일관성이 보장`된다.

그러나 In-Sync Replica 개수가 `최소 임계값 아래로 떨어지면` Partition에 쓰기를 사용할 수 없으므로 `가용성이 떨어진다.`

최소 임계값 아래로 떨어지면 Producer에 나타날 수 있는 에러 메시지
* `NOT_ENOUGH_REPLICAS` : 필요한 것보다 In-Sync Replica이 적기 때문에 메시지를 reject(거부). 재시도 가능
* `NOT_ENOUGH_REPLICAS_AFTER_APPEND` : 메시지가 로그에 기록되지만 필요한 것보다 적은 수의 In-Sync Replica에 기록. 재시도 가능
* 해당 메시지 두 개의 차이점
  * ISR 개수를 두 번 확인하기 때문
  * Leader에게 Write 전에 한 번, Ack하기 위해서 한 번
  * Leader에 Write 전에 small ISR 을 감지하면 첫 번째 에러가 발생
  * Leader에게 Write 한 후 Ack를 받기 전에 ISR이 축소되면 두 번째 에러가 발생  

<br>

## 왜 Out Of Sync Replica가 발생하는가?
* `Slow Replica` : 특정 기간 동안 지속적으로 Leader에 대한 Write를 따라잡지 못하는 Follower Replica  
  이에 대한 가장 일반적인 이유 중 하나는 Follower Replica 의 I/O 병목 현상으로 인해 Leader에서 사용할 수 있는 것보다 느린 속도로 복사된 메시지를 추가하기 때문
* `Stuck Replica` : 일정 기간 동안 Leader로부터 가져오기(fetch)가 중지된 Follower Replica  
  Replica는 GC pause 또는 실패 또는 중단으로 인해 Stuck될 수 있다.
* `Bootstrapping Replica` : 사용자가 Topic 의 Replication Factor를 늘리면 새로운 Follower  
  Replica는 Leader의 로그에 완전히 따라잡을 때까지 Out Of Sync

<br>

## 권장 사항
* 더 많은 Broker를 추가하여 클러스터의 개별 구성원 간에 작업 부하를 더 잘 분산
* 모든 Broker는 가능한 가장 빠른 persistent local storage 를 가져야 한다.  
  대부분의 경우 이는 SSD이며, 모든 Broker가 유사한 성능 특성을 가진 SSD를 사용하는지 확인
* 모든 Broker는 동일한 구성, 특히 네트워크 및 IO Thread, Buffer 크기 등을 갖도록 구성
* 짧은 지연 시간(Latency)을 위해 모든 Broker는 동일한 지역(geographical region)에 위치해야 한다.
* 네트워크 연결이 느리거나 다른 프로세스와 공유되는 NIC가 있는 경우 속도 저하가 불가피하다.
* Client에 문제가 발생하여 Kafka에 예상치 못한 양의 데이터가 넘쳐 Replication과 같은 다른 프로세스가 중단될 수 있다.  
  이를 방지하려면 Client에 Quotas(할당량) 옵션을 사용

<br>

## Summary
* Troubleshooting Checklist
* 일반적인 Troubleshooting 시나리오
* Broker is Alive or Failed
* Controller에서 손실된 ZooKeeper 세션 복구
* Broker에 더 많은 스토리지 추가
* Java GC 및 Heap Dump
* ISR vs OSR
* 권장 사항