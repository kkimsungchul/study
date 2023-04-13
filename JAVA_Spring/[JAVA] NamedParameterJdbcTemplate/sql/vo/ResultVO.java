package com.sungchul.etc.sql.vo;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "쿼리 결과", description = "쿼리 결과 VO")
public class ResultVO
{
	@ApiModelProperty(name = "columnList", value = "컬럼 리스트", dataType = "List")
	private List<String> columnList;

	@ApiModelProperty(name = "valueList", value = "결과값 리스트", dataType = "List")
	private List<List<Object>> valueList;
	
	@ApiModelProperty(name = "qryRtnType", value = "결과 쿼리 타입", dataType = "String")
	private String qryRtnType;
	
	@ApiModelProperty(name = "etcQryCnt", value = "기타 쿼리 카운트", dataType = "Integer")
	private int etcQryCnt;

	public List<String> getColumnList() {
		return columnList;
	}

	public void setColumnList(List<String> columnList) {
		this.columnList = columnList;
	}

	public List<List<Object>> getValueList() {
		return valueList;
	}

	public void setValueList(List<List<Object>> valueList) {
		this.valueList = valueList;
	}

	public String getQryRtnType() {
		return qryRtnType;
	}

	public void setQryRtnType(String qryRtnType) {
		this.qryRtnType = qryRtnType;
	}

	public int getEtcQryCnt() {
		return etcQryCnt;
	}

	public void setEtcQryCnt(int etcQryCnt) {
		this.etcQryCnt = etcQryCnt;
	}
	
}
