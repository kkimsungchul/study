

## ILM 설정 (Index Lifecycle Management)

- ILM이란?

  - 사용을 많이 하지 않는 문서는 주기적으로 삭제해야 디스크가 안정적으로 유지된다.
  - ILM을 사용하면 조건을 설정하여 해당 조건 충족 시 자동으로 인덱스를 삭제하거나 Roll Over를 할 수 있다.

- UI에서 설정 가능

- Hot Warm Cold 아키텍쳐

  -  index의 사용빈도(즉, disk의 I/O 빈도)에 따라 tier를 나눠서 보관하는 방법

  - | Tier   | Goal                 | Storage          | Memory:Storage ratio |
    | ------ | -------------------- | ---------------- | -------------------- |
    | Hot    | Optimize for search  | SSD SAN/DAS      | 1:30                 |
    | Warm   | Optimize for storage | HDD SAN/DAS      | 1:160                |
    | Frozen | Optimize for archive | Cheapest SAN/DAS | 1:1000+              |

- Delete phase

  - 생성된지 n일 후에 Index가 삭제되도록
  - Disk Pressure를 피할 수 있음

- API

- ```
  PUT _ilm/policy/ilm_policy_default
  {
    "policy": {
      "phases": {
        "hot": {
          "min_age": "0ms",
          "actions": {
            "rollover": {
              "max_age": "10d",
              "max_size": "50gb",
              "max_docs": 5000
            },
            "set_priority": {
              "priority": 100
            }
          }
        },
        "delete": {
          "min_age": "10d",
          "actions": {
            "delete": {}
          }
        }
      }
    }
  }
  ```

- Index Template 연결

  - ```
          "settings" : {
            "number_of_shards" : 1,
            "lifecycle": {
                  "name": "ilm_policy_default"}
    ```

- logstash 적용

  - ```
    output {
           elasticsearch {
                   hosts => "{public IP}"
                   user => "elastic"
                   password => "qlalfqjsgh"
                   ilm_enabled => true
                   ilm_policy => "{policy name}"
           }
    }
    ```





## Index Template

- logstash를 통해 데이터들이 Elasticsearch로 인입이 된다

- 미리 설정해 놓지 않는다면 데이터 타입의 수정은 힘들다

- 미리 설정해 놓는 설정이 바로 **Index Template**

  - ILM설정도 붙일 수 있다.

- Settings, Mappings를 설정한다

- Settings

- ```
  {
    "index": {
      "number_of_shards": "5",
      "number_of_replicas": "1"
    }
  }
  
  # Anaylzer 설정
  ```



- Index Template for apache-log

- Type 재설정

