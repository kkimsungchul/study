

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import cd.common.ApiException;
import cd.common.HttpClientErrorExceptionDetail;
import cd.common.RestService;
import cd.message.manage.DbMessageManager;
import cd.project.jenkins.JenkinsUtil;
import cd.project.jenkins.vo.JenkinsJobCdVO;
import cd.project.jenkins.vo.JenkinsStatusCountVO;
import cd.project.jenkins.vo.JobBuildInfoVO;
import cd.project.jenkins.vo.LastBuildJobVO;

@Service
public class NewJenkinsRestService
{
    private static final Logger logger = LoggerFactory.getLogger(NewJenkinsRestService.class);

    @Autowired
    private RestService restService;

    @Value("${jenkins.tb-ipc-host}")
    private String tbIpcHost;

    @Value("${jenkins.tb-ipc-admin}")
    private String tbIpcAdmin;

    @Value("${jenkins.tb-ipc-admin-token}")
    private String tbIpcAdminToken;

    @Value("${jenkins.tb-epc-host}")
    private String tbEpcHost;

    @Value("${jenkins.tb-epc-admin}")
    private String tbEpcAdmin;

    @Value("${jenkins.tb-epc-admin-token}")
    private String tbEpcAdminToken;

    @Value("${jenkins.tb-container-host}")
    private String tbContainerHost;

    @Value("${jenkins.tb-container-admin}")
    private String tbContainerAdmin;

    @Value("${jenkins.tb-container-admin-token}")
    private String tbContainerAdminToken;

    @Value("${jenkins.tb-rm-host}")
    private String tbRmHost;

    @Value("${jenkins.tb-rm-admin}")
    private String tbRmAdmin;

    @Value("${jenkins.tb-rm-admin-token}")
    private String tbRmAdminToken;

    @Value("${jenkins.prd-ipc-host}")
    private String prdIpcHost;

    @Value("${jenkins.prd-ipc-admin}")
    private String prdIpcAdmin;

    @Value("${jenkins.prd-ipc-admin-token}")
    private String prdIpcAdminToken;

    @Value("${jenkins.prd-epc-host}")
    private String prdEpcHost;

    @Value("${jenkins.prd-epc-admin}")
    private String prdEpcAdmin;

    @Value("${jenkins.prd-epc-admin-token}")
    private String prdEpcAdminToken;

    @Value("${jenkins.prd-container-host}")
    private String prdContainerHost;

    @Value("${jenkins.prd-container-admin}")
    private String prdContainerAdmin;

    @Value("${jenkins.prd-container-admin-token}")
    private String prdContainerAdminToken;

    @Autowired
    private JenkinsUtil jenkinsUtil;

    @Autowired
    Environment env;

    String getJenkinsUrl(String serverTypeCode, String serverZoneCode)
    {
        String jenkinsUrl = null;
        /**
         * serverTypeCode 10101 - TB, 10102 - PRD
         * serverZoneCode 10202 - IPC, 10203 - EPC, 10204 - Container, 10201 - RM
         */
        if("10101".equals(serverTypeCode) && "10202".equals(serverZoneCode))
        {
            jenkinsUrl = this.tbIpcHost;
        }else if("10101".equals(serverTypeCode) && "10203".equals(serverZoneCode))
        {
            jenkinsUrl = this.tbEpcHost;
        }else if("10101".equals(serverTypeCode) && "10204".equals(serverZoneCode))
        {
            jenkinsUrl = this.tbContainerHost;
        }else if("10101".equals(serverTypeCode) && "10201".equals(serverZoneCode))
        {
            jenkinsUrl = this.tbRmHost;
        }else if("10102".equals(serverTypeCode) && "10202".equals(serverZoneCode))
        {
            jenkinsUrl = this.prdIpcHost;
        }else if("10102".equals(serverTypeCode) && "10203".equals(serverZoneCode))
        {
            jenkinsUrl = this.prdEpcHost;
        }else if("10102".equals(serverTypeCode) && "10204".equals(serverZoneCode))
        {
            jenkinsUrl = this.prdContainerHost;
        }

        logger.debug("NewJenkinsRestService getJenkinsUrl() serverTypeCode="+serverTypeCode+", serverZoneCode="+serverZoneCode+", jenkinsUrl="+jenkinsUrl);
        return jenkinsUrl;
    }

