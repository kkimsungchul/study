

## Search API

- 검색을 할 때 사용하는 API
- 검색은 인덱스 단위로 이루어짐



## Request Body 검색

- Body에 검색할 field와 검색어를 Json형태로 전달하는 방식
- Query DSL이라는 특별한 문법을 사용
- Query DSL에서 자세하게 다룰 예정



## URI 검색

- Request Body 검색에 비해 사용하기 편리하지만, 복잡한 쿼리를 사용하기엔 어려움

- ```
  POST jake-one/_search?q=the
  POST jake-one/_search?q=field:the
  ```

- field:검색어 형태로 입력이 가능함

- AND 조건 OR 조건 사용 가능

- ``` 
  POST jake-one/_search?q=one OR third
  ```

- 다양한 파라미터들이 존재

  - https://www.elastic.co/guide/en/elasticsearch/reference/7.10/search-search.html#search-search-api-path-params

- 간단한 목적으로 조회할 때 사용, Request Body 검색을 사용 권장



## Multitenancy

- 여러 개 인덱스를 한번에 묶어서 검색하는 것

- 날짜별로 지정된 인덱스가 있다면 *를 사용해 묶어서 검색이 가능함

- ```
  GET logs-2023-*/_search
  ```



## QueryDSL

- 보다 강력한 검색을 위한 도구
- 정밀한 검색을 위해 Json 구조를 기반으로한 **QueryDSL**(Domain Specific Language) 사용



- Query에 사용되는 부분

- ```
  {
      "size" : # 반환받는 Document 개수의 값
      ,"from" : # 몇번째 문서부터 가져올지 지정. 기본값은 0
      ,"timeout" : # 검색 요청시 결과를 받는 데까지 걸리는 시간이다. 
      # timeout을 작게하면 전체 샤드에서 timeout을 넘기지 않은 문서만 결과로 출력됨
      # 기본 값은 제한없음
      ,"_source" : # 검색 시 필요한 필드만 출력하고 싶을 때 사용
      ,"query" : # 검색 조건문을 설정할 때 사용
      ,"aggs" : # 통계 및 집계 데이터를 설정할 때 사용
      ,"sort" : # 문서 결과의 정렬 조건을 설정할 때 사용
  }
  ```

- Query 결과에 대한 부분

  ```
  {
      "took" : # Query를 실행하는데 걸린 시간
      ,"timed_out" : # Query timeout이 초과했는 지 True, False로 나타낸다
      ,"_shards" : {
          "total" : # Query를 요청한 전체 샤드 개수
          ,"successful" : # Query에 성공적으로 응답한 샤드 개수
          ,"failed" : # Query에 실패한 샤드 개수
      }
      ,"hits" : {
          "total" : # Query에 매칭된 문서의 전체 개수
          ,"max_score" : # Query에 일치하는 문서의 스코어 값중 가장 높은 값
          ,"hits" : # Query 결과를 표시
      }
  }
  ```





### Range Query

- 숫자, 날짜 형식들에 대한 range로 검색이 가능함

- ```
  POST users/_bulk
  {"index":{"_id":1}}
  {"user_name":"Jake","age":17,"date":"2023-01-04"}
  {"index":{"_id":2}}
  {"user_name":"Dean","age":23,"date":"2023-01-12"}
  {"index":{"_id":3}}
  {"user_name":"Mike","age":21,"date":"2022-09-15"}
  {"index":{"_id":4}}
  {"user_name":"Derek","age":54,"date":"2021-12-25"}
  {"index":{"_id":5}}
  {"user_name":"Josh","age":43,"date":"2022-06-31"}
  ```

- range : { <필드명>: { <파라메터>:<값> } }

  - **gte** (Greater-than or equal to) - 이상 (같거나 큼)
  - **gt** (Greater-than) – 초과 (큼)
  - **lte** (Less-than or equal to) - 이하 (같거나 작음)
  - **lt** (Less-than) - 미만 (작음)

- ```
  GET users/_search
  {
    "query": {
      "range": {
        "age": {
          "gte": 21,
          "lt": 34
        }
      }
    }
  }
  ```

- 날짜 검색

  - Json에서 일반적으로 사용되는 ISO8601 형식

- ```
  GET users/_search
  {
    "query": {
      "range": {
        "date": {
          "gt": "2023-01-01"
        }
      }
    }
  }
  ```



### Bool Query

