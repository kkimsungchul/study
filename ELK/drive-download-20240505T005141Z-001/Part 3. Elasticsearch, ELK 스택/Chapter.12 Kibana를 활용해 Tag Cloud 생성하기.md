

## Tag Cloud 생성

- 영화 review 데이터에 대한 태그 클라우드 생성
- 리뷰에 많이 등장하는 말은 무엇일까?
- 한 눈에 보기 편하게 시각화 해서 제공해보자!
  - 준비한 데이터를 Import
  - Machine Learning 탭에서 Data Visualizer를 이용



### Visualize

- Kibana의 Visualize로 이동
- Create Visualization
- Tag Cloud 선택
  - review* 사용하기
  - index pattern을 사용하는 것



- Metrics 설정
  - count
- Buckets 설정
  - Aggregation Terms
  - Field 설정
- 제대로 선택되지 않는 것이 확인 됨
- Nori를 사용하기 위해서는 Tokenizer 설정이 Index에 필요하고 Text를 aggregation 할 수 있어야함



### Index Template 설정

- Index Template에 Nori Tokenizer와 Text에 대한 Mapping 설정을 수정함

- settings

- ```
  {
    "index": {
      "analysis": {
        "analyzer": {
          "default": {
            "type": "nori"
          }
        }
      },
      "number_of_shards": "1",
      "number_of_replicas": "1"
    }
  }
  ```

- mappings

- ```
  {
    "properties": {
      "sentence": {
            "fielddata": true,
            "type": "text",
            "fields": {
              "keyword": {
                "ignore_above": 256,
                "type": "keyword"
              }
      }
    }
  }
  }
  ```

- http://43.200.169.161:5601/goto/4fad1b1272ab508a960d9849e863de9f