    private HttpHeaders makeHttpHeader(String serverTypeCode, String serverZoneCode) {
        HttpHeaders headers = new HttpHeaders();

        /**
         * serverTypeCode 10101 - TB, 10102 - PRD
         * serverZoneCode 10202 - IPC, 10203 - EPC, 10204 - Container, 10201 - RM
         */
        if("10101".equals(serverTypeCode) && "10202".equals(serverZoneCode))
        {
            headers.setBasicAuth(tbIpcAdmin, tbIpcAdminToken);
            logger.debug("NewJenkinsRestService makeHttpHeader() TB IPC jenkinsAdmin="+tbIpcAdmin+", jenkinsAdmintoken="+tbIpcAdminToken);
        }else if("10101".equals(serverTypeCode) && "10203".equals(serverZoneCode))
        {
            headers.setBasicAuth(tbEpcAdmin, tbEpcAdminToken);
            logger.debug("NewJenkinsRestService makeHttpHeader() TB EPC jenkinsAdmin="+tbEpcAdmin+", jenkinsAdmintoken="+tbEpcAdminToken);
        }else if("10101".equals(serverTypeCode) && "10204".equals(serverZoneCode))
        {
            headers.setBasicAuth(tbContainerAdmin, tbContainerAdminToken);
            logger.debug("NewJenkinsRestService makeHttpHeader() TB Container jenkinsAdmin="+tbContainerAdmin+", jenkinsAdmintoken="+tbContainerAdminToken);
        }else if("10101".equals(serverTypeCode) && "10201".equals(serverZoneCode))
        {
            headers.setBasicAuth(tbRmAdmin, tbRmAdminToken);
            logger.debug("NewJenkinsRestService makeHttpHeader() TB RM jenkinsAdmin="+tbRmAdmin+", jenkinsAdmintoken="+tbRmAdminToken);
        }else if("10102".equals(serverTypeCode) && "10202".equals(serverZoneCode))
        {
            headers.setBasicAuth(prdIpcAdmin, prdIpcAdminToken);
            logger.debug("NewJenkinsRestService makeHttpHeader() PRD IPC jenkinsAdmin="+prdIpcAdmin+", jenkinsAdmintoken="+prdIpcAdminToken);
        }else if("10102".equals(serverTypeCode) && "10203".equals(serverZoneCode))
        {
            headers.setBasicAuth(prdEpcAdmin, prdEpcAdminToken);
            logger.debug("NewJenkinsRestService makeHttpHeader() PRD EPC jenkinsAdmin="+prdEpcAdmin+", jenkinsAdmintoken="+prdEpcAdminToken);
        }else if("10102".equals(serverTypeCode) && "10204".equals(serverZoneCode))
        {
            headers.setBasicAuth(prdContainerAdmin, prdContainerAdminToken);
            logger.debug("NewJenkinsRestService makeHttpHeader() PRD Container jenkinsAdmin="+prdContainerAdmin+", jenkinsAdmintoken="+prdContainerAdminToken);
        }

        Iterator<String> iter = headers.keySet().iterator();
        while(iter.hasNext())
        {
            String key = iter.next();
            List<String> valueList = headers.get(key);
            logger.debug("NewJenkinsRestService makeHttpHeader "+key+": "+Arrays.toString(valueList.toArray()));
        }

        return headers;
    }

