# Use Case - Microservices and Event Driven Architecture

## Event Driven Microservices 아키텍처로 진화 중
![Use Case](../images/21.Use%20Cases10.PNG)

<br>

### Monolith Application
![Use Case](../images/22.Use%20Cases11.PNG)  
* 상호 의존적인 부분이 매우 많은 복잡한 애플리케이션
* 다른 서비스에 영향을 주지 않고 배포하기 어렵다
* 열악한 개발자 경험 및 생산성 저하
* 서비스 출시 시간 지연 초래

<br>

### Microservices Application
![Use Case](../images/23.Use%20Cases12.PNG)  
* 여러 개의 더 작은 단일 기능 애플리케이션
* 독립적으로 배포 및 업그레이드 가능
* 다른 프로그래밍 언어로 구축 가능
* 서비스별로 확장 및 축소
* 더 빠른 서비스 출시

하지만 마이크로 서비스를 사용하다 보면 결국은 어떠한 도전과제들을 겪게 된다. 살펴보자.

<br>

## Microservice Challenges

### Microservice Challenges - Orchestration

#### Independent Scalability(독립적인 확장성)
![Use Case](../images/24.Use%20Cases13.PNG)  
* 컴포넌트 단위로 배포 및 확장/축소 가능
* 애플리케이션에 따라 개별의 라이프 사이클에 의해 서비스를 관리
* 개별 확장/축소 가능 - 병목 현상이 되는 특정 서비스만의 확장에 의해 전체 처리량 향상

<br>

#### Cascading Failure(연쇄 장애)
하지만 특정 서비스가 응답할 수 없게 되면, 그 서비스를 호출하는 서비스가 응답을 받을 수 없다. 즉 연쇄 장애가 벌어지게 된다.  
![Use Case](../images/25.Use%20Cases14.PNG)  
1) 서비스 장애, 고부하로 인한 반응 속도 저하가 전체적인 정지/성능 저하로 이어진다.

이러한 상황들을 극복하기 위해서 서비스간의 통신을 동기방식의 REST API가 아닌 비동기 방식의 Messaging으로 하는 경우들도 있었다.

<br>

### Microservice Challenges - Legacy Messaging

#### Asynchronous Communication(비동기 통신)
![Use Case](../images/26.Use%20Cases15.PNG)  
* 동기 통신 방식의 단점을 극복 가능
* 마이크로서비스 간에 비동기 방식의 통신을 사용하여 서비스간 종속성을 없앰
* 서비스 연쇄 장애 문제를 해결 가능

<br>

#### Lack of Message Persistence(메시지 유지 취약)
Queue에 저장된 데이터를 가져가면 그 데이터를 지움  
데이터 유실의 가능성이 높음  
1) 그 데이터를 필요로 하는 다른 서비스가 있으면 데이터를 다른 Queue로 복제해야 하는 문제 발생

<br>

#### Low Performance/High Latency(낮은 성능/높은 지연시간)
대용량의 데이터(BigData) 전송/처리를 위한 처리량  
제공 불가능  
2) 긴 대기시간(Latency)  
3) 이벤트 스트리밍 처리 불가능 - 실시간 분석 불가능  

<br>

### Buying an iPad(with REST) - 예시
* 주문 서비스는 배송 서비스를 호출하여 물건을 배송하도록 지시
* 배송 서비스는 배송할 주소를 조회(고객 서비스에서)

![Use Case](../images/27.Use%20Cases16.PNG)  
 
<br>

### 서비스 간 이벤트 기반 비동기 메시징
* 주문 서비스는 더 이상 배송 서비스(또는 기타 서비스)에 대해 알지 못함
* 주문 서비스는 주문 이벤트를 생성 및 Broker로 전송함

![Use Case](../images/28.Use%20Cases17.PNG)  

<br>

## Apache Kafka와 RabbitMQ 비교
많은 솔루션 중 왜 굳이 비동기 메시징 솔루션으로 Apache Kafka를 사용해야 할까?
### 매우 높은 처리량을 제공하는 Kafka
[Benchmarking Apache Kafka, Apache Pulsar, and RabbitMQ:Which is the Fastest?1)](https://www.confluent.io/blog/kafka-fastest-messaging-system/?utm_medium=sem&utm_source=google&utm_campaign=ch.sem_br.nonbrand_tp.prs_tgt.kafka_mt.mbm_rgn.apac_lng.eng_dv.all_con.kafka-rabbitMQ&utm_term=%2Bkafka%20%2Brabbitmq&creative=&device=c&placement=&gclid=CjwKCAjw3_KIBhA2EiwAaAAliiwLNAaE01pbjuDTGvedQ9G9qPJO-fyKJhHvGIxDq0s9RZ3oU2P9IRoCvT0QAvD_BwE)

![Use Case](../images/29.Use%20Cases18.PNG)

<br>

![Use Case](../images/30.Use%20Cases19.PNG)

<br>

## Summary
* Monolith / Microservices Application
* Microservice Challenges
* Kafka 기반의 Event Driven Microservices