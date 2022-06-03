package com.sungchul.etc.sql.vo;

import java.util.Map;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Update SQL 파라메터", description = "Update SQL 파라메터 VO")
public class UpdateParamVO
{
	@ApiModelProperty(name = "sql", value = "Update SQL 파라메터", dataType = "String", required = true, example = "update public.git_lab_project set service_status = 5, last_modified_by = :lastModifiedBy, last_modified_date = now() where git_lab_id = :gitLabId and git_project_id = :gitProjectId")
	private String sql;

	@ApiModelProperty(name = "params", value = "Update SQL 파라메터", dataType = "Map", example = "{\"gitLabId\": 85 , \"gitProjectId\": \"215\", \"lastModifiedBy\": \"91284781\"}")
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
