
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class RestService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestService.class);

    @Autowired
    RestTemplate restTemplate;
    
    public ResponseEntity<String> getRestApi(String uri, HttpHeaders headers, MultiValueMap<String, String> params)
    {
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        ResponseEntity<String> respEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
        return respEntity;
    }

    public ResponseEntity<String> postRestApi(String uri, HttpHeaders headers, MultiValueMap<String, String> params) {
        
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        ResponseEntity<String> respEntity = restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
        return respEntity;
    }
    
    public ResponseEntity<String> putRestApi(String uri, HttpHeaders headers, MultiValueMap<String, String> params) {
        
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        ResponseEntity<String> respEntity = restTemplate.exchange(uri, HttpMethod.PUT, entity, String.class);
        return respEntity;
    }
    
    public ResponseEntity<String> deleteRestApi(String uri, HttpHeaders headers, MultiValueMap<String, String> params) {
        
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        ResponseEntity<String> respEntity = restTemplate.exchange(uri, HttpMethod.DELETE, entity, String.class);
        return respEntity;
    }

    public ResponseEntity<String> getRestApiForJson(String uri, String params)
    {
        return getRestApiForJson(uri, new HttpHeaders(), params);
    }

    public ResponseEntity<Object> getRestApiForJsonObject(String uri, String params)
    {
        return getRestApiForJsonObject(uri, new HttpHeaders(), params);
    }

    public ResponseEntity<Object> getRestApiForJsonObject(String uri, HttpHeaders headers, String params)
    {
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(params, headers);

        ResponseEntity<Object> respEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, Object.class);
        return respEntity;
    }

    public ResponseEntity<String> getRestApiForJson(String uri, HttpHeaders headers, String params)
    {
        
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(params, headers);
        ResponseEntity<String> respEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
        return respEntity;
    }
    
    public ResponseEntity<String> getRestApiByUri(String strUri, HttpHeaders headers, MultiValueMap<String, String> params)
    {
        try {
            URI uri = new URI(strUri);
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
            ResponseEntity<String> respEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
            return respEntity;
            
        } catch (URISyntaxException e) {
            HttpClientErrorException hcee = new HttpClientErrorException(e.getMessage(), HttpStatus.NOT_FOUND, null, null, null, Charset.forName("UTF-8"));
            throw HttpErrorExceptionDetail.getApiException(hcee, "Get URI Call Error", strUri);
        }
    }

    public ResponseEntity<String> postRestApiForJson(String uri, String params)
    {
        return postRestApiForJson(uri, new HttpHeaders(), params);
    }
    public ResponseEntity<String> postRestApiForJson(String uri, HttpHeaders headers, String params) {
        
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(params, headers);
        ResponseEntity<String> respEntity = restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
        return respEntity;
    }

    public ResponseEntity<String> putRestApiForJson(String uri, HttpHeaders headers, String params)
    {
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(params, headers);
        ResponseEntity<String> respEntity = restTemplate.exchange(uri, HttpMethod.PUT, entity, String.class);
        return respEntity;
    }

    public ResponseEntity<String> getRestApiForXml(String uri, HttpHeaders headers, String params)
    {
        headers.setContentType(MediaType.TEXT_XML);
        HttpEntity<String> entity = new HttpEntity<>(params, headers);
        ResponseEntity<String> respEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
        return respEntity;
    }

    public ResponseEntity<String> postRestApiForXml(String uri, HttpHeaders headers, String params)
    {
        headers.setContentType(MediaType.TEXT_XML);
        HttpEntity<String> entity = new HttpEntity<>(params, headers);
        ResponseEntity<String> respEntity = restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
        return respEntity;
    }

    public ResponseEntity<String> putRestApiForXml(String uri, HttpHeaders headers, String params) {
        
        headers.setContentType(MediaType.TEXT_XML);
        HttpEntity<String> entity = new HttpEntity<>(params, headers);
        ResponseEntity<String> respEntity = restTemplate.exchange(uri, HttpMethod.PUT, entity, String.class);
        return respEntity;
    }

    /**
     * GET 호출 결과를 문자열로 반환
     * @param apiURL - 호출 URL(URL에 파라메터 이어붙은 상태(&))
     * @return String - 응답 결과 문자열
     */
    public String getRestApiForStream(String apiURL)
    {
        return getRestApiForStream(apiURL, null);
    }

    /**
     * GET 호출 결과를 문자열로 반환
     * @param apiURL - 호출 URL(URL에 파라메터 이어붙은 상태(&))
     * @param headers - Request Header Map
     * @return String - 응답 결과 문자열
     */
    public String getRestApiForStream(String apiURL, HashMap<String, String> headers)
    {
        StringBuffer buf = new StringBuffer();
        BufferedReader br = null;
        HttpStatus httpStatus = null;
        try
        {
            URL url = new URL(apiURL);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();

            conn.setRequestMethod("GET");

            //header 에 값 셋팅
            if(headers != null)
            {
                Set<String> key = headers.keySet();

                for(Iterator<String> iterator = key.iterator(); iterator.hasNext();)
                {
                    String keyName = iterator.next();
                    String valueName = headers.get(keyName);
                    conn.setRequestProperty(keyName, valueName);
                }
            }

            br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String inputLine = null;

            while ((inputLine = br.readLine()) != null)
            {
                buf.append(inputLine);
                buf.append("\n");
            }

            //http 요청 응답 코드 확인 실시
            int statusCode = conn.getResponseCode();
            String statusText = conn.getResponseMessage();
            httpStatus = HttpStatus.valueOf(statusCode);

            if(400 <= statusCode && 500 > statusCode)
            {
                HttpClientErrorException hcee = new HttpClientErrorException(statusText, httpStatus, null, null, null, Charset.forName("UTF-8"));
                throw HttpErrorExceptionDetail.getApiException(hcee, "Get URL Call Error", apiURL);
            }else if(500 <= statusCode && 600 > statusCode)
            {
                HttpServerErrorException hsee = new HttpServerErrorException(statusText, httpStatus, null, null, null, Charset.forName("UTF-8"));
                throw HttpErrorExceptionDetail.getApiException(hsee, "Get URL Call Error", apiURL);
            }
        }catch(MalformedURLException e)
        {
            HttpClientErrorException hcee = new HttpClientErrorException(e.getMessage(), HttpStatus.NOT_FOUND, null, null, null, Charset.forName("UTF-8"));
            throw HttpErrorExceptionDetail.getApiException(hcee, "Get URL Call Error", apiURL);
        }catch(IOException e)
        {
            HttpServerErrorException hsee = new HttpServerErrorException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null, null, null, Charset.forName("UTF-8"));
            throw HttpErrorExceptionDetail.getApiException(hsee, "Get URL Call Error", apiURL);
        }finally
        {
            if(br != null){try{br.close();}catch(IOException e){ LOGGER.info(e.getMessage()); }}
        }

        return buf.toString();
    }

    /**
     * POST 호출 결과를 문자열로 반환
     * @param apiURL - 호출 URL
     * @param parameters - Request Parameter Map
     * @return String - 응답 결과 문자열
     */
    public String postRestApiForStream(String apiURL, HashMap<String, String> parameters)
    {
        return postRestApiForStream(apiURL, null, parameters);
    }

    /**
     * POST 호출 결과를 문자열로 반환
     * @param apiURL - 호출 URL
     * @param headers - Request Header Map
     * @param parameters - Request Parameter Map
     * @return String - 응답 결과 문자열
     */
    public String postRestApiForStream(String apiURL, HashMap<String, String> headers, HashMap<String, String> parameters)
    {
        String resultStr = "";

        BufferedReader br = null;
        PrintWriter pw = null;
        HttpStatus httpStatus = null;
        try
        {
            // URL 설정하고 접속하기 
            URL url = new URL(apiURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // 전송 모드 설정 - 기본적인 설정
            conn.setDefaultUseCaches(false);
            conn.setDoInput(true); // 서버에서 읽기 모드 지정 
            conn.setDoOutput(true); // 서버로 쓰기 모드 지정  
            conn.setRequestMethod("POST"); // 전송 방식은 POST

            // 헤더 세팅
            if(headers != null)
            {
                //conn.setRequestProperty("content-type", "application/x-www-form-urlencoded");
                Set<String> key = headers.keySet();

                for(Iterator<String> iterator = key.iterator(); iterator.hasNext();)
                {
                    String keyName = iterator.next();
                    String valueName = headers.get(keyName);
                    conn.setRequestProperty(keyName, valueName);
                }
            }

            // 서버로 값 전송
            StringBuffer buffer = new StringBuffer();

            //HashMap으로 전달받은 파라미터가 null이 아닌경우 버퍼에 넣어준다
            if (parameters != null)
            {
                Set<String> key = parameters.keySet();

                int loopCnt = 0;
                for(Iterator<String> iterator = key.iterator(); iterator.hasNext();)
                {
                    String keyName = iterator.next();
                    String valueName = parameters.get(keyName);

                    if(loopCnt == 0)
                    {
                        buffer.append(keyName).append("=").append(valueName);
                    }else
                    {
                        buffer.append("&").append(keyName).append("=").append(valueName);
                    }

                    loopCnt++;
                }
            }

            pw = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
            pw.write(buffer.toString());
            pw.flush();

            //Response Code
            int statusCode = conn.getResponseCode();
            String statusText = conn.getResponseMessage();
            httpStatus = HttpStatus.valueOf(statusCode);

            if(400 <= statusCode && 500 > statusCode)
            {
                HttpClientErrorException hcee = new HttpClientErrorException(statusText, httpStatus, null, null, null, Charset.forName("UTF-8"));
                throw HttpErrorExceptionDetail.getApiException(hcee, "Post URL Call Error", apiURL);
            }else if(500 <= statusCode && 600 > statusCode)
            {
                HttpServerErrorException hsee = new HttpServerErrorException(statusText, httpStatus, null, null, null, Charset.forName("UTF-8"));
                throw HttpErrorExceptionDetail.getApiException(hsee, "Post URL Call Error", apiURL);
            }

            // 서버에서 전송받기
            br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuffer builder = new StringBuffer();

            String str = null;
            while((str = br.readLine()) != null)
            {
                builder.append(str);
                builder.append("\n");
            }

            resultStr = builder.toString();
            return resultStr;
        }catch(MalformedURLException e)
        {
            HttpClientErrorException hcee = new HttpClientErrorException(e.getMessage(), HttpStatus.NOT_FOUND, null, null, null, Charset.forName("UTF-8"));
            throw HttpErrorExceptionDetail.getApiException(hcee, "Post URL Call Error", apiURL);
        }catch(IOException e)
        {
            HttpServerErrorException hsee = new HttpServerErrorException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, null, null, null, Charset.forName("UTF-8"));
            throw HttpErrorExceptionDetail.getApiException(hsee, "Post URL Call Error", apiURL);
        }finally
        {
            if(br != null){try{br.close();}catch(IOException e){ LOGGER.info(e.getMessage()); }}
            if(pw != null){pw.close();}
        }
    }
}
