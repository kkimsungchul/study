# 로컬에 Kafka 설치하기

로컬에 Kafka를 설치하기

### java 설치, 8 이상 버전, 11 설치 추천

`$ java --version`

`$ brew search jdk`

`$ brew install --cask adoptopenjdk11`

`$ sudo apt-get install openjdk-1-jdk`

zshrc(`bash_profile`)에 자바 경로 설정

`$ export JAVA_HOME_11=$(/usr/libexec/java_home -v11)`

`$ source ~/.zshrc`

### kafka 다운로드

`$ wget http://mirror.navercorp.com/apache/kafka/3.3.2/kafka_2.13-3.3.2.tgz`

wget: command not found, $ sudo apt-get -y install wget으로 wget 설치

### kafka 압축풀기

`$ tar -xvzf kafka_2.13-3.3.2.tgz`

`$ cd kafka_2.13-3.3.2`

### zookeeper 설정

`$ vim config/zookeeper.properties`

dataDir=/tmp/zookeeper # zookeeper 기본 데이터 폴더
clientPort=2181  # zookeeper port
maxClientCnxns=0 # client 수 설정 0이면 unlimit

#로컬에서 실행할 것이기 때문에 추가해줄 부분은 없음

### 주키퍼 실행

`$ cd kafka_2.13-3.3.2`
`$ ./bin/zookeeper-server-start.sh config/zookeeper.properties`

-daemon 백그라운드 옵션

### kafka 설정

```yaml
broker.id=0     # kafka broker id
log.dirs=/tmp/kafka-logs    # kafka broker log file 폴더
num.partitions=1    # kafka topic 만들 때 default partition 설정
log.retention.hours # kafka message 저장 기간
log.retention.bytes # partition의 크기 , 크기 초과되면 삭제됌
# log.retention.bytes x partition의 수는 topic의 최대크기. 이 값이 -1이면 크기는 unlimit

zookeeper.connect=localhost:2181 # 연동할 zookeeper host 및 port
```

카프카에 대한 설정 외에도 클러스터라면 broker.id를 지정해줘야 하고 zookeeper.connect에도 호스트와 포트를 지정해 줘야함

### kafka 실행

`$ cd kafka_2.13-3.3.2`
`$ bin/kafka-server-start.sh config/server.properties`

-daemon 백그라운드 옵션

### 토픽 생성

`$ cd kafka_2.13-3.3.2`
`$ bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 5 --topic fastcampus`

-replication-factor broker에 복사되는 갯수 (안정성 증가) 로컬 서버니까 1개

-bootstrap-server kafka 주소

-zookeeper zookeeper 주소

### 토픽 확인

`$ cd kafka_2.13-3.3.2`
`$ ./bin/kafka-topics.sh --list --bootstrap-server localhost:9092`

### 토픽 삭제

`$ bin/kafka-topics.sh --delete -topic data --bootstrap-server localhost:9092`

### 토픽의 상세정보 확인

`$ cd kafka_2.13-3.3.2`
`$ ./bin/kafka-topics.sh --describe --bootstrap-server localhost:9092 --topic data`