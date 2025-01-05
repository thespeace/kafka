# Consumer Group의 Offset 관리
Offset에 대해 리마인드해보자.  
Offset이라는 것은 각 메시지들이 특정 Topic의 Partition으로 들어온 순서대로 선착순 번호표를 가지게 되는 개념이다.  
지금까지의 실습과정에서도 확인했다시피 Consumer Offset이라고 하는 internal Topic에 보관되고 있다.  
아주 예전 버전에서는 zookeeper가 Offset을 관리를 했었는데, kafka version 0.9 이후부터는 broker 내부에서 Topic 형태로 관리되고 있다.

<br>

실무에서 생각보다 Offset을 되돌려야 하는 경우가 많다.

* 데이터 손실 방지: Consumer가 특정 메시지를 처리하지 못했거나, 처리 중 에러가 발생한 경우 해당 메시지를 다시 처리해야 할 때 offset을 조정해야 합니다.
* 데이터 재처리: 특정 이유로 기존 데이터를 처음부터 다시 처리해야 할 때(예: 새로운 처리 로직을 도입했을 때).
* Retention 기간 초과로 인해 메시지가 삭제된 경우
* partition을 재구성한 경우
* 새 Consumer Group을 시작한 경우
* 오류 복구 및 데이터 불일치
* 시스템 변경 또는 배포

Offset Reset 과정과 Consume을 재개하는 경우를 살펴보자.

<br>

### 평소 상태 - 모든 메시지를 컨슘 완료
평소에는 Consumer Group들이 각 Topic의 각 Partition의 마지막 번호를 바라보고 있다.  

![Normal status All messages consumed completed](../../../md_resource/Offset%20reset%20example1.PNG)

<br>

### 특정 컨슈머그룹의 Offset을 Reset
여기서 만약 특정 Consumer Group의 Offset을 Reset하고 싶다면, 아래의 그림과 같이 이동을 해야할 것이다.

![Reset the offset of a specific consumer group](../../../md_resource/Offset%20reset%20example2.PNG)  
![Reset the offset of a specific consumer group](../../../md_resource/Offset%20reset%20example3.PNG)  

<br>

### 그 컨슈머그룹은 Lag이 생기게 됨
특정 Consumer Group의 Offset이 이동을 하고 나면, 메시지의 마지막 Offset과 차이가 생기면서 `Lag`이 발생한다.  

![That consumer group will have Lag](../../../md_resource/Offset%20reset%20example4.PNG)  

<br>

### 컨슈머를 다시 띄우면?
Consumer을 다시 재개시켜서 Consume을 하면, `Lag`을 따라잡으면서 다시 진행이 되고 평소 상태로 다시 원복을 하게 된다.

![resume consume](../../../md_resource/Offset%20reset%20example5.PNG)  
![resume consume](../../../md_resource/Offset%20reset%20example6.PNG)  

<br>

### 사실 이 과정은 파티션 단위로 이루어진다.
사실 위의 그림들에서 Topic이라 표기한건 단순화 차원에서 표기한 것이고, 사실은 위 과정들은 Partition 단위로 Offset 관리가 된다.  
각 Consumer Group은 Partition마다 Offset을 가리키고 있고, Offset Reset도 Partition별로 이루어진다. 

![Offset management by partition, not by topic](../../../md_resource/Offset%20reset%20example7.PNG)