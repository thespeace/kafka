# Troubleshooting ZooKeeper

<br>

## Apache ZooKeeper
* Apache ZooKeeper는 분산 애플리케이션을 제공하기 위한 인프라 서비스의 필수 구성 요소
* 이는 일반적으로 클러스터 전체에서 동기화되어야 하는 구성, 클러스터 리더십, 이름 지정 및 기타 메타데이터를 추적하는 일을 담당
* Apache Kafka가 제대로 작동하려면 안정적인 ZooKeeper가 필요

<br>

## Kafka에서의 ZooKeeper(ZK)의 역할
### Metadata 관리
KIP-500 으로 Kafka에서 ZK는 제거될 예정이지만, 여전히 사용되고 있다.
* `Topic Configuration` - ZK는 Topic 구성의 정보 소스
* `Controller Election` - Controller 역할을 하는 Broker는 ZK에 의해 선택
* `Access Control Lists (ACLs)` - 리소스에 대한 인증은 ZK에 의해 저장되고 동기화
* `Client Quotas` - 각 Kafka 클라이언트가 읽거나 쓸 수 있는 데이터의 양은 ZK에 저장된 내용에 따라 결정
* `Cluster Membership` - 클러스터에서 실제로 활성화된 Broker는 ZK 노드에 저장
* `Info about Leaders` - 각 Topic의 각 Partition에 대한 Leader는 ZK에 저장
* `Info about ISRs` - 동기화된 Replicas(복제본)에 대한 정보는 ZK에 저장
* `Dynamic Broker Configurations` - ZK는 동적으로 구성된 모든 Broker 설정값을 저장  
  (see KIP 226: https://cwiki.apache.org/confluence/display/KAFKA/KIP-226+-+Dynamic+Broker+Configuration)

<br>

## ZooKeeper 구성시 핵심 고려 사항
* ZooKeeper는 다른 모든 서비스(예, Brokers)를 동기화 상태로 유지하는 서비스
* `따라서, 대기 시간(Latency)이 짧은 상황에 있어야 한다.`

<br>

## Tuning ZooKeeper
### Hardware 고려 사항
ZooKeeper는 많은 하드웨어 리소스가 필요하지 않지만, 전용 리소스를 제공하는 것이 도움이 되는 경우가 많다.  
다음은 ZooKeeper 쿼럼에 사용할 하드웨어 구성 고려 사항의 몇 가지 예이다.
* ZK Quorum(쿼럼)의 `각 멤버를 위한 전용 서버`가 필요하다.
* dataLogDir에 의해 지정된 `트랜잭션 로그 디렉터리용 전용 디스크`
  * 3개의 전용 드라이브(루트 파일 시스템용, 스냅샷용, 트랜잭션 로그용)로 ZK 서버를 프로비저닝
  * 스냅샷 및 트랜잭션 로그를 SSD에 저장하는 것을 권장
* `서로 다른 Rack(랙)`에 ZK 서버를 구성 권장 - 동시에 Crash되는 것을 방지하기 위함이다.
* `swappiness를 최소로 낮추거나 비활성화`
  * 커널 버전에 따라 다르다.
  * 최신 Linux 커널에서는 vm.swappiness를 1로 설정하는 것을 권장하고 있다.
* `충분한 Physical Memory` - 통상 8 GB 이상
* Kafka에 사용된 것과 동일한 JVM 옵션 설정으로 JVM을 조정한다.
  * 예외: 대부분의 사용 사례에는 1GB의 Heap 사이즈 권장
  * Heap 사용량을 모니터링하여 Garbage Collection으로 인해 지연이 발생하지 않는지 확인

<br>

## Tuning ZooKeeper
### Service-Level 고려 사항
ZooKeeper는 안정적으로 일관된 시간 내에 Election(선택)을 수행하고 디스크에 정보를 읽고 쓸 수 있는 기능을 갖추어야 한다.  
다음은 해당 목표를 달성하는 데 도움이 되는 서비스 수준의 몇 가지 고려 사항이다.
* 최소 512 MB의 Heap Memory
  * Garbage Collection 때문에 일시 중지되었다는 로그가 있으면 즉시 이를 수정하기 위한 조치 필요하다.
* 쿼럼당 5개의 ZooKeeper 서버 구성
  * 안정적인 Leader Election을 수행하려면 홀수 개의 ZK 서버가 필요하다.
  * 3 노드 ZooKeeper 클러스터는 쓰기 지연 시간이 짧지만, 단일 서버 장애만 버틸 수 있다.

<br>

## Tuning ZooKeeper
### Static IP 고려 사항
* 현재 ZooKeeper 클라이언트(Kafka 포함)가 IP 주소를 다시 확인할 수 없기 때문에 서버 IP 주소가 변경될 수 있는 경우 ZooKeeper 서버를 호스팅해서는 안 된다.
* 연결 끊김으로 인한 디버깅 문제를 피하기 위해, 모든 클라이언트가 모든 ZooKeeper host/IP 를 포함하는 연결 문자열을 통해 모든 ZooKeeper 서버에 직접 연결할 수 있는지 확인하는 과정이 중요하다.
* ZooKeeper 서버 IP 주소가 변경되는 경우, 모든 클라이언트를 다시 시작해야 한다.

<br>

## Tuning ZooKeeper
### 기타 고려 사항
* ZooKeeper는 Kafka 인프라의 백본이므로 항상 안정적이고 사용 가능한 상태를 유지하는 것이 중요하다.
* ZooKeeper의 전체 중단은 Kafka 클러스터가 최적으로 작동할 수 없으며 새로운 Topic 생성 또는 기타 관리 기능과 같은 변경이 불가능하다는 것을 의미한다!

<br>

## Sanity Check
### 온전한지 확인
1. Leader가 하나만 있는지 확인
2. Quorum(쿼럼)이 존재하는지 확인
3. 모든 Follower가 동기화되어 있는지 확인

<br>

### The ZK Four Letter Words (예: `mntr`): "https://zookeeper.apache.org/doc/current/zookeeperAdmin.html#sc_zkCommands"
1. Leader가 어떤 노드인지 찾으려면 "`echo mntr | nc localhost 2181 | grep zk_server_state`"를 사용  
   (가능한 결과: "leader" 또는 "follower")
2. 쿼럼이 존재하는지 확인  
   `srvr` 명령을 사용하여 ZK 클러스터의 각 멤버에 대한 자세한 상태를 얻을 수 있다.  
   (https://stackoverflow.com/questions/34537530/how-to-validate-zookeeper-quorum 참고)
3. 모든 인스턴스에서 1번의 명령을 사용하여 전체 ZooKeeper 앙상블에 Leader가 하나만 있는지 확인 할 수 있다.
4. 모든 Follower가 Leader와 동기화되어 있는지 확인해야 한다.  
   "`echo mntr | nc localhost 2181 | grep zk_synced_followers`"는  
   "`echo mntr | nc localhost 2181 | grep zk_followers`"와 동일해야 한다.  

<br>

## Non-Recoverable ZK Instance
### 복구 불가능한 ZK 인스턴스
ZooKeeper가 실행되는 서버가 완전히 손실된 경우, 새 서버에서 새 ZooKeeper 인스턴스를 시작하여 복구할 수 있다.

* 이는 이전 ZK 인스턴스와 동일한 구성 및 myid 파일을 사용한다는 의미한다.
* Kafka와 마찬가지로 복제 프로세스가 ZK 서버에 필요한 모든 데이터를 새 ZK 서버로 전송한다.
* 이때 새 ZK 서버의 IP 주소가 변경된 경우, 새 ZK 서버 인스턴스에 대한 연결을 다시 확인해야 하므로 완전히 복구하려면 클러스터의 모든 ZooKeeper 서버를 순차적으로 다시 시작해야 한다.

<br>

### 새 서버에서 새 ZooKeeper 인스턴스를 시작하여 복구하는 과정
1. 항상 Leader ZooKeeper의 데이터를 백업하면 심각한 오류가 발생할 경우 최신 커밋 상태로 돌아갈 수 있다.
2. ZK 바이너리로 새 서버 준비
3. 계속 실행 중인 경우 ZooKeeper 프로세스를 정상적으로 중지  
   여기서 “정상적으로” 라는 말은 "kill -9"를 제외한 모든 것을 의미
4. 새 서버에서 ZooKeeper 프로세스를 시작
5. 서버에 새로운 IP가 있는 경우 모든 ZK 서버의 Rolling Restart 필요
6. 모든 Follower가 Leader와 동기화될 때까지 기다림  
   "`echo mntr | nc localhost 2181 | grep zk_synced_followers`"는  
   "`echo mntr | nc localhost 2181 | grep zk_followers`"와 동일해야 함  

<br>

## Summary
* Kafka에서의 ZooKeeper 의 역할
* ZooKeeper 구성시 핵심 고려 사항
* ZooKeeper 최적화
* ZooKeeper 장애시 복구 방법