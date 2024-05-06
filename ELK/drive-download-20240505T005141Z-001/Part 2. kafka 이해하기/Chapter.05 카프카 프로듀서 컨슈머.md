### 토픽 생성

```
bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 3 --topic fastcampus
```

### 프로듀서 실행

```
bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic fastcampus
```

### 컨슈머 실행

```
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic fastcampus
```

### 실행 내용 ui-for-kafka에서 확인