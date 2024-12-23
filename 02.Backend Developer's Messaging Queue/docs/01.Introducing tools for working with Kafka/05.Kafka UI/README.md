# Kafka UI

<br>

## 실습
이전 도구들처럼 우리가 실행시켜 놓은 docker compose를 통해서 `localhost:8081`로 접근할 수 있다.
* `Dashboard` : Redpanda console과 비슷한 UI로 Cluster의 정보를 확인할 수 있다.
* `Brokers` : Brokers 정보를 확인 할 수 있다.
* `Topics` : Topics 정보를 확인 할 수 있다.
  * Redpanda console과 비슷하게 검색이 가능하고 직접 Topic 생성도 가능하다.
  * 각 Topic의 세부적인 정보와 설정 정보, Message, Consumer Group 정보를 확인할 수 있다.
  * `특히 Consumer Group 관리에 용이하다.`
* `Consumers` : Consumers 정보를 확인 할 수 있다. 
* `ACL` : Access Control List 및 사용자 관리를 할 수 있다.

참고로 계속 진행할 앞으로의 실습은 알아본 많은 도구 중 해당 도구(Kafka UI)를 위주로 사용할 것이다.