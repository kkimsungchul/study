



## Filebeat 설치

- filebeat는 특정한 로그 파일들을 주기적으로 스캔하여 쌓이는 데이터들을 모으는 역할
- ![Filebeat](https://www.elastic.co/guide/en/beats/filebeat/current/images/filebeat.png)

- Filebeat
- inputs, harvesters, spooler
  - Inputs로 소스 파일 경로 설정
  - Harvester가 로그를 개별 파일로 처리
  - 파일을 Row단위로 읽어 출력으로 spooler에게 보냄, 집계하여 출력으로 전달

- https://www.elastic.co/kr/downloads/past-releases/filebeat-7-10-2
  - 사양에 맞게 다운로드
- wget을 사용해 다운로드
- tar xfz filebeat-7.10.2-linux-x86_64.tar.gz



- mkdir logs 를 통해 로그 파일을 받을 경로를 생성

- apache_logs 다운로드

  - https://raw.githubusercontent.com/elastic/examples/master/Common%20Data%20Formats/apache_logs/apache_logs

- /home/ubuntu/filebeat-7.10.2-linux-x86_64/logs/apache_logs

- filebeat.yml 조정

  - paths에 로그가 저장된 경로를 지정

  - input 타입 설정

  - ```
    filebeat inputs:
    	- type: log
    	  enabled: true
    ```


- elasticsearchdp 데이터 전송

  - output.elasticsearch 에 EC2 elasticsearch Public IP주소를 입력

- ```
  output.elasticsearch:
    # Array of hosts to connect to.
    hosts: ["xxxxx:9200"]
    username:elastic
    password: qlalfqjsgh
  ```



### Filebeat to Logstash

- logstash conf 파일

  - beats.conf

- ```
  input {
     beats {
       port => 5044
      }
  }
  
  filter {
  	grok {
      match => {
        "message" => '%{IPORHOST:clientip} %{USER:ident} %{USER:auth} \[%{HTTPDATE:timestamp}\] "%{WORD:verb} %{DATA:request} HTTP/%{NUMBER:httpversion}" %{NUMBER:response:int} (?:-|%{NUMBER:bytes:int}) %{QS:referrer} %{QS:cluster_agent}'
      }
    }
  
     # date {
     # match => [ "timestamp", "dd/MMM/YYYY:HH:mm:ss Z" ]
     #  locale => en
     #  }
  
    geoip {
      source => "clientip"
    }
  
  
    mutate {
    	remove_field => ["timestamp","agent","@version","log"]
    }
  }
  
  output {
   	 stdout {
     	 codec => "rubydebug"
     	 # codec => "dots"
    }
  
    elasticsearch {
      index => "apache.logstash-%{+yyyy.MM.dd}"
      hosts => ["43.200.169.161"]
      user => "elastic"
      password => "qlalfqjsgh"
    }
  }
  ```

- grok pattern 

  - %{SYNTAX:SEMANTIC}
    - SYNTAX : Input에서 감지해야 하는 정규식 패턴

    - SEMANTIC : 감지된 패턴을 할당할 logstash 변수

- date 

  - Elasticsearch에 데이터를 보내는 시간이 아닌 실제 로그 파일에 찍힌/적힌 시간

    - 적재하기 위해 원하는 filed로 재 설정을 해줄 수 있음

  - ```
    date {
    	match => {변경할 필드명, 날짜 format }, target => "@timestamp", timezone=>"Asia/Seoul" 
    }
    ```

    

- mutate 

  - remove_fields: 해당 필드를 삭제함
  - 보낼 메세지를 전처리 해야 하면 gsub을 사용

- output codec

  - https://www.elastic.co/guide/en/logstash/7.10/codec-plugins.html
  - json: json형식으로 보여줌
    - 전체 JSON 메시지를 디코딩(input 통해서) 및 인코딩(output 통해서)하는 데 사용
  - rubydebug : debug 하기 편리함
    - Ruby Amazing Print library
  - dots: .... 으로 표시되지만 ES에는 정상적으로 적재됨

- output index

  - 지정 안하게 되면 logstash-2023.xx.xx-00001 형식으로 들어감
  - 지정 하면 원하는 인덱스 명을 붙일 수 있음
    - "apachelog-%{+yyyy.MM.dd}"

- -config.test_and_exit

  - bin/logstash 뒤에 넣으면 테스트 가능

- filebeat.yml


- ```
  output.logstash:
    hosts: ["localhost:5044"]
  ```

  - chmod 700 filebeat
  - chmod go-w /home/ubuntu/filebeat-7.10.2-linux-x86_64/filebeat.yml
  

- beats에서 로그를 읽어서 logstash를 통해 Elasticsearch로 들어간다.

  