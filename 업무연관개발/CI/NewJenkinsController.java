
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import portal.common.ApiResponseEntity;
import portal.message.manage.DbMessageManager;
import portal.project.jenkins.JenkinsService;
import portal.project.vo.InsertProjectVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "NewJenkinsController")
@RestController
@RequestMapping(value = "/Newjenkins")
public class NewJenkinsController
{
	@Autowired
	private NewJenkinsService newJenkinsService;

	
	
	@ApiOperation(value = "Jenkins 목록 조회",notes = "프로젝트에 포함되어 있는 Jenkins 목록 조회")
	@RequestMapping(value = "/jenkins/{projectId}", method = RequestMethod.GET)
	public ResponseEntity<ApiResponseEntity> getJenkinsList(@PathVariable("projectId") Long projectId ) {
		NewJenkinsParamVO jenkins = newJenkinsService.getJenkinsList(projectId);
		String statusMsg = DbMessageManager.getMessage("I0001", "ko");
		ApiResponseEntity message = new ApiResponseEntity(statusMsg, jenkins, null, null);

		return new ResponseEntity<ApiResponseEntity>(message, HttpStatus.OK);   
	}
	
	
	@ApiOperation(value = "Jenkins 받아보기",notes = "프로젝트에 포함되어 있는 Jenkins 목록 조회")
	@RequestMapping(value = "/setJenkins", method = RequestMethod.POST)
	public void setJenkinsList(@RequestBody NewInsertJenkinsVO newInsertJenkinsVO ) {
		System.out.println(newInsertJenkinsVO);
	}
	
	@ApiOperation(value = "Jenkins 등록",notes = "프로젝트에 포함되어 있는 Jenkins 목록 조회")
	@RequestMapping(value = "/jenkins", method = RequestMethod.POST)
	public void insertJenkins(@RequestBody  NewInsertProjectVO newInsertProjectVO ) {
		if(newJenkinsService.insertJenkinsDBValidationCheck(newInsertProjectVO.getJenkins()).isEmpty()) {
			newJenkinsService.doJenkins(newInsertProjectVO);	
		}
	}
	
	@ApiOperation(value = "Jenkins insert 중복체크",notes = "프로젝트 생성시 전달되는 insertvo를 그대로 받아서 젠킨스만 중복체크함")
	@RequestMapping(value = "/jenkins/insertValidationCheck", method = RequestMethod.POST)
	public void jenkinsValidationCheck(@RequestBody  NewInsertProjectVO newInsertProjectVO ) {
		System.out.println(newJenkinsService.insertJenkinsDBValidationCheck(newInsertProjectVO.getJenkins()));
	}
	
	@ApiOperation(value = "Jenkins 수정",notes = "프로젝트 수정시 전달되는 projectvo를 그대로 받아서 젠킨스만 중복체크함")
	@RequestMapping(value = "/jenkins", method = RequestMethod.PUT)
	public void jenkinsUpdate(@RequestBody  NewUpdateProjectVO newUpdateProjectVO ) {
		
		if(newJenkinsService.updateJenkinsDBValidationCheck(newUpdateProjectVO.getJenkins()).isEmpty()) {
			newJenkinsService.doJenkinsUpdate(newUpdateProjectVO);	
		}
		
	}
	
	@ApiOperation(value = "Jenkins update 중복체크",notes = "프로젝트 수정시 전달되는 insertvo를 그대로 받아서 젠킨스만 중복체크함")
	@RequestMapping(value = "/jenkins/updateValidationCheck", method = RequestMethod.POST)
	public void updateValidationCheck(@RequestBody  NewUpdateProjectVO newUpdateProjectVO ) {
		System.out.println(newJenkinsService.updateJenkinsDBValidationCheck(newUpdateProjectVO.getJenkins()));
	}
	
	//의사결정필요
	@ApiOperation(value = "Jenkins 삭제",notes = "프로젝트 삭제시 전달되는 insertvo를 그대로 받아서 젠킨스만 disable 처리함")
	@RequestMapping(value = "/jenkins/{projectId}", method = RequestMethod.DELETE)
	public void deleteJenkins(@PathVariable("projectId") Long projectId  ) {
		newJenkinsService.jenkinsDisableList(projectId);
	}
}