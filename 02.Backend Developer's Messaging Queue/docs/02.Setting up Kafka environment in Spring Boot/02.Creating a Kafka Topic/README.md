# Kafka Topic 생성하기

CLI가 아닌 Kafka UI로 간단하게 Topic을 생성해보자.  
물론 에플리케이션 레벨에서 토픽이 있으면 원래 있는 토픽을 사용하고, 없으면 자동으로 생성하게끔 설정할 수 있지만 이러한 설정은 막아두자.  
그 이유는 암시적으로 토픽을 관리하는 것보다 명시적으로 관리하는 것이 partition 수 라던지 원하는 설정 값대로 토픽을 만들수가 있고, 더 안정적으로 운영할 수 있기 때문이다.

<br>

## 실습
1. docker compose를 실행 후 [localhost:8081](localhost:8081)로 접속하여 Kafka-UI 실행
2. 메뉴바 `Topics`에 들어가서 그동안 실습했던 topics 삭제
3. `Add a Topic`을 통해 Topic 생성
   * Topic Name : `my-json-topic`
   * Cleanup policy : `Delete`
   * Number of partitions(Leader partitons 수) : `3`
     * broker가 3개여서 각각 하나씩 Leader partitions을 할당하기 위함
     * 만약 kafka의 운영난이도를 최대한 낮추고 싶다면 1개를 사용 추천
   * Min In Sync Replicas : 
   * Replication Factor : `2`
     * Leader partition마다 복제 partitions은 하나씩 두기 위함
   * Time to retain data(in ms) : `604800000(7 days)`
     * 실습에서는 많은 데이터를 다루지 않기 때문에 느슨하게 설정.
     * 만약 kafka를 log와 연결하는 경우, 12시간이나 하루정도로 설정하는 것을 추천