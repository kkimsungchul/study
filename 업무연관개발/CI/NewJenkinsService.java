

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import portal.common.ApiException;
import portal.common.HttpErrorExceptionDetail;
import portal.common.KeyCloakSession;
import portal.common.RestService;
import portal.message.manage.DbMessageManager;


@Service
public class NewJenkinsService {
	
	private  static final Logger LOGGER = LoggerFactory.getLogger(NewJenkinsService.class);
    
	@Autowired
    private RestService restService;
    @Autowired
    private NewJenkinsMapper newJenkinsMapper;
	
    
	@Autowired
	private KeyCloakSession keyCloakSession;
	
    @Value("${jenkins.host}")
    String jenkinsUrl;

    @Value("${jenkins.admin}")
    private String jenkinsAdmin;
    
    @Value("${jenkins.admintoken}")
    private String jenkinsAdmintoken;
    
    
    private HttpHeaders makeHttpHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(jenkinsAdmin, jenkinsAdmintoken);

        return headers;
    }
    
    /**
     * Jenkins Rest API 실행 - callJenkinsRestApiForXml
     * 
     * @param method
     * @param uri
     * @param params
     */
    public ResponseEntity<String> callJenkinsRestApiForXml(String method, String uri, String params) {

        ResponseEntity<String> result = null;

        if(method.equals("GET")) {
            result = restService.getRestApiForXml(uri, makeHttpHeader(), params);
        }

        if(method.equals("POST")) {
            result = restService.postRestApiForXml(uri, makeHttpHeader(), params);
        }

        if(method.equals("PUT")) {
            result = restService.putRestApiForXml(uri, makeHttpHeader(), params);
        }

        return result;
    }
    
    /**
     * Jenkins Rest API 실행 - callJenkinsRestApi
     * 
     * @param method
     * @param uri
     * @param params
     */
    public ResponseEntity<String> callJenkinsRestApi(String method, String uri, MultiValueMap<String, String> params) {

        ResponseEntity<String> result = null;

        if(method.equals("GET")) {
            result = restService.getRestApi(uri, makeHttpHeader(), params);
        }

        if(method.equals("POST")) {
            HttpHeaders headers = makeHttpHeader();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            result = restService.postRestApi(uri, headers, params);
        }

        if(method.equals("DELETE")) {
            result = restService.deleteRestApi(uri, makeHttpHeader(), params);
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
    public ResponseEntity<String> callJenkinsRestApiForJson(String method, String uri, String params) {

        ResponseEntity<String> result = null;

        if(method.equals("GET")) {
            result = restService.getRestApiForJson(uri, makeHttpHeader(), params);
        }

        if(method.equals("POST")) {
            result = restService.postRestApiForJson(uri, makeHttpHeader(), params);
        }

        if(method.equals("PUT")) {
            result = restService.putRestApiForJson(uri, makeHttpHeader(), params);
        }

        return result;
    }
    public NewJenkinsParamVO getJenkinsProject(String jsonStr) {
        JSONObject jsonObjectResponse = new JSONObject(jsonStr);
        NewJenkinsParamVO newJenkinsParamVO = new NewJenkinsParamVO(jsonObjectResponse);
        return newJenkinsParamVO;
    }

    
    
	/**
	 * 프로젝트 상세 조회 - Jenkins 전체 목록 조회
	 * @param projectId
	 * @return List<NewJenkinsParamVO>
	 */
    public NewJenkinsParamVO getJenkinsList(Long projectId) {
    	
    	
    	List<NewJenkinsParamVO> jenkinsTopList = newJenkinsMapper.getJenkinsList(projectId);
    	NewJenkinsParamVO result = new NewJenkinsParamVO();
    	String uri="";
   	
    	for(int i=0;i<jenkinsTopList.size();i++) {
    		if("".equals(jenkinsTopList.get(i).getJenkinsFolderName()) || jenkinsTopList.get(i).getJenkinsFolderName()==null) {
    			uri = jenkinsUrl + "/job/" + jenkinsTopList.get(i).getJenkinsJobName();	
    		}else {
    			uri = jenkinsUrl + "/job/" + jenkinsTopList.get(i).getJenkinsFolderName();
    		}
    		
    		NewJenkinsParamVO newJenkinsParamVO = getJenkinsListAPI(uri,projectId);
    		newJenkinsParamVO.setProjectId(projectId);
    		newJenkinsParamVO.setId(jenkinsTopList.get(i).getId());
    		
    		if(newJenkinsParamVO.getDivision()==1) {
    			result.getFolders().add(newJenkinsParamVO);
    		}else {
    			result.getJobs().add(newJenkinsParamVO);
    		}
    		
    	}
    	return result;
    }
    
	/**
	 * 프로젝트 상세조회 - 최상단 job, folder 기준으로 하위 목록 job, folder 조회
	 * @param uri
	 * @return NewJenkinsParamVO
	 */
    public NewJenkinsParamVO getJenkinsListAPI(String uri , Long projectId) {
    	String params="";
    	ResponseEntity<String> result = null;
    	result = restService.getRestApiForJson(uri+"/api/json", makeHttpHeader(), params);
    	NewJenkinsParamVO newJenkinsParamVO =  getJenkinsProject(result.getBody());

    	//하위job에는 project id가 셋팅이 안됨 아래의 코드로 전부다 넣어주면 셋팅이 됨
    	for(int i=0; i<newJenkinsParamVO.getJobs().size();i++) {
    		newJenkinsParamVO.getJobs().get(i).setProjectId(projectId);
    	}
    	
    	newJenkinsParamVO.setProjectId(projectId);
    	if(newJenkinsParamVO.getFolders().size()>0) {
    		for(int i=0;i < newJenkinsParamVO.getFolders().size();i++) {
    			NewJenkinsParamVO newJenkinsFolderParamVO2 = new NewJenkinsParamVO();
    			newJenkinsFolderParamVO2 = getJenkinsListAPI(uri+"/job/"+newJenkinsParamVO.getFolders().get(i).getJenkinsFolderName(),projectId);
    			newJenkinsFolderParamVO2.setProjectId(projectId);
    			newJenkinsParamVO.getFolders().set(i, newJenkinsFolderParamVO2);
    		}
    	}
    	return newJenkinsParamVO;
    }
    
    
    /**
     * jenkins 최상위 folder , job 의 DB 중복체크 - insert 에서 사용
     * @param jenkins
     * @return Map<String, Object>
     */
    public Map<String, Object> insertJenkinsDBValidationCheck(NewInsertJenkinsVO jenkins) { 
    	
		Map<String, Object> result = new HashMap<String, Object>();
		List<NewInsertJenkinsVO> insertJenkinsFolderList = jenkins.getFolders();
		List<NewInsertJenkinsVO> insertJenkinsJobList = jenkins.getJobs();
		String compare = "";

		for(int i=0;i<insertJenkinsFolderList.size();i++) {
			//최상단 folder DB 목록 중복 체크
			if(newJenkinsMapper.insertValidJenkinsCheck(insertJenkinsFolderList.get(i))!=0) {
				result.put("msg", DbMessageManager.getMessage("E0016", "Jenkins 폴더명", "ko"));
				return result;				
			}
			//folder 정규식 체크
			result = insertJenkinsToolValidationCheck(insertJenkinsFolderList,null);
			if(!result.isEmpty()) {
				return result;
			}
			
			//같은 목록안에 중복된 값이 있는지 확인
			compare = insertJenkinsFolderList.get(i).getJenkinsFolderName();
			for(int j=0;j<insertJenkinsFolderList.size();j++) {
				if(i!=j) {
					if(insertJenkinsFolderList.get(j).getJenkinsFolderName().equals(compare)) {
						result.put("msg", DbMessageManager.getMessage("E0016", "Jenkins 폴더명", "ko"));
						return result;	
					}
				}
			}
		}
		
		for(int i=0;i<insertJenkinsJobList.size();i++) {
			//최상단 job 목록 DB Check 중복 체크
			if(newJenkinsMapper.insertValidJenkinsCheck(insertJenkinsJobList.get(i))!=0) {
				result.put("msg", DbMessageManager.getMessage("E0016", "Jenkins job명", "ko"));
				return result;		
			}
			//같은 목록안에 중복된 값이 있는지 확인
			compare = insertJenkinsJobList.get(i).getJenkinsJobName();
			for(int j=0;j<insertJenkinsJobList.size();j++) {
				if(i!=j) {
					if(insertJenkinsJobList.get(j).getJenkinsJobName().equals(compare)) {
						result.put("msg", DbMessageManager.getMessage("E0016", "Jenkins job명", "ko"));
						return result;	
					}
				}
			}
		}
		// 정규식 체크 및 tool중복 체크
		result = insertJenkinsToolValidationCheck(insertJenkinsJobList,null);
		if(!result.isEmpty()) {
			return result;
		}
		
    	return result;
    }
    /**
     * 젠킨스의 folder , job 의 정규식 체크, 재귀함수로 사용중이여서 하위폴더 , 하위job까지 확인 - insert 에서 사용
     * @param insertJenkinsFolderList
     * @param uri
     * @return Map<String, Object>
     */
    public Map<String, Object> insertJenkinsToolValidationCheck(List<NewInsertJenkinsVO> insertJenkinsFolderList,String paramURL){
		Matcher match;
		Map<String, Object> result = new HashMap<String, Object>();
		String regexJenkins  = "^([A-Za-z0-9ㄱ-힣]{1})[a-zA-Z0-9ㄱ-힣-_(),.\\s]{0,84}$";
		String url = paramURL;
		if(paramURL==null) {
			url = jenkinsUrl;
		}
		
		//List<NewInsertJenkinsVO> insertJenkinsFolderList = insertJenkinsFolderList
		for(int i=0; i<insertJenkinsFolderList.size();i++) {

			if(insertJenkinsFolderList.get(i).getDivision()==1) {
				String folderName = insertJenkinsFolderList.get(i).getJenkinsFolderName();	
				//정규식 체크
		        match = Pattern.compile(regexJenkins).matcher(folderName);
		        if(!match.find()) {
				    result.put("msg", DbMessageManager.getMessage("E0125", new Object[]{"Jenkins 폴더명", "특수문자로 시작할 수 없으며 한글,영문대소문자,숫자,특수문자 -_().,","1","85"}, "ko"));
				    return result;  
		        }
		        
		        
				for(int j=0;j<insertJenkinsFolderList.size();j++) {
					if(i!=j && insertJenkinsFolderList.get(j).getDivision()==1) {
						if(insertJenkinsFolderList.get(j).getJenkinsFolderName().equals(folderName)) {
							result.put("msg", DbMessageManager.getMessage("E0016", "Jenkins 폴더명", "ko"));
							return result;	
						}
					}
				}
		        
		        if(existFolderOrJob(url, insertJenkinsFolderList.get(i).getJenkinsFolderName())) {
					result.put("msg", DbMessageManager.getMessage("E0016", "Jenkins 폴더명", "ko"));
					return result;	
		        }
		        
		        //하위폴더 체크
		        if(insertJenkinsFolderList.get(i).getFolders().size()>0) {

		        	result = insertJenkinsToolValidationCheck(insertJenkinsFolderList.get(i).getFolders(),url+ "/job/" + insertJenkinsFolderList.get(i).getJenkinsFolderName());
		        	
					if(!result.isEmpty()) {
						return result;
					}
		        }
		        //하위 잡 체크
		        if(insertJenkinsFolderList.get(i).getJobs().size()>0) {

		        	result = insertJenkinsToolValidationCheck(insertJenkinsFolderList.get(i).getJobs(),url+ "/job/" + insertJenkinsFolderList.get(i).getJenkinsFolderName());

					if(!result.isEmpty()) {
						return result;
					}
		        }
				
			}else {
				
				String jobName = insertJenkinsFolderList.get(i).getJenkinsJobName();
				//정규식 체크
				match = Pattern.compile(regexJenkins).matcher(jobName);
				if(!match.find()) {
					result.put("msg", DbMessageManager.getMessage("E0125", new Object[]{"Jenkins job명", "특수문자로 시작할 수 없으며 한글,영문대소문자,숫자,특수문자 -_().,","1","85"}, "ko"));
					return result;  
				}
				for(int j=0;j<insertJenkinsFolderList.size();j++) {
					if(i!=j && insertJenkinsFolderList.get(j).getDivision()==2) {
						if(insertJenkinsFolderList.get(j).getJenkinsJobName().equals(jobName)) {
							result.put("msg", DbMessageManager.getMessage("E0016", "Jenkins job명", "ko"));
							return result;	
						}
					}
				}
				if(existFolderOrJob(url, insertJenkinsFolderList.get(i).getJenkinsJobName())) {
					result.put("msg", DbMessageManager.getMessage("E0016", "Jenkins job명", "ko"));
					return result;	
				}
			}
		}
		return result;
    }
    /**
     * jenkins 최상위 folder , job 의 DB 중복체크 - update 에서 사용
     * @param jenkins
     * @return Map<String, Object>
     */
    public Map<String, Object> updateJenkinsDBValidationCheck(NewUpdateJenkinsVO jenkins) { 
    	
		Map<String, Object> result = new HashMap<String, Object>();
		List<NewUpdateJenkinsVO> updateJenkinsFolderList = jenkins.getFolders();
		List<NewUpdateJenkinsVO> updateJenkinsJobList = jenkins.getJobs();
		String compare="";
		
		for(int i=0;i<updateJenkinsFolderList.size();i++) {
			//최상단 folder DB 목록 중복 체크

			if(newJenkinsMapper.updateValidJenkinsCheck(updateJenkinsFolderList.get(i))!=0 && ("".equals(updateJenkinsFolderList.get(i).url) || updateJenkinsFolderList.get(i).getUrl()==null)) {
				result.put("msg", DbMessageManager.getMessage("E0016", "Jenkins 폴더명", "ko"));
				return result;				
			}
			//folder 정규식 체크
			result = updateJenkinsToolValidationCheck(updateJenkinsFolderList,null);
			if(!result.isEmpty()) {
				return result;
			}
			compare = updateJenkinsFolderList.get(i).getJenkinsFolderName();
			for(int j=0;j<updateJenkinsFolderList.size();j++) {
				if(i!=j) {
					if(updateJenkinsFolderList.get(j).getJenkinsFolderName().equals(compare)) {
						result.put("msg", DbMessageManager.getMessage("E0016", "Jenkins 폴더명", "ko"));
						return result;	
					}
				}
			}
		}
		
		for(int i=0;i<updateJenkinsJobList.size();i++) {

			//최상단 job 목록 DB Check 중복 체크
			if(newJenkinsMapper.updateValidJenkinsCheck(updateJenkinsJobList.get(i))!=0  && ("".equals(updateJenkinsJobList.get(i).url) || updateJenkinsJobList.get(i).getUrl()==null)) {
				result.put("msg", DbMessageManager.getMessage("E0016", "Jenkins job명", "ko"));
				return result;		
			}
			
			compare = updateJenkinsJobList.get(i).getJenkinsJobName();
			for(int j=0;j<updateJenkinsJobList.size();j++) {
				if(i!=j) {
					if(updateJenkinsJobList.get(j).getJenkinsJobName().equals(compare)) {
						result.put("msg", DbMessageManager.getMessage("E0016", "Jenkins job명", "ko"));
						return result;	
					}
				}
			}
			
		

		}
		// 정규식 체크 및 tool중복 체크
		result = updateJenkinsToolValidationCheck(updateJenkinsJobList,null);
		if(!result.isEmpty()) {
			return result;
		}
		
    	return result;
    }
    /**
     * 젠킨스의 folder , job 의 정규식 체크, 재귀함수로 사용중이여서 하위폴더 , 하위job까지 확인 - update 에서 사용
     * @param updateJenkinsFolderList
     * @param uri
     * @return Map<String, Object>
     */
    public Map<String, Object> updateJenkinsToolValidationCheck(List<NewUpdateJenkinsVO> updateJenkinsFolderList,String paramURL){
		Matcher match;
		Map<String, Object> result = new HashMap<String, Object>();
		String regexJenkins  = "^([A-Za-z0-9ㄱ-힣]{1})[a-zA-Z0-9ㄱ-힣-_(),.\\s]{0,84}$";
		String url = paramURL;
		if(paramURL==null) {
			url = jenkinsUrl;
		}
		//List<NewUpdateJenkinsVO> updateJenkinsFolderList = updateJenkinsFolderList
		for(int i=0; i<updateJenkinsFolderList.size();i++) {
			if(updateJenkinsFolderList.get(i).getDivision()==1) {
				String folderName = updateJenkinsFolderList.get(i).getJenkinsFolderName();	
				
				//정규식 체크
		        match = Pattern.compile(regexJenkins).matcher(folderName);
		        if(!match.find()) {
				    result.put("msg", DbMessageManager.getMessage("E0125", new Object[]{"Jenkins 폴더명", "특수문자로 시작할 수 없으며 한글,영문대소문자,숫자,특수문자 -_().,","1","85"}, "ko"));
				    return result;  
		        }
		        
		        //수정시의 중복체크는, 기존에 생성한 jenkins와 비교해야 하기때문에 i 는 신규생성목록, j 는 전체목록으로 가져와서 비교함
				for(int j=0;j<updateJenkinsFolderList.size();j++) {
					if(i!=j && updateJenkinsFolderList.get(j).getDivision()==1) {
						if(updateJenkinsFolderList.get(j).getJenkinsFolderName().equals(folderName)) {
							result.put("msg", DbMessageManager.getMessage("E0016", "Jenkins 폴더명", "ko"));
							return result;	
						}
					}
				}
		        
				//URL 이 있을 경우 이미 생성한 jenkins 로 판단
		        if("".equals(updateJenkinsFolderList.get(i).getUrl()) || updateJenkinsFolderList.get(i).getUrl()==null) {
			        if(existFolderOrJob(url, updateJenkinsFolderList.get(i).getJenkinsFolderName())) {
						result.put("msg", DbMessageManager.getMessage("E0016", "Jenkins 폴더명", "ko"));
						return result;	
			        }		        	
		        }
		        //하위폴더 체크
		        if(updateJenkinsFolderList.get(i).getFolders().size()>0) {

		        	result = updateJenkinsToolValidationCheck(updateJenkinsFolderList.get(i).getFolders(),url+ "/job/" + updateJenkinsFolderList.get(i).getJenkinsFolderName());
		        	
					if(!result.isEmpty()) {
						return result;
					}
		        }
		        //하위 잡 체크
		        if(updateJenkinsFolderList.get(i).getJobs().size()>0) {

		        	result = updateJenkinsToolValidationCheck(updateJenkinsFolderList.get(i).getJobs(),url+ "/job/" + updateJenkinsFolderList.get(i).getJenkinsFolderName());

					if(!result.isEmpty()) {
						return result;
					}
		        }
				
			}else {
				String jobName = updateJenkinsFolderList.get(i).getJenkinsJobName();
				
				//정규식 체크
				match = Pattern.compile(regexJenkins).matcher(jobName);
				if(!match.find()) {
					result.put("msg", DbMessageManager.getMessage("E0125", new Object[]{"Jenkins job명", "특수문자로 시작할 수 없으며 한글,영문대소문자,숫자,특수문자 -_().,","1","85"}, "ko"));
					return result;  
				}
		        
				for(int j=0;j<updateJenkinsFolderList.size();j++) {
					if(i!=j && updateJenkinsFolderList.get(j).getDivision()==2) {
						if(updateJenkinsFolderList.get(j).getJenkinsJobName().equals(jobName)) {
							result.put("msg", DbMessageManager.getMessage("E0016", "Jenkins job명", "ko"));
							return result;	
						}
					}
				}
				
				//URL 이 있을 경우 이미 생성한 jenkins 로 판단
				if("".equals(updateJenkinsFolderList.get(i).getUrl()) || updateJenkinsFolderList.get(i).getUrl()==null) {
					if(existFolderOrJob(url, updateJenkinsFolderList.get(i).getJenkinsJobName())) {
						result.put("msg", DbMessageManager.getMessage("E0016", "Jenkins job명", "ko"));
						return result;	
					}	
				}
				
				
			}
			
		}

		return result;
    }
    
    
    /**
     * Jenkins Folder 또는 Job이 존재하는지 Validation Check
     * @param uri
     * @param name
     * @return boolean
     */
    public boolean existFolderOrJob(String uri,String name) {
        uri = uri +  "/job/" + name + "/api/json";
        LOGGER.info("existFolderOrJob :" +  uri);
        try {
            ResponseEntity<String> responseResult = callJenkinsRestApiForJson("GET", uri, null);

            if(responseResult.getStatusCode() == HttpStatus.OK) {
                LOGGER.info("Jenkins REST API : Get jenkins informatin is found!");
            } else {
                if(responseResult.getStatusCode() == HttpStatus.NOT_FOUND) {
                    LOGGER.info("Jenkins REST API : Get jenkins informatin is not found!");
                    return false;
                } else {
                    LOGGER.error("Jenkins REST API is fail, Return code : " + responseResult.getStatusCodeValue());
                }
            }

        } catch(HttpClientErrorException ce) {
            HttpStatus status = ce.getStatusCode();
            if(status == HttpStatus.NOT_FOUND) {
                LOGGER.info("Jenkins REST API : Get jenkins informatin is not found!");
                return false;
            } else {
                LOGGER.error("Jenkins REST API error: {}", ce.getMessage());
            }
        } catch(HttpServerErrorException se) {
            LOGGER.error("Jenkins REST API error: {}", se.getMessage());
        }
        
        
    	return true;
    }
    /**
     * jenkins 폴더, job 생성
     * @param newInsertProjectVo
     * @return boolean
     */
    public boolean doJenkins(NewInsertProjectVO newInsertProjectVo) {
	    boolean isSuccess = false;
		//String loginId = insertProjectVo.getCreatedBy();
	    String loginId = "91296885";
	    //long projectId = newInsertProjectVo.getId();
	    long projectId = (long)695;
		NewInsertJenkinsVO jenkins = newInsertProjectVo.getJenkins();
		
		List<NewInsertJenkinsVO> insertJenkinsFolderList = jenkins.getFolders();
		List<NewInsertJenkinsVO> insertJenkinsJobList = jenkins.getJobs();
		
		
		

		String uri = jenkinsUrl;
		createJenkinsFolder(insertJenkinsFolderList,uri , projectId, true);
		createJenkinsJob(insertJenkinsJobList,uri , projectId, true);
		
		isSuccess = true;
		return isSuccess;
	}
    

	/**
	 * Jenkins Folder 생성
	 * @param insertJenkinsFolderList folder 목록
	 * @param uri 생성할 경로
	 * @param projectId 포탈 project id
	 * @param topFlag 최상위 구분 여부 true :  최상위 
	 * @return Map<String, String>
	 */
    public Map<String, String> createJenkinsFolder(List<NewInsertJenkinsVO> insertJenkinsFolderList,String uri , long projectId , boolean topFlag) {
    	String templateJobName ="";
    	String loginId = keyCloakSession.getLoginId();
    	Map<String, String> result = new HashMap<String, String>();
    	
    	if(insertJenkinsFolderList.size()>0) {
    		for(int i=0;i<insertJenkinsFolderList.size();i++) {
    		    //폴더 생성
    			//String uri = jenkinsUrl + "/createItem?name=" + folderName +"&mode=com.cloudbees.hudson.plugins.folder.Folder";
    			String tempUri = uri+"/createItem?name=" + insertJenkinsFolderList.get(i).getJenkinsFolderName() + "&mode=com.cloudbees.hudson.plugins.folder.Folder";
    			
    			try {
    	            ResponseEntity<String> responseResult = callJenkinsRestApi("POST", tempUri, null);
    	            
    	            if (responseResult.getStatusCode() == HttpStatus.OK) {
    	                LOGGER.info("Jenkins REST API : Folder create success! , HttpStatus.OK");
    	                result.put("status", DbMessageManager.getMessage("I0001", "ko"));
    	                result.put("msg", "");
    	                //최상위일 경우에만 DB에 저장
    	                if(topFlag) {
    	                	insertJenkinsFolderList.get(i).setCreatedBy(loginId);
    	                	insertJenkinsFolderList.get(i).setLastModifiedBy(loginId);
    	                	insertJenkinsFolderList.get(i).setProjectId(projectId);
    	                	newJenkinsMapper.insertJenkins(insertJenkinsFolderList.get(i));	
    	                }
    	            } else {
    	                // ssl 오류에 대해서는 무시한다 (정상으로 처리)
    	                if (responseResult.getStatusCode() == HttpStatus.FOUND) {
    	                	LOGGER.info("Jenkins REST API : Folder create success! , HttpStatus.FOUND");
    	                    result.put("status", DbMessageManager.getMessage("I0001", "ko"));
    	                    result.put("msg", "");
        	                //최상위일 경우에만 DB에 저장
        	                if(topFlag) {
        	                	insertJenkinsFolderList.get(i).setCreatedBy(loginId);
        	                	insertJenkinsFolderList.get(i).setLastModifiedBy(loginId);
        	                	insertJenkinsFolderList.get(i).setProjectId(projectId);
        	                	newJenkinsMapper.insertJenkins(insertJenkinsFolderList.get(i));	
        	                }
    	                } else {
    	                    LOGGER.error("Jenkins REST API is fail, Return code : " + responseResult.getStatusCodeValue());
    	                    throw new ApiException(responseResult.getStatusCode(), "E0015");
    	                }
    	            }

    	        } catch(HttpClientErrorException ce) {
    	            throw HttpErrorExceptionDetail.getApiException(ce, "Jenkins REST API error", uri);
    	        } catch (HttpServerErrorException se) {
    	            throw HttpErrorExceptionDetail.getApiException(se, "Jenkins REST API error", uri);
    	        }
    			
    			if(insertJenkinsFolderList.get(i).getJobs().size()>0) {
    				String createJobUri = uri + "/job/" + insertJenkinsFolderList.get(i).getJenkinsFolderName() +"/";
    				createJenkinsJob(insertJenkinsFolderList.get(i).getJobs() , createJobUri , projectId ,false);
    			}
    			if(insertJenkinsFolderList.get(i).getFolders().size()>0) {
    				String createFolderUri = uri + "/job/" + insertJenkinsFolderList.get(i).getJenkinsFolderName() +"/";
    				createJenkinsFolder(insertJenkinsFolderList.get(i).getFolders() , createFolderUri , projectId , false);
    			}
    			
    			
    		}
    		
    	}
    	
    	return result;
    }
    
	/**
	 * Jenkins job 생성
	 * @param insertJenkinsJobList job목록
	 * @param uri 생성할 경로
	 * @param projectId 포탈 project id
	 * @param topFlag 최상위 구분 여부 true :  최상위 
	 * @return Map<String, String>
	 */
    public Map<String, String> createJenkinsJob(List<NewInsertJenkinsVO> insertJenkinsJobList,String uri, long projectId , boolean topFlag) {
    	String templateJobName ="";
    	String loginId = keyCloakSession.getLoginId();
    	Map<String, String> result = new HashMap<String, String>();
    	
    	if(insertJenkinsJobList.size()>0) {
    		for(int i=0;i<insertJenkinsJobList.size();i++) {
    			switch (insertJenkinsJobList.get(i).getJenkinsJobType()) {
    			case "10401": //FreeStyle Template Folder 내용 복사
    				templateJobName = "DSpace_FreeStyle_Template";
    				break;
    			case "10402":  //Pipeline Template Folder 내용 복사
    				templateJobName = "DSpace_Pipeline_Template";
    				break;
    			}
    		    //잡생성
    			//String uri = jenkinsUrl + "/job/" + folderName +"/createItem?name=" + jobName + "&mode=copy&from=" + copyJobName;
    			String tempUri = uri+"/createItem?name=" + insertJenkinsJobList.get(i).getJenkinsJobName() + "&mode=copy&from=" + templateJobName;
    			
    			try 
    	        {
    	            // resources의 jenkinsJobConfig.xml 파일을 읽어 온다
    	            String xmlStr = IOUtils.toString(getClass().getResourceAsStream("/xml/jenkens/jenkinsJobConfig.xml"), "UTF-8");
    	            ResponseEntity<String> responseResult = callJenkinsRestApiForXml("POST", tempUri, xmlStr);
    	            
    	            if (responseResult.getStatusCode() == HttpStatus.OK || responseResult.getStatusCode() == HttpStatus.FOUND) {
    	                LOGGER.info("Jenkins REST API : Job create success!");

    	                //최상위일 경우에만 DB에 저장
    	                if(topFlag) {
    	                	insertJenkinsJobList.get(i).setCreatedBy(loginId);
        	                insertJenkinsJobList.get(i).setLastModifiedBy(loginId);
        	                insertJenkinsJobList.get(i).setProjectId(projectId);
    	                	newJenkinsMapper.insertJenkins(insertJenkinsJobList.get(i));	
    	                }
    	                
    	                result.put("status", DbMessageManager.getMessage("I0001", "ko"));
    	                result.put("msg", "");
    	                
    	            } else {
    	                LOGGER.error("Jenkins REST API is fail, Return code : " + responseResult.getStatusCodeValue());
    	                throw new ApiException(responseResult.getStatusCode(), "E0015");
    	            }

    	        } catch(IOException ie) {
    	            // 파일 읽기 에러
    	            throw new ApiException("E0017", "Jenkins REST API error : " + "Job config File");
    	        } catch(HttpClientErrorException ce) {
    	            throw HttpErrorExceptionDetail.getApiException(ce, "Jenkins REST API error", tempUri);
    	        } catch (HttpServerErrorException se) {
    	            throw HttpErrorExceptionDetail.getApiException(se, "Jenkins REST API error", tempUri);
    	        } 
    		}
    		
    	}

        return result;
        
    }
    
    /**
     * jenkins 폴더, job 수정
     * @param newUpdateProjectVO
     * @return boolean
     */
    public boolean doJenkinsUpdate(NewUpdateProjectVO newUpdateProjectVO) {
	    boolean isSuccess = false;
		//String loginId = insertProjectVo.getCreatedBy();
	    String loginId = keyCloakSession.getLoginId();
	    long projectId = newUpdateProjectVO.getId();
		NewUpdateJenkinsVO jenkins = newUpdateProjectVO.getJenkins();
		
		List<NewUpdateJenkinsVO> updateJenkinsFolderList = jenkins.getFolders();
		List<NewUpdateJenkinsVO> updateJenkinsJobList = jenkins.getJobs();
		String uri = jenkinsUrl;
		updateJenkinsFolder(updateJenkinsFolderList,uri , projectId, true);
		updateJenkinsJob(updateJenkinsJobList,uri , projectId, true);
		
		isSuccess = true;
		return isSuccess;
	}
    
    
    
    /**
	 * Jenkins Folder 생성 - jenkinsUpdate 에서 사용함
	 * @param updateJenkinsFolderList folder 목록
	 * @param uri 생성할 경로
	 * @param projectId 포탈 project id
	 * @param topFlag 최상위 구분 여부 true :  최상위 
	 * @return Map<String, String>
	 */
    public Map<String, String> updateJenkinsFolder(List<NewUpdateJenkinsVO> updateJenkinsFolderList,String uri , long projectId , boolean topFlag) {
    	String templateJobName ="";
    	String loginId = keyCloakSession.getLoginId();
    	Map<String, String> result = new HashMap<String, String>();
    	
    	if(updateJenkinsFolderList.size()>0) {
    		for(int i=0;i<updateJenkinsFolderList.size();i++) {
    			
    			//이미생성된 jenkins의 경우 url이 존재함
    			if(!"".equals(updateJenkinsFolderList.get(i).getUrl()) && updateJenkinsFolderList.get(i).getUrl()!=null) {
    				
    			}else {
    				
        		    //폴더 생성
        			//String uri = jenkinsUrl + "/createItem?name=" + folderName +"&mode=com.cloudbees.hudson.plugins.folder.Folder";
        			String tempUri = uri+"/createItem?name=" + updateJenkinsFolderList.get(i).getJenkinsFolderName() + "&mode=com.cloudbees.hudson.plugins.folder.Folder";
        			
        			try {
        	            ResponseEntity<String> responseResult = callJenkinsRestApi("POST", tempUri, null);
        	            
        	            if (responseResult.getStatusCode() == HttpStatus.OK) {
        	                LOGGER.info("Jenkins REST API : Folder create success! , HttpStatus.OK");
        	                result.put("status", DbMessageManager.getMessage("I0001", "ko"));
        	                result.put("msg", "");
        	                //최상위일 경우에만 DB에 저장
        	                if(topFlag) {
        	                	NewInsertJenkinsVO newInsertJenkinsVO = new NewInsertJenkinsVO();
        	                	newInsertJenkinsVO.setCreatedBy(loginId);
        	                	newInsertJenkinsVO.setLastModifiedBy(loginId);
        	                	newInsertJenkinsVO.setProjectId(projectId);
        	                	newInsertJenkinsVO.setDivision(updateJenkinsFolderList.get(i).getDivision());
        	                	newInsertJenkinsVO.setJenkinsFolderName(updateJenkinsFolderList.get(i).getJenkinsFolderName());
        	                	newJenkinsMapper.insertJenkins(newInsertJenkinsVO);	
        	                }
        	            } else {
        	                // ssl 오류에 대해서는 무시한다 (정상으로 처리)
        	                if (responseResult.getStatusCode() == HttpStatus.FOUND) {
        	                	LOGGER.info("Jenkins REST API : Folder create success! , HttpStatus.FOUND");
        	                    result.put("status", DbMessageManager.getMessage("I0001", "ko"));
        	                    result.put("msg", "");
            	                //최상위일 경우에만 DB에 저장
            	                if(topFlag) {
            	                	NewInsertJenkinsVO newInsertJenkinsVO = new NewInsertJenkinsVO();
            	                	newInsertJenkinsVO.setCreatedBy(loginId);
            	                	newInsertJenkinsVO.setLastModifiedBy(loginId);
            	                	newInsertJenkinsVO.setProjectId(projectId);
            	                	newInsertJenkinsVO.setDivision(updateJenkinsFolderList.get(i).getDivision());
            	                	newInsertJenkinsVO.setJenkinsFolderName(updateJenkinsFolderList.get(i).getJenkinsFolderName());
            	                	
            	                	newJenkinsMapper.insertJenkins(newInsertJenkinsVO);	
            	                }
        	                } else {
        	                    LOGGER.error("Jenkins REST API is fail, Return code : " + responseResult.getStatusCodeValue());
        	                    throw new ApiException(responseResult.getStatusCode(), "E0015");
        	                }
        	            }

        	        } catch(HttpClientErrorException ce) {
        	        	ce.printStackTrace();
        	            throw HttpErrorExceptionDetail.getApiException(ce, "Jenkins REST API error", uri);
        	        } catch (HttpServerErrorException se) {
        	            throw HttpErrorExceptionDetail.getApiException(se, "Jenkins REST API error", uri);
        	        }
    			}

    			if(updateJenkinsFolderList.get(i).getJobs().size()>0) {
    				String createJobUri = uri + "/job/" + updateJenkinsFolderList.get(i).getJenkinsFolderName() +"/";
    				
    				updateJenkinsJob(updateJenkinsFolderList.get(i).getJobs() , createJobUri , projectId ,false);
    			}
    			if(updateJenkinsFolderList.get(i).getFolders().size()>0) {
    				String createFolderUri = uri + "/job/" + updateJenkinsFolderList.get(i).getJenkinsFolderName() +"/";
    				updateJenkinsFolder(updateJenkinsFolderList.get(i).getFolders() , createFolderUri , projectId , false);
    			}
    			
    			
    			
    		}
    		
    	}
    	
    	return result;
    }
    
	/**
	 * Jenkins job 생성 및 수정 - jenkinsUpdate 에서 사용함
	 * @param updateJenkinsJobList job목록
	 * @param uri 생성할 경로
	 * @param projectId 포탈 project id
	 * @param topFlag 최상위 구분 여부 true :  최상위 
	 * @return Map<String, String>
	 */
    public Map<String, String> updateJenkinsJob(List<NewUpdateJenkinsVO> updateJenkinsJobList,String uri, long projectId , boolean topFlag) {
    	String templateJobName ="";
    	String loginId = keyCloakSession.getLoginId();
    	Map<String, String> result = new HashMap<String, String>();
    	String tempUri="";
    	if(updateJenkinsJobList.size()>0) {
    		for(int i=0;i<updateJenkinsJobList.size();i++) {
    			switch (updateJenkinsJobList.get(i).getJenkinsJobType()) {
    			case "10401": //FreeStyle Template Folder 내용 복사
    				templateJobName = "DSpace_FreeStyle_Template";
    				break;
    			case "10402":  //Pipeline Template Folder 내용 복사
    				templateJobName = "DSpace_Pipeline_Template";
    				break;
    			}
    			
    			//이미생성된 jenkins의 경우 url이 존재함
    			if(!"".equals(updateJenkinsJobList.get(i).getUrl()) && updateJenkinsJobList.get(i).getUrl()!=null) {
    				
    				//활성화
    				if(updateJenkinsJobList.get(i).getServiceStatus()==2) {
    					
    					//enableJenkinsJob(uri+"/job/"+updateJenkinsJobList.get(i).getJenkinsJobName());
    					enableAndDisableJenkinsJob(updateJenkinsJobList.get(i).getUrl(),updateJenkinsJobList.get(i).getServiceStatus());
    				//비활성화
    				}else if(updateJenkinsJobList.get(i).getServiceStatus()==1) {
    					
    					//disableJenkinsJob(uri+"/job/"+updateJenkinsJobList.get(i).getJenkinsJobName());
    					enableAndDisableJenkinsJob(updateJenkinsJobList.get(i).getUrl(),updateJenkinsJobList.get(i).getServiceStatus());
    				}
    				
    				
    			//신규생성
    			}else {
    				 //잡생성
        			//uri = jenkinsUrl + "/job/" + folderName +"/createItem?name=" + jobName + "&mode=copy&from=" + copyJobName;
        			tempUri = uri+"/createItem?name=" + updateJenkinsJobList.get(i).getJenkinsJobName() + "&mode=copy&from=" + templateJobName;
        			
        			try 
        	        {
        	            // resources의 jenkinsJobConfig.xml 파일을 읽어 온다
        	            String xmlStr = IOUtils.toString(getClass().getResourceAsStream("/xml/jenkens/jenkinsJobConfig.xml"), "UTF-8");
        	            ResponseEntity<String> responseResult = callJenkinsRestApiForXml("POST", tempUri, xmlStr);
        	            
        	            if (responseResult.getStatusCode() == HttpStatus.OK || responseResult.getStatusCode() == HttpStatus.FOUND) {
        	                LOGGER.info("Jenkins REST API : Job create success!");

        	                //최상위일 경우에만 DB에 저장
        	                if(topFlag) {
        	                	NewInsertJenkinsVO newInsertJenkinsVO = new NewInsertJenkinsVO();
        	                	newInsertJenkinsVO.setCreatedBy(loginId);
        	                	newInsertJenkinsVO.setLastModifiedBy(loginId);
        	                	newInsertJenkinsVO.setProjectId(projectId);
        	                	newInsertJenkinsVO.setDivision(updateJenkinsJobList.get(i).getDivision());
        	                	newInsertJenkinsVO.setJenkinsJobName(updateJenkinsJobList.get(i).getJenkinsJobName());
        	                	newInsertJenkinsVO.setJenkinsJobType(updateJenkinsJobList.get(i).getJenkinsJobType());
        	                	newJenkinsMapper.insertJenkins(newInsertJenkinsVO);	
        	                }
        	                
        	                result.put("status", DbMessageManager.getMessage("I0001", "ko"));
        	                result.put("msg", "");
        	                
        	            } else {
        	                LOGGER.error("Jenkins REST API is fail, Return code : " + responseResult.getStatusCodeValue());
        	                throw new ApiException(responseResult.getStatusCode(), "E0015");
        	            }

        	        } catch(IOException ie) {
        	            // 파일 읽기 에러
        	            throw new ApiException("E0017", "Jenkins REST API error : " + "Job config File");
        	        } catch(HttpClientErrorException ce) {
        	            throw HttpErrorExceptionDetail.getApiException(ce, "Jenkins REST API error", tempUri);
        	        } catch (HttpServerErrorException se) {
        	            throw HttpErrorExceptionDetail.getApiException(se, "Jenkins REST API error", tempUri);
        	        } 
    			}
    			
    			
    		   
    		}
    		
    	}

        return result;
    }
    
    
    /**
     * Jenkins job 목록 전체 disable 처리를 위한 목록 조회
     * @param projectId
     * @return boolean
     */
    public boolean jenkinsDisableList(Long projectId) {
    	//아래의 메소드는 무조건 true가 리턴됨, 단방향 통신이기때문에 jenkins tool 에서 오류가 발생할 시 rollback 이 어렵기 때문에 무조건 true로 리턴
    	
    	//jenkins 전체 목록 조회
    	//List<NewJenkinsParamVO> jenkins = getJenkinsList(projectId);
    	NewJenkinsParamVO jenkins = getJenkinsList(projectId);
    	
    	if(jenkins.getFolders().size()>0) {
        	if(jenkinsDisable(jenkins.getFolders(),null)) {
        		NewUpdateJenkinsVO newUpdateJenkinsVO = new NewUpdateJenkinsVO();
        		newUpdateJenkinsVO.setLastModifiedBy(keyCloakSession.getLoginId());
        		newUpdateJenkinsVO.setServiceStatus(9);
        		
        		//DB에 저장되어 있는 최상단 목록의 serivce_status 를 9로 변경
        		for(NewJenkinsParamVO newJenkinsParamVO : jenkins.getFolders()) {
        			newUpdateJenkinsVO.setId(newJenkinsParamVO.getId());
        			
        			newJenkinsMapper.updateJenkins(newUpdateJenkinsVO);
        			LOGGER.info("ID : "+newJenkinsParamVO.getId());
        		}
        		
        	};
        	return true;
    	}
    	
    	if(jenkins.getJobs().size()>0) {
        	if(jenkinsDisable(jenkins.getJobs(),null)) {
        		NewUpdateJenkinsVO newUpdateJenkinsVO = new NewUpdateJenkinsVO();
        		newUpdateJenkinsVO.setLastModifiedBy(keyCloakSession.getLoginId());
        		newUpdateJenkinsVO.setServiceStatus(9);
        		
        		//DB에 저장되어 있는 최상단 목록의 serivce_status 를 9로 변경
        		for(NewJenkinsParamVO newJenkinsParamVO : jenkins.getJobs()) {
        			newUpdateJenkinsVO.setId(newJenkinsParamVO.getId());
        			
        			newJenkinsMapper.updateJenkins(newUpdateJenkinsVO);
        			LOGGER.info("ID : "+newJenkinsParamVO.getId());
        		}
        		
        	};
        	return true;
    	}
    	
    	 return true;
    }
    
    /**
     * Jenkins job 목록 전체를 disable 처리
     * @param jenkins url
     * @param url 
     * @return boolean
     */
    public boolean jenkinsDisable(List<NewJenkinsParamVO> jenkins , String url) {
    	if("".equals(url) || url==null) {
    		url = jenkinsUrl;
    	}
    	for(int i=0; i<jenkins.size();i++) {
    		//폴더
    		if(jenkins.get(i).getDivision()==1) {
    			if(jenkins.get(i).getFolders().size()>0) {
    				jenkinsDisable(jenkins.get(i).getFolders(),url + "/job"+jenkins.get(i).getJenkinsFolderName());	
    			}
    			if(jenkins.get(i).getJobs().size()>0) {
    				jenkinsDisable(jenkins.get(i).getJobs(),url + "/job"+jenkins.get(i).getJenkinsFolderName());
    			}
    			
    		//job
    		}else if(jenkins.get(i).getDivision()==2){
    			
    			//serviceStauts 를 2로 변경해서 보내면 전부다 복구됨
    			//1로 보낼경우 전부다 disable 처리함
    			
    			LOGGER.info("jenkinsDisable url : "+url);
    			enableAndDisableJenkinsJob(jenkins.get(i).getUrl(),1);
    		}
    	}
    	return true;
    }
    
    
    
    
    /**
     * Jenkins Job 비활성화 비활성화
     * @param folderName
     * @param jobName
     */
    public Map<String, String> enableAndDisableJenkinsJob(String uri , int status) {
        Map<String, String> result = new HashMap<String, String>();
        
        if(status==1) {
        	uri = uri+"/disable";	
        }else if(status==2) {
        	uri = uri+"/enable";
        }
        try {
            ResponseEntity<String> responseResult = callJenkinsRestApiForXml("POST", uri, null);
            
            LOGGER.info("Jenkins Job Disable : " + responseResult.getStatusCode());
            
            result.put("status", DbMessageManager.getMessage("I0001", "ko"));
            result.put("msg", "");
            return result;
            
        } catch(HttpClientErrorException ce) {
            throw HttpErrorExceptionDetail.getApiException(ce, "Jenkins REST API error", uri);
        } catch (HttpServerErrorException se) {
            throw HttpErrorExceptionDetail.getApiException(se, "Jenkins REST API error", uri);
        }
    }
    

    

}
