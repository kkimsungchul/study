

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cd.common.ApiResponseEntity;
import cd.common.KeyCloakSession;
import cd.message.manage.DbMessageManager;
import cd.project.jenkins.vo.CheckFolderNameParamVO;
import cd.project.jenkins.vo.CreateJenkinsJobsVO;
import cd.project.jenkins.vo.DeleteJenkinsJobsVO;
import cd.project.jenkins.vo.FindJenkinsJobsVO;
import cd.project.jenkins.vo.GetJenkinsJobBuildListParamVO;
import cd.project.jenkins.vo.JobBuildInfoVO;
import cd.project.jenkins.vo.LastBuildJobLogVO;
import cd.project.jenkins.vo.LastBuildJobVO;
import cd.project.jenkins.vo.UpdateJenkinsJobsVO;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "NewJenkinsController")
@RestController
@RequestMapping(value="/newJenkins")
public class NewJenkinsController
{
    private static final Logger logger = LoggerFactory.getLogger(NewJenkinsController.class);

    @Autowired
    private NewJenkinsService newJenkinsService;

    @Autowired
    private KeyCloakSession keyCloakSession;

    /**
     * 프로젝트 ID 기준으로 전체 Jenkins Job 상세 목록
     */
    @ApiOperation(value = "프로젝트 ID 기준으로 전체 Jenkins Job 상세 조회", notes = "프로젝트 ID 기준으로 전체 Jenkins Job 목록 조회합니다.", response = FindJenkinsJobsVO.class)
    @GetMapping(value = "/jobs/{projectId}")
    public ResponseEntity<ApiResponseEntity> findJenkinsJobList(@PathVariable("projectId") Long projectId)
    {
        FindJenkinsJobsVO jenkins = newJenkinsService.findJenkinsJobList(projectId);

        String statusMsg = DbMessageManager.getMessage("I0001", "ko");
        ApiResponseEntity message = new ApiResponseEntity(statusMsg, jenkins, null, null);

        return new ResponseEntity<ApiResponseEntity>(message, HttpStatus.OK);
    }

    /**
     * CD Jenkins Job 생성
     * 
     * @param project
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "CD Jenkins Job 생성", notes = "CD Jenkins Job 생성합니다." , response = CreateJenkinsJobsVO.class)
    @PostMapping(value = "/createJob")
    public ResponseEntity<ApiResponseEntity> createJob(@RequestBody CreateJenkinsJobsVO createJenkinsJobsVO)
    {
        newJenkinsService.createJobValidation(createJenkinsJobsVO);

        String loginId = keyCloakSession.getLoginId();

        logger.debug("NewJenkinsController createJob created loginId="+loginId);
        newJenkinsService.createJobsAsync2(loginId, createJenkinsJobsVO);

        ApiResponseEntity message = new ApiResponseEntity(DbMessageManager.getMessage("I0001", "ko"), true, null, null);

        return new ResponseEntity<ApiResponseEntity>(message, HttpStatus.OK);
    }

    /**
     * CD Jenkins Job 수정
     * @param projectDto
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "CD Jenkins Job 수정", notes = "CD Jenkins Job 수정합니다." , response = UpdateJenkinsJobsVO.class)
    @PostMapping(value = "/updateJobs")
    public ResponseEntity<ApiResponseEntity> updateJobs(@RequestBody UpdateJenkinsJobsVO updateJenkinsJobsVO)
    {
        newJenkinsService.updateJobValidation(updateJenkinsJobsVO);

        String loginId = keyCloakSession.getLoginId();

        logger.debug("NewJenkinsController createJob created loginId="+loginId);
        newJenkinsService.updateJobsAsync2(loginId, updateJenkinsJobsVO);

        ApiResponseEntity message = new ApiResponseEntity(DbMessageManager.getMessage("I0001", "ko"), true, null, null);

        return new ResponseEntity<ApiResponseEntity>(message, HttpStatus.OK);
    }

    /**
     * CD Jenkins Job 삭제 처리
     * 프로젝트 삭제시 DB상에만 jenkins_cd, jenkins_job_cd의 service_status=9(삭제)로 변경하고,
     * 실제 CD Jenkins Job들은 disable됨.
     * @param projectDto
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "CD Jenkins Job 삭제(프로젝트내 전체 jenkins job 대상)", notes = "CD Jenkins Job(프로젝트내 전체 jenkins job 대상) 삭제합니다." , response = DeleteJenkinsJobsVO.class)
    @PostMapping(value = "/deleteJobs")
    public ResponseEntity<ApiResponseEntity> deleteJobs(@RequestBody DeleteJenkinsJobsVO updateJenkinsJobsVO)
    {
        String loginId = keyCloakSession.getLoginId();
        Long projectId = updateJenkinsJobsVO.getProjectId();

        logger.debug("NewJenkinsController deleteJobs loginId="+loginId+", projectId="+projectId);

        if(projectId != null)
        {
        	newJenkinsService.deleteJobs(loginId, projectId);
        }

        ApiResponseEntity message = new ApiResponseEntity(DbMessageManager.getMessage("I0001", "ko"), true, null, null);

        return new ResponseEntity<ApiResponseEntity>(message, HttpStatus.OK);
    }

    /**
     * 프로젝트 ID 기준으로 소속된 전체 Jenkins job들 중에서 최근 실행 Job 정보 조회
     */
    @ApiOperation(value = "프로젝트 ID 기준으로 소속된 전체 Jenkins job들 중에서 최근 실행 Job 정보 조회",notes = "프로젝트 ID 기준으로 소속된 전체 Jenkins job들 중에서 최근 실행 Job 정보 조회합니다.", response = LastBuildJobVO.class, responseContainer = "List")
    @GetMapping(value = "/lastBuild/{projectId}")
    public ResponseEntity<ApiResponseEntity> findLastBuildJobList(@PathVariable("projectId") Long projectId)
    {
        List<LastBuildJobVO> jenkins = newJenkinsService.findLastBuildJobList(projectId);

        String statusMsg = DbMessageManager.getMessage("I0001", "ko");
        ApiResponseEntity message = new ApiResponseEntity(statusMsg, jenkins, null, null);

        return new ResponseEntity<ApiResponseEntity>(message, HttpStatus.OK);
    }

