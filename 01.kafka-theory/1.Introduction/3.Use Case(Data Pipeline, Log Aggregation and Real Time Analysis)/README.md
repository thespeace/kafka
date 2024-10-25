# Use Case - Data Pipeline, Log Aggregation and Real Time Analysis

## Apache Kafka 주요 Use Cases
![Use Cases](../images/12.Use%20Cases1.PNG)

<br>

## Data Pipeline Use Cases
![Use Cases](../images/13.Use%20Cases2.PNG)  
* 특히 MSA 통합을 Scalable하게 만드는 용도로 쓰이고 있다.
* 데이터 파이프라인을 배치 형태가 아니라 스트림 형태로 만드는 경우에도 사용된다.
* SIEM 최적화, 하이브리드 클라우드, 멀티 클라우드 서비스에서도 사용.

<br>

## Data Pipeline/Log Aggregation 예시
![Use Cases](../images/14.Use%20Cases3.PNG)  
위 그림과 같이 각각의 로그 별로 다 따로 취합하기 위한 독점적인 소프트웨어를 쓰는 경우가 많다.

### 도전 과제
* point to point로 각각 연결을 하게 되는 상황이 많다보니 서로 데이터 공유가 쉽지 않다.
* Single Source로만 데이터를 보낼 수 있는 독점적인(Proprietary) Forwarders.
* 공유되지 않도록 잠긴(Locked) Data.
* Data Volumes 증가에 따른 확장이 어렵다.
* 매우 높은 indexing 비용.
* Noisy Data를 필터링할 수 없다.
* 느린 Batch Processing.

위와 같은 도전과제를 극복하기 위해 Kafka가 사용된다.  
Kafka는 이런 다양한 로그들, 다양한 시스템들로부터 데이터를 쉽게 가져 올 수 있는 굉장히 많은 Connector들이 존재한다.

<br>

## Instantly Connect Popular Data Sources & Sinks
![Use Cases](../images/15.Use%20Cases4.PNG)  
위와 같은 Connector들 덕분에 좀 더 쉽게 Data Pipeline/Log Aggregation 구성을 쉽게 할 수 있다.  
그리고 모아진 데이터를 쓰고 싶어하는 곳으로 쉽게 전송을 할 수 있다.

<br>

### Confluent Platform
Confluent Platform에는 고객이 Kafka 및 Confluent Platform을 사용하여 다양한 생태계 세트를 빠르고 안정적으로 통합할 수 있도록 지원하는 120개 이상의 사전 구축된 Connector가 있다.  
![Use Cases](../images/16.Use%20Cases5.PNG)

<br>

### SIEM Optimization
![Use Cases](../images/17.Use%20Cases6.PNG)

<br>

### Real Time Analysis
![Use Cases](../images/18.Use%20Cases7.PNG)

<br>

### Real Time Analysis을 위한 도구
이벤트 스트리밍 애플리케이션 및 실시간 분석 기능을 구축하기 위해서는 ```Apache Spark```, ```Flink```, ```Storm``` 과 같은 여러 분산 시스템을 구축, 통합 및 관리해야 한다.  
![Use Cases](../images/19.Use%20Cases8.PNG)

<br>

## ksqlDB
ksqlDB는 SQL 구문을 사용하여 완전한 엔드투엔드 이벤트 스트리밍 애플리케이션을 구축하는 데 필요한 모든 것을 제공한다.  
![Use Cases](../images/20.Use%20Cases9.PNG)

<br>

## Summary
* Apache Kafka 주요 Use Cases
* Data Pipeline/Log Aggregation
* Real Time Analysis