- Mapping 설정

  - ```
    "mappings" : {
          "properties" : {
            "@timestamp" : {
              "type" : "date"
            },
            "auth" : {
              "type" : "text",
              "fields" : {
                "keyword" : {
                  "type" : "keyword",
                  "ignore_above" : 256
                }
              }
            },
            "bytes" : {
              "type" : "long"
            },
            "clientip" : {
              "type" : "text",
              "fields" : {
                "keyword" : {
                  "type" : "keyword",
                  "ignore_above" : 256
                }
              }
            },
            "cloud" : {
              "properties" : {
                "account" : {
                  "properties" : {
                    "id" : {
                      "type" : "text",
                      "fields" : {
                        "keyword" : {
                          "type" : "keyword",
                          "ignore_above" : 256
                        }
                      }
                    }
                  }
                },
                "availability_zone" : {
                  "type" : "text",
                  "fields" : {
                    "keyword" : {
                      "type" : "keyword",
                      "ignore_above" : 256
                    }
                  }
                },
                "image" : {
                  "properties" : {
                    "id" : {
                      "type" : "text",
                      "fields" : {
                        "keyword" : {
                          "type" : "keyword",
                          "ignore_above" : 256
                        }
                      }
                    }
                  }
                },
                "instance" : {
                  "properties" : {
                    "id" : {
                      "type" : "text",
                      "fields" : {
                        "keyword" : {
                          "type" : "keyword",
                          "ignore_above" : 256
                        }
                      }
                    }
                  }
                },
                "machine" : {
                  "properties" : {
                    "type" : {
                      "type" : "text",
                      "fields" : {
                        "keyword" : {
                          "type" : "keyword",
                          "ignore_above" : 256
                        }
                      }
                    }
                  }
                },
                "provider" : {
                  "type" : "text",
                  "fields" : {
                    "keyword" : {
                      "type" : "keyword",
                      "ignore_above" : 256
                    }
                  }
                },
                "region" : {
                  "type" : "text",
                  "fields" : {
                    "keyword" : {
                      "type" : "keyword",
                      "ignore_above" : 256
                    }
                  }
                }
              }
            },
            "cluster_agent" : {
              "type" : "text",
              "fields" : {
                "keyword" : {
                  "type" : "keyword",
                  "ignore_above" : 256
                }
              }
            },
            "ecs" : {
              "properties" : {
                "version" : {
                  "type" : "text",
                  "fields" : {
                    "keyword" : {
                      "type" : "keyword",
                      "ignore_above" : 256
                    }
                  }
                }
              }
            },
            "geoip" : {
              "properties" : {
                "city_name" : {
                  "type" : "text",
                  "fields" : {
                    "keyword" : {
                      "type" : "keyword",
                      "ignore_above" : 256
                    }
                  }
                },
                "continent_code" : {
                  "type" : "keyword"
                },
                "country_code2" : {
                  "type" : "keyword"
                },
                "country_code3" : {
                  "type" : "keyword"
                },
                "country_name" : {
                  "type" : "keyword"
                },
                "dma_code" : {
                  "type" : "long"
                },
                "ip" : {
                  "type" : "ip"
                },
                "latitude" : {
                  "type" : "half_float"
                },
                "location" : {
                  "type": "geopoint"
                },
                "longitude" : {
                  "type" : "half_float"
                },
                "postal_code" : {
                  "type" : "keyword"
                },
                "region_code" : {
                  "type" : "keyword"
                },
                "region_name" : {
                  "type" : "keyword"
                },
                "timezone" : {
                  "type" : "text",
                  "fields" : {
                    "keyword" : {
                      "type" : "keyword",
                      "ignore_above" : 256
                    }
                  }
                }
              }
            },
            "host" : {
              "properties" : {
                "architecture" : {
                  "type" : "text",
                  "fields" : {
                    "keyword" : {
                      "type" : "keyword",
                      "ignore_above" : 256
                    }
                  }
                },
                "containerized" : {
                  "type" : "boolean"
                },
                "hostname" : {
                  "type" : "text",
                  "fields" : {
                    "keyword" : {
                      "type" : "keyword",
                      "ignore_above" : 256
                    }
                  }
                },
                "id" : {
                  "type" : "text",
                  "fields" : {
                    "keyword" : {
                      "type" : "keyword",
                      "ignore_above" : 256
                    }
                  }
                },
                "ip" : {
                  "type" : "text",
                  "fields" : {
                    "keyword" : {
                      "type" : "keyword",
                      "ignore_above" : 256
                    }
                  }
                },
                "mac" : {
                  "type" : "text",
                  "fields" : {
                    "keyword" : {
                      "type" : "keyword",
                      "ignore_above" : 256
                    }
                  }
                },
                "name" : {
                  "type" : "text",
                  "fields" : {
                    "keyword" : {
                      "type" : "keyword",
                      "ignore_above" : 256
                    }
                  }
                },
                "os" : {
                  "properties" : {
                    "codename" : {
                      "type" : "text",
                      "fields" : {
                        "keyword" : {
                          "type" : "keyword",
                          "ignore_above" : 256
                        }
                      }
                    },
                    "family" : {
                      "type" : "text",
                      "fields" : {
                        "keyword" : {
                          "type" : "keyword",
                          "ignore_above" : 256
                        }
                      }
                    },
                    "kernel" : {
                      "type" : "text",
                      "fields" : {
                        "keyword" : {
                          "type" : "keyword",
                          "ignore_above" : 256
                        }
                      }
                    },
                    "name" : {
                      "type" : "text",
                      "fields" : {
                        "keyword" : {
                          "type" : "keyword",
                          "ignore_above" : 256
                        }
                      }
                    },
                    "platform" : {
                      "type" : "text",
                      "fields" : {
                        "keyword" : {
                          "type" : "keyword",
                          "ignore_above" : 256
                        }
                      }
                    },
                    "version" : {
                      "type" : "text",
                      "fields" : {
                        "keyword" : {
                          "type" : "keyword",
                          "ignore_above" : 256
                        }
                      }
                    }
                  }
                }
              }
            },
            "httpversion" : {
              "type" : "keyword"
            },
            "ident" : {
              "type" : "keyword",
              "fields" : {
                "keyword" : {
                  "type" : "keyword",
                  "ignore_above" : 256
                }
              }
            },
            "input" : {
              "properties" : {
                "type" : {
                  "type" : "text",
                  "fields" : {
                    "keyword" : {
                      "type" : "keyword",
                      "ignore_above" : 256
                    }
                  }
                }
              }
            },
            "log" : {
              "properties" : {
                "file" : {
                  "properties" : {
                    "path" : {
                      "type" : "text",
                      "fields" : {
                        "keyword" : {
                          "type" : "keyword",
                          "ignore_above" : 256
                        }
                      }
                    }
                  }
                },
                "offset" : {
                  "type" : "long"
                }
              }
            },
            "message" : {
              "type" : "text"
            },
            "referrer" : {
              "type" : "text",
              "fields" : {
                "keyword" : {
                  "type" : "keyword",
                  "ignore_above" : 256
                }
              }
            },
            "request" : {
              "type" : "text",
              "fields" : {
                "keyword" : {
                  "type" : "keyword",
                  "ignore_above" : 256
                }
              }
            },
            "response" : {
              "type" : "long"
            },
            "tags" : {
              "type" : "text",
              "fields" : {
                "keyword" : {
                  "type" : "keyword",
                  "ignore_above" : 256
                }
              }
            },
            "verb" : {
              "type" : "text",
              "fields" : {
                "keyword" : {
                  "type" : "keyword",
                  "ignore_above" : 256
                }
              }
            }
          }
        }
    ```

