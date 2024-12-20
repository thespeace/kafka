# Redpanda console 둘러보기
이전 이름은 kowl로 올빼기모양이 그려진 도구였는데, 현재는 우리가 익히 알고 있는 레서판다의 이름인 Redpanda console로 리뉴얼되었다.  
kowl이 Redpanda 회사로 인수가 되면서 Redpanda 생태계안으로 들어가게 되었다.

<br>

## 실습
CMAK과 마찬가지로 Redpanda console도 UI로 접근할 수 있는데, 우리가 실행시켜 놓은 docker compose를 통해서 이미 `localhost:8989`로 접근할 수 있다.  

* `Overview` : 현재 kafka 전체 구성에 대해 살펴볼 수 있다. CMAK보다 훨씬 많은 정보를 제공해준다.
* `Topics` : 클러스터에 있는 모든 Topic의 리스트와 상세 정보 제공해준다.
  * 특히 message를 보는데 특화되어 있는데 key-value, Timestamp, Partition, Offset을 한 눈에 확인하기 편하게 표로 작성되어 있다.
  * 심지어 검색 기능과 Filter 옵션을 지정( ex) `return value.age == 18`)해서 메시지를 확인 할 수 있고, 해당 메시지를 다운로드 할 수도 있다. 
* `Schema Registry` : (사용 시) Avro, Protobuf 등 메시지 스키마를 관리 및 조회할 수 있다.
* `Consumers Groups` : Consumer 그룹의 상태, 오프셋, 지연(latency) 정보를 확인할 수 있다. 
* `Security` : 인증 및 권한 부여 설정, 클러스터 보안 상태를 확인하고 관리할 수 있다.
* `Quotas` : 클라이언트나 그룹별로 리소스 사용량 제한(트래픽, 메시지 크기 등)을 설정하고 모니터링 할 수 있다.
* `Connectors` : 데이터 파이프라인의 커넥터(소스 및 싱크)를 구성하고 관리할 수 있다.