# Application 레벨에서의 CDC vs Infrastructure 레벨에서의 CDC
좁은 의미의 CDC는 인프라 레벨의 CDC를 의미하고, 넓은 의미의 CDC는 두 경우를 포함하게 된다. 

<br>

## Application 레벨에서의 CDC

* 구현 방식  
  ![CDC at the application level](../../../md_resource/CDC%20at%20the%20application%20level.PNG)

Application 레벨에서의 CDC는 위 그림처럼 애플리케이션 DB에서 write하고 Kafka Topic에서 메시지를 Produce해주는 모습이다.  
이를 Dual Write라 한다. 이러한 처리 과정을 트랜잭션으로 묶어서 Rollback을 추가 처리할 수 있겠지만, 결과적으로는 병렬적으로 데이터를 처리한다는 사실에는 변화가 없다.  
이렇게 데이터 변경사항을 애플리케이션에서 Dual Write하게 되면, 몇가지 단점이 있다.

1. DB에 C/U/D를 하는 로직이 여러 군데 흩어져 있을수도 있는데, 그 여러군데 다 Kafka Produce를 하는 로직을 추가해줘야 해서 개발자가 실수를 할 여지가 있다.
   * 예를 들어서 JPA를 통해서 DB에 write를 하는 곳에 Kafka Produce를 할 수 있도록 하나의 메서드로 묶어놨는데,  
     JDBC를 통해서 insert를 한다던지 그런 경우에는 예상치 못한 DB Write가 생기면서 Kafka에 Produce가 실패하면서 메시지 발행이 누락될 수 있는 휴먼 에러가 있을 수 있다.
2. 결과적으로 Dual Write로 병렬적으로 이루어지기 때문에, 두 개의 작업의 정합성이 깨지는 문제가 발생할 수 있다. 
   * DB에 Write를 하고나서 Kafka에 문제가 생긴다던지 아니면 이미 Kafka에 메시지를 발행했는데 DB 저장에 실패할 수 있다. 

<br>

## 인프라 레벨에서의 CDC
애플리케이션 레벨의 CDC보다 인프라 레벨에서의 CDC를 적용했을 때가 두 작업의 정합성을 보장할 수 있다. 

* 구현 방식  
  ![CDC at the infrastructure level](../../../md_resource/CDC%20at%20the%20infrastructure%20level.PNG)

위 그림처럼 데이터의 흐름이 직렬로 이루어 진다.  
DB에 데이터가 반영되면, 자동으로 kafka 메시지가 발행된다.

우리가 사용하는 대부분의 DB에는 리플리케이션을 위해서 변경 내역이 로그형태로 내부 관리가 되기때문에 이 로그를 읽어서 Kafka로 발행을 할 수 있는 원리이다.  
예를 들어서 Mysql의 바이너리 로그, MongoDB의 oplog들이 CDC의 소스가 되는 것이다.  

이러한 내용들을 살펴보면 애플리케이션 레벨의 CDC의 단점은 모두 해소가 되었지만, 인프라 레벨에서의 단점도 존재한다.  
1. 메시지의 타입을 세분화하고 싶은데, CDC에서는 기본적으로 Operation Type을 C/U/D로만 나눠서 메시지를 생산하기 때문에 메시지를 커스터마이징하기에 어려울 수 있다.
2. Delete를 실제로 하는게 아니라 Soft Delete(실제 데이터 삭제 X)를 해야하는 상황에서 Delete를 해도 DB에서는 Update로 인지하기 때문에 메시지 발행을 할 때,  
   Operation Type이 Update로 들어가서 결국에는 Topic을 Consume하는 측에서 이러한 내용들을 분기를 쳐서 대응해줘야 하는 불편함이 있을 수 있다.
3. 서비스를 관리하는 회사의 규모에 따라서 리소스적인 문제로 CDC를 관리하고, 구축하는 비용이 장점보다 더 클수도 있다.
   * 바이너리 로그라던지 oplog와 같은 DB에 직접 붙어서 읽어오는 과정이 DB에 부담이 될 수 있다.

결론적으로 CDC를 맹목적으로 사용하기 보다는 득과 실을 잘 따져보고 적용을 해야한다.

참고로 인프라 레벨에서의 CDC는 Kafka Connect라는 기술로 보통 이루어진다.  
하지만 우리가 앞으로 진행할 CDC의 과정들은 Kafka Connect를 이용한게 아니라 애플리케이션 레벨에서의 CDC의 니즈를 충족할 수 있게 실습을 할 예정이다.