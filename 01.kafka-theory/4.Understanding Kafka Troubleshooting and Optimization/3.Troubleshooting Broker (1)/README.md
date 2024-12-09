# Troubleshooting Broker (1)

<br>

## Troubleshooting Checklist
### 문제 해결 단계(1/2)
* `어떤 일이 일어나고 있나요?`: 어떤 일이 일어나고 있는지 물어볼 때는 최대한 정확하게 질문을 하고 확인을 해야 한다.  
  예) Broker 103이 다운되었거나 Broker 104가 Replica Leader를 따라잡지 못했습니다.
* `정확한 설명`: 이전 질문에 대한 대답이 ”Broker 103 is crashing"이었다면, ”Crashing이란 무엇을 의미합니까?  
  Broker Process가 다운되었거나 시스템에 접근할 수 없습니까?"라고 질문해야 한다.
* `타임 라인`: 언제 발생했는지, 언제 시작되었는지도 확인해야 한다.  
  예) "모든 것이 2시간 전에 시작되었습니다." 또는 "지난 30분 동안 관찰되었습니다". 시작 시간을 최대한 정확하게 확인해야 한다.
* `변경 사항`: 관찰된 문제의 발생 직전이나 도중에 변경된 사항이 있는지를 확인하는 것도 중요하다.  
  비난하는 것이 아니라, 최대한 빨리 근본 원인을 찾고 조치를 하기 위함이다.

<br>

### 문제 해결 단계(2/2)
* `모니터링`: 변경된 사항이 없어도 괜찮다. 어떤 모니터링을 실시하고 있나요?  
  예) Broker가 실패했지만 해당 프로세스는 여전히 제자리에 있다. → 과부하 신호일 수도 있다.
* `노드에 대한 SSH`: Broker에 연결할 수 없는 경우, Broker가 실행된 서버/VM에 로그인할 수 있는지도 확인할 수 있어야 한다.  
  로그인 할 수 없는 경우 클러스터 노드에 문제가 있는 것이다.  
  네트워크나 노드가 down 혹은 crash 되었을 수 있다.
* `로그 찾기`: Broker 시스템에 SSH로 연결할 수 있으면 Broker의 로그를 찾아야 한다.  
  일반적인/디폴트 위치에서 찾을 수 없는 경우, Broker가 사용하고 있는 `log4j.properties` 파일을 찾아 거기에서 찾을 수 있다.
* `로그 구문 분석`: 문제의 증상에 대한 정확한 정보를 가지고 있으면, 많은 로그 파일 중 오류를 발견할 가능성이 가장 높은 파일의 범위를 좁히는 데 도움이 된다.  
  또한, 로그 파일에 어떤 유형의 오류 메시지가 나타날 것인지에 대한 정보도 알 수 있다.

<br>

## Monitoring
* 성공적인 문제 해결을 위해서는 기록 데이터(Historical Data, 예: 시간 경과에 따른 처리량)를 바탕으로 건전하고 완전한 모니터링을 갖추는 것이 가장 중요하다.
* 추세에서 올바른 측정 기준 편차를 모니터링한다고 가정하면 무언가 잘못되었다는 경고가 될 수 있다.  
  예) 처리량이 급격히 감소하거나 CPU 사용량이 급격히 증가하면 문제가 있음을 나타낼 수 있다.
* 조기에 대응하고 오류를 방지할 수 있는 시간을 제공하는 Alert 기능을 사용하면, 모니터링을 강화할 수 있다.  
  예) 디스크 사용량이 지정된 임계값을 초과할 때 Alert를 받을 수 있다면, 디스크 부족 오류가 발생하기 전에 디스크를 더 추가할 수 있다.

<br>

## 일반적인 Troubleshooting 시나리오(1/3)
* `TLS`: 암호화 성능(SSL, 성능이 좋지 않은 이유는 무엇입니까?)은 종종 OS 수준 문제가 발생할 확률이 높다.  
  예) CPU 부하가 너무 높은 경우, 코어를 더 추가해야 한다.
* `NW`: 매우 일반적인 네트워크 문제는 Java.io broken pipe가 Broker 로그에 보고된다는 것은 심각한 네트워크 문제가 있음을 의미한다.  
  예) 네트워크가 끊어졌습니다.
* `"Unable to resolve hostname"`: 이 오류 메시지는 DNS에 문제가 있음을 나타낸다.
* `ZK와 Broker 간의 Timeout`: ZK 측에서 디스크 액세스 속도가 충분히 빠르지 않거나 ZK Ensemble 이 Kafka가 아닌 다른 서비스와 공유된다는 의미일 수 있다.  
  예) 로그 상의 오류 메시지: fsync error (warning) took x amount of time which is...
* `JVM의 OOM`: JVM에 충분한 Heap Memory를 설정하지 않은 경우  
  각 구성 요소들의 Heap 크기에 대한 권장 설정은 아래의 링크에서 찾을 수 있다.  
  https://docs.confluent.io/platform/current/installation/system-requirements.html

<br>

## 일반적인 Troubleshooting 시나리오(2/3)
* `Virtual Machine`: 많은 VM이 공유 리소스를 놓고 경쟁하는 일이 발생할 수 있다.  
  VM은 리소스를 얻을 수 있을 때까지 일시 중지되고 → 전체 시스템이 실패하는 경우도 발생한다.  
  이러한 경우는 문제를 해결하기 굉장히 어렵다. (예: Broker는 error를 제공하지 않음)  
  문제를 나타낼 수 있는 로그의 시간 간격을 찾아야 한다.(예: 30초 이상 로그에 아무 것도 보고되지 않음)  
  * 해결 방법: Linux에서 cgroup을 사용할 때 수행하는 것과 유사하게 격리된 리소스를 VM에 고정  
    예) VM xyz에는 최소 32GB RAM을 사용, 서로 다른 HW에 VM들을 배치
