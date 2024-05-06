

## Kibana 설치

![clustering](./images/clustering.png)

- 키바나 설치
- ![clustering](./images/cluster-tls-kibana.png)
- 다운로드
  - `wget https://artifacts.elastic.co/downloads/kibana/kibana-7.10.2-linux-x86_64.tar.gz `
- 설치
  - tar -xvf kibana-7.10.2-linux-x86_64.tar.gz
- tmp로 설치파일 이동
  - mv kibana-7.10.2-linux-x86_64.tar.gz tmp/



## configuration

- server.host : 해당 서버명
  - `es-1`
- server.name : 키바나 인스턴스 명
  - `jake-kibana`
- elasticsearch.hosts
  - `["http://es-1:9200"]`
  - 9200포트가 열려있어야 함
- server.port : 키바나 포트 설정
  - 기본 5601
  - 운영에서는 변경 가능
- elasticsearch.username: "kibana_system"
- elasticserach.password: "qlalfqjsgh"
  - => keystore에 저장하는 것이 안전함

- bin/kibana-keystore create
- bin/kibana-keystore add elasticsearch.password



- 보안 그룹 설정
  - 5601포트 open



### Stack Monitoring

- 노드, 샤드, 키바나 모니터링 가능

```
GET _cluster/settings

PUT _cluster/settings
{
  "persistent" : {
    "xpack" : {
      "monitoring" : {
        "collection" : {
          "enabled" : "false"
        }
      }
    }
  }
  }
```

