package com.sungchul.etc.sql.vo;

import java.util.Map;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Insert SQL 파라메터", description = "Insert SQL 파라메터 VO")
public class InsertParamVO
{
	@ApiModelProperty(name = "sql", value = "Insert SQL 파라메터", dataType = "String", required = true, example = "INSERT INTO public.git_lab_project(git_lab_id, git_project_id, service_status, created_by, created_date, last_modified_by, last_modified_date) VALUES(:gitLabId, :gitProjectId, 0, :createdBy, now(), :lastModifiedBy, now())")
	private String sql;

	@ApiModelProperty(name = "params", value = "Insert SQL 파라메터", dataType = "Map", example = "{\"gitLabId\": 85 , \"gitProjectId\": \"215\", \"createdBy\": \"91284781\", \"lastModifiedBy\": \"91284781\"}")
	private Map<String, Object> params;

	public String getSql() {
		return sql;
	}
	public void setSql(String sql) {
		this.sql = sql;
	}
	public Map<String, Object> getParams() {
		return params;
	}
	public void setParams(Map<String, Object> params) {
		this.params = params;
	}
}
