

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import cd.project.jenkins.vo.FindJenkinsJobListVO;
import cd.project.jenkins.vo.JenkinsCdVO;
import cd.project.jenkins.vo.JenkinsJobCdVO;

@Mapper
public interface NewJenkinsMapper {
	
	public Long insertJenkins(JenkinsCdVO vo);

	public void insertJenkinsJob(JenkinsJobCdVO vo);

	public List<JenkinsCdVO> findJenkins(JenkinsCdVO vo);

	public List<FindJenkinsJobListVO> findJenkinsJobList(Long projectId);

	
	public void updateJenkinsServiceStatus(JenkinsCdVO jenkins);

	public void updateJenkinsJobServiceStatus(JenkinsCdVO jenkins);

	public void updateJenkins(JenkinsCdVO jenkins);
	
	
	
	public boolean insertValidJenkinsCheck(NewInsertJenkinsVO newInsertJenkinsVO);
}
