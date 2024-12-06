# Exactly Once Semantics(EOS) 2

<br>

## Transaction
### 새로운 핵심 개념들을 도입
#### Transaction을 구현하기 위해, 몇 가지 새로운 개념들이 도입
* `Transaction Coordinator`  
  Consumer Group Coordinator와 비슷하게, 각 Producer에게는 Transaction Coordinator가 할당되며, PID 할당 및 Transaction 관리의 모든 로직을 수행하는 broker내에 있는 Transaction Coordinator Thread를 뜻한다.
* `Transaction Log`  
  새로운 Internal Kafka Topic으로써, Consumer Offset Topic과 유사하게, 모든 Transaction의 영구적이고 복제된 Record를 저장하는 Transaction Coordinator의 상태를 저장하기 위한 저장소 역할을 한다.
* `TransactionalId`  
  Producer를 고유하게 식별하기 위해 사용되며, 동일한 TransactionalId를 가진 Producer의 다른 인스턴스들은 이전 인스턴스에 의해 만들어진 모든 Transaction을 재개(또는 중단)하는 것을 구별하기 위해 사용된다.

<br>

## Transaction 관련 파라미터
### Broker Configs

![Broker Configs(Transaction)](../images/50.Broker%20Configs(Transaction).PNG)

<br>

### Producer Configs

![Producer Configs(Transaction)](../images/51.Producer%20Configs(Transaction).PNG)

<br>

### Consumer Configs

![Consumer Configs(Transaction)](../images/52.Consumer%20Configs(Transaction).PNG)

* Consumer가 중복해서 데이터 처리하는 것에 대해 보장하지 않으므로, Consumer의 중복처리는 따로 로직을 반드시 작성해야 한다.(Idempotent Consumer)
* 예를 들어, 메시지를 성공적으로 사용한 후 Kafka Consumer를 이전 Offset으로 되감으면 해당 Offset에서 최신 Offset까지 모든 메시지를 다시 수신하게 된다.

<br>

## Transaction Data Flow 관련 예제 소스 코드
### Consume 하고 Produce하는 과정을 Transaction으로 처리
KIP-98 : Exactly Once Delivery and Transactional Messaging  
https://cwiki.apache.org/confluence/display/KAFKA/KIP-98+-+Exactly+Once+Delivery+and+Transactional+Messaging

![Example source code related to Transaction Data Flow](../images/53.Example%20source%20code%20related%20to%20Transaction%20Data%20Flow.PNG)

* `initTransactions` 으로 시작
* `poll` 로 Source Topic에서 record를 가져온다.
* Transaction을 시작(`beginTransaction()`)
* `record`로 비즈니스로직 수행 후, 결과 record를 Target Topic으로 send
* `sendOffsetsToTransaction`을 호출하여 consume(poll)한 Source Topic에 consumer offset을 commit
* `commitTransaction` 또는 `abortTransaction` 으로 Transaction Commit 또는 Rollback수행

<br>

## Transaction Data Flow
### Transaction 처리 프로세스
KIP-98 : Exactly Once Delivery and Transactional Messaging  
https://cwiki.apache.org/confluence/display/KAFKA/KIP-98+-+Exactly+Once+Delivery+and+Transactional+Messaging

![Transaction Data Flow](../images/54.Transaction%20Data%20Flow.PNG)

<br>

## Transaction Data Flow
### Transaction 처리 프로세스
1. Transactions Coordinator 찾기  
   Producer가 `initTransactions()`를 호출하여 Broker에게 FindCoordinatorRequest를 보내서 Transaction Coordinator의 위치를 찾는다.  
   Transaction Coordinator는 PID를 할당

![Transaction Data Flow](../images/55.Transaction%20Data%20Flow.PNG)

<br>

2. Producer ID 얻기  
   Producer가 Transaction Coordinator에게 InitPidRequest를 보내서(TransactionalId를 전달) Producer의 PID를 가져온다.  
   PID의 Epoch를 높여 Producer의 이전 Zombie 인스턴스가 차단되고 Transaction을 진행할 수 없도록 한다.  
   해당 PID에 대한 매핑이 2a단계에서 Transaction Log에 기록

![Transaction Data Flow](../images/56.Transaction%20Data%20Flow2.PNG)

<br>

3. Transaction 시작  
   Producer가 `beginTransactions()`를 호출하여 새 Transaction의 시작을 알려준다.  
   Producer는 Transaction이 시작되었음을 나타내는 로컬 상태를 기록한다.  
   첫 번째 Record가 전송될 때까지 Transaction Coordinator의 관점에서는 Transaction이 시작되지 않는다.  

