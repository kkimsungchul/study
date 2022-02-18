

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cd.common.ApiException;
import cd.common.ApiResponseEntity;
import cd.common.CommonUtil;
import cd.message.manage.DbMessageManager;
import cd.project.jenkins.vo.CheckFolderNameParamVO;
import cd.project.jenkins.vo.CreateJenkinsJobsVO;
import cd.project.jenkins.vo.CreateJenkinsVO;
import cd.project.jenkins.vo.FindJenkinsJobListVO;
import cd.project.jenkins.vo.FindJenkinsJobsVO;
import cd.project.jenkins.vo.FindJenkinsVO;
import cd.project.jenkins.vo.GetJenkinsJobBuildListParamVO;
import cd.project.jenkins.vo.JenkinsCdVO;
import cd.project.jenkins.vo.JenkinsJobCdVO;
import cd.project.jenkins.vo.JobBuildInfoVO;
import cd.project.jenkins.vo.LastBuildJobLogVO;
import cd.project.jenkins.vo.LastBuildJobVO;
import cd.project.jenkins.vo.ResultJenkinsJobCdVO;
import cd.project.jenkins.vo.UpdateJenkinsJobsVO;
import cd.project.jenkins.vo.UpdateJenkinsVO;
import cd.project.workflow.WorkflowMapper;
import cd.project.workflow.vo.WorkflowTaskVO;
import cd.project.workflow.vo.WorkflowVO;
import cd.role.RoleService;
import cd.role.enums.RoleAction;


@Service
public class NewJenkinsService {
    private static final Logger logger = LoggerFactory.getLogger(NewJenkinsService.class);

    @Autowired
    private NewJenkinsRestService newJenkinsRestService;

    @Autowired
    private WorkflowMapper workflowMapper;

    @Autowired
    private NewJenkinsMapper newJenkinsMapper;

//    @Autowired
//    private RoleService roleService;