    /**
     * Jenkins Rest API 실행 - callJenkinsRestApi
     * 
     * @param method
     * @param uri
     * @param params
     */
    public ResponseEntity<String> callJenkinsRestApi(String serverTypeCode, String serverZoneCode, String method, String uri, MultiValueMap<String, String> params) {

        ResponseEntity<String> result = null;

        if(method.equals("GET")) {
            result = restService.getRestApi(uri, makeHttpHeader(serverTypeCode, serverZoneCode), params);
        }

        if(method.equals("POST")) {
            HttpHeaders headers = makeHttpHeader(serverTypeCode, serverZoneCode);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            result = restService.postRestApi(uri, headers, params);
        }

        if(method.equals("DELETE")) {
            result = restService.deleteRestApi(uri, makeHttpHeader(serverTypeCode, serverZoneCode), params);
        }

        return result;
    }

    /**
     * Jenkins Rest API 실행 - callJenkinsRestApiForJson
     * 
     * @param method
     * @param uri
     * @param params
     */
    public ResponseEntity<String> callJenkinsRestApiForJson(String serverTypeCode, String serverZoneCode, String method, String uri, String params) {

        ResponseEntity<String> result = null;

        if(method.equals("GET")) {
            result = restService.getRestApiForJson(uri, makeHttpHeader(serverTypeCode, serverZoneCode), params);
        }

        if(method.equals("POST")) {
            result = restService.postRestApiForJson(uri, makeHttpHeader(serverTypeCode, serverZoneCode), params);
        }

        if(method.equals("PUT")) {
            result = restService.putRestApiForJson(uri, makeHttpHeader(serverTypeCode, serverZoneCode), params);
        }

        return result;
    }

    /**
     * Jenkins Rest API 실행 - callJenkinsRestApiForXml
     * 
     * @param method
     * @param uri
     * @param params
     */
    public ResponseEntity<String> callJenkinsRestApiForXml(String serverTypeCode, String serverZoneCode, String method, String uri, String params) {

        ResponseEntity<String> result = null;

        if(method.equals("GET")) {
            result = restService.getRestApiForXml(uri, makeHttpHeader(serverTypeCode, serverZoneCode), params);
        }

        if(method.equals("POST")) {
            result = restService.postRestApiForXml(uri, makeHttpHeader(serverTypeCode, serverZoneCode), params);
        }

        if(method.equals("PUT")) {
            result = restService.putRestApiForXml(uri, makeHttpHeader(serverTypeCode, serverZoneCode), params);
        }

        return result;
    }

    /**
     * Jenkins Folder 생성
     * 
     * @param folderNm
     * @param jenkinsFolderName 
     * @param serverZoneCode 
     */
    public Map<String, String> createJenkinsFolder(String serverTypeCode, String serverZoneCode, String folderNm)
    {
        Map<String, String> result = new HashMap<String, String>();
        String uri = getJenkinsUrl(serverTypeCode, serverZoneCode) + "/createItem?name=" + folderNm +"&mode=com.cloudbees.hudson.plugins.folder.Folder";

        try
        {
            ResponseEntity<String> responseResult = callJenkinsRestApi(serverTypeCode, serverZoneCode, "POST", uri, null);

            if (responseResult.getStatusCode() == HttpStatus.OK) {

                logger.info("Jenkins REST API : Folder create success!");

                result.put("status", DbMessageManager.getMessage("I0001", "ko"));
                result.put("msg", "");
                return result;

            } else {

                // ssl 오류에 대해서는 무시한다 (정상으로 처리)
                if (responseResult.getStatusCode() == HttpStatus.FOUND) {
                    result.put("status", DbMessageManager.getMessage("I0001", "ko"));
                    result.put("msg", "");
                    return result;
                } else {
                    logger.error("Jenkins REST API is fail, Return code : " + responseResult.getStatusCodeValue());
                    throw new ApiException(responseResult.getStatusCode(), "E0015");
                }
            }

        } catch(HttpClientErrorException ce) {
            throw HttpClientErrorExceptionDetail.getApiException(ce, "Jenkins REST API error", uri);
        }
    }

