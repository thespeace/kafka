# 중복 Consume과 Consume 누락이 일어날 수 있는 Case 알아보기
Consume이 단 한번만 일어나면 가장 좋겠지만 그렇지 않은 경우가 많은데, 어떤 경우에 발생하는지 알아보자.

<br>

## 이 문제를 막기가 어려운 근본적인 이유
* 커밋 시점과 데이터처리 완료 시점이 완벽하게 일치할 수 없다. (그 시간 간극 동안 리밸런싱이 일어나면서 Partition과 Consumer 간의 재배치가 이루어 질 수가 있다!)
* 오프셋 관리가 Consumer측이 아니라 Broker 측에서 이루어지기 때문이다.
  * __consumer_offsets, Topic에서 Offset이 관리
    * Consumer가 자체적으로 Offset을 관리한다면, 데이터 처리 후 Offset을 바로 처리를 해줄 수 있을텐데 그게 아니라 데이터 처리하는 건 Consumer고, Offset을 관리하는 건 Broker이니까 동시 처리가 어렵다.
* 개발자 자의에 의해 offset reset을 해서 다시 컨슘할 일이 종종 있다.
  * 예를 들면, 로직을 다시 실행 해야 하는 경우가 있다.

<br>

## 자동 커밋(Auto Commit) - 누락 발생
아래 그림처럼 partition에 줄지어 있는 Offset을 기준으로 순차적으로 메시지를 가져와서 처리하고 commit도 하게 된다.  
데이터 처리와 commit이 정확히 동시에 일어나면 좋은데, 아무래도 주기가 있다보니 조금씩 편차가 생긴다.  

![Auto commit - Missing occurs](../../../md_resource/Auto%20commit(Missing%20occurs)1.PNG)

위 그림처럼 fetch를 진행하는데 101번, 102번의 데이터를 처리 하였다.  
그런데 자동 커밋을 할 주기(`auto.commit.interval.ms`)가 되어서 아래 그림처럼 미리 가져온 101~103번을 커밋을 하게 되었다.

![Auto commit - Missing occurs](../../../md_resource/Auto%20commit(Missing%20occurs)2.PNG)

이 상태에서 Rebalance가 시작되면, Consumer와 Partition간의 관계가 끊어지고 새로운 관계들을 만들게 된다.  
새로 만들진 관계로 다시 데이터의 Consume이 재개가 될 때에는, Consumer Offset이라는 Topic을 읽어서 Broker측의 기록을 기준으로 메시지 Consumer이 재개된다.  
그래서 아래 그림과 같이 103번의 데이터는 처리가 된 걸로 인식하여 누락이 발생하게 되어 데이터 유실이 발생하게 된다.

![Auto commit - Missing occurs](../../../md_resource/Auto%20commit(Missing%20occurs)3.PNG)  

![Auto commit - Missing occurs](../../../md_resource/Auto%20commit(Missing%20occurs)4.PNG)

<br>

## 자동 커밋(Auto Commit) - 중복 발생
위의 예시와 같이 fetch를 해와서 101번,102번 데이터를 처리한 후 아직 커밋할 시점이 되지 않았는데, Rebalance가 시작이 된 경우이다.

![Auto commit - duplicate occurrence](../../../md_resource/Auto%20Commit(duplicate%20occurrence)1.PNG)

그러면 broker 측에서는 100번까지만 데이터 처리가 완료되었다고 인식하게 되고 Consumer와 Partition간의 관계가 재형성이 되었을 때, 100번까지만 데이터를 읽었으니 101번부터 다시 가져가라고 할 것이다.  
101번부터 다시 fetch를 진행하게 되어 아래의 그림처럼 중복이 발생하게 된다.

![Auto commit - duplicate occurrence](../../../md_resource/Auto%20Commit(duplicate%20occurrence)2.PNG)

중복은 수동 커밋도 마찬가지이다.

<br>

## 수동 커밋(Manual Commit) - 중복 발생(자동 커밋과 마찬가지)
수동 커밋은 보통 데이터 처리를 다 하고 나서 커밋을 하게 된다.  
다시 말해서 데이터 처리는 했지만 커밋을 하기 전에 rebalance가 시작된다면, 자동 커밋의 케이스와 똑같이 겪게 된다.  
어쨋든 수동 커밋은 데이터 처리 타이밍과 커밋 타이밍의 순서를 개발자가 원하는대로 조절할 수 있지만, 그 시점을 일치시킬 수는 없다.  
즉 이런 근본적인 문제는 해결할 수 없다.

<br>

## 아무튼 Rebalancing이 문제다.
리밸런싱이 자주 일어나면 여러가지가 문제가 될 수 있다.
• 리밸런싱이 일어나면서, 중복 컨슘 혹은 컨슘 누락 가능성이 발생하는 타이밍이 생긴다.
• 심지어, 리밸런싱이 일어나는 동안에는 Partition과 Consumer의 관계를 재형성하는 과정이기 때문에 컨슘이 멈추기 때문에 성능상 문제도 생긴다.

<br>

## Rebalancing은 언제 생기나?
* Partition이 추가 되거나 reassign된 경우 (Broker 측의 변화)
* Consumer group 내 Consumer가 추가되거나 제거된 경우 (Consumer 측의 변화)
  * 능동적으로 변화할때가 아니더라도 Consumer 측의 변화 중에는 Consumer 비정상 상황에 대한 대응 개념도 같이 포함되어 있어서 좀 더 유심히 살펴야 한다.
    * `session.timeout.ms`와 `heatbeat.interval.ms`의 관계에 의해서 발생
      * heartbeat는 Consumer가 살아있는 신호를 보내주는 것이다.
      * heartbeat가 한동안 Broker측으로 오지 않아서 timeout으로 인지하게 되면, Consumer 상태가 비정상으로 판단하게 된다.
      * 보통은 `session.timeout.ms`대비 `heatbeat.interval.ms`을 3분의 1정도로 짧게 설정 한다.
* `max.poll.interval.ms`
  * poll을 한동안 해가지 않으면, Consumer 상태를 비정상으로 판단한다.
  * Consumer 상태가 실제로 비정상이 아니어도 문제가 생길 수 있다.
  * 대표적으로 데이터 처리 로직이 무거워 처리 시간이 오래 걸리는 경우인데, Consumer가 poll을 하지 못해서 `max.poll.interval.ms`를 넘기게 되면 Consumer가 중단되게 된다.
  * Consumer가 중단이 되면 지속적으로 Lag이 쌓이게 되고 시간이 지날수록 Lag이 계속 불어나게 된다.

<br>

## 정리
사실 중복 Consume 및 Consume 누락은, Consumer만의 입장이다.
* 커밋 관점에서는 단 한번만 레코드를 다루는 것이 맞다.
* 커밋을 하는건 Broker쪽이고, Consume을 하는건 Consumer쪽이다보니 데이터 처리를 두번하게 될 수도 있고 못하게 될수도 있는 상황이 발생하는 것이다.

대비하자면
* Rebalancing을 줄일 수 있게 옵션값을 잘 설정하자!
* 일반적으로, 중복이 누락보다 낫다. 따라서 자동커밋보다는 수동커밋을 활용하는게 좋다!
  * 반대로 어느정도의 누락은 크게 상관이 없는 경우에는 자동커밋을 활용하는 것도 괜찮다. ex) 로그 데이터에서 극히 일부의 누락은 전체적인 통계에 영향이 없는 경우