# 자동커밋과 수동커밋

<br>

## 자동 커밋 (Auto commit)
자동커밋은 주기에 따라서 커밋을 자동으로 해주는 기능으로 기본 값 5초로 설정되어 있는 `auto.commit.interval.ms` 라는 주기를 가지고 있다.  

![Auto Commit](../../../md_resource/Auto%20Commit1.PNG)

Consumer은 `poll()`을 해서 partition에 접근을 하는데, 매번 자동으로 커밋하지 않고, 특정 주기를 가지고 커밋의 유무를 결정한다.  
커밋할 시점이 되었다고 생각되면, 읽어온 데이터의 마지막 Offset을 기준으로 커밋을 자동으로 한다.

<br>

Kafka 내부 코드(`poll()`)로 살펴보자.

![Auto Commit](../../../md_resource/Auto%20Commit2.PNG)

![Auto Commit](../../../md_resource/Auto%20Commit3.PNG)

`nextAutoCommitTimer.isExpired()`로 만료여부를 확인하고, 만료시 오토커밋을 실행한다.

<br>

## 수동 커밋(Manual commit)

개발자가 명시적으로 커밋할 시점을 코드 내에 지정을 해주는 것이다.  
현재까지의 실습은 따로 커밋 시점을 명시하지 않았기 때문에 자동커밋이었다.