# CMAK 둘러보기
Kafka-cli의 기능 중 produce, consume으로 특화된게 `kcat`이었다면, `CMAK`은 cluster, broker 관리쪽으로 특화된 도구이다.  
CLI가 아닌 GUI형태라서 좀 더 보기 편하고, yahoo에서 kafka manager이라는 오픈소스로 개발을 하다가 Apache kafka측의 요청에 따라서 CMAK으로 이름을 변경하였다.

<br>

## 실습
우리는 미리 만들어둔 docker를 사용해서 CMAK 이미지를 컨테이너에 띄우면 `localhost:9000`으로 CMAK에 접근할 수 있다.

CMAK을 사용하기 위해선 Cluster를 등록해야 한다.
* add Cluster
  * Cluster Name : my-cluster
  * Cluster Zookeeper Hosts : zookeeper:2181
  * Kafka Version : 2.4.0
  * 나머지는 기본 설정 값 그대로 사용

<br>

클러스터를 등록하게 되면   
* Summary : kcat의 `-l` 옵션과 비슷하게 `Topics와 Brokers의 수`를 확인 할 수 있다.
  * Topics : Summary에서 숫자를 클릭하면, Topics의 이름과 `Partitions`, `Brokers`, `Brokers Spread %`, `Brokers Skew %`, `Brokers Leader Skew %`, `Replicas`, `Under Replicated %` 를 확인 할 수 있다. 또한 토픽의 구성이 한쪽으로 치우쳐져있거나 불균형 상태라면 해당 사항에 색깔을 부여해 사용자가 알아채기 쉽게 해준다.
    * 특정 토픽 클릭 : 토픽 구성 사항을 살펴볼 수 있다.
      * Topic Summary : Topic의 구성 상황을 UI로 보여준다.
        * `Number of Partitions` : Leader Partitions의 개수를 나타낸다.  
        * `Replication` :  해당 Topic의 설정 Replication을 나타낸다.(`Total Partitions` = `Number of Partitions` * `Replication`)
        * `Broker Skewed %` : 전체 brokers의 개수 대비 어느쪽으로 얼마나 쏠려있는지 %로 표시해준다. 
        * `Broker Leader Skewed %` : brokers의 개수 대비 어느쪽으로 얼마나 쏠려있는지 %로 표시해준다. 
      * Partitions by Broker : Broker 입장에서 각각의 Broker의 구성을 UI로 확인 할 수 있다.
        * `# of Partitions` : 모든 Partitions의 개수
        * `# as Leader` : Leader Partitions의 개수
        * `Partitions` : 가지고 있는 Partitions의 이름
        * `Skewed?` : Partitions의 쏠림 현상을 true, false 형태로 나타낸다.(true일 경우, 색칠)
        * `Leader Skewed?` : Leader Partitions의 쏠림 현상을 true, false 형태로 나타낸다.(true일 경우, 색칠)
      * Operations : Topic 관리, Partitions 관리, Assign까지 할 수 있다.
        * `Manual Partition Assignments` :  파티션의 리플리카 할당을 사용자가 직접 지정할 수 있는 기능이다. 자동 분배가 아닌 파티션 및 리플리카 위치를 세밀하게 제어할 때 사용된다.
          * 실습을 잘 따라왔다면, 현재 상태는 아래와 같을것이다.
            * ![Balancing Metrics-Current Status](../../../md_resource/Balancing%20Metrics-Current%20Status.PNG)
            * Leader Partitions이 고르게 분배되지 않았다. 즉 Broker2에 있는 2개의 Leader 중 하나를 Broker3으로 보내야 한다.
            * Broker2의 Leader Partition 0을 Broker3으로 보내고, Broker3의 Follower Partition 1을 Broker2로 보내도록 작업해보자.
            * 위의 작업을 잘 마치면 아래와 같이 균등하게 분배하여 불균형을 해소할 수 있다.
              * ![Balancing Metrics](../../../md_resource/Balancing%20Metrics.PNG)
        * `Reaasign Partitions` : 파티션의 리플리카 위치를 재조정한다. `Manual Partition Assignments` 작업을 마치고 반드시 해줘야 반영된다.
        * `Update Config` : Topic의 다양한 옵션들을 설정할 수 있다.
        * `Add Partitions` : Partitions을 늘릴 수 있다.

<br>

실습에 필요한 부분만 언급해 설명하였는데, 이런식으로 CMAK은 message 자체의 중점을 두기보다 인프라(broker, partitions ...)적인 요소를 관리하는데 맞춰진 도구이다.  
간단하게 CMAK을 살펴보았는데 단순하게 도구만 살펴볼뿐만 아니라 Kafka라는 추상적인 기술에 대해서 UI, CLI를 보면서 좀 더 가시화한다면 kafka를 이해하는데 도움이 될 것이다.  
또한 다른 도구와도 비교해보면 도움이 많이 될 것이다.