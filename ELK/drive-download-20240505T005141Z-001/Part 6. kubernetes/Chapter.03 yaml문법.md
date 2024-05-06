

## Yaml 기본 문법

- YAML은 기본적으로 사람 눈으로 보기가 편함
- 설정 파일 관리하는데 굉장히 용이
- yaml은 python과 마찬가지로 #을 주석을 다는 문자로 사용
- indentation에 민감
- scalar, collection



### Indentation

```yaml
person:
  name: jake #scalar
  job: DE
  skills: # collection
    - Kafka
    - Elasticsearch
    - Airflow
    - Kubernetes
```

- 기본적으로 2칸 들여쓰기

- 4칸 들여쓰기도 가능

- ```yaml
  person:
    	name: jake
      job: DE
      skills:
        - Kafka
        - Elasticsearch
        - Airflow
        - Kubernetes
  ```



### 데이터 정의

- 데이터는 기본적으로 key : value 형식으로 정의
- key와 value 사이에는 반드시 빈칸이 필요

- ```yaml
  apiVersion: v1
  kind: Pod
  metadata:
    name: example
    labels:
      type: app
  ```

- ```yaml
  apiVersion: v1
  kind: Pod
  metadata:
    name: example
    labels:
    	# 빈칸을 안붙이게 되면 값을 인식하지 못하고 string 처리된다
      type:app
  ```

- block

  - ```yaml
    name: example
    type: app
    ```

- inline

  - ```yaml
    {name: example, type: app}
    ```





### Array 정의

- block format

  - 배열은 `-` 로 표시

  - ```yaml
    person:
      	name: jake
        job: DE
        skills:
          - Kafka
          - Elasticsearch
          - Airflow
          - Kubernetes
    ```

- inline format

  - `[ ]`로 표시

  - ```yaml
    person:
      	name: jake
        job: DE
        skills: [Kafka, Elasticsearch, Airflow, Kubernetes]
    ```

    

### Boolean

- 참 거짓에 `true`, `false`, `yes`, `no` 지원

- ```yaml
  engineering: yes
  iphone: no
  kafka: true
  data: TRUE
  app: false
  ```

  

### 숫자

- 숫자값을 따옴표 없이 사용하면 숫자로 인식

- ``` yaml
  # number
  python: 3.9
  
  # string
  python: "3.9"
  ```

  

### 줄 바꿈

- `|` (pipe) 는 마지막 줄바꿈이 포함됩니다

- ```yaml
  something_example: |
              first line
  
              second line
  
              third line
  ```

  

### 한 파일에 여러 문서 입력

- `---` 를 사용하면 둘 이상의 yaml 문서를 단일 파일에 배치할 수 있음

- ```yaml
  ---
  something_example: |
              first line
  
              second line
  
              third line
  ---
  python: "3.9"
  ```

- 