# Replica Recovery
### acks=all 의 중요성
아래 예시는 3개의 Replica로 구성된 하나의 Partition  
Producer가 4개의 메시지(M1, M2, M3, M4)를 보냈다.  

![The importance of acks=all](../images/14.The%20importance%20of%20acks=all%201.PNG)

<br>

### Broker X가 장애가 나면, 새로운 Leader가 선출됨  
Controller가 Y를 Leader로 선출했다고 가정하자.

![The importance of acks=all](../images/15.The%20importance%20of%20acks=all%202.PNG)

한번 들어온 데이터는 지우지 않는다. 그리고 Follower가 Leader가 되는 순간, 전체의 데이터를 그대로 인지하게 된다.  
따라서 Commit 되어 있지 않은 데이터까지도 새로운 Leader가 다 가질 수 있게 된다.

<br>

### Broker X는 M3, M4에 대한 ack를 Producer에게 보내지 못했다.  
Producer는 재시도에 의해 M3, M4를 다시 보낸다.  

![The importance of acks=all](../images/16.The%20importance%20of%20acks=all%203.PNG)

<br>

### 만약 acks=1 이었다면?
Y, Z가 복제를 못했던 M4는 어떻게 될까?

![What if acks=1?](../images/17.What%20if%20acks=1.PNG)

<br>

### 장애가 발생했던 X가 복구되면?
X는 Follower가 되어서 Y로부터 복제한다.  

![What if X, which had a failure, is restored?](../images/18.What%20if%20X,%20which%20had%20a%20failure,%20is%20restored.PNG)  

![What if X, which had a failure, is restored?](../images/19.What%20if%20X,%20which%20had%20a%20failure,%20is%20restored%202.PNG)

<br>

## Availability 와 Durability
### 가용성과 내구성 중 어떠한 것을 선택해야 할까?
* Topic 파라미터 - `unclean.leader.election.enable`
  * ISR 리스트에 없는 Replica를 Leader로 선출할 것인지에 대한 옵션 (default : false)
  * ISR 리스트에 Replica가 하나도 없으면 Leader 선출을 하지 않는다. – 서비스 중단
  * ISR 리스트에 없는 Replica를 Leader로 선출한다. – 데이터 유실  

<br>

* Topic 파라미터 – `min.insync.replicas`
  * 최소 요구되는 ISR의 개수에 대한 옵션 (default : 1)
  * ISR 이 min.insync.replicas 보다 적은 경우, Producer는 NotEnoughReplicas 예외를 수신한다.
  * Producer에서 `acks=all`과 함께 사용할 때 더 강력한 보장 + `min.insync.replicas=2`
  * n개의 Replica가 있고, min.insync.replicas=2 인 경우 n-2개의 장애를 허용할 수 있다.  

<br>

* 데이터 유실이 없게 하려면?
  * Topic : `replication.factor` 는 2 보다 커야 함(`최소 3이상`)
  * Producer : `acks` 는 `all` 이어야 함
  * Topic : `min.insync.replicas` 는 1 보다 커야 함(`최소 2 이상`)

<br>

* 데이터 유실이 다소 있더라도 가용성을 높게 하려면?
  * Topic : `unclean.leader.election.enable` 를 `true` 로 설정

<br>

## Summary
## 가용성과 내구성 관련 파라미터
* `replication.factor`
* `acks`
* `min.insync.replicas`
* `unclean.leader.election.enable`