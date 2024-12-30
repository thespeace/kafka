# spring-cloud-stream에서 Produce, Consume 해보기

* docker compose 실행
  * ```docker compose up -d```
* Spring Application 실행
  * console 확인: `test-consumer-group: partitions assigned: [my-json-topic-0, my-json-topic-1, my-json-topic-2]`
    * consumer group에 leader partition 3개가 할당
* Kafka UI 확인
  * [Consumers 확인](http://localhost:8081/ui/clusters/local/consumer-groups)
    * consumer group 확인 후 비어 있는 messages 개수와 분포되어 있는 partitions를 확인 할 수 있다.
  * [my-json-topic의 message 확인](http://localhost:8081/ui/clusters/local/all-topics/my-json-topic/messages?keySerde=String&valueSerde=String&limit=100)
    * 비어있는 messages를 확인
* Produce(Message 발행)
  * [swagger-ui](http://localhost:8080/swagger-ui/index.html#/my-controller/message)에 접속
  * /message api 요청
  ```json
  {
    "id": 101,
    "age": 20,
    "name": "Thespeace",
    "content": "Hi, I am Thespeace. Nice to meet you!"
  }
  ```
* Consume(Message 확인)
  * console 확인: `Message arrived! - MyMessage(id=101, age=20, name=Thespeace, content=Hi, I am Thespeace. Nice to meet you!)`, payload가 잘 꺼내진걸 볼 수 있다.
  * [Kafka UI의 Topics](http://localhost:8081/ui/clusters/local/all-topics/my-json-topic/messages?keySerde=String&valueSerde=String&limit=100)에서 확인, [consumer group](http://localhost:8081/ui/clusters/local/all-topics/my-json-topic)에서도 어느 partition에 들어온지 확인 할 수 있다.