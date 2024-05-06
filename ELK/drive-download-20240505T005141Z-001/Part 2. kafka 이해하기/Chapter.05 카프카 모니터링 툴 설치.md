# 카프카 모니터링 툴 설치

### UI for Kafka를 사용

[https://github.com/provectus/kafka-ui](https://github.com/provectus/kafka-ui)

### docker 이미지 pull

`docker pull provectuslabs/kafka-ui`

### docker image 확인

`docker image ls`

### docker compose

> Docker Compose는 **여러 컨테이너를 가지는 애플리케이션을 통합적으로 Docker 이미지를 만들고, 만들어진 각각의 컨테이너를 시작 및 중지하는 등의 작업을 더 쉽게 수행할 수 있도록 도와주는 도구**
> 

**docker-compose.yml** 이 필요함

`mkdir ui-for-kafka`

`vim docker-compose.yml`

github yml 참조

```yaml
version: '2'
services:
  kafka-ui:
    image: provectuslabs/kafka-ui
    container_name: kafka-ui
    ports:
      - "8080:8080"
    restart: always
    environment:
      - KAFKA_CLUSTERS_0_NAME=local
      - KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS=kafka:9092
```

이대로 실행하면? 제대로 붙지가 않음, 에러가 발생

물론 zookpeeper와 kafka는 실행이 되어있어야 함

### config 설정

`vim config/server.properties`

```yaml
#listeners=PLAINTEXT://:9092
#advertised.listeners=PLAINTEXT://your.host.name:9092

listeners=PLAINTEXT://:9092
advertised.listeners=PLAINTEXT://192.168.45.62:9092
```

- listeners의 역할 : 내부에 연결할 IP, 카프카 브로커가 내부적으로 바인딩하는 주소
- advertised.listeners의 역할: 카프카 클라이언트나 커맨드 라인 툴을 브로커와 연결할 때 사용

### docker-compose.yml 설정대로 하면 연결이 안되는 이유

### docker - [localhost](http://localhost) 연결

![container-localhost](%E1%84%8F%E1%85%A1%E1%84%91%E1%85%B3%E1%84%8F%E1%85%A1%20%E1%84%86%E1%85%A9%E1%84%82%E1%85%B5%E1%84%90%E1%85%A5%E1%84%85%E1%85%B5%E1%86%BC%20%E1%84%90%E1%85%AE%E1%86%AF%20%E1%84%89%E1%85%A5%E1%86%AF%E1%84%8E%E1%85%B5%20eece75a85b854bd3a0b3c81bbbc4e788/Untitled.png)

- [localhost](http://localhost) 9092로 연결하려고 시도!
    - 하지만 docker container내에 있는 localhost에 연결을 하게됨
    - 진짜 localhost가 어디인지 알려주자!

### extra_hosts와 host.docker.internal

- host.docker.internal를 사용해야 진짜 localhost에 접근이 가능함
- host.docker.internal 를 위해서는 host-gateway가 필요
- host-gateway를 사용해서 도커 컨테이너와 로컬을 연결하는 것

extra_host를 통해 host-gateway를 열어주고 host.docker.internal를 사용해준다

```yaml
kafka-ui:
    image: provectuslabs/kafka-ui
    extra_hosts:
      - host.docker.internal:host-gateway
    container_name: kafka-ui
    ports:
      - "8080:8080"
    environment:
      - KAFKA_CLUSTERS_0_NAME=localhost-kafka
      - KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS=host.docker.internal:9092
      - KAFKA_CLUSTERS_0_ZOOKEEPER=host.docker.internal:2181
```

