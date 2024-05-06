

## Analyzer

- Tokenizer : 해당 단어(term 또는 token)를 분리하는 작업을 수행
  - Tokenizer는 정해진 separator ( **공백, .** , **/** 등)을 기준으로 토큰을 분리
  - 각 단어의 순서, 해당 단어의 시작과 끝자리의 offset을 기록
- Token Filter : 분리된 **단어들을 검색 가능하도록(searchable) 가공**하는 작업을 수행
  - Tokenizer로 분리된 토큰들을 가공
  - filters라는 파라미터를 통해 지정
    - 대표적으로 lowercase토큰 필터
    - 불필요한 단어들을 제거하는 stop토큰 필터
    - 동의어를 추가하는 synonym 토큰필터
- 두 개가 합쳐진 것이 바로 Analyzer



## Nori Tokenizer

- Nori 외의 한글 형태소 분석기
  - 커뮤니티 한글 형태소 분석기 - 아리랑, 은전한닢, Open Korean Text
  -  Elasticsearch가 한글을 지원하지 않던 때에 커뮤니티에서 제작한 분석기

- Elasticsearch 6.6 버전 부터 공식적으로 **Nori(노리)** 가 등장함
  - 한국인 개발자가 아닌 프랑스인 개발자 
    - https://github.com/jimczi
  - Nori 는 루씬의 기능으로 개발되었으며 루씬 소스에 반영되어 있다



### Nori 설치

- ![clustering](./images/cluster-tls-kibana.png)

- 설치

  ```
  bin/elasticsearch-plugin install analysis-nori
  ```

- 모든 노드에 설치해주고 재시작 해줘야함

- 제거

- ```
  bin/elasticsearch-plugin remove analysis-nori
  ```

  - Nori는 **nori_tokenizer** 토크나이저
  - **nori_part_of_speech**, **nori_readingform** 토큰 필터를 제공함



###  Nori 사용해보기

- ```
  GET _analyze
  {
    "tokenizer": "standard",
    "text": [
      "안녕 나는 제이크라고 해, 반가워"
    ],
  }
  ```

- ```
  GET _analyze
  {
    "tokenizer": "nori_tokenizer",
    "text": [
      "안녕 나는 제이크라고 해, 반가워"
    ],
     "explain": true
  }
  ```

  -  "explain": true 를 통해 품사 정보 확인가능
  - 한국어 형태소 분석기 품사정보
    - http://kkma.snu.ac.kr/documents/?doc=postag

- stop tags를 통해 특정 품사를 제거할 수 있음

  - position filter

- ```
  PUT example_pos
  {
    "settings": {
      "index": {
        "analysis": {
          "filter": {
            "example_stop_filter": {
              "type": "nori_part_of_speech",
              "stoptags": [
                "IC"
              ]
            }
          }
        }
      }
    }
  }
  ```

- IC는 감탄사

  - ```
    GET example_pos/_analyze
    {
      "tokenizer": "nori_tokenizer",
      "filter": [
        "example_stop_filter"
      ],
      "text": "우와 재밌다"
    }
    ```

    - "우와" 가 없어지는 것을 확인

- user_dictionary_rules로 사전 추가 가능

- ```
  PUT jake_example
  {
    "settings": {
      "analysis": {
        "tokenizer": {
          "my_nori_tokenizer": {
            "type": "nori_tokenizer",
            "user_dictionary_rules": [
              "이크"
            ]
          }
        }
      }
    }
  }
  ```

- ```
  GET jake_example/_analyze
  {
    "tokenizer": "my_nori_tokenizer",
    "text": [
      "제이크라고"
    ]
  }
  ```

  

- 데이터 준비