    /**
     * 프로젝트 ID 기준으로 소속된 전체 Jenkins job들 중에서 최근 실행 Job Log 정보 조회
     */
    @ApiOperation(value = "프로젝트 ID 기준으로 소속된 전체 Jenkins job들 중에서 최근 실행 Job Log 정보 조회",notes = "프로젝트 ID 기준으로 소속된 전체 Jenkins job들 중에서 최근 실행 Job Log 정보 조회합니다.", response = LastBuildJobLogVO.class, responseContainer = "List")
    @GetMapping(value = "/lastBuild/logText/{projectId}")
    public ResponseEntity<ApiResponseEntity> findLastBuildJobLogTextList(@PathVariable("projectId") Long projectId)
    {
        List<LastBuildJobLogVO> jenkins = newJenkinsService.findLastBuildJobLogTextList(projectId);

        String statusMsg = DbMessageManager.getMessage("I0001", "ko");
        ApiResponseEntity message = new ApiResponseEntity(statusMsg, jenkins, null, null);

        return new ResponseEntity<ApiResponseEntity>(message, HttpStatus.OK);
    }

    /**
     * CD Jenkins 폴더 존재 여부 체크
     * @param projectDto
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "Jenkins 폴더 존재 여부 체크", notes = "Jenkins 폴더 존재 여부 체크합니다." , response = Boolean.class)
    @PostMapping(value = "/existJenkinsFolderName")
    public ResponseEntity<ApiResponseEntity> existJenkinsFolderName(@RequestBody CheckFolderNameParamVO checkFolderNameParamVO)
    {
        return newJenkinsService.existJenkinsFolderName(checkFolderNameParamVO);
    }

    /**
     * CD Jenkins Job 빌드 정보 목록 조회
     * @param GetJenkinsJobBuildListParamVO
     * @return
     */
    @ApiOperation(value = "CD Jenkins Job 빌드 정보 목록 조회", notes = "CD Jenkins Job 빌드 정보 목록 조회합니다." , response = JobBuildInfoVO.class)
    @PostMapping(value = "/getJenkinsJobBuildList")
    public ResponseEntity<ApiResponseEntity> getJenkinsJobBuildList(@RequestBody GetJenkinsJobBuildListParamVO getJenkinsJobBuildListParamVO)
    {
        return newJenkinsService.getJenkinsJobBuildList(getJenkinsJobBuildListParamVO);
    }
    
    
    
    
    
    
    /////////////////////
    /**
     * CD Jenkins 중복체크
     * @param NewInsertJenkinsVO
     * @return
     */
	@ApiOperation(value = "Jenkins insert 중복체크",notes = "Jenkins 의 폴더와 job의 중복체크를 함")
	@RequestMapping(value = "/insertValidationCheck", method = RequestMethod.POST)
	public void jenkinsValidationCheck(@RequestBody  NewInsertJenkinsVO newInsertJenkinsVO ) {
		System.out.println(newInsertJenkinsVO);
		System.out.println(newJenkinsService.insertJenkinsDBValidationCheck(newInsertJenkinsVO));
	}
    /**
     * CD Jenkins 등록
     * @param NewInsertJenkinsVO
     * @return
     */
	@ApiOperation(value = "Jenkins 등록",notes = "Jnekins 등록")
	@RequestMapping(value = "/jenkins", method = RequestMethod.POST)
	public void insertJenkins(@RequestBody  NewInsertJenkinsVO newInsertJenkinsVO ) {
		if(newJenkinsService.insertJenkinsDBValidationCheck(newInsertJenkinsVO).isEmpty()) {
			//newJenkinsService.doJenkins(newInsertProjectVO);	
		}
	}
    
}
