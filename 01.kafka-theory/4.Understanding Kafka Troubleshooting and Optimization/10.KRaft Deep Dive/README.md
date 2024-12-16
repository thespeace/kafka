# KRaft Deep Dive

<br>

## ZooKeeper와 Kafka Controller
### ZooKeeper를 제거한 KRaft mode

![Using Zookeeper](../images/36.Using%20Zookeeper.PNG)

* Zookeeper는 아래와 같은 Metadata 정보를 저장한다.
  * broker IDs
  * racks
  * topics
  * partitions
  * ISR information
* Zookeeper는 아래의 용도로 사용한다.
  * Controller election
  * Watchers to determine broker availability


<br>

## ZooKeeper, KIP-500 and Kraft
### ZooKeeper를 제거한 KRaft mode
![ZooKeeper, KIP-500 and Kraft](../images/37.ZooKeeper,%20KIP-500%20and%20Kraft.PNG)

<br>

## KRaft 장점
![Advantages of KRaft](../images/38.Advantages%20of%20KRaft.PNG)

<br>

## KRaft Release Timeline
### KRaft marked "production-ready"

![KRaft Release Timeline](../images/39.KRaft%20Release%20Timeline.PNG)

<br>

## Isolation mode vs Combined mode
### Dedicated mode vs Shared mode
![Isolation mode vs Combined mode](../images/40.Isolation%20mode%20vs%20Combined%20mode.PNG)

<br>

## KRaft 하드웨어 권장 사양
* 일반적으로 ZooKeeper를 실행하는 서버와 사양이 비슷한 서버에서 KRaft를 실행한다.
* 운영환경의 경우 권장 사양
  * 최소 4 GB RAM
  * 서버를 공유하는 경우(예, VM등) 전용 CPU 코어를 고려해야 한다.
  * 최소 64 GB의 SSD 디스크를 권장
  * 최소 1 GB의 JVM Heap Memory가 권장
  * 자세한 내용은 아래 링크를 참조
    * https://docs.confluent.io/platform/current/kafka-metadata/config-kraft.html
    * https://docs.confluent.io/platform/current/kafka-metadata/zk-production.html#zk-hardware

<br>

## Summary
* ZooKeeper, KIP-500, KRaft
* Kraft 장점
* Kafka Release Timeline
* Isolation Mode vs Combined Mode
* KRaft 하드웨어 권장 사양