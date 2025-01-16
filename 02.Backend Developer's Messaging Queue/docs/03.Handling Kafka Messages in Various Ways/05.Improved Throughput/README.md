# 처리량 (Throughput) 개선
처리량이라는 것은 Kafka를 사용하는 Producer와 Consumer측의 조화를 통해서 데이터 처리를 가장 빠르게 효율화하는 과정이라 볼 수 있다.  
이 처리량을 늘리려면 여러 가지 방법이 있다.

1. Rebalancing이라는 악조건을 최대한 줄이는 것
   * Rebalancing이 자주 일어나지 않도록 설정 값을 잘 조절하고, 메시지를 전달할 때 압축해서 전달하는 방식
2. Consumer 로직측에서 오래 걸리는 경우가 많기 때문에 로직 개선 및 최적화
3. 위 방법으로도 한계를 부딪힌다면 병렬처리가 필요
   * 같은 Topic에 대해서 Consumer Group이 가지고 있는 Consumer 개수가 많아지면, 일꾼이 많아지는 것이니 병렬 처리 능력이 향상됨

<br>

## Partition이 Consumer보다 많을 때
하나의 Consumer가 세 개의 Partition을 다 맡기엔 한계가 있다.  
1:1 대응이 가능하도록 Consumer 개수를 맞추게 되면, 하나의 일꾼이 하나의 일만 하게 되어 처리량이 향상된다.

<br>

## Consumer가 Partition보다 많을 때
만일 하나의 일꾼을 더 늘리면 처리량이 더 빨라지리라 기대할 수도 있지만, 그렇지 않다.  
왜냐하면 한 개의 Partition에 최대 한 개의 Consumer만 할당되게 되어있기 때문이다.  
다시 말해서 Consumer의 수를 Partition 수보다 많게 설정하는 것은 처리량 개선에 도움이 되지 않는다.  
이렇게 제한하는 이유는 Kafka에서 순서 보장을 보장하기 위해서이다.  

<br>

## 실습
* Kafka concurrency 설정 변경으로 처리량 개선

Kcat을 통해서 가짜 데이터 전송 후 Offset reset을 통해 Lag을 생성 후 console의 데이터 처리 확인.(+ `Thread.sleep(1000);`)
```shell
  docker run -it --rm \
  --name=kcat \
  -v "$(pwd)/file.txt:/app/file.txt" \
  --network=fastcampus-kafka-message-queue_default \
  edenhill/kcat:1.7.1 \
  -b kafka1:19092,kafka2:19092,kafka3:19092 \
  -P -t my-json-topic -l -K: /app/file.txt
```

<br>

## 다른 방법
Kafka concurrency만으로 처리량을 올릴 수 있는건 아니다.  
여러가지 방법이 있는데, 한 번의 Batch Listener을 통해서 데이터를 많이 가져와서 내부 Thread pool을 사용해서 데이터를 처리 할 수도 있다.  
하지만 이런 경우는 메시지 사이의 순서가 크게 중요하지 않은 경우 사용 가능하다.  
예를 들어서 알림을 발송할 때, 각 유저한테 중복알림만 가지 않으면 되고 누가 먼저 받는지는 크게 상관이 없을 경우 이와 같은 병렬처리가 가능하다.

* 내부적으로 Thread pool을 사용해서 Multi Thread로 처리량 개선
   ```java
   private final ExecutorService executorService = Executors.newFixedThreadPool(10); //10개의 쓰레드 풀 사용
   executorService.submit();
   ```
