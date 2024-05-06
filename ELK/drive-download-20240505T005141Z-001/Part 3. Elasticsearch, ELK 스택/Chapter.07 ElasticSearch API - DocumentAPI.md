

## Document API

- index와 document를 다루는 API
- CRUD, 기본적인 설정



### PUT

- 데이터를 입력하고자 할 때 사용

- ```
  PUT jake_index/_doc/1
  {
    "name":"Jake",
    "message":"This is Elasticsearch"
  }
  ```

- 같은 주소에 도큐먼트 재 입력

  ```
  PUT jake_index/_doc/1
  {
    "name":"Jake",
    "message":"This is Kibana"
  }
  ```

  - 덮어 쓰여지는 것을 확인

- 이것을 방지하고 싶다면 _create를 사용



## GET

- 도큐먼트에 있는 데이터를 조회

- ```
  GET jake_index/_doc/1
  ```



## DELETE

- 인덱스나 도큐먼트를 삭제할 수 있음

- 도큐먼트 삭제

- ```
  DELETE jake_index/_doc/1
  ```

  - 인덱스는 남아있는 상태, 조회하면 도큐먼트를 못 찾았다는 "found" : false 결과 확인

- 전체 인덱스 삭제

- ```
  DELETE jake_index
  ```

  - "type" : "index_not_found_exception"` , `"status" : 404 확인



## POST

- 데이터 입력이 가능한 메서드

- PUT과 다르게 document id를 자동생성 할 수 있음

- ```
  POST jake_index/_doc
  {
    "name":"Jake",
    "message":"This is Elasticsearch"
  }
  ```

- 자동 생성된 id 확인



#### UPDATE

- 도큐먼트의 수정

- 원하는 필드의 내용만 업데이트가 가능

- ```
  POST jake_index/_update/1
  {
    "doc": {
      "message":"Hola Kibana"
    }
  }
  ```

- 업데이트가 실제 되었는지, PUT과 다른 점을 확인



## Bulk API

- 앞의 API들은 한 번에 하나의 문서만을 대상으로 동작했음
- `Bulk API`를 사용하면 한 번의 API사용으로 다수의 문서를 인덱싱하거나 삭제가 가능
  - 대량 색인이 필요하다면 사용

```
POST _bulk
{"index":{"_index":"jake-tmp", "_id":"1"}}
{"field":"the first"}
{"index":{"_index":"jake-tmp", "_id":"2"}}
{"field":"the second"}
{"delete":{"_index":"jake-tmp", "_id":"2"}}
{"create":{"_index":"jake-tmp", "_id":"3"}}
{"field":"the third"}
{"update":{"_index":"jake-tmp", "_id":"1"}}
{"doc":{"field":"the one"}}
```

- ```
  GET jake-tmp/_doc/1
  ```

  

### Reindex API

-  한 인덱스에서 다른 인덱스로 도큐먼트를 복사할 때 주로 사용

- ```
  POST _reindex
  {
    "source": {
      "index": "원본 인덱스 명"
    },
    "dest": {
      "index": "재색인할 인덱스 명"
    }
  }
  ```

- ```
  PUT jake-one
  ```

- ```
  POST _reindex
  {
  	"max_docs": 1000,
    "source": {
      "index": "jake-tmp"
    },
    "dest": {
      "index": "jake-one"
    }
  }
  ```

- source 부분에 쿼리를 포함시켜 쿼리 결과에 일치하는 문서만 복사 가능

- max_docs(size)를 지정할 수 있음, max_docs는 기본적으로 1000건 단위

  - 사이즈를 늘리면 많은 문서를 복사할 때 효과적, 전체적인 속도 향상

  