    /**
     * 특정 폴더 밑에 Jenkins Job 생성(다른 Job 복사를 통한 Job 생성)
     * 
     * @param folderName - 복사할 Job을 담을 폴더명
     * @param jobName - 생성할 Job 이름
     * @param copyJobName - 복사할 기존 Job 이름(혹은 Folder 명)
     */
    public Map<String, String> createJenkinsJobFromOtherJob(String serverTypeCode, String serverZoneCode, String folderName, String jobName, String copyJobName)
    {
        String uri = getJenkinsUrl(serverTypeCode, serverZoneCode) + "/job/" + folderName + "/createItem?name=" + jobName +"&mode=copy&from="+copyJobName;

        try 
        {
            // resources의 jenkinsJobConfig.xml 파일을 읽어 온다
            String xmlStr = IOUtils.toString(getClass().getResourceAsStream("/xml/jenkens/jenkinsJobConfig.xml"), "UTF-8");
            ResponseEntity<String> responseResult = callJenkinsRestApiForXml(serverTypeCode, serverZoneCode, "POST", uri, xmlStr);

            if(responseResult.getStatusCode() == HttpStatus.OK || responseResult.getStatusCode() == HttpStatus.FOUND)
            {
                logger.info("Jenkins REST API : Job create from other job success!");
                Map<String, String> result = new HashMap<String, String>();
                result.put("status", DbMessageManager.getMessage("I0001", "ko"));
                result.put("msg", "");
                return result;
            }else
            {
                logger.error("Jenkins REST API is fail, Return code : " + responseResult.getStatusCodeValue());
                throw new ApiException(responseResult.getStatusCode(), "E0015");
            }
        }catch(IOException ie)
        {
            // 파일 읽기 에러
            throw new ApiException("E0017", "Jenkins REST API error : " + "Job config File");
        }catch(HttpClientErrorException ce)
        {
            throw HttpClientErrorExceptionDetail.getApiException(ce, "Jenkins REST API error", uri);
        }
    }

    /**
     * Jenkins Job 생성
     * 
     * @param folderNm
     * @param jobNm
     */
    public Map<String, String> createJenkinsJob(String serverTypeCode, String serverZoneCode, String folderNm, String jobNm) {
        String uri = getJenkinsUrl(serverTypeCode, serverZoneCode) + "/job/" + folderNm +"/createItem?name=" + jobNm;

        try 
        {
            // resources의 jenkinsJobConfig.xml 파일을 읽어 온다
            String xmlStr = IOUtils.toString(getClass().getResourceAsStream("/xml/jenkens/jenkinsJobConfig.xml"), "UTF-8");
            ResponseEntity<String> responseResult = callJenkinsRestApiForXml(serverTypeCode, serverZoneCode, "POST", uri, xmlStr);

            if (responseResult.getStatusCode() == HttpStatus.OK) {
                logger.info("Jenkins REST API : Job create success!");
                Map<String, String> result = new HashMap<String, String>();
                result.put("status", DbMessageManager.getMessage("I0001", "ko"));
                result.put("msg", "");
                return result;
            } else {
                logger.error("Jenkins REST API is fail, Return code : " + responseResult.getStatusCodeValue());
                throw new ApiException(responseResult.getStatusCode(), "E0015");
            }

        } catch(IOException ie) {
            // 파일 읽기 에러
            throw new ApiException("E0017", "Jenkins REST API error : " + "Job config File");
        } catch(HttpClientErrorException ce) {
            throw HttpClientErrorExceptionDetail.getApiException(ce, "Jenkins REST API error", uri);
        }
    }