- Setting 설정



- index template 생성

- ```
  PUT _index_template/apache_template
  {
    "index_patterns" : [
      "apache*"
      ],
      "priority" : 100,
      "template" : {
        "settings" : {
          "number_of_shards" : 1,
          "lifecycle": {
                "name": "ilm_policy_default"}
        },
        "mappings" : {
          "properties" : {
            "@timestamp" : {
              "type" : "date"
            },
            "auth" : {
              "type" : "text",
              "fields" : {
                "keyword" : {
                  "type" : "keyword",
                  "ignore_above" : 256
                }
              }
            },
            "bytes" : {
              "type" : "long"
            },
            "clientip" : {
              "type" : "text",
              "fields" : {
                "keyword" : {
                  "type" : "keyword",
                  "ignore_above" : 256
                }
              }
            },
            "cloud" : {
              "properties" : {
                "account" : {
                  "properties" : {
                    "id" : {
                      "type" : "text",
                      "fields" : {
                        "keyword" : {
                          "type" : "keyword",
                          "ignore_above" : 256
                        }
                      }
                    }
                  }
                },
                "availability_zone" : {
                  "type" : "text",
                  "fields" : {
                    "keyword" : {
                      "type" : "keyword",
                      "ignore_above" : 256
                    }
                  }
                },
                "image" : {
                  "properties" : {
                    "id" : {
                      "type" : "text",
                      "fields" : {
                        "keyword" : {
                          "type" : "keyword",
                          "ignore_above" : 256
                        }
                      }
                    }
                  }
                },
                "instance" : {
                  "properties" : {
                    "id" : {
                      "type" : "text",
                      "fields" : {
                        "keyword" : {
                          "type" : "keyword",
                          "ignore_above" : 256
                        }
                      }
                    }
                  }
                },
                "machine" : {
                  "properties" : {
                    "type" : {
                      "type" : "text",
                      "fields" : {
                        "keyword" : {
                          "type" : "keyword",
                          "ignore_above" : 256
                        }
                      }
                    }
                  }
                },
                "provider" : {
                  "type" : "text",
                  "fields" : {
                    "keyword" : {
                      "type" : "keyword",
                      "ignore_above" : 256
                    }
                  }
                },
                "region" : {
                  "type" : "text",
                  "fields" : {
                    "keyword" : {
                      "type" : "keyword",
                      "ignore_above" : 256
                    }
                  }
                }
              }
            },
            "cluster_agent" : {
              "type" : "text",
              "fields" : {
                "keyword" : {
                  "type" : "keyword",
                  "ignore_above" : 256
                }
              }
            },
            "ecs" : {
              "properties" : {
                "version" : {
                  "type" : "text",
                  "fields" : {
                    "keyword" : {
                      "type" : "keyword",
                      "ignore_above" : 256
                    }
                  }
                }
              }
            },
            "geoip" : {
              "properties" : {
                "city_name" : {
                  "type" : "text",
                  "fields" : {
                    "keyword" : {
                      "type" : "keyword",
                      "ignore_above" : 256
                    }
                  }
                },
                "continent_code" : {
                  "type" : "keyword"
                },
                "country_code2" : {
                  "type" : "keyword"
                },
                "country_code3" : {
                  "type" : "keyword"
                },
                "country_name" : {
                  "type" : "keyword"
                },
                "dma_code" : {
                  "type" : "long"
                },
                "ip" : {
                  "type" : "ip"
                },
                "latitude" : {
                  "type" : "half_float"
                },
                "location" : {
                  "type": "geo_point"
                },
                "longitude" : {
                  "type" : "half_float"
                },
                "postal_code" : {
                  "type" : "keyword"
                },
                "region_code" : {
                  "type" : "keyword"
                },
                "region_name" : {
                  "type" : "keyword"
                },
                "timezone" : {
                  "type" : "text",
                  "fields" : {
                    "keyword" : {
                      "type" : "keyword",
                      "ignore_above" : 256
                    }
                  }
                }
              }
            },
            "host" : {
              "properties" : {
                "architecture" : {
                  "type" : "text",
                  "fields" : {
                    "keyword" : {
                      "type" : "keyword",
                      "ignore_above" : 256
                    }
                  }
                },
                "containerized" : {
                  "type" : "boolean"
                },
                "hostname" : {
                  "type" : "text",
                  "fields" : {
                    "keyword" : {
                      "type" : "keyword",
                      "ignore_above" : 256
                    }
                  }
                },
                "id" : {
                  "type" : "text",
                  "fields" : {
                    "keyword" : {
                      "type" : "keyword",
                      "ignore_above" : 256
                    }
                  }
                },
                "ip" : {
                  "type" : "text",
                  "fields" : {
                    "keyword" : {
                      "type" : "keyword",
                      "ignore_above" : 256
                    }
                  }
                },
                "mac" : {
                  "type" : "text",
                  "fields" : {
                    "keyword" : {
                      "type" : "keyword",
                      "ignore_above" : 256
                    }
                  }
                },
                "name" : {
                  "type" : "text",
                  "fields" : {
                    "keyword" : {
                      "type" : "keyword",
                      "ignore_above" : 256
                    }
                  }
                },
                "os" : {
                  "properties" : {
                    "codename" : {
                      "type" : "text",
                      "fields" : {
                        "keyword" : {
                          "type" : "keyword",
                          "ignore_above" : 256
                        }
                      }
                    },
                    "family" : {
                      "type" : "text",
                      "fields" : {
                        "keyword" : {
                          "type" : "keyword",
                          "ignore_above" : 256
                        }
                      }
                    },
                    "kernel" : {
                      "type" : "text",
                      "fields" : {
                        "keyword" : {
                          "type" : "keyword",
                          "ignore_above" : 256
                        }
                      }
                    },
                    "name" : {
                      "type" : "text",
                      "fields" : {
                        "keyword" : {
                          "type" : "keyword",
                          "ignore_above" : 256
                        }
                      }
                    },
                    "platform" : {
                      "type" : "text",
                      "fields" : {
                        "keyword" : {
                          "type" : "keyword",
                          "ignore_above" : 256
                        }
                      }
                    },
                    "version" : {
                      "type" : "text",
                      "fields" : {
                        "keyword" : {
                          "type" : "keyword",
                          "ignore_above" : 256
                        }
                      }
                    }
                  }
                }
              }
            },
            "httpversion" : {
              "type" : "keyword"
            },
            "ident" : {
              "type" : "keyword",
              "fields" : {
                "keyword" : {
                  "type" : "keyword",
                  "ignore_above" : 256
                }
              }
            },
            "input" : {
              "properties" : {
                "type" : {
                  "type" : "text",
                  "fields" : {
                    "keyword" : {
                      "type" : "keyword",
                      "ignore_above" : 256
                    }
                  }
                }
              }
            },
            "log" : {
              "properties" : {
                "file" : {
                  "properties" : {
                    "path" : {
                      "type" : "text",
                      "fields" : {
                        "keyword" : {
                          "type" : "keyword",
                          "ignore_above" : 256
                        }
                      }
                    }
                  }
                },
                "offset" : {
                  "type" : "long"
                }
              }
            },
            "message" : {
              "type" : "text"
            },
            "referrer" : {
              "type" : "text",
              "fields" : {
                "keyword" : {
                  "type" : "keyword",
                  "ignore_above" : 256
                }
              }
            },
            "request" : {
              "type" : "text",
              "fields" : {
                "keyword" : {
                  "type" : "keyword",
                  "ignore_above" : 256
                }
              }
            },
            "response" : {
              "type" : "long"
            },
            "tags" : {
              "type" : "text",
              "fields" : {
                "keyword" : {
                  "type" : "keyword",
                  "ignore_above" : 256
                }
              }
            },
            "verb" : {
              "type" : "text",
              "fields" : {
                "keyword" : {
                  "type" : "keyword",
                  "ignore_above" : 256
                }
            }
          }
        }
      }
      }
  }
  ```
  
  



