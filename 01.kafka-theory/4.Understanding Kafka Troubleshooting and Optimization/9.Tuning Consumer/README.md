# Tuning Consumer

<br>

## 병렬처리 최대화(1/2)

![Maximize parallel processing](../images/31.Maximize%20parallel%20processing1.PNG)

* 각 Partition은 특정 Consumer Group의 단일 Consumer에 의해서만 소비된다.
* Consumer는 Topic내의 다수의 Partition들을 Consume할 수 있다.
* Partition Assignment Strategy에 따라 어떤 Partition이 어떤 Consumer에게 할당되는지가 결정된다.
* Strategy 종류 - Range, RoundRobin, Sticky, CooperativeSticky 및 Custom
* 하나의 Consumer Group에 둘 이상의 Consumer들을 추가하여 Consume 작업을 병렬화하여 처리할 수 있다.

<br>

## 병렬처리 최대화(2/2)
* 단일 Consumer로 구성된 Consumer Group는 병렬처리가 최소화된다.
* 각 Consumer가 정확히 하나의 Partition을 Consumer해야 하는 경우는 병렬처리가 최대화된다.
* Topic의 Partition 개수보다 Consumer Group의 Consumer가 더 많으면 Extra Consumer는 Idle(유휴) 상태가 된다.  


* Partition 개수에 대한 중요 고려사항:
  * 클러스터에는 몇 개의 Broker가 있는지
  * Consumer Group의 Consumer는 몇 개인지
  * 특정 Partition 관련 요구 사항이 있는지
  * Partition 개수를 관리 가능하게 유지
  * Partition을 과도하게 할당하면 안된다.

<br>

## Consumer Liveness

![Consumer Liveness](../images/32.Consumer%20Liveness.PNG)

* 각 Consumer는 전용 Thread를 통해 Group Coordinator(Broker)에게 주기적인 Liveness Signal를 보낸다.
* `session.timeout.ms` 이상 Liveness Signal 이 수신되지 않으면 Consumer는 죽은 것으로 간주되고 Group Coordinator는 Partition 재할당을 Trigger한다.
* 나머지 Consumer Group 구성원에 대한 Partition 할당 계산은 Group Leader에게 위임된다.
* 그런 다음 Group Coordinator는 Group의 각 Consumer에게 새 Partition 할당을 전달한다.
* `heartbeat.interval.ms` 는 두 개의 연속 하트비트 신호 사이의 간격이 얼마나 긴지 정의한다.


* Consumer의 Liveness Thread가 여전히 작동하지만, 데이터에 대한 Polling을 수행하는 Main Thread가 ”hang”이 걸려서 `max.poll.interval.ms` 을 초과하면 해당 Consumer는 죽은 것으로 간주된다.
* Group Coordinator가 Partition 재할당을 Trigger한다.

<br>

## Consumer Group Join/Leave(1/2)

![Consumer Group Join/Leave(1/2)](../images/33.Consumer%20Group%20Join,Leave1.PNG)