    /**
     * Jenkins 빌드결과 조회 - 목록 조회용
     * 
     * @param folderNm
     * @param jobNm
     */
    public JenkinsStatusCountVO getJenkinsJobLastStableBuildForList(String serverTypeCode, String serverZoneCode, String folderNm, String jobNm) {
        JenkinsStatusCountVO result = new JenkinsStatusCountVO();
        result.setSuccessCnt(0);
        result.setFailCnt(0);

        String uri = getJenkinsUrl(serverTypeCode, serverZoneCode) + "/job/" + folderNm + "/job/" + jobNm +"/lastStableBuild/api/json";

        try
        {
            ResponseEntity<String> responseResult = callJenkinsRestApiForJson(serverTypeCode, serverZoneCode, "GET", uri, null);

            if (responseResult.getStatusCode() == HttpStatus.OK) {
                logger.info("Jenkins REST API : Get jenkins build informatin is found!");
                JenkinsJobCdVO jobVO = jenkinsUtil.getJenkinsProject(responseResult.getBody());

                // 빌드 성공, 실패 설정
                if(jobVO.getResult().equals("SUCCESS")) {
                    result.setSuccessCnt(1);
                } else if(jobVO.getResult().equals("FAIL")) {
                    result.setFailCnt(1);
                }

            } else {
                // Not found, 정상종료로 처리한다
                if(responseResult.getStatusCode() == HttpStatus.NOT_FOUND) {
                    logger.info("Jenkins REST API : Get jenkins build informatin is not found!");
                } else {
                    logger.error("Jenkins REST API is fail, Return code : " + responseResult.getStatusCodeValue());
                    throw new ApiException(responseResult.getStatusCode(), "E0015");
                }
            }

        } catch(HttpClientErrorException ce) {
            // Not found, 정상종료로 처리한다
            HttpStatus status = ce.getStatusCode();
            if(status == HttpStatus.NOT_FOUND) {
                logger.info("Jenkins REST API : Get jenkins build informatin is not found!");
            } else {
                logger.error("Jenkins REST API error: {}", ce.getMessage());
//                throw HttpClientErrorExceptionDetail.getApiException(ce, "Jenkins REST API error", uri);
            }
        }

        return result;
    }

    /**
     * Jenkins 빌드결과 조회 - 상세 조회용
     * 
     * @param jenkinsFolderName
     * @param jenkinsJobName
     */
    public LastBuildJobVO getJenkinsJobLastBuildForDetail(String serverTypeCode, String serverTypeName, String serverZoneCode, String serverZoneName, String jenkinsFolderName, String jenkinsJobName, Integer jobServiceStatus)
    {
        LastBuildJobVO result = new LastBuildJobVO();
        String uri = getJenkinsUrl(serverTypeCode, serverZoneCode) + "/job/" + jenkinsFolderName + "/job/" + jenkinsJobName +"/lastBuild/api/json";

        try
        {
            ResponseEntity<String> responseResult = callJenkinsRestApiForJson(serverTypeCode, serverZoneCode, "GET", uri, null);

            if(responseResult.getStatusCode() == HttpStatus.OK)
            {
                logger.info("Jenkins REST API : Get jenkins build informatin is found!");
                JenkinsJobCdVO jobVO = jenkinsUtil.getJenkinsProject(responseResult.getBody());

                // Job 상세정보 설정
                result.setId(Long.valueOf(jobVO.getId()));      // 빌드번호
                result.setServerTypeCode(serverTypeCode);
                result.setServerTypeName(serverTypeName);
                result.setServerZoneCode(serverZoneCode);
                result.setServerZoneName(serverZoneName);
                result.setJenkinsFolderName(jenkinsFolderName); // Folder명
                result.setJenkinsJobName(jenkinsJobName);       // Job명
                result.setServiceStatus(jobServiceStatus);      // Job Service Status(1 - 비활성화, 2 - 활성화, 9 - 삭제)
                result.setDescription(jobVO.getDescription());  // 설명
                result.setResult(jobVO.getResult());            // 빌드결과
                result.setLastBuildDate(convertTimestampToStr(jobVO.getLastBuildDate()));  // 마지막 빌드 시간

            }else
            {
                // Not found, 정상종료로 처리한다
                if(responseResult.getStatusCode() == HttpStatus.NOT_FOUND)
                {
                    logger.info("Jenkins REST API : Get jenkins build informatin is not found!");
                    return null;
                }else
                {
                    logger.error("Jenkins REST API is fail, Return code : " + responseResult.getStatusCodeValue());
                    throw new ApiException(responseResult.getStatusCode(), "E0015");
                }
            }

        }catch(HttpClientErrorException ce)
        {
            // Not found, 정상종료로 처리한다
            HttpStatus status = ce.getStatusCode();
            if(status == HttpStatus.NOT_FOUND)
            {
                logger.info("Jenkins REST API : Get jenkins build informatin is not found!");
                return null;
            }else
            {
                logger.error("Jenkins REST API error: {}", ce.getMessage());
//                throw HttpClientErrorExceptionDetail.getApiException(ce, "Jenkins REST API error", uri);
            }
        }

        return result;
    }

