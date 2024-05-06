

## Kibana 활용하기

- Kibana 둘러보기
- 기본 제공되는 데이터들
- e-commerce data 사용
  - Discover
  - Visualizer
  - Dashboard
- Stack Management
- Index Pattern
- Index Management
- Dev tools



- moview_review 데이터 import

  - kibana - machine learning

  - data visualizer

    - csv파일 import

    - sentence -> 리뷰

      - text, keyword 타입 지정

    - index setting

      - ``` yaml
        {
        	"number_of_shards": "2", 
        	"number_of_replicas": "2",
        }
        ```

      - "number_of_shards": "5", 

      - "number_of_replicas": "1",