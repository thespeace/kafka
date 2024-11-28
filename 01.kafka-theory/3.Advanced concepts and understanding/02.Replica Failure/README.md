# Replica Failure

<br>

## In-Sync Replicas 리스트 관리
### Leader가 관리함
메시지가 ISR 리스트의 모든 Replica(복제본)에서 수신되면 Commit된 것으로 간주된다.  
Leader는, Kafka Cluster의 Controller에 의해 모니터링되는, ZooKeeper의 ISR 목록에 대한 변경 사항을 항상 잘 유지하도록 기능을 제공한다.  
n개의 Replica가 있는 경우 n-1개의 장애를 허용할 수 있다.

* Follower가 실패하는 경우
  * Leader에 의해 ISR 리스트에서 삭제된다.
  * Leader는 새로운 ISR을 사용하여 Commit한다.


* Leader가 실패하는 경우
  * Controller는 Follower 중에서 새로운 Leader를 선출한다.
  * Controller는 새 Leader와 ISR 정보를 먼저 ZooKeeper에 Push한 다음 로컬 캐싱을 위해 Broker에 Push한다.

<br>

## ISR은 Leader가 관리
### ZooKeeper에 ISR 업데이트, Controller가 ZooKeeper로부터 수신
1. Follower가 너무 느리면 Leader는 ISR에서 Follower를 제거하고 ZooKeeper에 ISR을 유지
2. Controller는 Partition Metadata에 대한 변경 사항에 대해서 Zookeeper로부터 수신

![ISR is managed by the Leader](../images/10.ISR%20is%20managed%20by%20the%20Leader.PNG)

<br>

## Leader Failure
### Controller가 새로운 Leader 선출
Controller가 새로 선출한 Leader 및 ISR 정보는, Controller 장애로부터 보호하기 위해, ZooKeeper에 기록된 다음 클라이언트 메타데이터 업데이트를 위해 모든 Broker에 전파

![Leader Failure](../images/11.Leader%20Failure.PNG)

<br>

## Broker Failure
### Broker 4 대, Partition 4, Replication Factor가 3 일 경우를 가정
Partition 생성시 Broker들 사이에서 Partition들이 분산하여 배치된다.

![Broker Failure](../images/12.Broker%20Failure.PNG)

<br>

### Broker 4에 장애가 발생하면?

![Broker Failure](../images/13.Broker%20Failure2.PNG)

<br>

## Partition Leader가 없으면
Partition에 Leader가 없으면, Leader가 선출될 때까지 해당 Partition을 사용할 수 없게 된다.  
Producer의 send() 는 `retries 파라미터`가 설정되어 있으면 재시도한다.  
만약 `retries=0` 이면, `NetworkException`이 발생함  

<br>

## Summary
### Replica Failure
* Follower가 실패하는 경우, Leader에 의해 ISR 리스트에서 삭제되고, Leader는 새로운 ISR을 사용하여 Commit을 하면서 복제가 이루어진다.
* Leader가 실패하는 경우, Controller는 Follower 중에서 새로운 Leader를 선출하고, Controller는 새 Leader와 ISR 정보를 먼저 ZooKeeper에 Push한 다음 로컬 캐싱을 위해 Broker에 Push해서 알려준다.
* Leader가 선출될 때까지 해당 Partition을 사용할 수 없게 된다.