* `Docker Container`: Kubernetes에서 최소 및 최대 RAM, 최소 및 최대 IO와 같은 리소스 제한을 확인하지 못하는 경우가 많다.  
  이로 인해 Linux가 임의로 프로세스(Broker 프로세스 또는 컨테이너 데몬일 수 있음)를 종료하는 OOM과 같은 오류가 발생

<br>

## 일반적인 Troubleshooting 시나리오(3/3)
* `Security`: 많은 보안 관련 문제(AD 통합, Kerberos)  
  근본 원인: 대부분 경우, 이해 부족이 원인
* `Open File 수 제한`: 주로 Broker 및 ZooKeeper 관련, Kafka Connect에서 발생할 수 있다.(매우 일반적인 문제)  
  OS 수준 뿐만 아니라 서비스 수준에서도 지정해야 한다.  
  때때로 이 문제에 직면한 다음에, OS 수준에서 엄청나게 높은 숫자를 구성하는 경우가 많다.

<br>

## Broker Metrics for Troubleshooting
* Kafka-specific Metrics  
  * `TotalTimeMs`  
  * `BytesInPerSec`, `BytesOutPerSec`  
  * `kafka.server:type=SessionExpireListener`,`name=ZooKeeperExpiresPerSec`
* Host Metrics  
  * Page Cache Reads Ratio
  * Disk Usage
  * CPU Usage
  * Network bytes Sent/Received
* GC Metrics
  * `java.lang:type=GarbageCollector`,`name=G1 Young|Old Generation`
  * `CollectionCount`
  * `CollectionTime`

<br>

## Broker - 중요한 File들

![Broker - Important Files](../images/07.Broker%20-%20Important%20Files.PNG)

위 파일들은 Broker의 `server.properties` 파일의 `log.dirs` 파라미터로 지정된 `각 directory 들에 존재한다.`

<br>

## Broker is Alive or Failed?(1/3)
대부분의 분산 시스템과 마찬가지로, 장애를 자동으로 처리하려면, 노드가 ”alive"인 것이 무엇을 의미하는지 정확하게 정의해야 한다.  
Kafka에서는 ”Controller"로 정해진 Broker 노드가 클러스터의 Broker 등록을 관리한다. Kafka 노드 Liveness에는 두 가지 조건이 있다.

1. Broker는 정기적인 메타데이터 업데이트를 수신하기 위해 컨트롤러와의 Active Session을 유지해야 한다.
2. Follower 역할을 하는 Broker는 Leader의 쓰기를 복제해야 하며 "너무 멀리" 뒤쳐져서는 안 된다.

https://kafka.apache.org/documentation/

<br>

## Broker is Alive or Failed?(2/3)
### ”Active Session"의 의미는 클러스터 구성에 따라 다르다.
1. Broker는 정기적인 메타데이터 업데이트를 수신하기 위해 컨트롤러와의 Active Session을 유지해야 한다.
2. Follower 역할을 하는 Broker는 Leader의 쓰기를 복제해야 하며 "너무 멀리" 뒤쳐져서는 안 된다.

`KRaft Mode`의 경우 주기적인 하트비트를 컨트롤러에 전송하여 Active Session을 유지한다.  
`broker.session.timeout.ms`에 의해 구성된 제한 시간이 만료되기 전에 Controller가 하트비트를 수신하지 못하면 노드는 Offline으로 간주된다.

`Zookeeper Mode`의 경우 Active Session은 Zookeeper 세션 초기화 시 Broker가 생성하는 임시 노드(ephemeral node)의 존재를 통해 간접적으로 결정된다.  
`zookeeper.session.timeout.ms`가 만료되기 전에 Zookeeper에 하트비트를 보내지 못해 Broker가 세션을 잃으면 노드가 삭제된다.  
그런 다음 Controller는 Zookeeper 감시를 통해 노드 삭제를 확인하고 Broker를 Offline으로 표시한다.

https://kafka.apache.org/documentation/

<br>

## Broker is Alive or Failed?(3/3)
### "너무 멀리" 뒤쳐져서는 안 된다.
1. Broker는 정기적인 메타데이터 업데이트를 수신하기 위해 컨트롤러와의 Active Session을 유지해야 한다.
2. Follower 역할을 하는 Broker는 Leader의 쓰기를 복제해야 하며 "너무 멀리" 뒤쳐져서는 안 된다.

”alive" 또는 ”failed"라는 모호함을 피하기 위해, 위 두 가지 조건을 충족하는 노드를 ”in sync"된 것으로 지칭한다.  
Leader는 ISR로 알려진 ”in sync" replica set를 추적한다.

이러한 조건 중 하나라도 충족되지 않으면 Broker는 ISR에서 제거된다.  
예) Follower가 죽으면 Controller는 세션 손실을 통해 실패를 인지하고 ISR에서 Broker를 제거한다.

반면, Follower가 Leader보다 너무 뒤처져 있지만 여전히 Active Session이 있는 경우, Leader는 이를 ISR에서 제거할 수도 있다.  

지연되는 복제본(lagging replicas)의 결정은 `replica.lag.time.max.ms` 구성을 통해 제어된다.  
이 구성에 의해 설정된 최대 시간 내에 Leader의 로그 끝까지 따라잡을 수 없는 복제본은 ISR에서 제거됩니다.

https://kafka.apache.org/documentation/