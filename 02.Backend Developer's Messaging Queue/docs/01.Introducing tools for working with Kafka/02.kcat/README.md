# kcat 둘러보기
Kafka-cli와는 다르게 message produce 와 comsume에 특화되어 있는 Tool이라고 보면 된다.  
예전에는 Kafkacat이라는 이름을 가지고 있었지만, Kafka측의 요청으로 인해 kcat으로 변경되었다.  

<br>

## kcat의 주요 커맨드
```shell
docker run -it --rm \
  --name=kcat \
  --network=fastcampus-kafka-message-queue_default \
  edenhill/kcat:1.71 \
  -b kafka1:19092,kafka2:19092,kafka3:19092 \
  {모드 옵션} {상세 옵션}
```
`모드 옵션`과 `상세 옵션`을 가지고 kcat을 사용한다고 보면 된다.

* 모드 옵션
  * **-P** : Produce 모드
  * **-C** : Comsume 모드
  * **-L** : Metadata List 모드
  * **-Q** : Query 모드


* 상세 옵션
  * 각 모드 옵션들마다 모드내에서 필요한 다양한 옵션들이 있다.

<br>

## 실습
보통은 로컬에서 설치해서 사용하는 것이 일반적이지만, 해당 프로젝트에서는 Docker을 사용해서 실습을 해보자.  
따라서 우리는 docker compose를 사용하기 때문에 항상 이미지들이 컨테이너에 잘 실행 되어있는지 확인해주자.  

1. Topic 명령어
   * kcat으로는 Topic을 생성할 수 없다. 따라서 Kafka-cli를 통해서 생성해야 한다.
   * Topic 생성
     * ```shell
       docker compose exec kafka1 \
          kafka-topics.sh --create --topic my-json-topic \
            --bootstrap-server localhost:19092 \
            --replication-factor 2 \
            -- partition 3
       ```
   * kcat Metadata List 모드 명령어
     * topics와 partitions 정보를 list로 확인할 수 있다.
       * ```shell
         docker run -it --rm \
         --name=kcat \
         --network=fastcampus-kafka-message-queue_default \
         edenhill/kcat:1.71 \
         -b kafka1:19092,kafka2:19092,kafka3:19092 \
         -L
         ```
2. kcat Producer 모드 명렁어
   * ```shell
     docker run -it --rm \
     --name=kcat \
     -v "$(pwd)/file.txt:/app/file.txt" \
     --network=fastcampus-kafka-message-queue_default \
     edenhill/kcat:1.7.1 \
     -b kafka1:19092,kafka2:19092,kafka3:19092 \
     -P -t my-json-topic -l -K: /app/file.txt
     ```
     * json형식으로 된 파일을 발행하는 명령어다.
     * `-l` : line단위로 파일을 읽을 것이라는 옵션
     * `-K` : key는 `:` 으로 구분하겠다는 옵션
     * /app/file.txt : 해당 파일을 읽어서 Produce 진행, 컨테이너에서 접근할 수 있는 경로이다.
     * `-v` "$(pwd)/file.txt:/app/file.txt" : `$(pwd)`는 현재 위치를 뜻한다.  

3. kcat Consumer 모드 명령어
   * ```shell
     docker run -it --rm \
     --name=kcat \
     --network=fastcampus-kafka-message-queue_default \
     edenhill/kcat:1.71 \
     -b kafka1:19092,kafka2:19092,kafka3:19092 \
     -C -t my-json-topic
     ```
        * `-C -t {topic 이름}` : `-t`를 붙여 Topic 을 지정하여 읽어올 수 있다.
        * `-C -t {topic 이름} -p {partition 이름}` : `-p`를 붙여 특정 partition을 읽어올 수 있다.
        * 결과를 확인해보면 순서가 섞인 것 처럼 보이지만 partitioning된 topic에 메시지를 발행한 것이어서, 실제로는 메시지의 순서보장이 partition내에서 잘 이루어지고 있다.