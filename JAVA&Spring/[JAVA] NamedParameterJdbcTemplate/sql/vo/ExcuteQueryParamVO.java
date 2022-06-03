package com.sungchul.etc.sql.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Query 파라메터", description = "Query 파라메터 VO")
public class ExcuteQueryParamVO
{
	@ApiModelProperty(name = "sql", value = "Query 파라메터", dataType = "String", required = true, example = "select * from git_lab_project")
	private String sql;
	
	@ApiModelProperty(name = "sqlType", value = "쿼리 타입", dataType = "String", required = true, example = "SELECT,INSERT,UPDATE,DELETE")
	private String sqlType;

	public String getSql() {
		return sql;
	}
	public void setSql(String sql) {
		this.sql = sql;
	}
	public String getSqlType() {
		return sqlType;
	}
	public void setSqlType(String sqlType) {
		this.sqlType = sqlType;
	}

}
