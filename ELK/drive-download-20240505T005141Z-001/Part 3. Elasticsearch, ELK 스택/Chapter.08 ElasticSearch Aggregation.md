

## Aggregation API

- data 먼저 파악하기

- ```
  GET kibana_sample_data_ecommerce/_search
  ```



## Metric Aggregations

### sum, avg, min, max

- ```
  GET kibana_sample_data_ecommerce/_search
  {
    "size": 0,
    "aggs": {
      "sum_of_base_price": {
        "sum": {
          "field": "products.base_price"
        }
      }
    }
  }
  ```

- 숫자 값만 사용, keyword (X)

-  `"size": 0` 을 사용하는 이유?

  - "hits": [ ] 에 불필요한 도큐먼트 내용이 나타나지 않음, 도큐먼트를 fetch 해 오는 과정을 생략

- ```
  GET kibana_sample_data_ecommerce/_search
  {
    "query": {
      "match": {
        "customer_gender": "MALE"
      }
    },
    "size": 0,
    "aggs": {
      "sum_of_base_price": {
        "sum": {
          "field": "products.base_price"
        }
      }
    }
  }
  
  ```

- match를 사용해 원하는 조건에 맞는 값에만 aggregation 적용 가능



- stats를 사용하면 count, min, max, avg, sum을 한꺼번에 얻을 수 있음

- ``` 
  GET kibana_sample_data_ecommerce/_search
  {
    "size": 0,
    "aggs": {
      "stats_of_product_price": {
        "stats": {
          "field": "products.price"
        }
      }
    }
  }
  ```

  

### cardinality

- 유니크한 값이 몇 개인지 알기 위해서는 cardinality를 사용

- 숫자, keyword, ip 등에서 사용이 가능함

-  ```
   GET kibana_sample_data_ecommerce/_search
   {
     "size": 0,
     "aggs": {
       "number_of_category": {
         "cardinality": {
           "field": "products.category.keyword"
         }
       }
     }
   }
   
   
   GET kibana_sample_data_ecommerce/_search
   {
     "size": 0,
     "aggs": {
       "number_of_sku": {
         "cardinality": {
           "field": "products.sku"
         }
       }
     }
   }
   ```



### percentiles, percentile_ranks

- 백분위값

- ```
  GET kibana_sample_data_ecommerce/_search
  {
    "size": 0,
    "aggs": {
      "percentiles_of": {
        "percentiles": {
          "field": "products.price"
        }
      }
    }
  }
  ```

- percents 옵션을 주면 원하는 구간을 정할 수 있음

- ```
  GET kibana_sample_data_ecommerce/_search
  {
    "size": 0,
    "aggs": {
      "percentiles_of": {
        "percentiles": {
          "field": "products.price",
          "percents": [30,80]
        }
      }
    }
  }
  ```

- percentile_ranks aggregation 해당하는 값이 있는 백분위를 알려줌

- ```
  GET kibana_sample_data_ecommerce/_search
  {
    "size": 0,
    "aggs": {
      "percentiles_ranks": {
        "percentile_ranks": {
          "field": "products.price",
          "values": [23.12]
        }
      }
    }
  }
  ```





## Bucket Aggregations

- 조건으로 분류된 **버킷** 들을 생성하고 그룹으로 모음
- 버킷 별로 포함되는 도큐먼트의 개수는 **doc_count** 값으로 표시

### Range

- 숫자 필드 값으로 범위를 지정, 범위에 해당하는 버킷을 만듬

-  **from** 은 **이상**  **to** 는 **미만**

- ```
  GET kibana_sample_data_ecommerce/_search
  {
    "size": 0,
    "aggs": {
      "range_price": {
        "range": {
          "field": "products.price",
          "ranges": [
            {
            "from" : 0,
            "to": 100
          }
          ]
        }
      }
    }
  }
  ```

- 0부터 100까지 해당하는 문서의 갯수



### Histogram

- 주어진 interval 크기대로 버킷을 구분, Interval에 해당하는 문서들이 얼마나 있는지 파악 가능

- ```
  GET kibana_sample_data_ecommerce/_search
  {
    "size": 0,
    "aggs": {
      "histogram_price": {
        "histogram": {
          "field": "products.price",
          "interval" : 10 
        }
      }
    }
  }
  ```



### Date range, histogram

- 날짜 간격에 대해서 버킷을 생성 가능

- ```
  GET kibana_sample_data_ecommerce/_search
  {
    "size": 0,
    "aggs": {
      "order_date_histogram": {
        "date_histogram": {
          "field": "order_date",
          "calendar_interval" : "month" 
        }
      }
    }
  }
  ```

- interval 옵션은 사용 종료 권장되고 있음

- ```
  GET kibana_sample_data_ecommerce/_search
  {
    "size": 0,
    "aggs": {
      "order_date_range": {
        "date_range": {
          "field": "order_date"
          , "ranges": [
            {
              "from" : "2023-02-01",
               "to" : "2023-03-01"
            }
          ]
        }
      }
    }
  }
  ```



### Terms

- keyword 필드의 문자열 별로 버킷을 생성

- ```
  GET kibana_sample_data_ecommerce/_search
  {
    "size": 0,
    "aggs": {
      "manufacturer_count": {
        "terms": {
          "field": "products.manufacturer.keyword"
        }
      }
    }
  }
  ```

- top 5만 가져오고 싶다면 size를 5로 변경하면 된다

- 정렬 옵션은 order에 desc, asc를 넣는다

- ```
  GET kibana_sample_data_ecommerce/_search
  {
    "size": 0,
    "aggs": {
      "brands": {
        "terms": {
          "field": "products.manufacturer.keyword",
          "order": {
            "_count": "asc"}
        }
      }
    }
  }
  ```



### Sub-aggregation

- aggregation에 aggregation을 하는 것

- **하위 버킷이 점점 깊어질수록 작업량과 메모리를 많이 소모하기 때문에 오류 가능성이 증가함**

  - 2레벨 이상 설정하지 않는 것을 권장

- ```
  GET kibana_sample_data_ecommerce/_search
  {
    "size": 0,
    "aggs": {
      "manufacturer_count": {
        "terms": {
          "field": "products.manufacturer.keyword"
        }
        , 
        "aggs": {
        "sum_of_manufactures_prices": {
          "sum": {
            "field": "products.price"
          }
          }
        }
      }
    }
  }
  ```

  