# Kafka Log File

<br>

## Topic, Partition, Segment
### Physical View
* Partition은 Broker들에 분산되며, 각 Partition은 Segment File들로 구성된다.
* Rolling Strategy : `log.segment.bytes`(default 1 GB), `log.roll.hours`(default 168 hours)

![Topic, Partition, Segment](../images/41.Topic,%20Partition,%20Segment.PNG)

<br>

## Kafka Log Segment File Directory
### 각 Broker의 log.dirs 파라미터로 정의

Kafka Log Segment File은 Data File이라고 부르기도 한다.  
Segment File이 생성되는 위치는 각 Broker의 `server.properties` 파일 안에서 `log.dirs` 파라미터로 정의된다.(comma로 구분하여 여러 디렉토리 지정 가능하다.)  
```
log.dirs=/data/kafka/kafka-log-a,/data/kafka/kafka-log-b,/data/kafka/kafka-log-c 
```
각 Topic과 그 Partition은 log.dirs 아래에 하위 디렉토리로 구성된다.  
예로, `test_topic의 Partition 0`은 `/data/kafka/kafka-log-a/test_topic-0 디렉토리`로 생성된다.  

<br>

## Partition 디렉토리 안의 Log File들
### 파일명에 의미가 크다.
`test_topic` 의 `Partition 0` 디렉토리에 생성되는 파일의 예  
```
$ ls /data/kafka/kafka-log-a/test_topic-0
00000000000000123453.index
00000000000000123453.timeindex
00000000000000123453.log
00000000000007735204.index
00000000000007735204.timeindex
00000000000007735204.log
leader-epoch-checkpoint
```
`00000000000000123453.*` 파일은 00000000000000123453 offset부터 00000000000007735203 offset까지의 메시지를 저장/관리  
`[00000000000007735203 = 00000000000007735204 -1]`

<br>

## Partition 디렉토리에 생성되는 파일들의 타입
### 확장자 혹은 파일명으로 구분한다.
Partition 디렉토리에 생성되는 Files Types 은 최소 4가지  
* Log Segment File – 메시지와 metadata를 저장하는 가장 중요한 파일
  * `.log`
* Index File – 각 메시지의 Offset을 Log Segment 파일의 Byte 위치에 매핑시켜주는 일종의 index 파일
  * `.index`
* Time-based Index File – 각 메시지의 timestamp를 기반으로 메시지를 검색하는 데 사용
  * `.timeindex`
* Leader Epoch Checkpoint File – Leader Epoch과 관련 Offset 정보를 저장
  * `leader-epoch-checkpoint`

<br>

#### 특별한 Producer 파라미터 사용하면 Partition 디렉토리에 생기는 Files Types
* Idempotent Producer를 사용하면
  * `.snapshot`
* Transactional Producer를 사용하면
  * `.txnindex`

<br>

## Log Segment File의 특징
### 첫번째로 저장되는 메시지의 Offset이 파일명이 된다.
Partition은 하나 이상의 Segment File로 구성

![Features of Log Segment File](../images/42.Features%20of%20Log%20Segment%20File.PNG)  

Log Segment File의 파일명은 해당 Segment File에 저장된 첫번째 메시지의 Offset번호와 같다.

<br>

## Log Segment File Rolling
### 여러 파라미터가 존재
* 아래의 파라미터 중 하나라도 해당되면 새로운 Segment File로 Rolling
  * `log.segment.bytes` (default: 1 GB)
  * `log.roll.ms` (default: 168 시간)
  * `log.index.size.max.bytes` (default: 10 MB)

<br>

* `__consumer_offset` (Offset Topic)의 Segment File Rolling 파라미터는 별도
  * `offsets.topic.segment.bytes` (default: 100 MB)

<br>

## Checkpoint File
각 Broker에는 2 개의 Checkpoint File이 존재한다.
* `log.dirs` 디렉토리에 위치하게 된다.
* `replication-offset-checkpoint`
  * 마지막으로 Commit된 메시지의 ID인 High Water Mark를 저장하고 있다.
  * 시작시 Follower가 이를 사용하여 Commit되지 않은 메시지를 Truncate 한다.
* `recovery-point-offset-checkpoint`
  * 데이터가 디스크로 Flush된 지점
  * 복구 중 Broker는 이 시점 이후의 메시지가 손실되었는지 여부를 확인하기 위한 checkpoint다.

<br>

## Summary
### Kafka Log File
* Segment File이 생성되는 위치는 각 Broker의 server.properties 파일 안에서 log.dirs 파라미터로 정의한다.(comma로 구분하여 여러 디렉토리 지정 가능)
* 새로운 Segment File로 Rolling 하기 위한 여러 파라미터가 존재하고 있다.