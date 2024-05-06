

## Elasticsearch EC2

- EC2 생성 시 인스턴스 설정
  - 메모리 4GB 이상으로 선택
  - 메모리가 부족하면 실습도중 메모리 에러가 발생할 수 있음
- 볼륨도 넉넉하게 15GB (늘어날 수록 비용 발생)
- 키는 이전에 생성했던 키를 사용할 것



### 설치 파일 다운로드

- 7.10.2 버전 사용
  - 7.10.2 를 사용하는 이유는 지난 강의를 참조
- 다운로드
  - `wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-7.10.2-linux-x86_64.tar.gz`
- 압축 해제
  - `tar -xvf elasticsearch-7.10.2-linux-x86_64.tar.gz`
- tmp 디렉토리 생성 후 tar파일 옮기기





## 실행

- elasticsearch 홈 경로로 이동
- 실행
  - `bin/elasticsearch`
  - -d 를 하게 되면 daemon
  - -p pid, pid 파일 생성
    - process id를 남기는 파일
    - 실행과 종료하는 shell에 활용가능
      - shell을 만든다면 권한 추가 필 (chmod 755)
- 실행 상태 확인
  - `curl -XGET localhost:9200`
  - 보안그룹 설정이 필요
    - 9200포트 Open
    - 재실행

### 기본 포트

- 9200

- 9300

- 클러스터 구성 

  <img src="./images/cluster.png" alt="cluster" style="zoom:80%;" />

  - 9200 
    - http를 통해 통신, 클라이언트와 통신
  - 9300
    - tcp를 통해 통신, 내부 통신

  



### config 설정

- cluster.name : 클러스터 명 지정 
  - elastic-cluster
  
- node.name : 노드 명 지정 (지정 안하면 임의로 지정 됨)
  - node1
  
- path.data : 인덱스 경로를 지정 함, 지정하지 않으면 data 디렉토리에 인덱스 생성
  - 운영 시 디스크 관리를 위해서는 다른 곳에 두는 것을 추천
  
- path.logs : 노드 및 클러스터 log 저장 경로

- bootstrap.memory_lock : 엘라스틱서치가 잡은 heap 메모리를 다른 프로그램이 사용하지 못하도록 Lock
  - true
  
- network.host : 지정된 IP만 엘라스틱서치에 접근할 수 있도록 설정
  - `_local_` : 127.0.0.1과 같음. 로컬 주소
  - `_site_` : 로컬 네트워크 주소로 설정됨. 서버가 재시작되어도 yml 변경하지 않아도 됨
  - `_global_` : 네트워크 외부에서 바라보는 주소
  
- 운영 모드, bootstrap check
  - network.host 설정시 운영 모드로 변경됨
  - 변경 전에는 개발모드, local작업환경
  
- http.port : 클라이언트와 통신하는 http 포트
  - 9200, 운영에선 변경하는 걸 권장
  
- transport.port : 내부 통신, 노드들 끼리 통신하는 포트
  - 9300
  
- discovery.seed_hosts : 활성화된 다른 서버를 찾는 설정. 같은 클러스터로 묶인 노드의 IP, 도메인을 넣는다

  - 원격 노드를 찾아 바인딩 하는 것을 discovery라고 부름

  - 작성되어 있는 대로 노드가 있는지를 확인

    1. 노드가 있으면 cluster.name 일치여부 확인

       - 일치 : 바인딩

       - 불일치 : 다음 주소 확인

    2. 노드가 없으면 다음 주소 확인

    3. 배열 안 주소를 모두 확인했는데 노드를 찾지 못한다면
       - 새로운 클러스터를 시작

- cluster.initial_master_nodes : 마스터 후보 노드를 지정

- $와 같이 환경변수명을 가져와서 사용 가능

  - ```yaml
    cluster.name: elastic-cluster
    node.name: node-1
    network.host: ["_local_","_site_"]
    ```

- `max virtual memory areas vm.max_map_count [65530] is too low, increase to at least [262144]`

  - sudo vim /etc/sysctl.conf

  - ```
    vm.max_map_count=262144
    ```

    - sudo sysctl -p