    /**
     * 젠킨스 CI JOB 정규식 체크 : 한글 가능, 특수문자 `,.-_(){} 가능 varchar100
     * (다만 특수문자가 들어갈 시 호출 API 에서 오류 발생, API호출에서 사용할 수 있는 특수문자 -_().,)
     */
    private Pattern regexJenkinsPattern = Pattern.compile("^([A-Za-z0-9ㄱ-힣]{1})[a-zA-Z0-9ㄱ-힣-_(),.\\s]{0,84}$");

    
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
			if(newJenkinsMapper.insertValidJenkinsCheck(insertJenkinsFolderList.get(i))) {
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
			if(newJenkinsMapper.insertValidJenkinsCheck(insertJenkinsJobList.get(i))) {
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

		//List<NewInsertJenkinsVO> insertJenkinsFolderList = insertJenkinsFolderList
		for(int i=0; i<insertJenkinsFolderList.size();i++) {
			
			if(paramURL==null) {
				url = newJenkinsRestService.getJenkinsUrl(insertJenkinsFolderList.get(i).getServerTypeCode(),insertJenkinsFolderList.get(i).getServerZoneCode());
			}
			
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
		        
		        if(existFolderOrJob(insertJenkinsFolderList.get(i).getServerTypeCode() , insertJenkinsFolderList.get(i).getServerZoneCode(),url, insertJenkinsFolderList.get(i).getJenkinsFolderName())) {
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
				if(existFolderOrJob(insertJenkinsFolderList.get(i).getServerTypeCode() , insertJenkinsFolderList.get(i).getServerZoneCode(), url, insertJenkinsFolderList.get(i).getJenkinsJobName())) {
					result.put("msg", DbMessageManager.getMessage("E0016", "Jenkins job명", "ko"));
					return result;	
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
    public boolean existFolderOrJob(String serverTypeCode , String serverZoneCode , String uri,String name) {
        uri = uri +  "/job/" + name + "/api/json";
        logger.info("existFolderOrJob :" +  uri);
        try {
        	//    public ResponseEntity<String> callJenkinsRestApiForJson(String serverTypeCode, String serverZoneCode, String method, String uri, String params) {
            ResponseEntity<String> responseResult = newJenkinsRestService.callJenkinsRestApiForJson(serverTypeCode, serverZoneCode, "GET", uri, null);

            if(responseResult.getStatusCode() == HttpStatus.OK) {
            	logger.info("Jenkins REST API : Get jenkins informatin is found!");
            } else {
                if(responseResult.getStatusCode() == HttpStatus.NOT_FOUND) {
                	logger.info("Jenkins REST API : Get jenkins informatin is not found!");
                    return false;
                } else {
                	logger.error("Jenkins REST API is fail, Return code : " + responseResult.getStatusCodeValue());
                }
            }

        } catch(HttpClientErrorException ce) {
            HttpStatus status = ce.getStatusCode();
            if(status == HttpStatus.NOT_FOUND) {
            	logger.info("Jenkins REST API : Get jenkins informatin is not found!");
                return false;
            } else {
            	logger.error("Jenkins REST API error: {}", ce.getMessage());
            }
        } catch(HttpServerErrorException se) {
        	logger.error("Jenkins REST API error: {}", se.getMessage());
        }
        
        
    	return true;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    /**
     * Jenkins Job 생성 전 validation check
     * 
     * @param createJobValidation
     */
    public void createJobValidation(CreateJenkinsJobsVO createJenkinsJobsVO)
    {
        if(createJenkinsJobsVO == null)
        {
            //E0009 - 프로젝트 등록 정보는 필수입니다
            throw new ApiException(HttpStatus.OK, "E0009", "프로젝트 등록 정보");
        }

        // 프로젝트 생성 권한 체크
//        roleCheck(insertProjectVo.getProjectGroupId(), null, getLoginId(), RoleAction.CREATE);

        // trim 체크
        CommonUtil.trimForVOStringFields(createJenkinsJobsVO);

        List<CreateJenkinsVO> jenkins = createJenkinsJobsVO.getJenkins();
        if(jenkins != null)
        {
            int jenkinsCount = jenkins.size();
            for(int i=0;i<jenkinsCount;i++)
            {
                CreateJenkinsVO ijv = jenkins.get(i);
                CommonUtil.trimForVOStringFields(ijv);

                // Jenkins Check
                String jenkinsFolderName = ijv.getJenkinsFolderName();
                String serverTypeCode = ijv.getServerTypeCode();
                String serverZoneCode = ijv.getServerZoneCode();

                List<JenkinsJobCdVO> jenkinsJobCdList = ijv.getJobs();

                if(jenkinsFolderName == null || "".equals(jenkinsFolderName))
                {
                    //E0009 - Jenkins 폴더명은(는) 필수값입니다.
                    throw new ApiException(HttpStatus.OK, "E0009", "Jenkins 폴더명");
                }else if(jenkinsFolderName != null)
                {
                    /*
                     * 젠킨스 CI JOB 정규식 체크 : 한글 가능, 특수문자 `,.-_(){} 가능 varchar100
                     * (다만 특수문자가 들어갈 시 호출 API 에서 오류 발생, API호출에서 사용할 수 있는 특수문자 -_().,)
                     */
                    Matcher match = regexJenkinsPattern.matcher(jenkinsFolderName);

                    if(!match.find())
                    {
                        throw new ApiException(HttpStatus.OK, "E0125", new Object[]{"Jenkins 폴더명", "특수문자로 시작할 수 없으며 한글,영문대소문자,숫자,특수문자 -_().,","1","85"});
                    }

                    checkJenkinsFolderNameDuplicate(serverTypeCode, serverZoneCode, jenkinsFolderName);
                }

                if(jenkinsJobCdList != null && jenkinsJobCdList.size() > 0)
                {
                    for(JenkinsJobCdVO jobInfo : jenkinsJobCdList)
                    {
                        String jenkinsJobName = jobInfo.getJenkinsJobName();
                        Long jenkinsJobCdId = jobInfo.getJenkinsJobCdId();

                        if(jenkinsJobCdId == null && (jenkinsJobName == null || "".equals(jenkinsJobName)))
                        {
                            //E0009 - Jenkins Job 이름은(는) 필수값입니다.
                            throw new ApiException(HttpStatus.OK, "E0009", "Jenkins Job 이름");
                        }else if(jenkinsJobCdId == null)
                        {
                            Matcher match = regexJenkinsPattern.matcher(jenkinsJobName);

                            if(!match.find())
                            {
                                throw new ApiException(HttpStatus.OK, "E0125", new Object[]{"Jenkins job명", "특수문자로 시작할 수 없으며 한글,영문대소문자,숫자,특수문자 -_().,","1","85"});
                            }

                            //신규 입력한 Job명이 기존 Job명과 중복되는지 체크
                            checkJenkinsJobNameDuplicate(serverTypeCode, serverZoneCode, jenkinsFolderName, jenkinsJobName);
                        }

                        jobInfo.setJenkinsJobName(CommonUtil.trim(jenkinsJobName));
                    }
                }else
                {
                    //E0009 - Jenkins 폴더명는 필수입니다
                    throw new ApiException(HttpStatus.OK, "E0009", "Job 이름");
                }
            }
        }
    }

    /**
     * Jenkins Job 수정 후 validation check
     * 
     * @param updateJobValidation
     */
    public void updateJobValidation(UpdateJenkinsJobsVO updateJenkinsJobsVO)
    {
        if(updateJenkinsJobsVO == null)
        {
            //E0009 - 프로젝트 등록 정보는 필수입니다
            throw new ApiException(HttpStatus.OK, "E0009", "프로젝트 등록 정보");
        }

        // 프로젝트 생성 권한 체크
//        roleCheck(insertProjectVo.getProjectGroupId(), null, getLoginId(), RoleAction.CREATE);

        // trim 체크
        CommonUtil.trimForVOStringFields(updateJenkinsJobsVO);

        //Long projectId = updateJenkinsJobsVO.getProjectId();

        List<UpdateJenkinsVO> jenkins = updateJenkinsJobsVO.getJenkins();
        if(jenkins != null)
        {
            int jenkinsCount = jenkins.size();
            for(int i=0;i<jenkinsCount;i++)
            {
                UpdateJenkinsVO ujk = jenkins.get(i);
                CommonUtil.trimForVOStringFields(ujk);

                // Jenkins Check
//                Long jenkinsCdId = ujk.getJenkinsCdId();
                Long jenkinsCdId = ujk.getJenkinsCdId();
                String jenkinsFolderName = ujk.getJenkinsFolderName();
                String serverTypeCode = ujk.getServerTypeCode();
                String serverZoneCode = ujk.getServerZoneCode();

                List<JenkinsJobCdVO> jenkinsJobCdList = ujk.getJobs();

                if(jenkinsFolderName == null || "".equals(jenkinsFolderName))
                {
                    //E0009 - Jenkins 폴더명은(는) 필수값입니다.
                    throw new ApiException(HttpStatus.OK, "E0009", "Jenkins 폴더명");
                }else if(jenkinsCdId == null)
                {
                    /*
                     * 젠킨스 CI JOB 정규식 체크 : 한글 가능, 특수문자 `,.-_(){} 가능 varchar100
                     * (다만 특수문자가 들어갈 시 호출 API 에서 오류 발생, API호출에서 사용할 수 있는 특수문자 -_().,)
                     */
                    Matcher match = regexJenkinsPattern.matcher(jenkinsFolderName);

                    if(!match.find())
                    {
                        throw new ApiException(HttpStatus.OK, "E0125", new Object[]{"Jenkins 폴더명", "특수문자로 시작할 수 없으며 한글,영문대소문자,숫자,특수문자 -_().,","1","85"});
                    }

                    //Jenkins 폴더 신규 추가인 경우 중복 폴더명 체크
                    checkJenkinsFolderNameDuplicate(serverTypeCode, serverZoneCode, jenkinsFolderName);
                }

                if(jenkinsJobCdList != null && jenkinsJobCdList.size() > 0)
                {
                    for(JenkinsJobCdVO jobInfo : jenkinsJobCdList)
                    {
                        Long jenkinsJobCdId = jobInfo.getJenkinsJobCdId();
                        String jenkinsJobName = jobInfo.getJenkinsJobName();

                        
                        //수정에서 신규 Job 등록(jenkinsJobCdId=null)시 Job명 공백 체크
                        if(jenkinsJobCdId == null && (jenkinsJobName == null || "".equals(jenkinsJobName)))
                        {
                            //E0009 - Jenkins Job 이름은(는) 필수값입니다.
                            throw new ApiException(HttpStatus.OK, "E0009", "Jenkins Job 이름");
                        }else if(jenkinsJobCdId == null)
                        {
                            Matcher match = regexJenkinsPattern.matcher(jenkinsJobName);

                            if(!match.find())
                            {
                                throw new ApiException(HttpStatus.OK, "E0125", new Object[]{"Jenkins job명", "특수문자로 시작할 수 없으며 한글,영문대소문자,숫자,특수문자 -_().,","1","85"});
                            }

                            //신규 입력한 Job명이 기존 Job명과 중복되는지 체크
                            checkJenkinsJobNameDuplicate(serverTypeCode, serverZoneCode, jenkinsFolderName, jenkinsJobName);
                        }

                        jobInfo.setJenkinsJobName(CommonUtil.trim(jenkinsJobName));
                    }
                }else
                {
                    //E0009 - Jenkins 폴더명는 필수입니다
                    throw new ApiException(HttpStatus.OK, "E0009", "Job 이름");
                }
            }
        }
    }



    /**
     * Jenkins Job 생성(Async 처리)
     * @param createJenkinsJobsVO
     */
    @Async
    public void createJobsAsync(String loginId, Long workflowId, CreateJenkinsJobsVO createJenkinsJobsVO)
    {
        logger.debug("NewJenkinsService createJobsAsync workflowId="+workflowId+", createJobs call!!");
        boolean jenkinsSuccess = createJobs(loginId, workflowId, createJenkinsJobsVO);
        Long projectId = createJenkinsJobsVO.getProjectId();

        //상태코드 0: 신청완료, 1: 진행중, 2: 일부완료, 3: 완료, 9: 삭제
        if (isWorkflowSuccess(projectId))
        {
            updateWorkflowStatus(loginId, projectId, workflowId, 0);
        }else if(!jenkinsSuccess)
        {
            updateWorkflowStatus(loginId, projectId, workflowId, 2);
        }else
        {
            // 모든 Tool 다 실패시, 프로젝트생성 실패 '3' 로 update
            updateWorkflowStatus(loginId, projectId, workflowId, 3);
        }

    }

    /**
     * Jenkins Job 생성2(Async 처리)
     * @param createJenkinsJobsVO
     */
    @Async
    public void createJobsAsync2(String loginId, CreateJenkinsJobsVO createJenkinsJobsVO)
    {
        logger.debug("NewJenkinsService createJobsAsync2 createJobs call!!");
        boolean jenkinsSuccess = createJobs2(loginId, createJenkinsJobsVO);
        logger.debug("NewJenkinsService createJobsAsync2 jenkinsSuccess="+jenkinsSuccess);
    }

    /**
     * 현재 Workflow Status 확인
     *   !"2": 일부완료가 아니면 => true
     *   "2": 일부완료이면 그대로 유지 => false
     * 중간에 에러난 task가 없으면 정상 완료로 업데이트하기 위함
     * 중간에 에러난 task가 있으면 "2"(일부완료) 로 유지
     * 
     * @param id
     * @return
     */
    private boolean isWorkflowSuccess(Long id) {
        WorkflowVO vo = new WorkflowVO();
        vo.setProjectId(id);
        WorkflowVO workflow = workflowMapper.findWorkflow(vo);

        int status = workflow.getServiceStatus();
        if (status != 2) {
            return true;
        } else {
            return false;
        }
    }

    private WorkflowVO updateWorkflowStatus(String loginId, Long projectId, Long workflowId , Integer status) {
        WorkflowVO vo = new WorkflowVO();
        vo.setId(workflowId);
        vo.setProjectId(projectId);
        vo.setServiceStatus(status);
        vo.setLastModifiedBy(loginId);
        workflowMapper.updateWorkflowStatus(vo);

        return vo;
    }

    private boolean createJobs(String loginId, Long workflowId, CreateJenkinsJobsVO createJenkinsJobsVO)
    {
        boolean isSuccess = false;

        Long projectId = createJenkinsJobsVO.getProjectId();
        List<CreateJenkinsVO> jenkinsList = createJenkinsJobsVO.getJenkins();

        logger.debug("NewJenkinsService createJobs projectId="+projectId+", workflowId="+workflowId+", jenkinsList.size()="+(jenkinsList != null ? jenkinsList.size() : 0));
        if(jenkinsList != null)
        {
            logger.debug("NewJenkinsService createJobs jenkinsList != null getLoginId() loginId="+loginId);

            ObjectMapper objectMapper = new ObjectMapper();
            String createJenkinsJobsJson = null;
            try
            {
                createJenkinsJobsJson = objectMapper.writeValueAsString(createJenkinsJobsVO);
                logger.debug("NewJenkinsService createJobs projectId="+projectId+", workflowId="+workflowId+", createJenkinsJobsJson="+createJenkinsJobsJson);
            }catch(JsonProcessingException e1)
            {
                createJenkinsJobsJson = "";
            }

            int jenkinsListCount = jenkinsList.size();
            for(int i=0;i<jenkinsListCount;i++)
            {
                CreateJenkinsVO ijv = jenkinsList.get(i);
                String jenkinsFolderName = ijv.getJenkinsFolderName();
                String serverTypeCode = ijv.getServerTypeCode();
                String serverZoneCode = ijv.getServerZoneCode();

                logger.info("NewJenkinsService createJobs jenkinsFolderName="+jenkinsFolderName);
                if(jenkinsFolderName != null && !jenkinsFolderName.isEmpty())
                {
                    logger.info("NewJenkinsService createJobs insertWorkflowTask call !!");
                    Long workflowTaskId = insertWorkflowTask(loginId, workflowId, "JENKINS_CD", createJenkinsJobsJson);
                    Long jenkinsCdId = null;

                    JenkinsCdVO vo = new JenkinsCdVO();
                    vo.setCreatedBy(loginId);
                    vo.setLastModifiedBy(loginId);

                    try
                    {
                        logger.info("NewJenkinsService createJobs newJenkinsRestService.createJenkinsFolder serverTypeCode="+serverTypeCode+", serverZoneCode="+serverZoneCode+", jenkinsFolderName="+jenkinsFolderName);
                        // 젠킨스 폴더 생성
                        newJenkinsRestService.createJenkinsFolder(serverTypeCode, serverZoneCode, jenkinsFolderName);

                        // 젠킨스 폴더 정보 DB 저장
                        vo.setProjectId(projectId);
                        vo.setJenkinsFolderName(jenkinsFolderName);
                        vo.setServerTypeCode(serverTypeCode);
                        vo.setServerZoneCode(serverZoneCode);

                        logger.info("NewJenkinsService createJobs newJenkinsMapper.insertJenkins vo="+vo);
                        newJenkinsMapper.insertJenkins(vo);

                        jenkinsCdId = vo.getId();

                    }catch(Exception e)
                    {
                        updateWorkflowTaskFail(loginId, workflowTaskId, e.getMessage());
                        return isSuccess;
                    }

                    List<JenkinsJobCdVO> jobList = ijv.getJobs();

                    logger.info("NewJenkinsService createJobs jobList.size()="+jobList.size());

                    for (JenkinsJobCdVO job : jobList)
                    {
                        job.setJenkinsCdId(jenkinsCdId);    // 발번된 jenkins_cd.id
                        job.setCreatedBy(loginId);
                        job.setLastModifiedBy(loginId);

                        String jenkinsJobName = job.getJenkinsJobName();
                        String jenkinsJobTypeCode =job.getJenkinsJobTypeCode();
                        String templateJobName = null;

                        try
                        {
                            //FreeStyle Template Folder 내용 복사
                            if("10301".equals(jenkinsJobTypeCode))
                            {
                                templateJobName = "DSpace_FreeStyle_Template";
                            }
                            //Pipeline Template Folder 내용 복사
                            else if("10302".equals(jenkinsJobTypeCode))
                            {
                                templateJobName = "DSpace_Pipeline_Template";
                            }

                            logger.info("NewJenkinsService createJobs newJenkinsRestService.createJenkinsJobFromOtherJob serverTypeCode="+serverTypeCode+", serverZoneCode="+serverZoneCode+", jenkinsFolderName="+jenkinsFolderName+", templateFolderName="+templateJobName);
                            // 젠킨스 job 생성(Template Folder 내용 복사 방식으로 Job 생성
                            newJenkinsRestService.createJenkinsJobFromOtherJob(serverTypeCode, serverZoneCode, jenkinsFolderName, jenkinsJobName, templateJobName);

                            logger.info("NewJenkinsService createJobs newJenkinsMapper.insertJenkinsJob job="+job);
                            // 젠킨스 job 정보 DB 저장
                            newJenkinsMapper.insertJenkinsJob(job);
                        }catch(Exception e)
                        {
                            logger.error("NewJenkinsService createJobs Error=", e);
                            updateWorkflowTaskFail(loginId, workflowTaskId, e.getMessage());
                            return isSuccess;
                        }
                    }

                    updateWorkflowTaskSuccess(loginId, workflowTaskId);
                }else
                {
                    logger.info("No Insert Jenkins Data.");
                }
            }

            isSuccess = true;
        }else
        {
            isSuccess = false;
        }

        return isSuccess;
    }

    private boolean createJobs2(String loginId, CreateJenkinsJobsVO createJenkinsJobsVO)
    {
        boolean isSuccess = false;

        Long projectId = createJenkinsJobsVO.getProjectId();
        List<CreateJenkinsVO> jenkinsList = createJenkinsJobsVO.getJenkins();

        logger.debug("NewJenkinsService createJobs2 projectId="+projectId+", jenkinsList.size()="+(jenkinsList != null ? jenkinsList.size() : 0));
        if(jenkinsList != null)
        {
            logger.debug("NewJenkinsService createJobs jenkinsList != null getLoginId() loginId="+loginId);

            ObjectMapper objectMapper = new ObjectMapper();
            String createJenkinsJobsJson = null;
            try
            {
                createJenkinsJobsJson = objectMapper.writeValueAsString(createJenkinsJobsVO);
                logger.debug("NewJenkinsService createJobs projectId="+projectId+", createJenkinsJobsJson="+createJenkinsJobsJson);
            }catch(JsonProcessingException e1)
            {
                createJenkinsJobsJson = "";
            }

            int jenkinsListCount = jenkinsList.size();
            for(int i=0;i<jenkinsListCount;i++)
            {
                CreateJenkinsVO ijv = jenkinsList.get(i);
                String jenkinsFolderName = ijv.getJenkinsFolderName();
                String serverTypeCode = ijv.getServerTypeCode();
                String serverZoneCode = ijv.getServerZoneCode();

                logger.info("NewJenkinsService createJobs jenkinsFolderName="+jenkinsFolderName);
                if(jenkinsFolderName != null && !jenkinsFolderName.isEmpty())
                {
                    logger.info("NewJenkinsService createJobs insertWorkflowTask call !!");
                    Long jenkinsCdId = null;

                    JenkinsCdVO vo = new JenkinsCdVO();
                    vo.setCreatedBy(loginId);
                    vo.setLastModifiedBy(loginId);

                    try
                    {
                        logger.info("NewJenkinsService createJobs newJenkinsRestService.createJenkinsFolder serverTypeCode="+serverTypeCode+", serverZoneCode="+serverZoneCode+", jenkinsFolderName="+jenkinsFolderName);
                        // 젠킨스 폴더 생성
                        newJenkinsRestService.createJenkinsFolder(serverTypeCode, serverZoneCode, jenkinsFolderName);

                        // 젠킨스 폴더 정보 DB 저장
                        vo.setProjectId(projectId);
                        vo.setJenkinsFolderName(jenkinsFolderName);
                        vo.setServerTypeCode(serverTypeCode);
                        vo.setServerZoneCode(serverZoneCode);

                        logger.info("NewJenkinsService createJobs newJenkinsMapper.insertJenkins vo="+vo);
                        newJenkinsMapper.insertJenkins(vo);

                        jenkinsCdId = vo.getId();

                    }catch(Exception e)
                    {
                        return isSuccess;
                    }

                    List<JenkinsJobCdVO> jobList = ijv.getJobs();

                    logger.info("NewJenkinsService createJobs jobList.size()="+jobList.size());

                    for (JenkinsJobCdVO job : jobList)
                    {
                        job.setJenkinsCdId(jenkinsCdId);    // 발번된 jenkins_cd.id
                        job.setCreatedBy(loginId);
                        job.setLastModifiedBy(loginId);

                        String jenkinsJobName = job.getJenkinsJobName();
                        String jenkinsJobTypeCode =job.getJenkinsJobTypeCode();
                        String templateJobName = null;

                        try
                        {
                            //FreeStyle Template Folder 내용 복사
                            if("10301".equals(jenkinsJobTypeCode))
                            {
                                templateJobName = "DSpace_FreeStyle_Template";
                            }
                            //Pipeline Template Folder 내용 복사
                            else if("10302".equals(jenkinsJobTypeCode))
                            {
                                templateJobName = "DSpace_Pipeline_Template";
                            }

                            logger.info("NewJenkinsService createJobs newJenkinsRestService.createJenkinsJobFromOtherJob serverTypeCode="+serverTypeCode+", serverZoneCode="+serverZoneCode+", jenkinsFolderName="+jenkinsFolderName+", templateFolderName="+templateJobName);
                            // 젠킨스 job 생성(Template Folder 내용 복사 방식으로 Job 생성
                            newJenkinsRestService.createJenkinsJobFromOtherJob(serverTypeCode, serverZoneCode, jenkinsFolderName, jenkinsJobName, templateJobName);

                            logger.info("NewJenkinsService createJobs newJenkinsMapper.insertJenkinsJob job="+job);
                            // 젠킨스 job 정보 DB 저장
                            newJenkinsMapper.insertJenkinsJob(job);
                        }catch(Exception e)
                        {
                            logger.error("NewJenkinsService createJobs Error=", e);
                            return isSuccess;
                        }
                    }
                }else
                {
                    logger.info("No Insert Jenkins Data.");
                }
            }

            isSuccess = true;
        }else
        {
            isSuccess = false;
        }

        return isSuccess;
    }

    private Long insertWorkflowTask(String loginId, Long workflowId, String typeName, String inputParams)
    {
        WorkflowTaskVO workflowTask = new WorkflowTaskVO();
        workflowTask.setWorkflowId(workflowId);
        workflowTask.setTypeId(findWorkflowTaskType(typeName));
        workflowTask.setInputParams(inputParams);
        workflowTask.setCreatedBy(loginId);
        workflowTask.setLastModifiedBy(loginId);
        workflowMapper.insertWorkflowTask(workflowTask);

        return workflowTask.getId();
    }

    private Integer findWorkflowTaskType(String typeName) {
        return workflowMapper.findWorkflowTaskType(typeName);
    }

    private void updateWorkflowTask(String loginId, long workflowTaskId, Integer status, String errMsg)
    {
        WorkflowTaskVO workflowTask = new WorkflowTaskVO();
        workflowTask.setId(workflowTaskId);
        workflowTask.setServiceStatus(status);
        workflowTask.setTaskErrMsg(errMsg);
        workflowTask.setLastModifiedBy(loginId);

        workflowMapper.updateWorkflowTask(workflowTask);
    }

    /**
     * Jenkins 조회
     * @param id
     * @return
     */
//    private CreateJenkinsVO getJenkinsByProjectId(long id) {
//        JenkinsCdVO vo = new JenkinsCdVO();
//        vo.setProjectId(id);
//        List<JenkinsCdVO> jenkins = newJenkinsMapper.findJenkins(vo);
//        
//        CreateJenkinsVO resultVo = new CreateJenkinsVO();
//
//        if(jenkins!= null && jenkins.size() > 0)
//        {
//            resultVo.setJenkinsFolderName(jenkins.get(0).getJenkinsFolderName());
//            
//            List<JenkinsJobCdVO> jobNms = new ArrayList<JenkinsJobCdVO>();
//
//            for(JenkinsCdVO jenkinsInfo : jenkins)
//            {
//                if(jenkinsInfo.getJenkinsJobName() != null)
//                {
//                    JenkinsJobCdVO jjcv = new JenkinsJobCdVO();
//
////                    jobNms.add(jenkinsInfo.getJenkinsJobName());
//                }
//            }
//
//            resultVo.setJobs(jobNms);
//        }
//        
//        
//        return resultVo;
//    }

    /**
     * Jenkins Job 정보 수정 Async로 진행
     * @param createJenkinsJobsVO
     */
    @Async
    public void updateJobsAsync(String loginId, Long workflowId, UpdateJenkinsJobsVO updateJenkinsJobsVO)
    {
        boolean jenkinsSuccess = updateJobs(loginId, workflowId, updateJenkinsJobsVO);
        Long projectId = updateJenkinsJobsVO.getProjectId();

        if(!jenkinsSuccess)
        {
            updateWorkflowStatus(loginId, projectId, workflowId, 2);
        }
        else
        {
            updateWorkflowStatus(loginId, projectId, workflowId, 0);
        }
    }

    /**
     * Jenkins Job 정보 수정 Async로 진행
     * @param createJenkinsJobsVO
     */
    @Async
    public void updateJobsAsync2(String loginId, UpdateJenkinsJobsVO updateJenkinsJobsVO)
    {
        updateJobs2(loginId, updateJenkinsJobsVO);
    }

    /**
     * Jenkins Job 수정
     * 
     * UI상 Jenkins 구조 및 입력값 제한사항.
     * 1.특정 Jenkins 서버당 생성할 수 있는 폴더는 하나임.
     * 2.한번 생성한 폴더와 하위 Job 이름은 변경되지 않는다.
     * 
     * @param updateProjectVo
     * @param workflowId
     * @return 
     */
    private boolean updateJobs(String loginId, Long workflowId, UpdateJenkinsJobsVO updateJenkinsJobsVO)
    {
        List<UpdateJenkinsVO> jenkinsList = updateJenkinsJobsVO.getJenkins();
        Long projectId = updateJenkinsJobsVO.getProjectId();

        if(jenkinsList != null)
        {
            ObjectMapper objectMapper = new ObjectMapper();
            String updateJenkinsJobsJson = null;
            try
            {
                updateJenkinsJobsJson = objectMapper.writeValueAsString(updateJenkinsJobsVO);
            }catch(JsonProcessingException e1)
            {
                updateJenkinsJobsJson = "";
            }

            int jenkinsListCount = jenkinsList.size();

            logger.info("NewJenkinsService updateJobs jenkinsListCount="+jenkinsListCount);

            Long workflowTaskId = null;
            WorkflowTaskVO workflowTask = null;

            WorkflowVO workflowVo = new WorkflowVO();
            workflowVo.setProjectId(projectId);
            workflowVo.setTypeId(findWorkflowTaskType("JENKINS_CD"));

            // 기존 WorkFlow Task 조회
            workflowTask = workflowMapper.findWorkflowAndWorkflowTask(workflowVo);

            if(workflowTask == null)
            {
                // 기존 WorkFlow Task가 없으면 WorkFlow Task 생성
                workflowTaskId = insertWorkflowTask(loginId, workflowId, "JENKINS_CD", updateJenkinsJobsJson);
            }else
            {
                // 기존 WorkFlow Task ID 재사용
                workflowTaskId = workflowTask.getId();
            }

            for(int i=0;i<jenkinsListCount;i++)
            {
                UpdateJenkinsVO ujk = jenkinsList.get(i);
                Long jenkinsCdId = ujk.getJenkinsCdId();
                String jenkinsFolderName = ujk.getJenkinsFolderName();
                String serverTypeCode = ujk.getServerTypeCode();
                String serverZoneCode = ujk.getServerZoneCode();

                List<JenkinsJobCdVO> jobs = ujk.getJobs();

                if(jenkinsFolderName != null && !"".equals(jenkinsFolderName))
                {
                    // 신규 폴더 생성 및 폴더내 소속 Job 신규 생성
                    if(jenkinsCdId == null)
                    {
                        // 신규 폴더 생성
                        try
                        {
                            newJenkinsRestService.createJenkinsFolder(serverTypeCode, serverZoneCode, jenkinsFolderName);

                            JenkinsCdVO newJenkins = new JenkinsCdVO();
                            newJenkins.setProjectId(projectId);
                            newJenkins.setJenkinsFolderName(jenkinsFolderName);
                            newJenkins.setServerTypeCode(serverTypeCode);
                            newJenkins.setServerZoneCode(serverZoneCode);
                            newJenkins.setCreatedBy(loginId);
                            newJenkins.setLastModifiedBy(loginId);

                            newJenkinsMapper.insertJenkins(newJenkins);

                            jenkinsCdId = newJenkins.getId();
                            logger.info("Jenkins Folder Create & DB Insert Succeed : jenkinsCdId={}", jenkinsCdId);
                        }catch(Exception e)
                        {
                            updateWorkflowTaskFail(loginId, workflowTaskId, e.getMessage());
                            return false;
                        }

                        // 신규 폴더 소속에 신규 Job 생성
                        int jobsCount = jobs.size();
                        for(int j=0;j<jobsCount;j++)
                        {
                            JenkinsJobCdVO job = jobs.get(j);
                            String jenkinsJobName = job.getJenkinsJobName();
                            String jenkinsJobTypeCode = job.getJenkinsJobTypeCode();
                            String templateJobName = null;

                            try
                            {
                                //FreeStyle Template Folder 내용 복사
                                if("10301".equals(jenkinsJobTypeCode))
                                {
                                    templateJobName = "DSpace_FreeStyle_Template";
                                }
                                //Pipeline Template Folder 내용 복사
                                else if("10302".equals(jenkinsJobTypeCode))
                                {
                                    templateJobName = "DSpace_Pipeline_Template";
                                }

                                // 젠킨스 job 생성(Template Folder 내용 복사 방식으로 Job 생성)
                                newJenkinsRestService.createJenkinsJobFromOtherJob(serverTypeCode, serverZoneCode, jenkinsFolderName, jenkinsJobName, templateJobName);

                                // 젠킨스 job 정보 DB 저장
                                job.setJenkinsCdId(jenkinsCdId);    // 발번된 jenkins_cd.id
                                job.setCreatedBy(loginId);
                                job.setLastModifiedBy(loginId);
                                newJenkinsMapper.insertJenkinsJob(job);

                                logger.info("Jenkins Create Job & DB Insert Succeed : jenkinsCdId={}", jenkinsCdId);
                            }catch(Exception e)
                            {
                                updateWorkflowTaskFail(loginId, workflowTaskId, e.getMessage());
                                return false;
                            }
                        }

                        logger.info("NewJenkinsService updateJobs jenkinsCdId == null workflowTaskId="+workflowTaskId);
                    }
                    // 폴더내 Job 생성 혹은 기존 Job에 대한 활성/비활성화 처리
                    // 즉, 기존 폴더 소속 Job에 대한 추가/수정만 처리
                    else
                    {
                        int jobsCount = jobs.size();
                        for(int j=0;j<jobsCount;j++)
                        {
                            JenkinsJobCdVO job = jobs.get(j);
                            Long jenkinsJobCdId = job.getJenkinsJobCdId();
                            Integer serviceStatus = job.getServiceStatus();
                            String jenkinsJobName = job.getJenkinsJobName();
                            String jenkinsJobTypeCode = job.getJenkinsJobTypeCode();
                            String templateJobName = null;

                            logger.info("NewJenkinsService updateJobs serverTypeCode="+serverTypeCode+", serverZoneCode="+serverZoneCode+", jenkinsCdId="+jenkinsCdId+", jenkinsFolderName="+jenkinsFolderName+", jobsCount="+jobsCount+", jenkinsJobCdId="+jenkinsJobCdId+", jenkinsJobName="+jenkinsJobName+", jenkinsJobTypeCode="+jenkinsJobTypeCode+", serviceStatus="+serviceStatus);

                            // 기존 폴더내 신규 Job 생성
                            if(jenkinsJobCdId == null)
                            {
                                try
                                {
                                    //FreeStyle Template Folder 내용 복사
                                    if("10301".equals(jenkinsJobTypeCode))
                                    {
                                        templateJobName = "DSpace_FreeStyle_Template";
                                    }
                                    //Pipeline Template Folder 내용 복사
                                    else if("10302".equals(jenkinsJobTypeCode))
                                    {
                                        templateJobName = "DSpace_Pipeline_Template";
                                    }

                                    // 젠킨스 job 생성(Template Folder 내용 복사 방식으로 Job 생성)
                                    newJenkinsRestService.createJenkinsJobFromOtherJob(serverTypeCode, serverZoneCode, jenkinsFolderName, jenkinsJobName, templateJobName);

                                    // 젠킨스 job 정보 DB 저장
                                    job.setJenkinsCdId(jenkinsCdId);    // 발번된 jenkins_cd.id
                                    job.setCreatedBy(loginId);
                                    job.setLastModifiedBy(loginId);
                                    newJenkinsMapper.insertJenkinsJob(job);

                                    logger.info("Jenkins Job Create & DB Insert Succeed : jenkinsCdId={}", jenkinsCdId);
                                }catch(Exception e)
                                {
                                    updateWorkflowTaskFail(loginId, workflowTaskId, e.getMessage());
                                    return false;
                                }
                            }
                            // 기존 폴더내 기존 Job은 활성/비활성화 처리만 함.
                            else
                            {
                                try
                                {
                                    if(serviceStatus != null && 1 == serviceStatus.intValue())
                                    {
                                        // 젠킨스 job 정보 DB 저장
                                        job.setJenkinsCdId(jenkinsCdId);    // 발번된 jenkins_cd.id
                                        job.setCreatedBy(loginId);
                                        job.setLastModifiedBy(loginId);

                                        //비활성화
                                        newJenkinsRestService.disableJenkinsJob(serverTypeCode, serverZoneCode, jenkinsFolderName, jenkinsJobName);

                                        updateServiceStatusForJenkinsJob(loginId, jenkinsJobCdId, serviceStatus);
                                    }else if(serviceStatus != null && 2 == serviceStatus.intValue())
                                    {
                                        //활성화
                                        newJenkinsRestService.enableJenkinsJob(serverTypeCode, serverZoneCode, jenkinsFolderName, jenkinsJobName);

                                        updateServiceStatusForJenkinsJob(loginId, jenkinsJobCdId, serviceStatus);
                                    }
                                }catch(Exception e)
                                {
                                    updateWorkflowTaskFail(loginId, workflowTaskId, e.getMessage());
                                    return false;
                                }
                            }
                        }
                    }
                }
            }

            updateWorkflowTaskSuccess(loginId, workflowTaskId);
        }

        return true;
    }

    /**
     * Jenkins Job2 수정
     * 
     * UI상 Jenkins 구조 및 입력값 제한사항.
     * 1.특정 Jenkins 서버당 생성할 수 있는 폴더는 하나임.
     * 2.한번 생성한 폴더와 하위 Job 이름은 변경되지 않는다.
     * 
     * @param updateProjectVo
     * @param workflowId
     * @return 
     */
    private boolean updateJobs2(String loginId, UpdateJenkinsJobsVO updateJenkinsJobsVO)
    {
        List<UpdateJenkinsVO> jenkinsList = updateJenkinsJobsVO.getJenkins();
        Long projectId = updateJenkinsJobsVO.getProjectId();

        if(jenkinsList != null)
        {
            int jenkinsListCount = jenkinsList.size();

            logger.info("NewJenkinsService updateJobs jenkinsListCount="+jenkinsListCount);

            for(int i=0;i<jenkinsListCount;i++)
            {
                UpdateJenkinsVO ujk = jenkinsList.get(i);
                Long jenkinsCdId = ujk.getJenkinsCdId();
                String jenkinsFolderName = ujk.getJenkinsFolderName();
                String serverTypeCode = ujk.getServerTypeCode();
                String serverZoneCode = ujk.getServerZoneCode();

                List<JenkinsJobCdVO> jobs = ujk.getJobs();

                if(jenkinsFolderName != null && !"".equals(jenkinsFolderName))
                {
                    // 신규 폴더 생성 및 폴더내 소속 Job 신규 생성
                    if(jenkinsCdId == null)
                    {
                        // 신규 폴더 생성
                        try
                        {
                            newJenkinsRestService.createJenkinsFolder(serverTypeCode, serverZoneCode, jenkinsFolderName);

                            JenkinsCdVO newJenkins = new JenkinsCdVO();
                            newJenkins.setProjectId(projectId);
                            newJenkins.setJenkinsFolderName(jenkinsFolderName);
                            newJenkins.setServerTypeCode(serverTypeCode);
                            newJenkins.setServerZoneCode(serverZoneCode);
                            newJenkins.setCreatedBy(loginId);
                            newJenkins.setLastModifiedBy(loginId);

                            newJenkinsMapper.insertJenkins(newJenkins);

                            jenkinsCdId = newJenkins.getId();
                            logger.info("Jenkins Folder Create & DB Insert Succeed : jenkinsCdId={}", jenkinsCdId);
                        }catch(Exception e)
                        {
                            return false;
                        }

                        // 신규 폴더 소속에 신규 Job 생성
                        int jobsCount = jobs.size();
                        for(int j=0;j<jobsCount;j++)
                        {
                            JenkinsJobCdVO job = jobs.get(j);
                            String jenkinsJobName = job.getJenkinsJobName();
                            String jenkinsJobTypeCode = job.getJenkinsJobTypeCode();
                            String templateJobName = null;

                            try
                            {
                                //FreeStyle Template Folder 내용 복사
                                if("10301".equals(jenkinsJobTypeCode))
                                {
                                    templateJobName = "DSpace_FreeStyle_Template";
                                }
                                //Pipeline Template Folder 내용 복사
                                else if("10302".equals(jenkinsJobTypeCode))
                                {
                                    templateJobName = "DSpace_Pipeline_Template";
                                }

                                // 젠킨스 job 생성(Template Folder 내용 복사 방식으로 Job 생성)
                                newJenkinsRestService.createJenkinsJobFromOtherJob(serverTypeCode, serverZoneCode, jenkinsFolderName, jenkinsJobName, templateJobName);

                                // 젠킨스 job 정보 DB 저장
                                job.setJenkinsCdId(jenkinsCdId);    // 발번된 jenkins_cd.id
                                job.setCreatedBy(loginId);
                                job.setLastModifiedBy(loginId);
                                newJenkinsMapper.insertJenkinsJob(job);

                                logger.info("Jenkins Job Create & DB Insert Succeed : jenkinsCdId={}", jenkinsCdId);
                            }catch(Exception e)
                            {
                                return false;
                            }
                        }
                    }
                    //폴더내 Job 생성 및 기존 Job 활성/비활성화 처리
                    //결국 기존 폴더 소속 Job에 대한 추가/수정만 처리
                    else
                    {
                        int jobsCount = jobs.size();
                        for(int j=0;j<jobsCount;j++)
                        {
                            JenkinsJobCdVO job = jobs.get(j);
                            Long jenkinsJobCdId = job.getJenkinsJobCdId();
                            Integer serviceStatus = job.getServiceStatus();
                            String jenkinsJobName = job.getJenkinsJobName();
                            String jenkinsJobTypeCode = job.getJenkinsJobTypeCode();
                            String templateJobName = null;

                            logger.info("NewJenkinsService updateJobs serverTypeCode="+serverTypeCode+", serverZoneCode="+serverZoneCode+", jenkinsCdId="+jenkinsCdId+", jenkinsFolderName="+jenkinsFolderName+", jobsCount="+jobsCount+", jenkinsJobCdId="+jenkinsJobCdId+", jenkinsJobName="+jenkinsJobName+", jenkinsJobTypeCode="+jenkinsJobTypeCode+", serviceStatus="+serviceStatus);

                            //기존 폴더내 신규 Job 생성
                            if(jenkinsJobCdId == null)
                            {
                                try
                                {
                                    //FreeStyle Template Folder 내용 복사
                                    if("10301".equals(jenkinsJobTypeCode))
                                    {
                                        templateJobName = "DSpace_FreeStyle_Template";
                                    }
                                    //Pipeline Template Folder 내용 복사
                                    else if("10302".equals(jenkinsJobTypeCode))
                                    {
                                        templateJobName = "DSpace_Pipeline_Template";
                                    }

                                    // 젠킨스 job 생성(Template Folder 내용 복사 방식으로 Job 생성)
                                    newJenkinsRestService.createJenkinsJobFromOtherJob(serverTypeCode, serverZoneCode, jenkinsFolderName, jenkinsJobName, templateJobName);

                                    // 젠킨스 job 정보 DB 저장
                                    job.setJenkinsCdId(jenkinsCdId);    // 발번된 jenkins_cd.id
                                    job.setCreatedBy(loginId);
                                    job.setLastModifiedBy(loginId);
                                    newJenkinsMapper.insertJenkinsJob(job);

                                    logger.info("Jenkins Job Create & DB Insert Succeed : jenkinsCdId={}", jenkinsCdId);
                                }catch(Exception e)
                                {
                                    return false;
                                }
                            }
                            //기존 폴더내 기존 Job은 활성/비활성화 처리만 함.
                            else
                            {
                                try
                                {
                                    if(serviceStatus != null && 1 == serviceStatus.intValue())
                                    {
                                        // 젠킨스 job 정보 DB 저장
                                        job.setJenkinsCdId(jenkinsCdId);    // 발번된 jenkins_cd.id
                                        job.setCreatedBy(loginId);
                                        job.setLastModifiedBy(loginId);

                                        //비활성화
                                        newJenkinsRestService.disableJenkinsJob(serverTypeCode, serverZoneCode, jenkinsFolderName, jenkinsJobName);

                                        updateServiceStatusForJenkinsJob(loginId, jenkinsJobCdId, serviceStatus);
                                    }else if(serviceStatus != null && 2 == serviceStatus.intValue())
                                    {
                                        //활성화
                                        newJenkinsRestService.enableJenkinsJob(serverTypeCode, serverZoneCode, jenkinsFolderName, jenkinsJobName);

                                        updateServiceStatusForJenkinsJob(loginId, jenkinsJobCdId, serviceStatus);
                                    }
                                }catch(Exception e)
                                {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    /**
     * CD Jenkins Job 삭제 처리
     * 프로젝트 삭제시 DB상에만 jenkins_cd, jenkins_job_cd의 service_status=9(삭제)로 변경하고,
     * 실제 CD Jenkins Job들은 disable됨.
     * @param projectId
     * @return
     */
    public void deleteJobs(String loginId, Long projectId)
    {
        if(projectId != null)
        {
            // Jenkins job disable 처리
            JenkinsCdVO vo = new JenkinsCdVO();
            vo.setProjectId(projectId);

            List<JenkinsCdVO> jenkinsList = newJenkinsMapper.findJenkins(vo);

            if(jenkinsList != null && jenkinsList.size() > 0)
            {
                Long jenkinsCdId = null;
                Long tempJenkinsCdId = null;
                String jenkinsFolderName = null;
                Long jenkinsJobCdId = null;
                String jenkinsJobName = null;
                String serverTypeCode = null;
                String serverZoneCode = null;

                for(JenkinsCdVO jenkinsJob : jenkinsList)
                {
                    jenkinsCdId = jenkinsJob.getId();
                    jenkinsJobCdId = jenkinsJob.getJenkinsJobCdId();
                    serverTypeCode = jenkinsJob.getServerTypeCode();
                    serverZoneCode = jenkinsJob.getServerZoneCode();
                    jenkinsFolderName = jenkinsJob.getJenkinsFolderName();
                    jenkinsJobName = jenkinsJob.getJenkinsJobName();

                    try
                    {
                        newJenkinsRestService.disableJenkinsJob(serverTypeCode, serverZoneCode, jenkinsFolderName, jenkinsJobName);
                        logger.info("Jenkins Job Disable Succeed. => ProjectID: {}, Jenkins Folder/Job Name: {}", projectId, jenkinsFolderName+"/"+jenkinsJobName);
                    }catch(Exception e)
                    {
                        logger.error("Jenkins Job Disable Fail. => ProjectID: {}, Jenkins Folder/Job Name: {}", projectId, jenkinsFolderName+"/"+jenkinsJobName);
                    }

                    // jenkins_job_cd service_status 0 -> 9 업데이트
                    deleteServiceStatusForJenkinsJob(loginId, jenkinsJobCdId);

                    /*
                     * jenkins_cd service_status 0 -> 9 업데이트
                     * 아래는 폴더 단위(jenkins_cd)로 한번만 삭제 처리되도록 로직 추가 
                     */
                    if(tempJenkinsCdId == null || tempJenkinsCdId.intValue() != jenkinsCdId.intValue())
                    {
                        deleteServiceStatusForJenkins(loginId, projectId, jenkinsCdId, null, null, null);
                        tempJenkinsCdId = jenkinsCdId;
                    }
                }
            }
        }else
        {
            logger.info("No Project in DB => ProjectID: {}", projectId);
        }
    }

    /**
     * jenkins_job_cd 테이블의 id 기준으로 특정 Job disable=1 혹은 enable=2 처리
     * @param jenkinsCdId
     */
    private void updateServiceStatusForJenkinsJob(String loginId, Long jenkinsJobCdId, Integer serviceStatus)
    {
        JenkinsCdVO jenkins = new JenkinsCdVO();
        jenkins.setServiceStatus(serviceStatus);
        jenkins.setJenkinsJobCdId(jenkinsJobCdId); //jenkins_job_cd.id
        jenkins.setLastModifiedBy(loginId);
        newJenkinsMapper.updateJenkinsJobServiceStatus(jenkins);
    }

    /**
     * Jenkins_cd 테이블 Delete 처리
     * @param projectId
     */
    private void deleteServiceStatusForJenkins(String loginId, Long projectId, Long jenkinsCdId, String serverTypeCode, String serverZoneCode, String jenkinsFolderName)
    {
        JenkinsCdVO jenkins = new JenkinsCdVO();
        jenkins.setServiceStatus(9);
        jenkins.setProjectId(projectId);            //jenkins_cd.project_id
        jenkins.setJenkinsCdId(jenkinsCdId);        //jenkins_cd.id
        jenkins.setServerTypeCode(serverTypeCode);
        jenkins.setServerZoneCode(serverZoneCode);
        jenkins.setJenkinsFolderName(jenkinsFolderName);
        jenkins.setLastModifiedBy(loginId);
        newJenkinsMapper.updateJenkinsServiceStatus(jenkins);
    }

    /**
     * jenkins_job_cd 테이블의 id 기준으로 특정 Job Delete 처리
     * @param jenkinsCdId
     */
    private void deleteServiceStatusForJenkinsJob(String loginId, Long jenkinsJobCdId)
    {
        JenkinsCdVO jenkins = new JenkinsCdVO();
        jenkins.setServiceStatus(9);
        jenkins.setJenkinsJobCdId(jenkinsJobCdId); //jenkins_job_cd.id
        jenkins.setLastModifiedBy(loginId);
        newJenkinsMapper.updateJenkinsJobServiceStatus(jenkins);
    }

    /**
     * jenkins_job_cd 테이블의 jenkins_cd_id기준으로 모든 Job Delete 처리
     * @param jenkinsCdId
     */
//    private void deleteServiceStatusAllForJenkinsJob(String loginId, Long jenkinsCdId)
//    {
//        JenkinsCdVO jenkins = new JenkinsCdVO();
//        jenkins.setServiceStatus(9);
//        jenkins.setJenkinsCdId(jenkinsCdId);        //jenkins_cd.id
//        jenkins.setLastModifiedBy(loginId);
//        newJenkinsMapper.updateJenkinsJobServiceStatus(jenkins);
//    }

    /**
     * Jenkins 폴더명 중복 체크
     * @param serverTypeCode
     * @param serverZoneCode
     * @param projectId
     * @param jenkinsFolderName
     * @return
     */
    public void checkJenkinsFolderNameDuplicate(String serverTypeCode, String serverZoneCode, String jenkinsFolderName)
    {
        boolean isDuplicated = false;

        isDuplicated = newJenkinsRestService.getValidJenkinsFolderNm(serverTypeCode, serverZoneCode, jenkinsFolderName);

        if(isDuplicated)
        {
            //E0016 - 중복된 Jenkins 폴더명(XXXX)이(가) 존재합니다.
            throw new ApiException(HttpStatus.OK, "E0016", "Jenkins 폴더명("+jenkinsFolderName+")");
        }
    }

    /**
     * 신규 추가한 Jenkins Job명이 기존 Job명과 중복이 발생하는지 체크
     * @param serverTypeCode
     * @param serverZoneCode
     * @param projectId
     * @param jenkinsFolderName
     * @param jenkinsJobName
     * @return
     */
    public void checkJenkinsJobNameDuplicate(String serverTypeCode, String serverZoneCode, String jenkinsFolderName, String jenkinsJobName)
    {
        JobBuildInfoVO jobBuildInfoVO = null;

        //사용자가 신규 입력한 경우
        jobBuildInfoVO = newJenkinsRestService.getJenkinsJobBuildList(serverTypeCode, serverZoneCode, jenkinsFolderName, jenkinsJobName);

        //신규 입력 이고 기존 Job명과 중복되고, 그 Job이 disable 된 상태인 경우
        if(jobBuildInfoVO != null && jobBuildInfoVO.isDisabled())
        {
            //E0122 - {0}은 이미 한번 사용한 {1}이므로 새로운 {2}으로 입력해 주십시오.
            throw new ApiException(HttpStatus.OK, "E0122", new Object[]{jenkinsJobName, "Job명(보관정책)", "이름"});
        }
        //신규 입력이 기존 Job명과 중복인 경우
        else if(jobBuildInfoVO != null)
        {
            //E0016 - 중복된 Jenkins Job명(XXXX)이(가) 존재합니다.
            throw new ApiException(HttpStatus.OK, "E0016", "Jenkins Job명("+jenkinsJobName+")");
        }
    }

    /**
     * Jenkins Folder명 존재 여부(
     * @param CheckFolderNameParamVO - 배포 서버 타입코드(10101 - TB, 10102 - PRD), 배포 서버 영역코드(10201 - RM, 10202 - IPC, 10203 - EPC, 10204 - Container), Jenkins Folder명
     * @return ResponseEntity - 응답 Entity
     */
    public ResponseEntity<ApiResponseEntity> existJenkinsFolderName(CheckFolderNameParamVO checkFolderNameParamVO)
    {
        String serverTypeCode = checkFolderNameParamVO.getServerTypeCode();
        String serverZoneCode = checkFolderNameParamVO.getServerZoneCode();
        String jenkinsFolderOrJobName = checkFolderNameParamVO.getJenkinsFolderName();
        ApiResponseEntity message = null;

        if(serverTypeCode == null || "".equals(serverTypeCode))
        {
            //E0009 - Jenkins 폴더명은(는) 필수값입니다.
            throw new ApiException(HttpStatus.OK, "E0009", "배포 서버 타입코드(10101 - TB, 10102 - PRD)");
        }

        if(serverZoneCode == null || "".equals(serverZoneCode))
        {
            //E0009 - 배포 서버 영역코드(10201 - RM, 10202 - IPC, 10203 - EPC, 10204 - Container)은(는) 필수값입니다.
            throw new ApiException(HttpStatus.OK, "E0009", "배포 서버 영역코드(10201 - RM, 10202 - IPC, 10203 - EPC, 10204 - Container)");
        }

        if(jenkinsFolderOrJobName == null || "".equals(jenkinsFolderOrJobName))
        {
            //E0009 - Jenkins Folder명 혹은 Job명은(는) 필수값입니다.
            throw new ApiException(HttpStatus.OK, "E0009", "Jenkins Folder명 혹은 Job명");
        }

        boolean isDuplicated = newJenkinsRestService.getValidJenkinsFolderNm(serverTypeCode, serverZoneCode, jenkinsFolderOrJobName);

        String statusMsg = DbMessageManager.getMessage("I0001", "ko");

        if(isDuplicated)
        {
            //E0016 - 중복된 Jenkins 이름(XXXX)이(가) 존재합니다.
            message = new ApiResponseEntity(statusMsg, isDuplicated, "E0016", "Jenkins 이름("+jenkinsFolderOrJobName+")");
        }else
        {
            message = new ApiResponseEntity(statusMsg, isDuplicated, null, null);
        }

        return new ResponseEntity<ApiResponseEntity>(message, HttpStatus.OK);
    }

    /**
     * Jenkins Job 빌드 정보 목록 조회
     * @param CheckFolderNameParamVO - 배포 서버 타입코드(10101 - TB, 10102 - PRD)
     *                              배포 서버 영역코드(10201 - RM, 10202 - IPC, 10203 - EPC, 10204 - Container)
     *                              Jenkins Folder명
     *                              Jenkins Job명 
     * @return ResponseEntity - 응답 Entity
     */
    public ResponseEntity<ApiResponseEntity> getJenkinsJobBuildList(GetJenkinsJobBuildListParamVO getJenkinsJobBuildListParamVO)
    {
        String serverTypeCode = getJenkinsJobBuildListParamVO.getServerTypeCode();
        String serverZoneCode = getJenkinsJobBuildListParamVO.getServerZoneCode();
        String jenkinsFolderName = getJenkinsJobBuildListParamVO.getJenkinsFolderName();
        String jenkinsJobName = getJenkinsJobBuildListParamVO.getJenkinsJobName();

        ApiResponseEntity message = null;

        if(serverTypeCode == null || "".equals(serverTypeCode))
        {
            //E0009 - Jenkins 폴더명은(는) 필수값입니다.
            throw new ApiException(HttpStatus.OK, "E0009", "배포 서버 타입코드(10101 - TB, 10102 - PRD)");
        }

        if(serverZoneCode == null || "".equals(serverZoneCode))
        {
            //E0009 - 배포 서버 영역코드(10201 - RM, 10202 - IPC, 10203 - EPC, 10204 - Container)은(는) 필수값입니다.
            throw new ApiException(HttpStatus.OK, "E0009", "배포 서버 영역코드(10201 - RM, 10202 - IPC, 10203 - EPC, 10204 - Container)");
        }

        if(jenkinsFolderName == null || "".equals(jenkinsFolderName))
        {
            //E0009 - Jenkins Folder명은(는) 필수값입니다.
            throw new ApiException(HttpStatus.OK, "E0009", "Jenkins Folder명");
        }

        if(jenkinsJobName == null || "".equals(jenkinsJobName))
        {
            //E0009 - Jenkins Job명은(는) 필수값입니다.
            throw new ApiException(HttpStatus.OK, "E0009", "Jenkins Job명");
        }

        String statusMsg = DbMessageManager.getMessage("I0001", "ko");

        JobBuildInfoVO jenkinsJobBuildInfo = newJenkinsRestService.getJenkinsJobBuildList(serverTypeCode, serverZoneCode, jenkinsFolderName, jenkinsJobName);

        if(jenkinsJobBuildInfo != null)
        {
            message = new ApiResponseEntity(statusMsg, jenkinsJobBuildInfo, null, null);
        }else
        {
            //I0006 - 데이터가 존재하지 않습니다.
            message = new ApiResponseEntity(statusMsg, null, "I0006", DbMessageManager.getMessage("I0006", "ko"));
        }

        return new ResponseEntity<ApiResponseEntity>(message, HttpStatus.OK);
    }

    private void updateWorkflowTaskFail(String loginId, Long workflowTaskId, String errorMsg)
    {
        updateWorkflowTask(loginId, workflowTaskId, 2, errorMsg);
    }

    private void updateWorkflowTaskSuccess(String loginId, Long workflowTaskId)
    {
        updateWorkflowTask(loginId, workflowTaskId, 1, null);
    }

    /**
     * ProjectID / projectId / UserID / RoleAction 으로 Role 체크 (생성/수정/삭제의 경우)
     * @param projectGroupId 
     * @param projectId
     * @param loginId
     * @param roleAction
     */
//    private void roleCheck(Long projectGroupId, Long projectId, String loginId, RoleAction roleAction)
//    {
//        roleService.checkProjectPermission(projectGroupId, projectId, loginId, roleAction, true);
//    }

    /**
     * 프로젝트 ID 기준으로 소속된 전체 Jenkins job들 중에서 최근 실행 job 정보 조회
     * @param projectId
     * @return
     */
    public List<LastBuildJobVO> findLastBuildJobList(Long projectId)
    {
        JenkinsCdVO vo = new JenkinsCdVO();
        vo.setProjectId(projectId);
        List<JenkinsCdVO> jenkins = newJenkinsMapper.findJenkins(vo);

        List<LastBuildJobVO> result = new ArrayList<LastBuildJobVO>();
        if(jenkins != null)
        {
            for(JenkinsCdVO jenkinsVo : jenkins)
            {
                String serverTypeCode = jenkinsVo.getServerTypeCode();
                String serverTypeName = jenkinsVo.getServerTypeName();
                String serverZoneCode = jenkinsVo.getServerZoneCode();
                String serverZoneName = jenkinsVo.getServerZoneName();
                String jenkinsFolderName = jenkinsVo.getJenkinsFolderName();
                String jenkinsJobTypeCode = jenkinsVo.getJenkinsJobTypeCode();
                String jenkinsJobTypeName = jenkinsVo.getJenkinsJobTypeName();
                String jenkinsJobName = jenkinsVo.getJenkinsJobName();
                Integer jobServiceStatus = jenkinsVo.getJobServiceStatus();

                LastBuildJobVO getJenkinsVo = newJenkinsRestService.getJenkinsJobLastBuildForDetail(serverTypeCode, serverTypeName, serverZoneCode, serverZoneName, jenkinsFolderName, jenkinsJobName, jobServiceStatus);

                if(getJenkinsVo == null)
                {
                    LastBuildJobVO jobVo = new LastBuildJobVO();
                    jobVo.setId(jenkinsVo.getId());
                    jobVo.setServerTypeCode(serverTypeCode);
                    jobVo.setServerTypeName(serverTypeName);
                    jobVo.setServerZoneCode(serverZoneCode);
                    jobVo.setServerZoneName(serverZoneName);
                    jobVo.setJenkinsFolderName(jenkinsFolderName);
                    jobVo.setJenkinsJobTypeCode(jenkinsJobTypeCode);
                    jobVo.setJenkinsJobTypeName(jenkinsJobTypeName);
                    jobVo.setJenkinsJobName(jenkinsJobName);
                    jobVo.setServiceStatus(jobServiceStatus);

                    jobVo.setDescription("");
                    jobVo.setResult("");
                    jobVo.setLastBuildDate("");
                    result.add(jobVo);
                }else
                {
                    result.add(getJenkinsVo);
                }
            }
        }

        return result;
    }

    /**
     * 프로젝트 ID 기준으로 전체 Jenkins Job 상세 정보를 가져옴.
     * @param projectId - 프로젝트 ID
     * @return FindJenkinsJobsVO - Jenkins Job 상세 정보
     */
    public FindJenkinsJobsVO findJenkinsJobList(Long projectId)
    {
        FindJenkinsJobsVO findJenkinsJobs = new FindJenkinsJobsVO();

        List<FindJenkinsJobListVO> jenkinsJobList = newJenkinsMapper.findJenkinsJobList(projectId);

        int jenkinsJobListCount = jenkinsJobList.size();

        if(jenkinsJobListCount > 0)
        {
            findJenkinsJobs.setProjectId(projectId);

            Map<String, List<ResultJenkinsJobCdVO>> jss = new HashMap<String, List<ResultJenkinsJobCdVO>>();
            for(int i=0;i<jenkinsJobListCount;i++)
            {
                FindJenkinsJobListVO fjjl = jenkinsJobList.get(i);

                Long jenkinsCdId = fjjl.getJenkinsCdId();
                String serverTypeCode = fjjl.getServerTypeCode();
                String serverTypeName = fjjl.getServerTypeName();
                String serverZoneCode = fjjl.getServerZoneCode();
                String serverZoneName = fjjl.getServerZoneName();
                String jenkinsFolderName = fjjl.getJenkinsFolderName();

                Long jenkinsJobCdId = fjjl.getJenkinsJobCdId();
                String jenkinsJobName = fjjl.getJenkinsJobName();
                String jenkinsJobTypeCode = fjjl.getJenkinsJobTypeCode();
                String jenkinsJobTypeName = fjjl.getJenkinsJobTypeName();
                Integer serviceStatus = fjjl.getServiceStatus();

                String keyStr = ""+jenkinsCdId+"^^^"+serverTypeCode+"^^^"+serverTypeName+"^^^"+serverZoneCode+"^^^"+serverZoneName+"^^^"+jenkinsFolderName;
                List<ResultJenkinsJobCdVO> jobs = jss.get(keyStr);

                if(jobs == null)
                {
                    List<ResultJenkinsJobCdVO> jobs1 = new ArrayList<ResultJenkinsJobCdVO>();
                    ResultJenkinsJobCdVO rjjcv = new ResultJenkinsJobCdVO();
                    rjjcv.setJenkinsJobCdId(jenkinsJobCdId);
                    rjjcv.setJenkinsJobName(jenkinsJobName);
                    rjjcv.setJenkinsJobTypeCode(jenkinsJobTypeCode);
                    rjjcv.setJenkinsJobTypeName(jenkinsJobTypeName);
                    rjjcv.setServiceStatus(serviceStatus);
                    jobs1.add(rjjcv);

                    jss.put(keyStr, jobs1);
                }else
                {
                    ResultJenkinsJobCdVO rjjcv = new ResultJenkinsJobCdVO();
                    rjjcv.setJenkinsJobCdId(jenkinsJobCdId);
                    rjjcv.setJenkinsJobName(jenkinsJobName);
                    rjjcv.setJenkinsJobTypeCode(jenkinsJobTypeCode);
                    rjjcv.setJenkinsJobTypeName(jenkinsJobTypeName);
                    rjjcv.setServiceStatus(serviceStatus);
                    jobs.add(rjjcv);

                    jss.put(keyStr, jobs);
                }
            }

            int jssCount = jss.size();
            if(jssCount > 0)
            {
                List<FindJenkinsVO> jenkins = new ArrayList<FindJenkinsVO>();
                Iterator<String> keyIter = jss.keySet().iterator();

                while(keyIter.hasNext())
                {
                    String key = keyIter.next();
                    String[] keySplit = key.split("\\^\\^\\^");

                    logger.debug("NewJenkinsService findJenkinsJobList keySplit="+Arrays.toString(keySplit));
                    Long jenkinsCdId = Long.parseLong(keySplit[0]);
                    String serverTypeCode = keySplit[1];
                    String serverTypeName = keySplit[2];
                    String serverZoneCode = keySplit[3];
                    String serverZoneName = keySplit[4];
                    String jenkinsFolderName = keySplit[5];

                    List<ResultJenkinsJobCdVO> jobs = jss.get(key);

                    FindJenkinsVO fjkv = new FindJenkinsVO();
                    fjkv.setJenkinsCdId(jenkinsCdId);
                    fjkv.setServerTypeCode(serverTypeCode);
                    fjkv.setServerTypeName(serverTypeName);
                    fjkv.setServerZoneCode(serverZoneCode);
                    fjkv.setServerZoneName(serverZoneName);
                    fjkv.setJenkinsFolderName(jenkinsFolderName);

                    fjkv.setJobs(jobs);

                    jenkins.add(fjkv);
                }

                findJenkinsJobs.setJenkins(jenkins);
            }
        }

        return findJenkinsJobs;
    }

    /**
     * 프로젝트 ID 기준으로 소속된 전체 Jenkins job들 중에서 최근 실행 job Log 정보 조회
     * @param jenkinsFolderNm
     * @return
     */
    public List<LastBuildJobLogVO> findLastBuildJobLogTextList(Long projectId)
    {
        JenkinsCdVO vo = new JenkinsCdVO();
        vo.setProjectId(projectId);
        List<JenkinsCdVO> jenkins = newJenkinsMapper.findJenkins(vo);

        List<LastBuildJobLogVO> result = new ArrayList<>();
        
        if(!ObjectUtils.isEmpty(jenkins))
        {
            for(JenkinsCdVO jenkinsVo : jenkins)
            {
                String serverTypeCode = jenkinsVo.getServerTypeCode();
                String serverTypeName = jenkinsVo.getServerTypeName();
                String serverZoneCode = jenkinsVo.getServerZoneCode();
                String serverZoneName = jenkinsVo.getServerZoneName();
                String folderName     = jenkinsVo.getJenkinsFolderName();
                String jenkinsJobTypeCode = jenkinsVo.getJenkinsJobTypeCode();
                String jenkinsJobTypeName = jenkinsVo.getJenkinsJobTypeName();
                String jenkinsJobName = jenkinsVo.getJenkinsJobName();
                Integer jobServiceStatus = jenkinsVo.getJobServiceStatus();

                String jobLog = newJenkinsRestService.findLastBuildJobLogTextList(serverTypeCode, serverZoneCode, folderName, jenkinsJobName);

                LastBuildJobLogVO jobVo = new LastBuildJobLogVO();
                jobVo.setId(jenkinsVo.getId());
                jobVo.setServerTypeCode(serverTypeCode);
                jobVo.setServerTypeName(serverTypeName);
                jobVo.setServerZoneCode(serverZoneCode);
                jobVo.setServerZoneName(serverZoneName);
                jobVo.setJenkinsFolderName(folderName);
                jobVo.setJenkinsJobTypeCode(jenkinsJobTypeCode);
                jobVo.setJenkinsJobTypeName(jenkinsJobTypeName);
                jobVo.setJenkinsJobName(jenkinsJobName);
                jobVo.setServiceStatus(jobServiceStatus);

                if(ObjectUtils.isEmpty(jobLog))
                {
                    jobVo.setDescription("");
                    jobVo.setResult("");
                }else
                {
                    jobVo.setResult(jobLog);
                }
                result.add(jobVo);
            }
        }

        return result;
    }
}