    /**
     * Jenkins Job 삭제
     * 
     * @param folderNm
     * @param jobNm
     */
    public Map<String, String> deleteJenkinsJob(String serverTypeCode, String serverZoneCode, String folderNm, String jobNm) {
        Map<String, String> result = new HashMap<String, String>();
        String uri = getJenkinsUrl(serverTypeCode, serverZoneCode) + "/job/" + folderNm + "/job/" + jobNm +"/doDelete";

        try
        {
            ResponseEntity<String> responseResult = callJenkinsRestApiForXml(serverTypeCode, serverZoneCode, "POST", uri, null);

            if (responseResult.getStatusCode() == HttpStatus.FOUND) {

                logger.info("Jenkins REST API : Job delete success!");

                result.put("status", DbMessageManager.getMessage("I0001", "ko"));
                result.put("msg", "");
                return result;

            } else {
                logger.error("Jenkins REST API is fail, Return code : " + responseResult.getStatusCodeValue());
                throw new ApiException(responseResult.getStatusCode(), "E0015");
            }

        } catch(HttpClientErrorException ce) {
            // Not found, 정상종료로 처리한다
            HttpStatus status = ce.getStatusCode();
            if(status == HttpStatus.NOT_FOUND) {
                logger.info("Jenkins REST API : Get jenkins build informatin is not found!");

                result.put("status", DbMessageManager.getMessage("I0002", "ko"));
                result.put("msg", DbMessageManager.getMessage("E0017", "지정한 Jenkins Job", "ko"));
                return result;
            } else {
                throw HttpClientErrorExceptionDetail.getApiException(ce, "Jenkins REST API error", uri);
            }
        }
    }

    /**
     * Jenkins Job 비활성화
     * 
     * @param jenkinsFolderName
     * @param jenkinsJobName
     */
    public Map<String, String> disableJenkinsJob(String serverTypeCode, String serverZoneCode, String jenkinsFolderName, String jenkinsJobName)
    {
        Map<String, String> result = new HashMap<String, String>();
        String uri = getJenkinsUrl(serverTypeCode, serverZoneCode) + "/job/" + jenkinsFolderName + "/job/" + jenkinsJobName +"/disable";
        
        try {
            ResponseEntity<String> responseResult = callJenkinsRestApiForXml(serverTypeCode, serverZoneCode, "POST", uri, null);
            logger.info(responseResult.getBody());
            result.put("status", DbMessageManager.getMessage("I0001", "ko"));
            result.put("msg", "");
            return result;
            
        } catch(HttpClientErrorException ce) {
            throw HttpClientErrorExceptionDetail.getApiException(ce, "Jenkins REST API error", uri);
        }
    }

    /**
     * Jenkins Job 활성화
     * 
     * @param jenkinsFolderName
     * @param jenkinsJobName
     */
    public Map<String, String> enableJenkinsJob(String serverTypeCode, String serverZoneCode, String jenkinsFolderName, String jenkinsJobName)
    {
        Map<String, String> result = new HashMap<String, String>();
        String uri = getJenkinsUrl(serverTypeCode, serverZoneCode) + "/job/" + jenkinsFolderName + "/job/" + jenkinsJobName +"/enable";

        try
        {
            callJenkinsRestApiForXml(serverTypeCode, serverZoneCode, "POST", uri, null);
            
            result.put("status", DbMessageManager.getMessage("I0001", "ko"));
            result.put("msg", "");
            return result;
        }catch(HttpClientErrorException ce)
        {
            throw HttpClientErrorExceptionDetail.getApiException(ce, "Jenkins REST API error", uri);
        }
    }