- 여러 쿼리를 조합하기 위해서는 true, false 값을 갖는 bool 쿼리를 사용하고 다른 쿼리를 안에 넣어야 함

  - **must**: 쿼리가 true인 도큐먼트들을 검색
  - **must_not**: 쿼리가 false 도큐먼트들을 검색
  - **should**: 쿼리에 해당하는 도큐먼트들의 점수를 높임
  - **filter**: 쿼리가 true인 도큐먼트들을 검색, 스코어 계산은 하지 않음. 따라서, must보다 속도가 빠르고 캐싱 가능

- ```
  GET <인덱스명>/_search
  {
    "query": {
      "bool": {
        "must": [
          { <쿼리> }, …
        ],
        "must_not": [
          { <쿼리> }, …
        ],
        "should": [
          { <쿼리> }, …
        ],
        "filter": [
          { <쿼리> }, …
        ]
      }
    }
  }
  ```

- ```
  GET kibana_sample_data_ecommerce/_search
  {
    "query": {
      "bool": {
        "must": {
          "term" : { "customer_id": 4}
        },
        "filter": {
          "term": {"day_of_week":"Wednesday"}
        },
        
        "should": [
          {"term": { "customer_last_name":"Garza"}}
        ],
        "boost": 1 # 검색에 가중치를 부여
      }
    }
  }
  ```

- range 쿼리와의 조합

- ```
  GET users/_search
  {
    "query": {
      "bool": {
        "must": {
          "range" :{
            "age": { "gte" : 21,
            "lte" : 55
            }
        }
      }
    }
  }
  }
  
  ```



### Relevancy

- 얼마나 정확한지에 대한 지표, 관련도
- **Score**
  - 결과가 검색 조건과 얼마나 일치하는지를 나타냄, desc
  - BM25를 사용
    - ![](https://velog.velcdn.com/images/k-yooon/post/eb214d27-a948-459e-b9e2-56e26bb1268d/image.png)
    - https://en.wikipedia.org/wiki/Okapi_BM25
  - **TF, IDF, Field Length**
    - TF(Term Frequency) : 특정 문서에서 특정 단어의 출현빈도
      - 도큐먼트 내에 텀이 많을수록 관련도가 높을 가능성이 있음
    - IDF(Inverse Document Frequency) :  DF는 전체 문서들 중에서 해당 문서를 제외한 나머지 문서에서 해당 단어가 몇 번 사용되었는지를 의미
      - 검색한 텀을 포함하는 도큐먼트 개수가 많을 수록 텀에 대한 점수가 감소함, 텀이 증가할 수록 IDF 감소
    - Field Lengh : 도큐먼트에서 필드길이가 짧은 필드에 있는 텀의 중요도가 더 큼, 텍스트 길이가 짧은 필드에 검색어를 포함하면 점수가 더 높음



### Full Text Query

- 풀텍스트 쿼리를 사용하면 텍스트 필드를 검색할 수 있음

### match

- 텍스트와 매치되는 도큐먼드들을 검색할 수 있다.

- text는 analyzer를 통해 분석된 후에 검색에 사용된다, 보통 Standard Analyzer가 기본으로 설정되어 있다

- ```
  GET users/_search
  {
      "query": {
          "match": {
              "user_name": {
                  "query" : "Jake Dean"
              }
          }
      }
  }
  ```

  - 기본적으로 OR로 검색이 되고 AND를 사용하고 싶다면 operator를 붙여준다

  - ```
    GET users/_search
    {
        "query": {
            "match": {
                "user_name": {
                    "query" : "Jake Dean",
                    "operator": "and"
                }
            }
        }
    }
    ```

  - 





### match_phrase query

- 띄어쓰기까지 포함한 정확한 phrase를 검색할 때 사용됨

- ```
  GET kibana_sample_data_ecommerce/_search
  {
    "query": {
      "match_phrase": {
        "customer_full_name": "Eddie Underwood"
      }
    }
  }
  ```

- slop 설정 가능

  - 지정된 숫자 만큼 단어 사이에 다른 검색어가 들어가는 것을 허용



### multi-match query

- 여러개의 필드에서 쿼리를 검색할 수 있음

- 동일 쿼리를 여러 다른 필드에 match query를 사용한 것과 동일한 결과를 가져옴

- ```
  GET users/_search
  {
      "query": {
          "multi_match": {
              "query" : 21,
              "fields" : ["age", "user_name"]
          }
      }
  }
  ```



### match all

- 쿼리에 매치되는 모든 도큐먼트를 검색

- ```
  GET users/_search
  {
    "query": {"match_all": {}
  }
  }
  ```

  



