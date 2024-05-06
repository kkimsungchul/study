# AWS EC2 api를 통한 실습

### api 구축

```
sudo apt-get update
```



### app.py

```python
import time
import json
from bs4 import BeautifulSoup
import requests
import openpyxl
from openpyxl import Workbook

li = []
searchPath = "https://search.naver.com/search.naver?sm=tab_hty.top&where=news&query=%EB%8D%B0%EC%9D%B4%ED%84%B0%EC%97%94%EC%A7%80%EB%8B%88%EC%96%B4&oquery=%EB%8D%B0%EC%9D%B4%ED%84%B0%EA%B3%BC%ED%95%99+%ED%8C%8C%EC%9D%B4%EC%8D%AC&tqi=h%2FhuTlp0YihssP5P7rsssssssUK-471017&nso=so%3Ar%2Cp%3A6m%2Ca%3Aall&de=2023.01.31&ds=2022.08.04&mynews=0&office_section_code=0&office_type=0&pd=6&photo=0&sort=0"
pageCount = [i if i == 0 else i * 10 + 1 for i in range(21)]

for i in range(7):
    resp = requests.get(f'{searchPath}{pageCount[i]}')
    html = resp.text
    soup = BeautifulSoup(html, 'html.parser')
    totalNews= soup.select('div.news_area')

	for j in range(6):
        m = totalNews[j]
        url = m.select_one('a.news_tit')['href']
        title = m.select_one('a.news_tit').text
        message = m.select_one('div.dsc_wrap > a.api_txt_lines.dsc_txt_wrap').text
        date = m.select_one('span.info').text
        press = m.select_one('a.info.press').text
        dict_data = {"url": f"{url}","title": f"{title}", "message":f"{message}", "date":f"{date}","press":f"{press}"}
        json_data = json.dumps(dict_data)
        value = json.loads(json_data)
        li.append(value)
        
@app.route("/")
def index():
    return 'Hellow World'

@app.route("/api")
def api():
    return li[random.randint(0,len(li)-1)]


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=int(os.environ.get("PORT", 5000)))
```



### kafka producer 세팅

`sudo apt install python3-pip` 

`mkdir producer`

`mkdir consumer` 

`pip3 install requests confluent-kafka`



### producer_mini.py

```python
import json
import requests
from confluent_kafka import Producer
import time

def delivery_report(err, msg):
    """ Called once for each message produced to indicate delivery result.
        Triggered by poll() or flush(). """
    if err is not None:
        print('Message delivery failed: {}'.format(err))
    else:
        print('Message delivered to {} [{}]'.format(msg.topic(), msg.partition()))
  
producer = Producer({'bootstrap.servers': '172.31.0.225:9092'})
while True:
    api_server='http://3.36.88.81:5000/api'
    request=requests.get(api_server)
    data=json.loads(request.content)
    json_data = json.dumps(data)
    producer.poll(0)
    producer.produce("news", json_data, callback=delivery_report)
    producer.flush()
    time.sleep(1)
```



### Consumer

```python
from confluent_kafka import Consumer
import json
import time

consumer = Consumer({

    'bootstrap.servers': '172.31.0.225:9092',

    'group.id': 'news-tracker',
    'auto.offset.reset': 'smallest'


})
consumer.subscribe(['news'])

while True:
    msg = consumer.poll(1.0)
    if msg is None:
        continue
    if msg.error():
        print("Consumer error: {}".format(msg.error()))
        continue
    print('Received message: {}'.format(json.loads(msg.value())))
    time.sleep(1)
consumer.close()
```



### 현재 아키텍쳐

![kafka-실습](./images/kafka-실습.png)



### kafka topic 생성

`../kafka_2.13-3.3.2/bin/kafka-topics.sh --create --bootstrap-server kafka_node1:9092 --replication-factor 1 --partitions 3 --topic news` 