    /**
     * Jenkins 폴더명 중복 체크
     * @param jenkinsFolderNm
     * @return true/false
     */
    public boolean getValidJenkinsFolderNm(String serverTypeCode, String serverZoneCode, String jenkinsFolderNm) {
        boolean isDuplicated = false;
        String uri = getJenkinsUrl(serverTypeCode, serverZoneCode) + "/checkJobName?value=" + jenkinsFolderNm;
        
        try {

            ResponseEntity<String> responseResult = callJenkinsRestApiForXml(serverTypeCode, serverZoneCode, "GET", uri, null);

            if (responseResult.getStatusCode() == HttpStatus.OK) {
                if(responseResult.getBody().contains("error")) {
                    logger.info("Jenkins REST API : Get jenkins folder name is found!");
                    // 중복된 폴더명이 있음
                    isDuplicated = true;
                }
            }
            
        } catch (HttpClientErrorException ce) {
            throw HttpClientErrorExceptionDetail.getApiException(ce, "Jenkins REST API error", uri);
        }

        return isDuplicated;
    }

    /**
     * Jenkins 유저 정보 취득
     * @param loginId
     * @return true/false
     */
    public boolean getJenkinsUser(String serverTypeCode, String serverZoneCode, String loginId) {
        boolean isDuplicated = false;
        String uri = getJenkinsUrl(serverTypeCode, serverZoneCode) + "/user/" + loginId + "/api/json";

        try {

            ResponseEntity<String> responseResult = callJenkinsRestApiForXml(serverTypeCode, serverZoneCode, "GET", uri, null);

            if (responseResult.getStatusCode() == HttpStatus.OK) {
                logger.info("Jenkins REST API : Get jenkins user informatin is found!");
                isDuplicated = true;
            }

        } catch (HttpClientErrorException ce) {
            // Not found, 정상종료로 처리한다
            HttpStatus status = ce.getStatusCode();
            if(status == HttpStatus.NOT_FOUND) {
                logger.info("Jenkins REST API : Get jenkins user informatin is not found!");
            } else {
                logger.error("Jenkins REST API error: {}", ce.getMessage());
//                throw HttpClientErrorExceptionDetail.getApiException(ce, "Jenkins REST API error", uri);
            }
        }

        return isDuplicated;
    }

    /**
     * 프로젝트 ID 기준으로 소속된 전체 Jenkins job들 중에서 최근 실행 job Log 정보 조회
     * 
     * @param folderNm
     * @param jobNm
     */
    public String findLastBuildJobLogTextList(String serverTypeCode, String serverZoneCode, String folderNm, String jobNm) {
        String result = null;
        String uri = getJenkinsUrl(serverTypeCode, serverZoneCode) + "/job/" + folderNm + "/job/" + jobNm +"/lastBuild/logText/progressiveText";

        try
        {
            ResponseEntity<String> responseResult = callJenkinsRestApiForJson(serverTypeCode, serverZoneCode, "GET", uri, null);

            if (responseResult.getStatusCode() == HttpStatus.OK) {
                logger.info("Jenkins REST API : Get jenkins build informatin is found! 1001");
                result = responseResult.getBody();
            } else {
                // Not found, 정상종료로 처리한다
                if(responseResult.getStatusCode() == HttpStatus.NOT_FOUND) {
                    logger.info("Jenkins REST API : Get jenkins build informatin is not found! 1001");
                    return null;
                } else {
                    logger.error("Jenkins REST API is fail, Return code : " + responseResult.getStatusCodeValue());
                    throw new ApiException(responseResult.getStatusCode(), "E0015");
                }
            }

        } catch(HttpClientErrorException ce) {
            // Not found, 정상종료로 처리한다
            HttpStatus status = ce.getStatusCode();
            if(status == HttpStatus.NOT_FOUND) {
                logger.info("Jenkins REST API : Get jenkins build informatin is not found! 1001");
                return null;
            } else {
                logger.error("Jenkins REST API error 1001 : {}", ce.getMessage());
            }
        }

        return result;
    }

