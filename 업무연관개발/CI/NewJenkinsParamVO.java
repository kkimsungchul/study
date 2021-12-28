

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "New 잰킨스 VO", description = "New 잰킨스 VO")
public class NewJenkinsParamVO {

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
	@ApiModelProperty(name = "jenkinsFolderName", value = "folder명", dataType = "String")
	public String jenkinsFolderName;
	
    /**
     * Jenkins job 명
     */
	@ApiModelProperty(name = "jenkinsJobName", value = "job명", dataType = "String")
	public String jenkinsJobName;
	
    /**
     * folder job 구분
     */
	@ApiModelProperty(name = "flagClass", value = "folder , job 구분", dataType = "String")
	public String flagClass;
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
    /**
     * Jenkins URL , 해당 폴더나 job의 URL
     */
	@ApiModelProperty(name = "url", value = "jenkins url", dataType = "String")
	public String url;
	
	@ApiModelProperty(name = "newJenkinsFolderList", value = "folder 목록", dataType = "String")
	public ArrayList<NewJenkinsParamVO> folders;
	
	@ApiModelProperty(name = "newJenkinsJobList", value = "job 목록", dataType = "String")
	public ArrayList<NewJenkinsParamVO> jobs;
	
    /**
     * jenkins service status
     */
    @ApiModelProperty(name = "serviceStatus", value = "Jenkins 상태코드", dataType = "Integer")
    private Integer serviceStatus;
	
    public NewJenkinsParamVO() {
		super();
		folders=new ArrayList<>();
		jobs=new ArrayList<>();
	}
	public NewJenkinsParamVO(JSONObject object) {
    	
		folders=new ArrayList<>();
		jobs=new ArrayList<>();
        
    	if(object.has("_class")) {
	        if(object.isNull("_class")) {
	        	this.flagClass = "";
	        } else {
	        	String tempFlagClass[] = object.get("_class").toString().split("\\.");
	        	this.flagClass = tempFlagClass[tempFlagClass.length-1];
	        	if(this.flagClass.contains("Folder")) {
	        		this.jenkinsFolderName = object.getString("name");
	        		this.division = 1;
	        	}else {
	        		this.jenkinsJobName = object.getString("name");
	        		this.division = 2;
	        		if(this.flagClass.equals("FreeStyleProject")) {
		        		this.jenkinsJobTypeName="FreeStyle";
		        		this.jenkinsJobType="10401";
	        		}else if(this.flagClass.equals("WorkflowJob")) {
		        		this.jenkinsJobTypeName="Pipeline";
		        		this.jenkinsJobType="10402";
	        		}

	        	}
	        	
	        }
    	}
    	if(object.has("url")) {
    		if(object.isNull("url")) {
	        	this.url = "";
	        } else {
	        	this.url = object.getString("url");
	        	
	        }
    	}
    	if(object.has("color")) {
    		if(object.isNull("color")) {
	        	this.serviceStatus = 0;
	        } else {
	        	if(object.get("color").equals("disabled")) {
	        		this.serviceStatus = 1;
	        	}else {
	        		this.serviceStatus = 2;
	        	}
	        }
    	}else {
    		this.serviceStatus=0;
    	}
    	

    	if(object.has("jobs")) {
    		//하위 jobs 존재여부 확인
	    	if(object.get("jobs") != JSONObject.NULL) {
	    		JSONArray jsonArray = (JSONArray) object.get("jobs");
	    		for(int i=0;i<jsonArray.length();i++) {
	    			
	    			if(((String) jsonArray.getJSONObject(i).get("_class")).contains("Folder")) {
	    				NewJenkinsParamVO tempNewJenkinsVO = new NewJenkinsParamVO(jsonArray.getJSONObject(i));
	    				folders.add(tempNewJenkinsVO);
	    			}else {
	    				NewJenkinsParamVO tempNewJenkinsJobVO = new NewJenkinsParamVO(jsonArray.getJSONObject(i));
	    				jobs.add(tempNewJenkinsJobVO);
	    			}
	    		}
	    	}
    	}

    	
    	
    }
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
	public String getFlagClass() {
		return flagClass;
	}
	public void setFlagClass(String flagClass) {
		this.flagClass = flagClass;
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
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public ArrayList<NewJenkinsParamVO> getFolders() {
		return folders;
	}
	public void setFolders(ArrayList<NewJenkinsParamVO> folders) {
		this.folders = folders;
	}
	public ArrayList<NewJenkinsParamVO> getJobs() {
		return jobs;
	}
	public void setJobs(ArrayList<NewJenkinsParamVO> jobs) {
		this.jobs = jobs;
	}
	public Integer getServiceStatus() {
		return serviceStatus;
	}
	public void setServiceStatus(Integer serviceStatus) {
		this.serviceStatus = serviceStatus;
	}
	@Override
	public String toString() {
		return "NewJenkinsParamVO [id=" + id + ", projectId=" + projectId + ", jenkinsFolderName=" + jenkinsFolderName
				+ ", jenkinsJobName=" + jenkinsJobName + ", flagClass=" + flagClass + ", jenkinsJobType="
				+ jenkinsJobType + ", jenkinsJobTypeName=" + jenkinsJobTypeName + ", division=" + division + ", url="
				+ url + ", folders=" + folders + ", jobs=" + jobs + ", serviceStatus=" + serviceStatus + ", getId()="
				+ getId() + ", getProjectId()=" + getProjectId() + ", getJenkinsFolderName()=" + getJenkinsFolderName()
				+ ", getJenkinsJobName()=" + getJenkinsJobName() + ", getFlagClass()=" + getFlagClass()
				+ ", getJenkinsJobType()=" + getJenkinsJobType() + ", getJenkinsJobTypeName()="
				+ getJenkinsJobTypeName() + ", getDivision()=" + getDivision() + ", getUrl()=" + getUrl()
				+ ", getFolders()=" + getFolders() + ", getJobs()=" + getJobs() + ", getServiceStatus()="
				+ getServiceStatus() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode() + ", toString()="
				+ super.toString() + "]";
	}
	
}
