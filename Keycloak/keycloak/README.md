# 테스트방법

curl -X POST "http://localhost:8080/auth/realms/demo/protocol/openid-connect/token" ^
--header "Content-Type:application/x-www-form-urlencoded" ^
--data-urlencode "grant_type=password" ^
--data-urlencode "client_id=my_client" ^
--data-urlencode "client_secret=XafEZWyEQDgd0rmn86rO4H7K674l5zM9" ^
--data-urlencode "username=sungchul" ^
--data-urlencode "password=admin"