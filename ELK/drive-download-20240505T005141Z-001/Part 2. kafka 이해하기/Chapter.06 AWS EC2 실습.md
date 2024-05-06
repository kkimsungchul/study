# AWS EC2 실습

### jdk설치

```
sudo apt-get update
sudo apt-get install openjdk-11-jdk
```



### ubuntu 사용시 bash 쉘 사용



### kafka 설치

`wget http://mirror.navercorp.com/apache/kafka/3.3.2/kafka_2.13-3.3.2.tgz`

`tar -xvzf kafka_2.13-3.3.2.tgz`



### kafka home

```yaml
vi .bashrc
......
export KAFKA_HOME=/home/ubuntu/kafka/kafka_2.13-3.3.2
......

exit
sudo su - kafka
```

### /etc/hosts 설정

```yaml
{server_ip} kafka_node1
{server_ip} kafka_node2
{server_ip} kafka_node3
```

자기자신의 host는 0.0.0.0

### configuration

```yaml
vi $KAFKA_HOME/config/server.properties
......
broker.id=1
......
listeners=PLAINTEXT://:9092
advertised.listeners=PLAINTEXT://kafka_node0:9092
......
log.dirs=/home/ubuntu/kafka/kafka_2.13-3.3.2/logs
......
zookeeper.connect=kafka_node0:2181,kafka_node1:2181,kafka_node2:2181
```



### Zookeeper추가 설정

```yaml
echo "1" > $KAFKA_HOME/data/myid

vi $KAFKA_HOME/config/zookeeper.properties
......
dataDir=/home/ubuntu/kafka/kafka_2.13-3.3.2/data
......
initLimit=20
syncLimit=5
server.1=kafka_node1:2888:3888
server.2=kafka_node2:2888:3888
server.3=kafka_node3:2888:3888
......
```

- myid `0` 으로 설정하게 되면 에러 발생
  - 1부터 시작하는 것을 권장

**initLimit**

- **follower** 가 leader와 처음 연결을 시도할 때 가지는 tick 횟수. **제한 횟수 넘으면 timeout**

**syncLimit**

- **follower** 가 leader와 연결 된 후에 앙상블 안에서 leader와의 연결을 유지하기 위한 tick 횟수
- **제한 횟수 넘으면 time out**

**server.(zookeeper_server.pid의 내용)=(host name 이나 host ip):2888:3888**

- **앙상블**을 이루기 위한 서버의 정보
- 2888은 동기화를 위한 포트, 3888은 클러스터 구성시 leader를 산출하기 위한 포트
- 여기서 서버의 id 를 dataDir 에 설정해 줘야 한다.



### **보안그룹 생성 및 적용**

![Untitled](./images/sg.png)





### **인스턴스 이미지 생성**

- 이미지가 복사되는데 5-10분 정도 시간이 소요됩니다

### **인스턴스 설정수정**

```yaml
# kafka_node2
echo "2" > $KAFKA_HOME/data/myid

vi $KAFKA_HOME/config/server.properties
......
broker.id=2
......

# ssh kafka_node2
echo "3" > $KAFKA_HOME/data/myid

vi $KAFKA_HOME/config/server.properties
......
broker.id=3
......
```



### 인스턴스 실행

```yaml
# kafka_node1
$KAFKA_HOME/bin/zookeeper-server-start.sh -daemon $KAFKA_HOME/config/zookeeper.properties
$KAFKA_HOME/bin/kafka-server-start.sh -daemon $KAFKA_HOME/config/server.properties

# kafka_node2
$KAFKA_HOME/bin/zookeeper-server-start.sh -daemon $KAFKA_HOME/config/zookeeper.properties
$KAFKA_HOME/bin/kafka-server-start.sh -daemon $KAFKA_HOME/config/server.properties

# kafka_node3
$KAFKA_HOME/bin/zookeeper-server-start.sh -daemon $KAFKA_HOME/config/zookeeper.properties
$KAFKA_HOME/bin/kafka-server-start.sh -daemon $KAFKA_HOME/config/server.properties
```