    /**
     * Jenkins 특정 Folder내 특정 Job에 대한 빌드 정보 목록 조회
     * Jenkins Job 생성후 빌드 한번도 하지 않거나, Job이 disabled 된 경우에도 빌드 정보 가져옴.
     * Jenkins Job 생성후 빌드 한번도 하지 않은 경우 builds 정보만 빈값이고, Job 기본 정보는 반환함.
     * 
     * @param jenkinsFolderName
     * @param jenkinsJobName
     */
    public JobBuildInfoVO getJenkinsJobBuildList(String serverTypeCode, String serverZoneCode, String jenkinsFolderName, String jenkinsJobName)
    {
    	JobBuildInfoVO jobBuildInfoVO = null;
        String uri = getJenkinsUrl(serverTypeCode, serverZoneCode) + "/job/" + jenkinsFolderName + "/job/" + jenkinsJobName +"/api/json";

        try
        {
        	logger.info("Jenkins REST API : Get jenkins job build list uri="+uri);

            ResponseEntity<String> responseResult = callJenkinsRestApiForJson(serverTypeCode, serverZoneCode, "GET", uri, null);

            if(responseResult.getStatusCode() == HttpStatus.OK)
            {
                String resultJsonStr = responseResult.getBody();
                logger.info("Jenkins REST API : Get jenkins build informatin is found! uri="+uri+", resultJsonStr="+resultJsonStr);
                jobBuildInfoVO = jenkinsUtil.getJenkinsJobBuildInfo(resultJsonStr);

                // Job 상세정보 설정
                jobBuildInfoVO.setServerTypeCode(serverTypeCode);   // serverTypeCode - 배포 서버 타입코드(10101 - TB, 10102 - PRD)
                jobBuildInfoVO.setServerZoneCode(serverZoneCode);   // serverZoneCode - 배포 서버 영역코드(10202 - IPC, 10203 - EPC, 10204 - Container, 10201 - RM)
                jobBuildInfoVO.setJenkinsFolderName(jenkinsFolderName);    // folder명
                jobBuildInfoVO.setJenkinsJobName(jenkinsJobName);          // Job명
            }else
            {
                // Not found, 정상종료로 처리한다
                if(responseResult.getStatusCode() == HttpStatus.NOT_FOUND)
                {
                    logger.info("Jenkins REST API : Get jenkins build informatin is not found!");
                    return null;
                }else
                {
                    logger.error("Jenkins REST API is fail, Return code : " + responseResult.getStatusCodeValue());
                    throw new ApiException(responseResult.getStatusCode(), "E0015");
                }
            }
        }catch(HttpClientErrorException ce)
        {
            // Not found, 정상종료로 처리한다
            HttpStatus status = ce.getStatusCode();
            if(status == HttpStatus.NOT_FOUND)
            {
                logger.info("Jenkins REST API : Get jenkins build informatin is not found!");
                return null;
            }else
            {
                logger.error("Jenkins REST API error: {}", ce.getMessage());
//                throw HttpClientErrorExceptionDetail.getApiException(ce, "Jenkins REST API error", uri);
            }
        }catch(HttpServerErrorException se)
        {
            logger.error("Jenkins REST API error: {}", se.getMessage());
        }

        return jobBuildInfoVO;
    }

    private String convertTimestampToStr(long value)
    {
        String result = "";

        if(value > 0L)
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.KOREA);
            Date dt = new Date();
            dt.setTime(value);
            result = sdf.format(dt);
        }

        return result;
    }
}
