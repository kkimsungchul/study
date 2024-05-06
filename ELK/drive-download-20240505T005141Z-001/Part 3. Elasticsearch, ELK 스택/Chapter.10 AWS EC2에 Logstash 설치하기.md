

## Logstash 설치

- 로컬 pc에 먼저 올려본 다음 EC2에 똑같이 설치해보자

- https://www.elastic.co/kr/downloads/past-releases/logstash-7-10-2

  - 위 링크에서 로컬 pc사양에 맞게 지정한 다음 wget 또는 다운로드
  - tar xfz logstash-7.10.2-linux-x86_64.tar.gz 로 압축해제

  

- logstash는 pipeline을 작성해야 정상적으로 동작
- bin/logstash -e ' input {stdin { } } output { stdout { } } '
  - java 에러 발생 시 java 버전 1.8, 11, 12, 14 중 하나 설치
  - 버전 별 JAVA_HOME 설정 후 반영
    - export JAVA_HOME=$(/usr/libexec/java_home -v12)
  - 재 실행



- message 필드 확인



- conf 설정

  - bin/logastah -f example.conf

  - ``` 
    input {
    	stdin { }
    } 
    
    output {
    	stdout { } 
    }
    
    ```

  - example2.conf

  - ``` 
    input {
    	tcp {
        port => 8899
        }
    } 
    
    output {
    	stdout { } 
    }
    
    ```

    - `echo "my name is jake" |nc localhost 8899`

  - example3.conf

  - ```
    input {
    	tcp {
        port => 8899
        }
    } 
    
    output {
    	#stdout { } 
    	elasticsearch {
    		hosts => ["{ES_PublicIP}:9200"]
    		user => "elastic"
    		password => "qlalfqjsgh"
    	}
    }
    
    
    ```

  - `echo "my name is jake" |nc localhost 8899`

  - stdout이 나오지 않는다

    - 당황하지 말고 kibana에서 확인한다