![Transaction Data Flow](../images/57.Transaction%20Data%20Flow3.PNG)

4. 1.AddPartitionsToTxnRequest  
   Producer는 Transaction의 일부로 새 TopicPartition이 처음 Write될 때 이 요청을 Transaction Coordinator에게 보낸다/  
   이 TopicPartition을 Transaction에 추가하면 Transaction Coordinator가 4.1a 단계에서 기록  
   Transaction에 추가된 첫 번째 Partition인 경우 Transaction Coordinator는 Transaction Timer도 시작  

![Transaction Data Flow](../images/58.Transaction%20Data%20Flow4.PNG)

<br>

4. 2.ProduceRequest  
   Producer는 하나 이상의 ProduceRequests(Producer의 `send()`에서 시작됨)를 통해 User Topic Partitions에 메시지를 Write  
   이러한 요청에는 4.2a 에 표시된 대로 PID, Epoch 및 Sequence Number가 포함  

![Transaction Data Flow](../images/59.Transaction%20Data%20Flow5.PNG)

<br>

4. 3.AddOffsetCommitsToTxnRequest  
   Producer에는 Consume되거나 Produce되는 메시지를 Batch 처리할 수 있는 `sendOffsetsToTransaction()` 가 있다.  
   sendOffsetsToTransaction 메서드는 groupId가 있는 AddOffsetCommitsToTxnRequests를 Transaction Coordinator에게 보낸다.  
   여기서 Transaction Coordinator 는 내부 __consumer_offsets Topic에서 이 Consumer Group에 대한 TopicPartition을 추론  
   Transaction Coordinator는 4.3a 단계에서 Transaction Log에 이 Topic Partition의 추가를 기록

![Transaction Data Flow](../images/60.Transaction%20Data%20Flow6.PNG)

<br>

4. 4.TxnOffsetCommitRequest  
   Producer는 __consumer_offsets Topic에서 Offset을 유지하기 위해 TxnOffsetCommitRequest를 Consumer Coordinator에게 보낸다.  
   Consumer Coordinator는 전송되는 PID 및 Producer Epoch를 사용하여 Producer가 이 요청을 할 수 있는지(Zombie가 아님) 확인  
   Transaction이 Commit 될 때까지 해당 Offset은 외부에서 볼 수 없다.  

![Transaction Data Flow](../images/61.Transaction%20Data%20Flow7.PNG)

<br>

5. 1.EndTxnRequest  
   Producer는 Transaction을 완료하기 위해 `commitTransaction()` 또는 `abortTransaction()`을 호출  
   Producer는 Commit되거나 Abort되는지를 나타내는 데이터와 함께 Transaction Coordinator에게 EndTxnRequest를 보낸다.  
   Transaction Log에 PREPARE_COMMIT 또는 PREPARE_ABORT 메시지를 write  

![Transaction Data Flow](../images/62.Transaction%20Data%20Flow8.PNG)

<br>

5. 2.WriteTxnMarkerRequest  
   Transaction Coordinator가 Transaction에 포함된 각 TopicPartition의 Leader에게 이 요청을 보낸다.  
   이 요청을 받은 각 Broker는 COMMIT(PID) 또는 ABORT(PID) 제어 메시지를 로그에 기록  
   __consumer_offsets Topic에도 Commit (또는 Abort) 가 로그에 기록  
   Consumer Coordinator는 Commit의 경우 이러한 오프셋을 구체화하거나 Abort의 경우 무시해야 한다는 알림을 받게 된다.  

![Transaction Data Flow](../images/63.Transaction%20Data%20Flow9.PNG)

<br>

5. 3.Writing the final Commit or Abort Message  
   Transaction Coordinator는 Transaction이 완료되었음을 나타내는 최종 COMMITTED 또는 ABORTED를 Transaction Log에 기록  
   이 시점에서 Transaction Log에 있는 Transaction과 관련된 대부분의 메시지를 제거할 수 있다.  
   Timestamp와 함께 완료된 Transaction의 PID만 유지하면 되므로 결국 Producer에 대한 TransactionalId->PID 매핑을 제거할 수 있다.  

![Transaction Data Flow](../images/64.Transaction%20Data%20Flow10.PNG)

<br>

## Summary
### Transaction
* Transaction 관련 파라미터
* Transaction Data Flow