* Partition Reassignment (또는 ”Rebalancing")은 Consumer가 Group에 Join하거나 기존 Consumer가 Group을 Leave 할 때 Trigger된다.
* 위 그림에서는 방금 새 구성원이 Join한 Consumer Group A를 볼 수 있다.  
  Group Coordinator (Broker 1)는 Reassignment/Rebalancing을 Trigger한다.  
  또한 기존 멤버가 Leave한 Consumer Group B가 있으며 Group Coordinator (Broker 3)는 해당 Consumer로부터 더 이상 Liveness Signal 이 없다는 사실로 인해 이를 알아차린다.  
  이에 따라서 Partition Reassignment/Rebalancing 이 Trigger된다.

<br>

## Consumer Group Join/Leave(2/2)

![Consumer Group Join/Leave(2/2)](../images/34.Consumer%20Group%20Join,Leave2.PNG)

* 그림에서 볼 수 있듯이, 하나의 Broker는 여러 Consumer Group의 Coordinator가 될 수 있다.
* Consumer Group은 Coordinator가 될 Broker를 어떻게 식별할까?  
  * hash(`group.id`) % (`__consumer_offsets` Topic의 Partition 개수) - 왼쪽의 수식을 통해 구해진 숫자를 가지는 Partition(`__consumer_offsets` Topic)의 Leader를 가지는 Broker가 해당 Consumer Group의 Group Coordinator이며, 물론 해당 Broker가 Fail하면 Coordinator는 자동으로 해당 Partition의 새 Leader로 Failover된다.

<br>

## Fetch Request

![Fetch Request](../images/35.Fetch%20Request.PNG)

Broker의 Consumer Fetch Request 처리 구조는 Producer Request와 매우 유사하다.  
유일하게 다른 점은, Request가 ISR의 Response를 기다리지 않고, 두 Consumer 속성 중 하나가 초과될 때까지 기다리는 Purgatory에 있다.  
`fetch.max.wait.ms`(default 500)  
`fetch.min.bytes`(default 1)


`fetch.min.bytes`=1 인 Fetch Request(가져오기 요청)은 Consumer가 사용할 새로운 Data byte가 최소한 1 byte 이상 있을 때까지 응답되지 않는다.  
이를 통해 Consumer는 새 데이터가 도착했는지 확인하느라 바빠질 필요가 없도록 ”Long Polling"을 허용한다.  
얼마나 오래 기다릴지는 `fetch.wait.max.ms`로 정의한다.

<br>

## fetch.min.bytes / fetch.max.wait.ms
* Topic에 데이터가 많지 않으면 어떻게 될까?  
  `fetch.min.bytes`를 기본값인 1 로 유지하여 Latency(대기 시간)을 최소화할 수 있다.  
  (`fetch.wait.max.ms`도 기본값으로 유지)
* Fetch Request(가져오기 요청)이 데이터를 조금 기다리도록 하여 Broker의 부하를 줄일 수 있다.
* Throughput(처리량)을 늘리려면 Latency(대기 시간)을 추가해야 한다.  
  `fetch.min.bytes`를 높은 값으로 설정하고, 합리적인 `fetch.wait.max.ms` 시간을 선택  
  예) 500ms
* 주의: 처리할 수 있는 것보다 더 많은 것을 가져오게 하면 안된다.
  * 그렇지 않으면 앞에서 언급한 것과 동일한 문제가 발생
  * 즉, Consumer가 죽은 것으로 간주되어 Consumer Group에서 Reassignment 발생
  * 한 번에 가져오는 데이터 양에 대한 상한을 설정  
    `fetch.max.bytes`
  * `max.partition.fetch.bytes`

<br>

## Commit(1/3)
Consumer Offset을 Commit하는 데는 무시할 수 없는 시간이 걸린다.  
사용 사례와 해당 Consumer가 허용하는 경우 Commit 빈도를 줄이면 줄일 수록 부담은 덜해진다.

### Auto Commit
`enable.auto.commit=true` 로 설정하면 Consumer는 5 초 마다 클라이언트가 `poll()`에서 받은 가장 큰 Offset을 Commit한다.  
5초 간격이 default이며 `auto.commit.interval.ms` 를 설정하여 제어한다.  
Consumer의 다른 모든 것과 마찬가지로 Auto Commit은 Polling Loop(폴링 루프)에 의해 구동된다.  
Polling할 때마다 Consumer는 Commit할 시간인지 확인하고, Commit할 시간이 되면 마지막 Poll 에서 반환한 Offset을 Commit한다.

Auto Commit은 5 초(default) 마다 발생한다는 점은 생각해볼만한 부분이다.  
가장 최근 Commit 후 3 초가 지났고 Rebalancing이 Trigger되었다고 가정해보자.  
Rebalancing 후 모든 Consumer는 Commit된 마지막 Offset부터 다시 Consume을 시작한다.  
이 경우, Offset은 3 초가 지났기 때문에 해당 3 초 내에 도착한 모든 이벤트가 두 번 처리된다.  
더 자주 Commit하도록 Commit Interval을 구성하고 Record가 중복되는 Window를 줄이는 것은 가능하지만, 완전히 제거하는 것은 불가능하다.

