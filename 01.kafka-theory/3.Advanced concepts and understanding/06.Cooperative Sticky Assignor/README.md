# Cooperative Sticky Assignor

<br>

## Consumer Rebalancing Process
### 시간 흐름에 따른 Consumer Rebalance 과정
1. Consumer들이 JoinGroup 요청을 Group Coordinator 에 보내면서 리밸런싱이 시작되며-
2. JoinGroup의 응답이 Consumer들에 전송되고(Group Leader는 Consumer들 정보를 수신)-
3. 모든 구성원은 Broker에 SyncGroup 요청을 보내야 하며(Group Leader는 각 Consumer의 Partition 할당을 계산해서 Group Coordinator에게 전송)-
4. Broker는 SyncGroup 응답에서 각 Consumer별 Partition 할당을 전송한다.

![Consumer Rebalancing Process](../images/36.Consumer%20Rebalancing%20Process.PNG)

<br>

## Eager Rebalancing protocol
### 오랫동안 사용되었던 방식
Eager Rebalancing 프로토콜은 최대한 단순하게 유지하기 위해 만들어져있다.
* 각 구성원은 JoinGroup 요청을 보내고 재조정에 참여하기 전에 소유한 모든 Partition을 취소해야 한다.
* 안전면에서는 좋지만 이 "Stop-the-World" 프로토콜은 그룹의 구성원이 재조정 기간 동안 작업을 수행할 수 없는 단점이 있다.

![Eager Rebalancing protocol](../images/37.Eager%20Rebalancing%20protocol.PNG)

<br>

## Incremental Cooperative Rebalancing Protocol
### 이전 Eager Rebalancing 프로토콜 보다 발전한 방식
Revoke 할 Partition만 Revoke 하면 되지 않는가!!!

![Incremental Cooperative Rebalancing Protocol](../images/38.Incremental%20Cooperative%20Rebalancing%20Protocol.PNG)

<br>

## Incremental Cooperative Rebalancing Protocol
### 이상적인 Consumer Rebalancing 프로토콜
Consumer A, B가 Consume하고 있는 상태에서 처리량을 늘이기 위해서 Consumer C를 추가하는 경우라고 가정해보자.
* Consumer A에 할당된 Partition중 하나만 Consumer C로 이동하는 것이 가장 이상적이다.
* 전체 재조정 동안 모두 정지 상태로 있는 대신, Consumer A만 하나의 Partition을 취소하는 동안만 가동 중지

![Incremental Cooperative Rebalancing Protocol](../images/39.Incremental%20Cooperative%20Rebalancing%20Protocol.PNG)

<br>

## Cooperative Sticky Assignor
### Rebalancing을 두 번 수행
* JoinGroup 요청을 보내면서 시작하지만, 소유한 모든 Partition을 보유하고, 그 정보를 Group Coordinator에게 보낸다.
* Group Leader는 원하는 대로 Consumer에 Partition을 할당하지만, 소유권을 이전하는 Partition들만 취소한다.
* Partition을 취소한 구성원은 그룹에 ReJoin하여 취소된 Partition을 할당할 수 있도록 두 번째 재조정을 트리거

![Cooperative Sticky Assignor](../images/40.Cooperative%20Sticky%20Assignor.PNG)

<br>

## Cooperative Sticky Assignor
### Rebalancing을 두 번 수행
* Basic Cooperative Rebalancing 프로토콜은 Apache Kafka 2.4 에서 도입이 되었다.
* `Incremental Cooperative Rebalancing 프로토콜은 Apache Kafka 2.5에서 추가 되었다.`
* 빈번하게 Rebalancing되는 상황이거나 스케일 인/아웃으로 인한 다운타임이 우려가 된다면, 최신 버전의 Kafka(2.5 이상)기반으로 사용하는 것을 권장

<br>

## Summary
### Cooperative Sticky Assignor
* Cooperative Sticky Assignor
* `Incremental Cooperative Rebalancing 프로토콜은 Apache Kafka 2.5에서 추가`