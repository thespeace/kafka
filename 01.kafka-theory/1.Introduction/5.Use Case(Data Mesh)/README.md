# Use Case - Data Mesh

## Data Mesh?
![Use Cases](../images/31.Use%20Cases20.PNG)  
기존의 데이터 웨어하우스나 데이터 레이크에서 중앙 집중적으로 관리되었던 분석 데이터들을 탈 중앙화해서 관리하는 데이터 아키텍처 패턴을 의미한다.  
쉽게 말하자면 MSA개념을 데이터 관리 및 운영에 적용한 데이터 아키텍처라고 할 수 있다.  
Data Mesh는 실제로 물리적인 것이 아니고, 기술에 구애 받지 않는 형식으로 최신 데이터 아키텍처를 설계하는 방법을 설명하는 4가지 원칙을 가지고 있다.

<br>

## Data Mesh의 4가지 원칙
![Use Cases](../images/32.Use%20Cases21.PNG)  
1. 첫번째로 데이터는 특정 도메인이 소유한다. 이것은 마이크로 서비스가 각각 특정 비즈니스 기능을 소유하는 것하고 굉장히 유사하다.  
   데이터에 대한 접근은 당연히 분산되어 있다.
2. 두번재로 데이터는 그것을 게시하는 각 팀에 의해서 제품으로 간주된다.  
   각 팀이 데이터를 제품으로 생각해서 전적으로 그 책임을 가지게 된다.
3. 세번재는 데이터는 누구나 회사 내 어디서나 셀프 서비스로 이용 가능해야 한다.
4. 데이터는 어디에 있든 통제하에 잘 관리되어야만 한다.  
   데이터 아키텍처가 완벽할 수 없고, 또한 데이터 아키텍처가 정적일 수가 없고 진화하고 성장하다 보니까 연합 거버넌스를 통해서 Data Mesh 데이터를 신뢰하고 보다 빠르게 탐색할 수 있도록 만들어야 한다.

<br>

### Principle 1: Domain-driven Decentralization
* 목표 : 데이터를 진정으로 이해하는 사람이 데이터를 소유하도록 하자.
![Use Cases](../images/33.Use%20Cases22.PNG)  
일반적으로 데이터 웨어하우스와 같이 중앙팀이 아닌 그 데이터가 생성된 곳, 데이터가 시작된 조직의 일부, 데이터의 도메인에 존재하는 팀이 소유해야 한다.  
이러한 접근 방식을 탈 중앙화라고 부르는 이유이다.

<br>

### Principle 2: Data as a First-Class Product
* 목표: 공유 데이터를 검색 가능하고, 주소 지정 가능하며, 신뢰할 수 있고, 안전하게 만들어, 다른 팀이 이를 잘 활용할 수 있도록 하자. 즉 해당 데이터를 사용하는 다른 팀을 고객으로 생각하자.
* 데이터는 부산물이 아닌 실제 제품으로 취급하자.  

![Use Cases](../images/34.Use%20Cases23.PNG)  

<br>

#### Data Product, a “Microservice for the data world”
* 데이터 제품(Data Product)은 도메인 내에 위치한 데이터 메시의 Node를 의미한다.
* 데이터 제품(Node)의 역할은 메시 내에서 고품질 데이터를 생성하고 소비할 수 있어야 한다.
* Node는 기능에 필요한 모든 요소, 즉 Data + Code + Infrastructure 를 캡슐화한다.

![Use Cases](../images/35.Use%20Cases24.PNG)  

<br>

#### 데이터 메시 내의 노드간의 연결은…
![Use Cases](../images/36.Use%20Cases25.PNG)  

<br>

#### Kafka 기반의 Data Streaming이 적합하다.
![Use Cases](../images/37.Use%20Cases26.PNG)  

<br>

## Data Streaming이 Data Mesh에 적합한 이유
![Use Cases](../images/38.Use%20Cases27.PNG)  
* Streams are **real-time, low latency** ⇒ 데이터를 **즉시** 전파
* Streams are **highly scalable** ⇒ 오늘날의 **방대한 데이터 량**을 처리
* Streams are **stored, replayable** ⇒ **실시간 및 과거** 데이터를 모두 캡처
* Streams are **immutable** ⇒ Audit 가능한 **레코드 소스**
* Streams are **addressable, discoverable, …** ⇒ 메시 데이터에 대한 **주요 기준**을 충족
* Streams are **popular for Microservices** ⇒ Data Mesh 적용에 **쉬움**

<br>

## [developer.confluent.io](https://developer.confluent.io/)
* **Free Courses** on all things Kafka and Data Streaming
* 50+ **Design Patterns** for Data Streaming
* **And more**: Quickstarts, Tutorials, ...

<br>

## Summary
* Data Mesh
* Data Mesh의 4가지 원칙(1,2번째 원칙 위주)
* Kafka가 Data Mesh에 적합한 이유
* developer.confluent.io