<br>

## Commit(2/3)
### Commit Current Offset
대부분의 개발자는 메시지 누락 가능성을 제거하고 Rebalancing 중에 중복 처리되는 메시지 수를 줄이기 위해 Offset이 Commit되는 시간을 더 효과적으로 제어할 수 있다.  
Consumer API에는 Timer를 기반으로 하기 보다는 애플리케이션 개발자가 이해할 수 있는 지점에서 Current Offset을 Commit하는 옵션이 있다.

`enable.auto.commit=false`를 설정하면 애플리케이션이 명시적으로 선택한 경우에만 Offset이 Commit된다.  
Commit API 중 가장 간단하고 안정적인 것은 `commitSync()`방식이다.  
이 API는 `poll()`에서 반환된 최신 Offset을 Commit하고 Offset이 Commit되면 return하며 어떤 이유로 Commit이 실패하면 Exception(예외)를 발생시킨다.

<br>

## Commit(3/3)
### Asynchronous Commit
Manual Commit의 한 가지 단점은 Broker가 Commit 요청에 응답할 때까지 응용 프로그램이 Block 된다는 것이다.  
이로 인해 애플리케이션의 Throughput(처리량)이 제한된다.  
Commit 빈도를 줄여 처리량을 향상할 수 있지만, Rebalancing으로 인해 생성될 잠재적인 중복 항목 수가 늘어날 수 있다.  
또 다른 옵션은 Asynchronous Commit API인 `commitAsync()`이다.

Broker가 Commit에 응답할 때까지 기다리는 대신, 요청을 보내고 계속 진행한다.  
단점은 `commitSync()`가 성공하거나 재시도할 수 없는 오류가 발생할 때까지 Commit을 재시도하는 반면, `commitAsync()`는 재시도하지 않는다는 것이다.

<br>

## Monitoring
주요한 관찰 대상 Metrics
* records-lag-max
* fetch-rate
* fetch-latency
* records-per-request, bytes-per-request

Consumer Group을 튜닝하려면, 피드백 루프가 절대적으로 중요하다.  
가장 좋은 방법은 Consumer의 필수 파라미터를 모니터링하는 것이다.

<br>

## 서비스 목표에 따른 Optimization(최적화)
* Throughput(처리량)
* Latency(지연시간)
* Durability(내구성)
* Availability(가용성)

최우선 순위를 잘 정해야 한다.

https://www.confluent.io/ko-kr/resources/white-paper/optimizing-your-apache-kafka-deployment/
 
<br>

## Optimization(최적화) for Throughput(처리량)
Consumer:
* `fetch.min.bytes`: 100,000 까지 증가 (default 1)

https://www.confluent.io/ko-kr/white-paper/optimizing-your-apache-kafka-deployment/

<br>

## Optimization(최적화) for Latency(지연시간)
Consumer:
* `fetch.min.bytes=1` (default 1)

https://www.confluent.io/ko-kr/white-paper/optimizing-your-apache-kafka-deployment/

<br>

## Optimization(최적화) for Durability(내구성)
Consumer:
* `enable.auto.commit=false` (default true)
* `isolation.level=read_committed` (EOS Transaction을 사용할 때)
 
https://www.confluent.io/ko-kr/white-paper/optimizing-your-apache-kafka-deployment/

<br>

## Optimization(최적화) for Availability(가용성)
Consumer:
* `session.timeout.ms`: 가능한 한 낮게 (default 10000)

https://www.confluent.io/ko-kr/white-paper/optimizing-your-apache-kafka-deployment/

<br>

## Summary
* 병렬처리 최대화
* Consumer Liveness
* Consumer Group Join/Leave
* Fetch Request
* Commit
* 서비스 목표에 따른 Optimization(최적화)