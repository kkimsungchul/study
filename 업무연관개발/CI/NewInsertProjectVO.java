

import java.util.ArrayList;
import java.util.List;

import portal.member.vo.GetMembersVO;
import portal.jira.vo.GetProjectJiraVO;
import portal.project.codeeyes.vo.GetCodeEyesVO;
import portal.project.gitlab.vo.InsertGitLabVO;
import portal.project.jenkins.vo.InsertJenkinsVO;
import portal.project.sparrow.vo.GetSparrowsVO;
import portal.user.vo.GetUserToolsLogin;

import io.swagger.annotations.ApiModelProperty;

public class NewInsertProjectVO {
    /**
     * 순번 - 프로젝트 ID
     */
    @ApiModelProperty(required = true, name = "id", value = "순번 - 프로젝트 ID", dataType = "Long")
    private Long id;
	
    /**
     * 프로젝트 그룹 ID
     */
    @ApiModelProperty(name = "projectGroupId", value = "프로젝트 그룹 ID", dataType = "Long")
    private Long projectGroupId;
    
    /**
     * 프로젝트 그룹 Slug
     */
    @ApiModelProperty(name = "projectGroupSlug", value = "프로젝트 그룹 Slug", dataType = "String")
    private String projectGroupSlug;
    
    /**
     * 프로젝트명
     */
    @ApiModelProperty(name = "projectName", value = "프로젝트명", dataType = "String")
    private String projectName;
    
    /**
     * 프로젝트 설명
     */
    @ApiModelProperty(name = "projectDesc", value = "프로젝트 설명", dataType = "String")
    private String projectDesc;

    /**
     * 프로젝트 Slug
     */
    @ApiModelProperty(name = "projectSlug", value = "프로젝트 Slug", dataType = "String")
    private String projectSlug;
    
    /**
     * 애플리케이션에 소속된 MemberList VO
     */
    @ApiModelProperty(name = "members", value = "애플리케이션에 소속된 MemberList VO", dataType = "List")
    private List<GetMembersVO> members = new ArrayList<>();
    
    /**
     * 애플리케이션에 소속된 Jira VO
     */
    @ApiModelProperty(name = "jira", value = "애플리케이션에 소속된 Jira VO", dataType = "JiraVO")
    private GetProjectJiraVO jira;
    
    /**
     * 애플리케이션에 소속된 GitList VO
     */
    @ApiModelProperty(name = "git", value = "애플리케이션에 소속된 GitList VO", dataType = "InsertGitLabVO")
    private InsertGitLabVO git;
    
    /**
     * 애플리케이션에 소속된 JenkinsList VO
     */
    @ApiModelProperty(name = "jenkins", value = "애플리케이션에 소속된 JenkinsList VO", dataType = "InsertJenkinsVO")
    private NewInsertJenkinsVO jenkins;
    
    /**
     * 애플리케이션에 소속된 SparrowList VO
     */
    @ApiModelProperty(name = "sparrow", value = "애플리케이션에 소속된 SparrowList VO", dataType = "List")
    private List<GetSparrowsVO> sparrows = new ArrayList<>();
    
    /**
     * 애플리케이션에 소속된 CodeEyesList VO
     */
    @ApiModelProperty(name = "codeeye", value = "애플리케이션에 소속된 CodeeyeList VO", dataType = "List")
    private List<GetCodeEyesVO> codeeyes = new ArrayList<>();
    
    /** 각 Tool 별 접속여부 VO
     * => JIRA / Git / Jenkins / Nexus (추후 Tool 은 추가될 수 있음)
     */
    @ApiModelProperty(name = "exists", value = "Tool 별 접속여부 VO", dataType = "GetUserToolsLogin")
    private GetUserToolsLogin exists;
    
    /**
     * 등록자
     */
    @ApiModelProperty(name = "createdBy", value = "등록자", dataType = "String")
    private String createdBy;
    
    /**
     * 수정자
     */
    @ApiModelProperty(name = "lastModifiedBy", value = "수정자", dataType = "String")
    private String lastModifiedBy;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getProjectGroupId() {
		return projectGroupId;
	}

	public void setProjectGroupId(Long projectGroupId) {
		this.projectGroupId = projectGroupId;
	}

	public String getProjectGroupSlug() {
		return projectGroupSlug;
	}

	public void setProjectGroupSlug(String projectGroupSlug) {
		this.projectGroupSlug = projectGroupSlug;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getProjectDesc() {
		return projectDesc;
	}

	public void setProjectDesc(String projectDesc) {
		this.projectDesc = projectDesc;
	}

	public String getProjectSlug() {
		return projectSlug;
	}

	public void setProjectSlug(String projectSlug) {
		this.projectSlug = projectSlug;
	}

	public List<GetMembersVO> getMembers() {
		return members;
	}

	public void setMembers(List<GetMembersVO> members) {
		this.members = members;
	}

	public GetProjectJiraVO getJira() {
		return jira;
	}

	public void setJira(GetProjectJiraVO jira) {
		this.jira = jira;
	}

	public InsertGitLabVO getGit() {
		return git;
	}

	public void setGit(InsertGitLabVO git) {
		this.git = git;
	}

	public NewInsertJenkinsVO getJenkins() {
		return jenkins;
	}

	public void setJenkins(NewInsertJenkinsVO jenkins) {
		this.jenkins = jenkins;
	}

	public List<GetSparrowsVO> getSparrows() {
		return sparrows;
	}

	public void setSparrows(List<GetSparrowsVO> sparrows) {
		this.sparrows = sparrows;
	}

	public List<GetCodeEyesVO> getCodeeyes() {
		return codeeyes;
	}

	public void setCodeeyes(List<GetCodeEyesVO> codeeyes) {
		this.codeeyes = codeeyes;
	}

	public GetUserToolsLogin getExists() {
		return exists;
	}

	public void setExists(GetUserToolsLogin exists) {
		this.exists = exists;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	@Override
	public String toString() {
		return "NewInsertProjectVO [id=" + id + ", projectGroupId=" + projectGroupId + ", projectGroupSlug="
				+ projectGroupSlug + ", projectName=" + projectName + ", projectDesc=" + projectDesc + ", projectSlug="
				+ projectSlug + ", members=" + members + ", jira=" + jira + ", git=" + git + ", jenkins=" + jenkins
				+ ", sparrows=" + sparrows + ", codeeyes=" + codeeyes + ", exists=" + exists + ", createdBy="
				+ createdBy + ", lastModifiedBy=" + lastModifiedBy + "]";
	}

}
