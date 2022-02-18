
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "NewInsertJenkinsVO", description = "젠킨스 insert VO")
public class NewInsertJenkinsVO {
	
    /**
     * Jenkins id
     */
	@ApiModelProperty(name = "id", value = "Jenkins id", dataType = "String")
	public int id;
	
    /**
     * 프로젝트 ID
     */
    @ApiModelProperty(name = "projectId", value = "프로젝트 ID", dataType = "Long")
    private Long projectId;
    /**
     * Jenkins 폴더명
     */
	@ApiModelProperty(name = "jenkinsFolderName", value = "Jenkins 폴더명", dataType = "String")
	public String jenkinsFolderName;
	
    /**
     * Jenkins job 명
     */
	@ApiModelProperty(name = "jenkinsJobName", value = "Jenkins job 명", dataType = "String")
	public String jenkinsJobName;
	
    /**
     * jenkins service status
     */
    @ApiModelProperty(name = "serviceStatus", value = "Jenkins 상태코드", dataType = "Integer")
    private Integer serviceStatus;
	
    /**
     * Jenkins 등록자
     */
    @ApiModelProperty(name = "createdBy", value = "Jenkins 등록자", dataType = "String")
    private String createdBy;
    
    /**
     * Jenkins 등록일시
     */
    @ApiModelProperty(name = "createdDate", value = "Jenkins 등록일시", dataType = "String")
    private String createdDate;
    /**
     * Jenkins 수정자
     */
    @ApiModelProperty(name = "lastModifiedBy", value = "Jenkins 수정자", dataType = "String")
    private String lastModifiedBy;
    
    /**
     * Jenkins 수정일시
     */
    @ApiModelProperty(name = "lastModifiedDate", value = "Jenkins 수정일시", dataType = "String")
    private String lastModifiedDate;

    /**
     * Jenkins job 타입명(FreeStyle or Pipeline or...)
     */
    @ApiModelProperty(name = "jenkinsJobType", value = "Jenkins Job 타입 코드(10301 - FreeStyle, 10302 - Pipeline)", dataType = "String", example = "10302")
    public String jenkinsJobType;
	
    /**
     * Jenkins job 타입명(FreeStyle or Pipeline or...)
     */
    @ApiModelProperty(name = "jenkinsJobTypeName", value = "Jenkins Job 타입명(FreeStyle, Pipeline)", dataType = "String", example = "Pipeline")
    private String jenkinsJobTypeName;
    
    /**
     * 구분코드 1: folder, 2: job
     */
    @ApiModelProperty(name = "division", value = "구분코드 1: folder, 2: job", dataType = "String", example = "10302")
    public int division;
    
    /**
     * Jenkins deleteFlag 
     */
    @ApiModelProperty(name = "deleteFlag", value = "Jenkins deleteFlag 값이 존재할경우 삭제 (1)", dataType = "String", example = "1")
	public String deleteFlag;
	

	@ApiModelProperty(name = "folders", value = "폴더목록", dataType = "List")
	public List<NewInsertJenkinsVO> folders;
	
	@ApiModelProperty(name = "jobs", value = "job목록", dataType = "List")
	public List<NewInsertJenkinsVO> jobs;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public String getJenkinsFolderName() {
		return jenkinsFolderName;
	}

	public void setJenkinsFolderName(String jenkinsFolderName) {
		this.jenkinsFolderName = jenkinsFolderName;
	}

	public String getJenkinsJobName() {
		return jenkinsJobName;
	}

	public void setJenkinsJobName(String jenkinsJobName) {
		this.jenkinsJobName = jenkinsJobName;
	}

	public Integer getServiceStatus() {
		return serviceStatus;
	}

	public void setServiceStatus(Integer serviceStatus) {
		this.serviceStatus = serviceStatus;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public String getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(String lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public String getJenkinsJobType() {
		return jenkinsJobType;
	}

	public void setJenkinsJobType(String jenkinsJobType) {
		this.jenkinsJobType = jenkinsJobType;
	}

	public String getJenkinsJobTypeName() {
		return jenkinsJobTypeName;
	}

	public void setJenkinsJobTypeName(String jenkinsJobTypeName) {
		this.jenkinsJobTypeName = jenkinsJobTypeName;
	}

	public int getDivision() {
		return division;
	}

	public void setDivision(int division) {
		this.division = division;
	}

	public String getDeleteFlag() {
		return deleteFlag;
	}

	public void setDeleteFlag(String deleteFlag) {
		this.deleteFlag = deleteFlag;
	}

	public List<NewInsertJenkinsVO> getFolders() {
		return folders;
	}

	public void setFolders(List<NewInsertJenkinsVO> folders) {
		this.folders = folders;
	}

	public List<NewInsertJenkinsVO> getJobs() {
		return jobs;
	}

	public void setJobs(List<NewInsertJenkinsVO> jobs) {
		this.jobs = jobs;
	}

	@Override
	public String toString() {
		return "NewInsertJenkinsVO [id=" + id + ", projectId=" + projectId + ", jenkinsFolderName=" + jenkinsFolderName
				+ ", jenkinsJobName=" + jenkinsJobName + ", serviceStatus=" + serviceStatus + ", createdBy=" + createdBy
				+ ", createdDate=" + createdDate + ", lastModifiedBy=" + lastModifiedBy + ", lastModifiedDate="
				+ lastModifiedDate + ", jenkinsJobType=" + jenkinsJobType + ", jenkinsJobTypeName=" + jenkinsJobTypeName
				+ ", division=" + division + ", deleteFlag=" + deleteFlag + ", folders=" + folders + ", jobs=" + jobs
				+ "]";
	}

}
