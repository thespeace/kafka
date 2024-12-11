# Troubleshooting Producer

<br>

## Producer Metrics for Troubleshooting
`kafka.producer:type=producer-metrics,client-id=<client_id>`
* Response rate
* Request rate
* Request latency avg
* Outgoing byte rate
* IO ratio & IO wait ratio
* Record retry & error rate

<br>

## 일반적인 Producer Issues(1/2)
`Can not connect to Kafka`
* 단일 Bootstrap 서버만 정의했을 수 있으며, 이 서버에 연결할 수 없는 문제
* Broker의 잘못 구성된 `listeners` 및 `advertised.listeners`
* Producer(Port 포함)에서 올바른 Endpoint를 사용하고 있는지 확인
* Kafka 클러스터에 보안이 적용되어 있는데, 잘못된 Credentials을 사용하거나 전혀 Credentials 없이 액세스하려는 경우

<br>

`Can not write to Topic`
* Topic이 존재하지 않으며 Auto Topic Creation이 off 되어 있는 경우
* Topic이 ACL에 의해 보호되고, Producer에게 필요한 Authorization(권한)이 없는 경우

<br>

## 일반적인 Producer Issues(2/2)
`Producer is very slow`
* 레코드를 보낼 때마다 Producer를 다시 생성하는 경우(동일한 Producer 인스턴스 재사용해야 한다!)
* 최적이 아닌 Producer 구성, 특히 `batch.size`, `linger.ms`, `compression.type`, `acks`를 최적화를 안한 경우
* Kafka 클러스터에 Quotas(할당량)이 설정되어 있는 경우, 처리량이 제한될 수 있다.
* 레코드를 보낼 때 retries 및 errors가 많이 발생하는 경우, `record-retry-rate`와 `record-errorrate`를 관찰해야 한다.

<br>

`일반적인 Producer 관련 문제를 해결하려면, kafkacat 도구가 매우 유용하다.`
* 아래에 표시된 첫 번째 명령은 Kafka에서 메타데이터(모든 Topic에 대한)를 검색하는 명령어
* 두 번째 명령은 CSV 파일의 내용을 iot-data Topic에 기록
    ```
    $ kafkacat -L -b kafka:9092
    $ cat iot_data.csv | kafkacat -P -p -1 -b kafka:9092 -t iot-data
    ```
 
<br>

## librdkafka Debug Contexts

![librdkafka Debug Contexts](../images/12.librdkafka%20Debug%20Contexts.PNG)  
https://docs.confluent.io/platform/7.5/clients/librdkafka/html/md_INTRODUCTION.html


<br>

## Debugging Settings for Troubleshooting
For example,  
Troubleshooting common producer issues  
Set `debug=broker,topic,msg`  

![Debugging Settings for Troubleshooting](../images/13.Debugging%20Settings%20for%20Troubleshooting.PNG)  
https://docs.confluent.io/platform/7.5/clients/librdkafka/html/md_INTRODUCTION.html

<br>

## Summary
* Producer Metrics for Troubleshooting
* 일반적인 Producer Issues
* librdkafka Debug Contexts
* Debugging Settings for Troubleshooting