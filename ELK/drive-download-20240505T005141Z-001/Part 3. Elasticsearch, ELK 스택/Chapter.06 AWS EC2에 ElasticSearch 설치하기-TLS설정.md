

## TLS?

- 현재 인스턴스들은 9200으로 누구나 들어올 수 있음
- 보안 설정이 필요
- Transport Layer Security
  - 보안 통신을 하기 위한 프로토콜
- Https?
  - http 상위에서 tls 암호화를 구현한 것.
    - TLS + HTTP
  - HTTPS를 사용한다면 TLS암호화를 사용한 것
  - HTTPS가 아니라면 정보 탈취의 위험이 있음
- 공개키, 대칭키 방식



## Elasticsearch TLS 설정하기

- xpack.security.enabled: true

  - `ERROR: [1] bootstrap checks failed
    [1]: Transport SSL must be enabled if security is enabled on a [basic] license. Please set [xpack.security.transport.ssl.enabled] to [true] or disable security by setting [xpack.security.enabled] to [false]`
  - TLS 설정을 해야 elasticsearch를 제대로 띄울 수 있음

- xapck.security.transport.ssl.enabled: true

  - 대칭키가 필요

  - ./bin/elasticsearch-certutil ca

    - output file명 elastic.p12

  - elastic-stack-ca.p12 패스워드 지정 => qlalfqjsgh (비밀번호)

    - ```shell
      ./bin/elasticsearch-certutil cert \
      --ca elastic-stack-ca.p12 \
      --dns es-1,es-2,es-3 \
      --ip  172.31.31.48,172.31.28.207,172.31.31.252 \
      --out config/certs/elastic.p12
      ```

    - es-cluster.p12 패스워드 지정 => qlalfqjsgh 

  - config/elasticsearch.yml

    - ```yaml
      xpack.security.transport.ssl.keystore.path: certs/{설정한_이름의.p12}.p12 
      xpack.security.transport.ssl.truststore.path: certs/{설정한_이름의.p12}.p12 
      ```

    - ``` yaml
      xpack.security.enabled: true
      xpack.security.transport.ssl.enabled: true
      xpack.security.transport.ssl.keystore.path: certs/elastic.p12
      xpack.security.transport.ssl.truststore.path: certs/elastic.p12
      ```

      

  - 설정 후 재실행

    - 오류 발생

    - `org.elasticsearch.bootstrap.StartupException: ElasticsearchSecurityException[failed to load SSL configuration [xpack.security.transport.ssl]]; nested: ElasticsearchException[failed to initialize SSL TrustManager]; nested: IOException[keystore password was incorrect`

      

    - ```sh
      ./bin/elasticsearch-keystore create 
      ./bin/elasticsearch-keystore add xpack.security.transport.ssl.keystore.secure_password
      ./bin/elasticsearch-keystore add xpack.security.transport.ssl.truststore.secure_password
      ```

      - 패스워드 노출 위험으로 오류가 발생한 것
      - 키 스토어에 저장해놔야 함
        - bin/elasticsearch-keystore list 로 등록된 키 확인 가능
        - 키 생성 완료



## 키 전송

- cyberduck을 사용해서 키를 전송
- mkdir config/certs
  - mv elasticsearch-7.10.2/config/certs
  - config도 동일하게 설정
- 위와 같이 keystore를 만들어주고 password를 등록
- 세 노드를 실행
  - bin/elasticsearch
- 이제 curl을 통한 명령어는 id와 패스워드가 필요함



## 패스워드 설정

- bin/elasticsearch-setup-passwords interactive
- curl localhost:9200
  - `{"error":{"root_cause":[{"type":"security_exception","reason":"missing authentication credentials for REST request [/]","header":{"WWW-Authenticate":"Basic realm=\"security\" charset=\"UTF-8\""}}],"type":"security_exception","reason":"missing authentication credentials for REST request [/]","header":{"WWW-Authenticate":"Basic realm=\"security\" charset=\"UTF-8\""}},"status":401}`
  - 비밀번호가 필요하다 
    - curl localhost:9200 -u elastic을 넣고 비밀번호를 입력한다