

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
     * ????????? CI JOB ????????? ?????? : ?????? ??????, ???????????? `,.-_(){} ?????? varchar100
     * (?????? ??????????????? ????????? ??? ?????? API ?????? ?????? ??????, API???????????? ????????? ??? ?????? ???????????? -_().,)
     */
    private Pattern regexJenkinsPattern = Pattern.compile("^([A-Za-z0-9???-???]{1})[a-zA-Z0-9???-???-_(),.\\s]{0,84}$");

    
    /**
     * jenkins ????????? folder , job ??? DB ???????????? - insert ?????? ??????
     * @param jenkins
     * @return Map<String, Object>
     */
    public Map<String, Object> insertJenkinsDBValidationCheck(NewInsertJenkinsVO jenkins) { 
    	
		Map<String, Object> result = new HashMap<String, Object>();
		List<NewInsertJenkinsVO> insertJenkinsFolderList = jenkins.getFolders();
		List<NewInsertJenkinsVO> insertJenkinsJobList = jenkins.getJobs();
		String compare = "";

		for(int i=0;i<insertJenkinsFolderList.size();i++) {
			//????????? folder DB ?????? ?????? ??????
			if(newJenkinsMapper.insertValidJenkinsCheck(insertJenkinsFolderList.get(i))) {
				result.put("msg", DbMessageManager.getMessage("E0016", "Jenkins ?????????", "ko"));
				return result;				
			}
			//folder ????????? ??????
			result = insertJenkinsToolValidationCheck(insertJenkinsFolderList,null);
			if(!result.isEmpty()) {
				return result;
			}
			
			//?????? ???????????? ????????? ?????? ????????? ??????
			compare = insertJenkinsFolderList.get(i).getJenkinsFolderName();
			for(int j=0;j<insertJenkinsFolderList.size();j++) {
				if(i!=j) {
					if(insertJenkinsFolderList.get(j).getJenkinsFolderName().equals(compare)) {
						result.put("msg", DbMessageManager.getMessage("E0016", "Jenkins ?????????", "ko"));
						return result;	
					}
				}
			}
		}
		
		for(int i=0;i<insertJenkinsJobList.size();i++) {
			//????????? job ?????? DB Check ?????? ??????
			if(newJenkinsMapper.insertValidJenkinsCheck(insertJenkinsJobList.get(i))) {
				result.put("msg", DbMessageManager.getMessage("E0016", "Jenkins job???", "ko"));
				return result;		
			}
			//?????? ???????????? ????????? ?????? ????????? ??????
			compare = insertJenkinsJobList.get(i).getJenkinsJobName();
			for(int j=0;j<insertJenkinsJobList.size();j++) {
				if(i!=j) {
					if(insertJenkinsJobList.get(j).getJenkinsJobName().equals(compare)) {
						result.put("msg", DbMessageManager.getMessage("E0016", "Jenkins job???", "ko"));
						return result;	
					}
				}
			}
		}
		// ????????? ?????? ??? tool?????? ??????
		result = insertJenkinsToolValidationCheck(insertJenkinsJobList,null);
		if(!result.isEmpty()) {
			return result;
		}
		
    	return result;
    }
    /**
     * ???????????? folder , job ??? ????????? ??????, ??????????????? ?????????????????? ???????????? , ??????job?????? ?????? - insert ?????? ??????
     * @param insertJenkinsFolderList
     * @param uri
     * @return Map<String, Object>
     */
    public Map<String, Object> insertJenkinsToolValidationCheck(List<NewInsertJenkinsVO> insertJenkinsFolderList,String paramURL){
		Matcher match;
		Map<String, Object> result = new HashMap<String, Object>();
		String regexJenkins  = "^([A-Za-z0-9???-???]{1})[a-zA-Z0-9???-???-_(),.\\s]{0,84}$";
		String url = paramURL;

		//List<NewInsertJenkinsVO> insertJenkinsFolderList = insertJenkinsFolderList
		for(int i=0; i<insertJenkinsFolderList.size();i++) {
			
			if(paramURL==null) {
				url = newJenkinsRestService.getJenkinsUrl(insertJenkinsFolderList.get(i).getServerTypeCode(),insertJenkinsFolderList.get(i).getServerZoneCode());
			}
			
			if(insertJenkinsFolderList.get(i).getDivision()==1) {
				String folderName = insertJenkinsFolderList.get(i).getJenkinsFolderName();	
				//????????? ??????
		        match = Pattern.compile(regexJenkins).matcher(folderName);
		        if(!match.find()) {
				    result.put("msg", DbMessageManager.getMessage("E0125", new Object[]{"Jenkins ?????????", "??????????????? ????????? ??? ????????? ??????,??????????????????,??????,???????????? -_().,","1","85"}, "ko"));
				    return result;  
		        }
		        
		        
				for(int j=0;j<insertJenkinsFolderList.size();j++) {
					if(i!=j && insertJenkinsFolderList.get(j).getDivision()==1) {
						if(insertJenkinsFolderList.get(j).getJenkinsFolderName().equals(folderName)) {
							result.put("msg", DbMessageManager.getMessage("E0016", "Jenkins ?????????", "ko"));
							return result;	
						}
					}
				}
		        
		        if(existFolderOrJob(insertJenkinsFolderList.get(i).getServerTypeCode() , insertJenkinsFolderList.get(i).getServerZoneCode(),url, insertJenkinsFolderList.get(i).getJenkinsFolderName())) {
					result.put("msg", DbMessageManager.getMessage("E0016", "Jenkins ?????????", "ko"));
					return result;	
		        }
		        
		        //???????????? ??????
		        if(insertJenkinsFolderList.get(i).getFolders().size()>0) {

		        	result = insertJenkinsToolValidationCheck(insertJenkinsFolderList.get(i).getFolders(),url+ "/job/" + insertJenkinsFolderList.get(i).getJenkinsFolderName());
		        	
					if(!result.isEmpty()) {
						return result;
					}
		        }
		        //?????? ??? ??????
		        if(insertJenkinsFolderList.get(i).getJobs().size()>0) {

		        	result = insertJenkinsToolValidationCheck(insertJenkinsFolderList.get(i).getJobs(),url+ "/job/" + insertJenkinsFolderList.get(i).getJenkinsFolderName());

					if(!result.isEmpty()) {
						return result;
					}
		        }
				
			}else {
				
				String jobName = insertJenkinsFolderList.get(i).getJenkinsJobName();
				//????????? ??????
				match = Pattern.compile(regexJenkins).matcher(jobName);
				if(!match.find()) {
					result.put("msg", DbMessageManager.getMessage("E0125", new Object[]{"Jenkins job???", "??????????????? ????????? ??? ????????? ??????,??????????????????,??????,???????????? -_().,","1","85"}, "ko"));
					return result;  
				}
				for(int j=0;j<insertJenkinsFolderList.size();j++) {
					if(i!=j && insertJenkinsFolderList.get(j).getDivision()==2) {
						if(insertJenkinsFolderList.get(j).getJenkinsJobName().equals(jobName)) {
							result.put("msg", DbMessageManager.getMessage("E0016", "Jenkins job???", "ko"));
							return result;	
						}
					}
				}
				if(existFolderOrJob(insertJenkinsFolderList.get(i).getServerTypeCode() , insertJenkinsFolderList.get(i).getServerZoneCode(), url, insertJenkinsFolderList.get(i).getJenkinsJobName())) {
					result.put("msg", DbMessageManager.getMessage("E0016", "Jenkins job???", "ko"));
					return result;	
				}
			}
		}
		return result;
    }    
    
    /**
     * Jenkins Folder ?????? Job??? ??????????????? Validation Check
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
     * Jenkins Job ?????? ??? validation check
     * 
     * @param createJobValidation
     */
    public void createJobValidation(CreateJenkinsJobsVO createJenkinsJobsVO)
    {
        if(createJenkinsJobsVO == null)
        {
            //E0009 - ???????????? ?????? ????????? ???????????????
            throw new ApiException(HttpStatus.OK, "E0009", "???????????? ?????? ??????");
        }

        // ???????????? ?????? ?????? ??????
//        roleCheck(insertProjectVo.getProjectGroupId(), null, getLoginId(), RoleAction.CREATE);

        // trim ??????
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
                    //E0009 - Jenkins ????????????(???) ??????????????????.
                    throw new ApiException(HttpStatus.OK, "E0009", "Jenkins ?????????");
                }else if(jenkinsFolderName != null)
                {
                    /*
                     * ????????? CI JOB ????????? ?????? : ?????? ??????, ???????????? `,.-_(){} ?????? varchar100
                     * (?????? ??????????????? ????????? ??? ?????? API ?????? ?????? ??????, API???????????? ????????? ??? ?????? ???????????? -_().,)
                     */
                    Matcher match = regexJenkinsPattern.matcher(jenkinsFolderName);

                    if(!match.find())
                    {
                        throw new ApiException(HttpStatus.OK, "E0125", new Object[]{"Jenkins ?????????", "??????????????? ????????? ??? ????????? ??????,??????????????????,??????,???????????? -_().,","1","85"});
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
                            //E0009 - Jenkins Job ?????????(???) ??????????????????.
                            throw new ApiException(HttpStatus.OK, "E0009", "Jenkins Job ??????");
                        }else if(jenkinsJobCdId == null)
                        {
                            Matcher match = regexJenkinsPattern.matcher(jenkinsJobName);

                            if(!match.find())
                            {
                                throw new ApiException(HttpStatus.OK, "E0125", new Object[]{"Jenkins job???", "??????????????? ????????? ??? ????????? ??????,??????????????????,??????,???????????? -_().,","1","85"});
                            }

                            //?????? ????????? Job?????? ?????? Job?????? ??????????????? ??????
                            checkJenkinsJobNameDuplicate(serverTypeCode, serverZoneCode, jenkinsFolderName, jenkinsJobName);
                        }

                        jobInfo.setJenkinsJobName(CommonUtil.trim(jenkinsJobName));
                    }
                }else
                {
                    //E0009 - Jenkins ???????????? ???????????????
                    throw new ApiException(HttpStatus.OK, "E0009", "Job ??????");
                }
            }
        }
    }

    /**
     * Jenkins Job ?????? ??? validation check
     * 
     * @param updateJobValidation
     */
    public void updateJobValidation(UpdateJenkinsJobsVO updateJenkinsJobsVO)
    {
        if(updateJenkinsJobsVO == null)
        {
            //E0009 - ???????????? ?????? ????????? ???????????????
            throw new ApiException(HttpStatus.OK, "E0009", "???????????? ?????? ??????");
        }

        // ???????????? ?????? ?????? ??????
//        roleCheck(insertProjectVo.getProjectGroupId(), null, getLoginId(), RoleAction.CREATE);

        // trim ??????
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
                    //E0009 - Jenkins ????????????(???) ??????????????????.
                    throw new ApiException(HttpStatus.OK, "E0009", "Jenkins ?????????");
                }else if(jenkinsCdId == null)
                {
                    /*
                     * ????????? CI JOB ????????? ?????? : ?????? ??????, ???????????? `,.-_(){} ?????? varchar100
                     * (?????? ??????????????? ????????? ??? ?????? API ?????? ?????? ??????, API???????????? ????????? ??? ?????? ???????????? -_().,)
                     */
                    Matcher match = regexJenkinsPattern.matcher(jenkinsFolderName);

                    if(!match.find())
                    {
                        throw new ApiException(HttpStatus.OK, "E0125", new Object[]{"Jenkins ?????????", "??????????????? ????????? ??? ????????? ??????,??????????????????,??????,???????????? -_().,","1","85"});
                    }

                    //Jenkins ?????? ?????? ????????? ?????? ?????? ????????? ??????
                    checkJenkinsFolderNameDuplicate(serverTypeCode, serverZoneCode, jenkinsFolderName);
                }

                if(jenkinsJobCdList != null && jenkinsJobCdList.size() > 0)
                {
                    for(JenkinsJobCdVO jobInfo : jenkinsJobCdList)
                    {
                        Long jenkinsJobCdId = jobInfo.getJenkinsJobCdId();
                        String jenkinsJobName = jobInfo.getJenkinsJobName();

                        
                        //???????????? ?????? Job ??????(jenkinsJobCdId=null)??? Job??? ?????? ??????
                        if(jenkinsJobCdId == null && (jenkinsJobName == null || "".equals(jenkinsJobName)))
                        {
                            //E0009 - Jenkins Job ?????????(???) ??????????????????.
                            throw new ApiException(HttpStatus.OK, "E0009", "Jenkins Job ??????");
                        }else if(jenkinsJobCdId == null)
                        {
                            Matcher match = regexJenkinsPattern.matcher(jenkinsJobName);

                            if(!match.find())
                            {
                                throw new ApiException(HttpStatus.OK, "E0125", new Object[]{"Jenkins job???", "??????????????? ????????? ??? ????????? ??????,??????????????????,??????,???????????? -_().,","1","85"});
                            }

                            //?????? ????????? Job?????? ?????? Job?????? ??????????????? ??????
                            checkJenkinsJobNameDuplicate(serverTypeCode, serverZoneCode, jenkinsFolderName, jenkinsJobName);
                        }

                        jobInfo.setJenkinsJobName(CommonUtil.trim(jenkinsJobName));
                    }
                }else
                {
                    //E0009 - Jenkins ???????????? ???????????????
                    throw new ApiException(HttpStatus.OK, "E0009", "Job ??????");
                }
            }
        }
    }



    /**
     * Jenkins Job ??????(Async ??????)
     * @param createJenkinsJobsVO
     */
    @Async
    public void createJobsAsync(String loginId, Long workflowId, CreateJenkinsJobsVO createJenkinsJobsVO)
    {
        logger.debug("NewJenkinsService createJobsAsync workflowId="+workflowId+", createJobs call!!");
        boolean jenkinsSuccess = createJobs(loginId, workflowId, createJenkinsJobsVO);
        Long projectId = createJenkinsJobsVO.getProjectId();

        //???????????? 0: ????????????, 1: ?????????, 2: ????????????, 3: ??????, 9: ??????
        if (isWorkflowSuccess(projectId))
        {
            updateWorkflowStatus(loginId, projectId, workflowId, 0);
        }else if(!jenkinsSuccess)
        {
            updateWorkflowStatus(loginId, projectId, workflowId, 2);
        }else
        {
            // ?????? Tool ??? ?????????, ?????????????????? ?????? '3' ??? update
            updateWorkflowStatus(loginId, projectId, workflowId, 3);
        }

    }

    /**
     * Jenkins Job ??????2(Async ??????)
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
     * ?????? Workflow Status ??????
     *   !"2": ??????????????? ????????? => true
     *   "2": ?????????????????? ????????? ?????? => false
     * ????????? ????????? task??? ????????? ?????? ????????? ?????????????????? ??????
     * ????????? ????????? task??? ????????? "2"(????????????) ??? ??????
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
                        // ????????? ?????? ??????
                        newJenkinsRestService.createJenkinsFolder(serverTypeCode, serverZoneCode, jenkinsFolderName);

                        // ????????? ?????? ?????? DB ??????
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
                        job.setJenkinsCdId(jenkinsCdId);    // ????????? jenkins_cd.id
                        job.setCreatedBy(loginId);
                        job.setLastModifiedBy(loginId);

                        String jenkinsJobName = job.getJenkinsJobName();
                        String jenkinsJobTypeCode =job.getJenkinsJobTypeCode();
                        String templateJobName = null;

                        try
                        {
                            //FreeStyle Template Folder ?????? ??????
                            if("10301".equals(jenkinsJobTypeCode))
                            {
                                templateJobName = "DSpace_FreeStyle_Template";
                            }
                            //Pipeline Template Folder ?????? ??????
                            else if("10302".equals(jenkinsJobTypeCode))
                            {
                                templateJobName = "DSpace_Pipeline_Template";
                            }

                            logger.info("NewJenkinsService createJobs newJenkinsRestService.createJenkinsJobFromOtherJob serverTypeCode="+serverTypeCode+", serverZoneCode="+serverZoneCode+", jenkinsFolderName="+jenkinsFolderName+", templateFolderName="+templateJobName);
                            // ????????? job ??????(Template Folder ?????? ?????? ???????????? Job ??????
                            newJenkinsRestService.createJenkinsJobFromOtherJob(serverTypeCode, serverZoneCode, jenkinsFolderName, jenkinsJobName, templateJobName);

                            logger.info("NewJenkinsService createJobs newJenkinsMapper.insertJenkinsJob job="+job);
                            // ????????? job ?????? DB ??????
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
                        // ????????? ?????? ??????
                        newJenkinsRestService.createJenkinsFolder(serverTypeCode, serverZoneCode, jenkinsFolderName);

                        // ????????? ?????? ?????? DB ??????
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
                        job.setJenkinsCdId(jenkinsCdId);    // ????????? jenkins_cd.id
                        job.setCreatedBy(loginId);
                        job.setLastModifiedBy(loginId);

                        String jenkinsJobName = job.getJenkinsJobName();
                        String jenkinsJobTypeCode =job.getJenkinsJobTypeCode();
                        String templateJobName = null;

                        try
                        {
                            //FreeStyle Template Folder ?????? ??????
                            if("10301".equals(jenkinsJobTypeCode))
                            {
                                templateJobName = "DSpace_FreeStyle_Template";
                            }
                            //Pipeline Template Folder ?????? ??????
                            else if("10302".equals(jenkinsJobTypeCode))
                            {
                                templateJobName = "DSpace_Pipeline_Template";
                            }

                            logger.info("NewJenkinsService createJobs newJenkinsRestService.createJenkinsJobFromOtherJob serverTypeCode="+serverTypeCode+", serverZoneCode="+serverZoneCode+", jenkinsFolderName="+jenkinsFolderName+", templateFolderName="+templateJobName);
                            // ????????? job ??????(Template Folder ?????? ?????? ???????????? Job ??????
                            newJenkinsRestService.createJenkinsJobFromOtherJob(serverTypeCode, serverZoneCode, jenkinsFolderName, jenkinsJobName, templateJobName);

                            logger.info("NewJenkinsService createJobs newJenkinsMapper.insertJenkinsJob job="+job);
                            // ????????? job ?????? DB ??????
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
     * Jenkins ??????
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
     * Jenkins Job ?????? ?????? Async??? ??????
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
     * Jenkins Job ?????? ?????? Async??? ??????
     * @param createJenkinsJobsVO
     */
    @Async
    public void updateJobsAsync2(String loginId, UpdateJenkinsJobsVO updateJenkinsJobsVO)
    {
        updateJobs2(loginId, updateJenkinsJobsVO);
    }

    /**
     * Jenkins Job ??????
     * 
     * UI??? Jenkins ?????? ??? ????????? ????????????.
     * 1.?????? Jenkins ????????? ????????? ??? ?????? ????????? ?????????.
     * 2.?????? ????????? ????????? ?????? Job ????????? ???????????? ?????????.
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

            // ?????? WorkFlow Task ??????
            workflowTask = workflowMapper.findWorkflowAndWorkflowTask(workflowVo);

            if(workflowTask == null)
            {
                // ?????? WorkFlow Task??? ????????? WorkFlow Task ??????
                workflowTaskId = insertWorkflowTask(loginId, workflowId, "JENKINS_CD", updateJenkinsJobsJson);
            }else
            {
                // ?????? WorkFlow Task ID ?????????
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
                    // ?????? ?????? ?????? ??? ????????? ?????? Job ?????? ??????
                    if(jenkinsCdId == null)
                    {
                        // ?????? ?????? ??????
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

                        // ?????? ?????? ????????? ?????? Job ??????
                        int jobsCount = jobs.size();
                        for(int j=0;j<jobsCount;j++)
                        {
                            JenkinsJobCdVO job = jobs.get(j);
                            String jenkinsJobName = job.getJenkinsJobName();
                            String jenkinsJobTypeCode = job.getJenkinsJobTypeCode();
                            String templateJobName = null;

                            try
                            {
                                //FreeStyle Template Folder ?????? ??????
                                if("10301".equals(jenkinsJobTypeCode))
                                {
                                    templateJobName = "DSpace_FreeStyle_Template";
                                }
                                //Pipeline Template Folder ?????? ??????
                                else if("10302".equals(jenkinsJobTypeCode))
                                {
                                    templateJobName = "DSpace_Pipeline_Template";
                                }

                                // ????????? job ??????(Template Folder ?????? ?????? ???????????? Job ??????)
                                newJenkinsRestService.createJenkinsJobFromOtherJob(serverTypeCode, serverZoneCode, jenkinsFolderName, jenkinsJobName, templateJobName);

                                // ????????? job ?????? DB ??????
                                job.setJenkinsCdId(jenkinsCdId);    // ????????? jenkins_cd.id
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
                    // ????????? Job ?????? ?????? ?????? Job??? ?????? ??????/???????????? ??????
                    // ???, ?????? ?????? ?????? Job??? ?????? ??????/????????? ??????
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

                            // ?????? ????????? ?????? Job ??????
                            if(jenkinsJobCdId == null)
                            {
                                try
                                {
                                    //FreeStyle Template Folder ?????? ??????
                                    if("10301".equals(jenkinsJobTypeCode))
                                    {
                                        templateJobName = "DSpace_FreeStyle_Template";
                                    }
                                    //Pipeline Template Folder ?????? ??????
                                    else if("10302".equals(jenkinsJobTypeCode))
                                    {
                                        templateJobName = "DSpace_Pipeline_Template";
                                    }

                                    // ????????? job ??????(Template Folder ?????? ?????? ???????????? Job ??????)
                                    newJenkinsRestService.createJenkinsJobFromOtherJob(serverTypeCode, serverZoneCode, jenkinsFolderName, jenkinsJobName, templateJobName);

                                    // ????????? job ?????? DB ??????
                                    job.setJenkinsCdId(jenkinsCdId);    // ????????? jenkins_cd.id
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
                            // ?????? ????????? ?????? Job??? ??????/???????????? ????????? ???.
                            else
                            {
                                try
                                {
                                    if(serviceStatus != null && 1 == serviceStatus.intValue())
                                    {
                                        // ????????? job ?????? DB ??????
                                        job.setJenkinsCdId(jenkinsCdId);    // ????????? jenkins_cd.id
                                        job.setCreatedBy(loginId);
                                        job.setLastModifiedBy(loginId);

                                        //????????????
                                        newJenkinsRestService.disableJenkinsJob(serverTypeCode, serverZoneCode, jenkinsFolderName, jenkinsJobName);

                                        updateServiceStatusForJenkinsJob(loginId, jenkinsJobCdId, serviceStatus);
                                    }else if(serviceStatus != null && 2 == serviceStatus.intValue())
                                    {
                                        //?????????
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
     * Jenkins Job2 ??????
     * 
     * UI??? Jenkins ?????? ??? ????????? ????????????.
     * 1.?????? Jenkins ????????? ????????? ??? ?????? ????????? ?????????.
     * 2.?????? ????????? ????????? ?????? Job ????????? ???????????? ?????????.
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
                    // ?????? ?????? ?????? ??? ????????? ?????? Job ?????? ??????
                    if(jenkinsCdId == null)
                    {
                        // ?????? ?????? ??????
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

                        // ?????? ?????? ????????? ?????? Job ??????
                        int jobsCount = jobs.size();
                        for(int j=0;j<jobsCount;j++)
                        {
                            JenkinsJobCdVO job = jobs.get(j);
                            String jenkinsJobName = job.getJenkinsJobName();
                            String jenkinsJobTypeCode = job.getJenkinsJobTypeCode();
                            String templateJobName = null;

                            try
                            {
                                //FreeStyle Template Folder ?????? ??????
                                if("10301".equals(jenkinsJobTypeCode))
                                {
                                    templateJobName = "DSpace_FreeStyle_Template";
                                }
                                //Pipeline Template Folder ?????? ??????
                                else if("10302".equals(jenkinsJobTypeCode))
                                {
                                    templateJobName = "DSpace_Pipeline_Template";
                                }

                                // ????????? job ??????(Template Folder ?????? ?????? ???????????? Job ??????)
                                newJenkinsRestService.createJenkinsJobFromOtherJob(serverTypeCode, serverZoneCode, jenkinsFolderName, jenkinsJobName, templateJobName);

                                // ????????? job ?????? DB ??????
                                job.setJenkinsCdId(jenkinsCdId);    // ????????? jenkins_cd.id
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
                    //????????? Job ?????? ??? ?????? Job ??????/???????????? ??????
                    //?????? ?????? ?????? ?????? Job??? ?????? ??????/????????? ??????
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

                            //?????? ????????? ?????? Job ??????
                            if(jenkinsJobCdId == null)
                            {
                                try
                                {
                                    //FreeStyle Template Folder ?????? ??????
                                    if("10301".equals(jenkinsJobTypeCode))
                                    {
                                        templateJobName = "DSpace_FreeStyle_Template";
                                    }
                                    //Pipeline Template Folder ?????? ??????
                                    else if("10302".equals(jenkinsJobTypeCode))
                                    {
                                        templateJobName = "DSpace_Pipeline_Template";
                                    }

                                    // ????????? job ??????(Template Folder ?????? ?????? ???????????? Job ??????)
                                    newJenkinsRestService.createJenkinsJobFromOtherJob(serverTypeCode, serverZoneCode, jenkinsFolderName, jenkinsJobName, templateJobName);

                                    // ????????? job ?????? DB ??????
                                    job.setJenkinsCdId(jenkinsCdId);    // ????????? jenkins_cd.id
                                    job.setCreatedBy(loginId);
                                    job.setLastModifiedBy(loginId);
                                    newJenkinsMapper.insertJenkinsJob(job);

                                    logger.info("Jenkins Job Create & DB Insert Succeed : jenkinsCdId={}", jenkinsCdId);
                                }catch(Exception e)
                                {
                                    return false;
                                }
                            }
                            //?????? ????????? ?????? Job??? ??????/???????????? ????????? ???.
                            else
                            {
                                try
                                {
                                    if(serviceStatus != null && 1 == serviceStatus.intValue())
                                    {
                                        // ????????? job ?????? DB ??????
                                        job.setJenkinsCdId(jenkinsCdId);    // ????????? jenkins_cd.id
                                        job.setCreatedBy(loginId);
                                        job.setLastModifiedBy(loginId);

                                        //????????????
                                        newJenkinsRestService.disableJenkinsJob(serverTypeCode, serverZoneCode, jenkinsFolderName, jenkinsJobName);

                                        updateServiceStatusForJenkinsJob(loginId, jenkinsJobCdId, serviceStatus);
                                    }else if(serviceStatus != null && 2 == serviceStatus.intValue())
                                    {
                                        //?????????
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
     * CD Jenkins Job ?????? ??????
     * ???????????? ????????? DB????????? jenkins_cd, jenkins_job_cd??? service_status=9(??????)??? ????????????,
     * ?????? CD Jenkins Job?????? disable???.
     * @param projectId
     * @return
     */
    public void deleteJobs(String loginId, Long projectId)
    {
        if(projectId != null)
        {
            // Jenkins job disable ??????
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

                    // jenkins_job_cd service_status 0 -> 9 ????????????
                    deleteServiceStatusForJenkinsJob(loginId, jenkinsJobCdId);

                    /*
                     * jenkins_cd service_status 0 -> 9 ????????????
                     * ????????? ?????? ??????(jenkins_cd)??? ????????? ?????? ??????????????? ?????? ?????? 
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
     * jenkins_job_cd ???????????? id ???????????? ?????? Job disable=1 ?????? enable=2 ??????
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
     * Jenkins_cd ????????? Delete ??????
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
     * jenkins_job_cd ???????????? id ???????????? ?????? Job Delete ??????
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
     * jenkins_job_cd ???????????? jenkins_cd_id???????????? ?????? Job Delete ??????
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
     * Jenkins ????????? ?????? ??????
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
            //E0016 - ????????? Jenkins ?????????(XXXX)???(???) ???????????????.
            throw new ApiException(HttpStatus.OK, "E0016", "Jenkins ?????????("+jenkinsFolderName+")");
        }
    }

    /**
     * ?????? ????????? Jenkins Job?????? ?????? Job?????? ????????? ??????????????? ??????
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

        //???????????? ?????? ????????? ??????
        jobBuildInfoVO = newJenkinsRestService.getJenkinsJobBuildList(serverTypeCode, serverZoneCode, jenkinsFolderName, jenkinsJobName);

        //?????? ?????? ?????? ?????? Job?????? ????????????, ??? Job??? disable ??? ????????? ??????
        if(jobBuildInfoVO != null && jobBuildInfoVO.isDisabled())
        {
            //E0122 - {0}??? ?????? ?????? ????????? {1}????????? ????????? {2}?????? ????????? ????????????.
            throw new ApiException(HttpStatus.OK, "E0122", new Object[]{jenkinsJobName, "Job???(????????????)", "??????"});
        }
        //?????? ????????? ?????? Job?????? ????????? ??????
        else if(jobBuildInfoVO != null)
        {
            //E0016 - ????????? Jenkins Job???(XXXX)???(???) ???????????????.
            throw new ApiException(HttpStatus.OK, "E0016", "Jenkins Job???("+jenkinsJobName+")");
        }
    }

    /**
     * Jenkins Folder??? ?????? ??????(
     * @param CheckFolderNameParamVO - ?????? ?????? ????????????(10101 - TB, 10102 - PRD), ?????? ?????? ????????????(10201 - RM, 10202 - IPC, 10203 - EPC, 10204 - Container), Jenkins Folder???
     * @return ResponseEntity - ?????? Entity
     */
    public ResponseEntity<ApiResponseEntity> existJenkinsFolderName(CheckFolderNameParamVO checkFolderNameParamVO)
    {
        String serverTypeCode = checkFolderNameParamVO.getServerTypeCode();
        String serverZoneCode = checkFolderNameParamVO.getServerZoneCode();
        String jenkinsFolderOrJobName = checkFolderNameParamVO.getJenkinsFolderName();
        ApiResponseEntity message = null;

        if(serverTypeCode == null || "".equals(serverTypeCode))
        {
            //E0009 - Jenkins ????????????(???) ??????????????????.
            throw new ApiException(HttpStatus.OK, "E0009", "?????? ?????? ????????????(10101 - TB, 10102 - PRD)");
        }

        if(serverZoneCode == null || "".equals(serverZoneCode))
        {
            //E0009 - ?????? ?????? ????????????(10201 - RM, 10202 - IPC, 10203 - EPC, 10204 - Container)???(???) ??????????????????.
            throw new ApiException(HttpStatus.OK, "E0009", "?????? ?????? ????????????(10201 - RM, 10202 - IPC, 10203 - EPC, 10204 - Container)");
        }

        if(jenkinsFolderOrJobName == null || "".equals(jenkinsFolderOrJobName))
        {
            //E0009 - Jenkins Folder??? ?????? Job??????(???) ??????????????????.
            throw new ApiException(HttpStatus.OK, "E0009", "Jenkins Folder??? ?????? Job???");
        }

        boolean isDuplicated = newJenkinsRestService.getValidJenkinsFolderNm(serverTypeCode, serverZoneCode, jenkinsFolderOrJobName);

        String statusMsg = DbMessageManager.getMessage("I0001", "ko");

        if(isDuplicated)
        {
            //E0016 - ????????? Jenkins ??????(XXXX)???(???) ???????????????.
            message = new ApiResponseEntity(statusMsg, isDuplicated, "E0016", "Jenkins ??????("+jenkinsFolderOrJobName+")");
        }else
        {
            message = new ApiResponseEntity(statusMsg, isDuplicated, null, null);
        }

        return new ResponseEntity<ApiResponseEntity>(message, HttpStatus.OK);
    }

    /**
     * Jenkins Job ?????? ?????? ?????? ??????
     * @param CheckFolderNameParamVO - ?????? ?????? ????????????(10101 - TB, 10102 - PRD)
     *                              ?????? ?????? ????????????(10201 - RM, 10202 - IPC, 10203 - EPC, 10204 - Container)
     *                              Jenkins Folder???
     *                              Jenkins Job??? 
     * @return ResponseEntity - ?????? Entity
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
            //E0009 - Jenkins ????????????(???) ??????????????????.
            throw new ApiException(HttpStatus.OK, "E0009", "?????? ?????? ????????????(10101 - TB, 10102 - PRD)");
        }

        if(serverZoneCode == null || "".equals(serverZoneCode))
        {
            //E0009 - ?????? ?????? ????????????(10201 - RM, 10202 - IPC, 10203 - EPC, 10204 - Container)???(???) ??????????????????.
            throw new ApiException(HttpStatus.OK, "E0009", "?????? ?????? ????????????(10201 - RM, 10202 - IPC, 10203 - EPC, 10204 - Container)");
        }

        if(jenkinsFolderName == null || "".equals(jenkinsFolderName))
        {
            //E0009 - Jenkins Folder??????(???) ??????????????????.
            throw new ApiException(HttpStatus.OK, "E0009", "Jenkins Folder???");
        }

        if(jenkinsJobName == null || "".equals(jenkinsJobName))
        {
            //E0009 - Jenkins Job??????(???) ??????????????????.
            throw new ApiException(HttpStatus.OK, "E0009", "Jenkins Job???");
        }

        String statusMsg = DbMessageManager.getMessage("I0001", "ko");

        JobBuildInfoVO jenkinsJobBuildInfo = newJenkinsRestService.getJenkinsJobBuildList(serverTypeCode, serverZoneCode, jenkinsFolderName, jenkinsJobName);

        if(jenkinsJobBuildInfo != null)
        {
            message = new ApiResponseEntity(statusMsg, jenkinsJobBuildInfo, null, null);
        }else
        {
            //I0006 - ???????????? ???????????? ????????????.
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
     * ProjectID / projectId / UserID / RoleAction ?????? Role ?????? (??????/??????/????????? ??????)
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
     * ???????????? ID ???????????? ????????? ?????? Jenkins job??? ????????? ?????? ?????? job ?????? ??????
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
     * ???????????? ID ???????????? ?????? Jenkins Job ?????? ????????? ?????????.
     * @param projectId - ???????????? ID
     * @return FindJenkinsJobsVO - Jenkins Job ?????? ??????
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
     * ???????????? ID ???????????? ????????? ?????? Jenkins job??? ????????? ?????? ?????? job Log ?????? ??????
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
