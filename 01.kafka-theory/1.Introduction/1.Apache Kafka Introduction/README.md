# Kafka Introduction

## Apache Kafka는 무엇인가?
### [Data in Motion Platform for Enterprise](https://www.confluent.io/product/confluent-platform/) || Data Streaming Platform
![Data Streaming Platform](../images/01.Data%20Streaming%20Platform.PNG)  
Apache Kafka는 Data in Motion Platform으로 좁은 의미로는 비동기 메시징 플랫폼이로 불리우며, 데이터를 실시간으로 처리하고 전송하는 플랫폼이라서 Data Streaming Platform이라고 불린다.  
Kafka는 끊임없이 흐르는 강물처럼 데이터를 흐르게 해주는 역할을 하는데, 이런 데이터의 실시간 흐름을 끊김 없이 유지해주는 역할을 한다.  
마치 강물이 상류에서 하류로 흘러가며 다양한 곳에서 물을 취하듯, Kafka는 실시간 데이터를 생산하고 소비하는 다양한 애플리케이션들 간의 연결고리 역할을 한다.

<br>

## Event란?
### "비즈니스에서 일어나는 모든 일, 그 때 발생하는 Data"를 의미한다.
* 웹사이트에서 무언가를 클릭하는 것
* 청구서 발행
* 송금
* 배송 물건의 위치 정보
* 택시의 GPS 좌표
* 센서의 온도/압력 데이터

### 이벤트(데이터)를 비즈니스에 활용

<br>

## Event(Data) Stream은 무엇인가?
### Event(Data)는 BigData의 특징을 가진다.
* 비즈니스의 모든 영역에서 광범위하게 발생(모든 시스템에서 동시 다발적으로 발생)
* 대용량의 데이터(Big Data) 발생
### Event(Data) Stream은 연속적인 많은 이벤트(데이터)들의 흐름을 의미한다.

<br>

## Apache Kafka의 탄생
### Event Stream을 처리하기 위해 만들어진 솔루션이 바로 Apache Kafka이다.
### Linkedin에서 개발
* 하루 4.5조 개 이상의 이벤트 스트림 처리
* 하루 3,000억 개 이상의 사용자 관련 이벤트 스트림을 처리
* 기존의 Messaging Platform(ex) MQ)로 처리 불가능
* 데이터(이벤트) 스트림 처리를 위해 개발
* 2011년에 Apache Software Foundation에 기부되어 오픈소스화 되었다.

<br>

## Apache Kafka의 등장
### 2012년 최상위 Apache 프로젝트
* 2012년 Apache Incubator 과정을 벗어나 최상위 프로젝트가 되었다.
* [Fortune 100 기업 중 80% 이상이 사용](https://kafka.apache.org/)

<br>

## Apache Kafka의 특징
### 3가지 주요 특징
1. 데이터 스트림을 안전하게 전송(Publish & Subscribe)
2. 데이터 스트림을 디스크에 저장(Write to Disk)
3. 데이터 스트림을 처리 및 분석(Processing & Analysis)

<br>

## Apache Kafka 유즈케이스
### Event(메시지/데이터)가 사용되는 모든 곳에서 사용
* Legacy Messaging System 대체 - 백엔드 시스템 엔지니어
* IOT 디바이스 혹은 애플리케이션으로부터 데이터 수집 및 전송 - 애플리케이션 개발자/백엔드 시스템 엔지니어
* 시스템 혹은 애플리케이션에서 발생하는 로그 수집 및 전송 - 백엔드 시스템 엔지니어
* Realtime Data Stream Processing(Fraud Detection, 이상 감지 등) - 데이터 엔지니어
* 실시간 ETL - 백엔드 시스템 엔지니어
* Spark, Flink, Storm, Hadoop 과 같은 빅데이터 솔루션과 같이 사용 - 데이터 엔지니어

<br>

## 산업 분야별 Apache Kafka 유즈케이스
### 다양한 산업 분야에 사용
* 교통 : 운전자-탑승자 매치, 도착예상시간(ETA) 업데이트, 실시간 차량 진단
* 금융 : 사기 감지, 중복거래 감지, 거래,위험 시스템, 모바일 애플리케이션/고객 경험
* 오락 : 실시간 추천, 사기 감지, In-App 구매
* 온라인 마켓 : 실시간 재고 정보, 대용량 주문의 안전한 처리

<br>

## Apache Kafka의 성능
### 저렴한 장비로 초당 약 2백만 Writes 가능
### [Benchmarking Apache Kafka: 2 Million Writes Per Second (On Three Cheap Machines)](https://engineering.linkedin.com/kafka/benchmarking-apache-kafka-2-million-writes-second-three-cheap-machines)
* Intel Xeon 2.5 GHz processor(6 코어)
* 7200 RPM SATA 드라이버 6 개 : RAID 아닌 JBOD 로 구성
* 32 GB of RAM
* 1 Gb Ethernet
* 상기 스펙의 총 6 대 HW 사용  
  3 대 – Zookeeper/Load Generator, 3 대 – Kafka Broker
* Three Producer, 3x async replication :  
  2,024,032 records/sec (193.0 MB/sec)

<br>

## Apache Kafka와 RabbitMQ 비교
### [Benchmarking Apache Pulsar, Kafka, and RabbitMQ](https://www.confluent.io/blog/kafka-fastest-messaging-system/?utm_medium=sem&utm_source=google&utm_campaign=ch.sem_br.nonbrand_tp.prs_tgt.kafka_mt.mbm_rgn.apac_lng.eng_dv.all_con.kafka-rabbitMQ&utm_term=%2Bkafka%20%2Brabbitmq&creative=&device=c&placement=&gclid=CjwKCAjw3_KIBhA2EiwAaAAliiwLNAaE01pbjuDTGvedQ9G9qPJO-fyKJhHvGIxDq0s9RZ3oU2P9IRoCvT0QAvD_BwE)
![Apache Kafka와 RabbitMQ 비교](../images/02.Benchmarking%20Apache%20Kafka,%20Apache%20Pulsar,%20and%20RabbitMQ1.PNG)
![Apache Kafka와 RabbitMQ 비교](../images/03.Benchmarking%20Apache%20Kafka,%20Apache%20Pulsar,%20and%20RabbitMQ2.PNG)  
### 매우 높은 처리량을 제공하는 Kafka