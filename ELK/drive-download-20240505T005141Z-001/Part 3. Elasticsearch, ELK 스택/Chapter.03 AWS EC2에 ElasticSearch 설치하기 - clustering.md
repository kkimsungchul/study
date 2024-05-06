

## Elasticsearch 클러스터링

- 서로 다른 서버들을 클러스터로 묶는 실습

  ![clustering](./images/clustering.png)

  

1. etc/hosts 지정
   - etc/hosts 를 통해 편하게 설정하기
   
2. 보안 그룹 설정
   - 9200, 9300 open
   
3. elasticsearch.yml 설정
   - cluster.name : `"elastic-cluster"`
   
   - node.name : `"node-n"`
   
   - network.host : [`"_local_"`,`"_site_"`]
   
   - discovery.seed_hosts: [`"es-1"`,`"es-2"`,`"es-3"`]
     - 원격 노드 es-1,2,3를 서로 찾게 됨
     
   - cluster.initial_master.nodes: [`"node-1"`,`"node-2"`,`"node-3"`]
     - 마스터 노드 후보 알아서 찾아서 선출
     
       ```yaml
       cluster.name: elastic-cluster
       node.name: node-n
       network.host: ["_local_","_site_"]
       discovery.seed_hosts: ["es-1","es-2","es-3"]
       cluster.initial_master_nodes: ["node-1", "node-2","node-3"]
       ```
     
       
   
4. host 접속 확인
   - es-1 -> es-2, es-3 
   - es-2 -> es-1, es-3
   - es-3 -> es-1, es-2
   
5. `curl {external_ip}:9200/_cat/nodes`
   - node들 검색이 되는지 확인



### Trouble Shooting

* max virtual memory areas vm.max_map_count [65530] is too low, increase to at least [262144]

  * sudo vim /etc/sysctl.conf

    * ```
      vm.max_map_count=262144
      
      sudo sysctl -p
      ```
