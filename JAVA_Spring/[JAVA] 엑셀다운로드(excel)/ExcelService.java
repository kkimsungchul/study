

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;


@Service
public class ExcelService {
	
	@Autowired
	private ExcelMapper excelMapper;
	
	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	/**
     *
     * parma으로 VO생성 후 조회
     * @param response,name
     * @return
     */
	public String exportExcel(HttpServletResponse response,String tableName ,String getLimit,String orderBy,String loginIdFirst) {
		String query = excelMakeQuery(tableName,getLimit,orderBy,loginIdFirst);
		if(query.startsWith("select")) {
			findTableQuery(response,query,tableName);
		}else {
			return query;
		}
		return "success";
	}
	
	/**
    *
    * Query 실행
    * @param response,queryParamVO
    * @return
    */
	public void exportExcelQuery(HttpServletResponse response,QueryParamVO queryParamVO) {
		String query = queryParamVO.getSql();
        Map<String, Object> paramsMap = queryParamVO.getParams();
        List<Map<String, Object>> resultList = namedParameterJdbcTemplate.queryForList(query, paramsMap);
        if(resultList != null && resultList.size() > 0){
        	excelExport(response,resultList,"query");
        }
	}
	/**
    *
    * Query 실행,엑셀 다운로드로 보냄
    * @param response,queryParamVO
    * @return 결과값 리턴(String)
    */
	public String findTableQuery(HttpServletResponse response,String query,String tableName) {
		try {
			HashMap<String, Object> paramsMap = new HashMap<String, Object>();
			List<Map<String, Object>> resultList = namedParameterJdbcTemplate.queryForList(query, paramsMap);
			if(resultList != null && resultList.size() > 0){
				excelExport(response,resultList,tableName);
			}
		}catch(OutOfMemoryError e) {
			return "OutOfMemory";
		}catch(Exception e) {
			return "fail";
		}
		return "success";
	}
	
	/**
    *
    * dspace.portal_user 테이블의 정해진 query 실행
    * @param response,tableName
    * @return 
    */
	public void exportExcelPortalUser(HttpServletResponse response,String tableName) {
		List<Map<String,Object>> resultList = excelMapper.exportExcelPortalUser();
		excelExport(response,resultList,tableName);
	}
	/**
    *
    * dspace.member 테이블의 정해진 query 실행
    * @param response,tableName
    * @return 
    */
	public void exportExcelMember(HttpServletResponse response,String tableName) {
		List<Map<String,Object>> resultList = excelMapper.exportExcelMember();
		excelExport(response,resultList,tableName);
	}
	/**
     * 해당 데이터를 가지고 엑셀로 만들어서 다운로드
     * 추후 검색조건 추가
     * HttpServletResponse , List<Map<String,Object>>
     * @param response,dataList
     * @return
     */
	//List<Map<string,Object> 형식의 데이터를 넘기면 엑셀로 만들어 리턴
	public void excelExport(HttpServletResponse response,List<Map<String,Object>> dataList,String tableName){
		try {
			String fileName = tableName + "_excel.xlsx";
			Workbook workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet();
			try {
		        Row row;
		        Cell cell;
		        int rowNo = 0;
		        
				if(dataList.size() > 0) {
					//excel header start 
					ArrayList<String> keyList = new ArrayList<String>();
					for(String keyName : dataList.get(0).keySet()) {
						keyList.add(keyName);
					}
					row = sheet.createRow(rowNo++);
					for(int j=0;j<keyList.size();j++) {
						cell = row.createCell(j);
						cell.setCellValue(keyList.get(j));
					}
					
					//excel header end
					
					
					for(int i = 0 ; i < dataList.size(); i++) {		
						row = sheet.createRow(rowNo++);
						for(int j=0;j<keyList.size();j++) {
							cell = row.createCell(j);
							cell.setCellValue(nullCheck(dataList.get(i).get(keyList.get(j))));
						}
					}
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
			// 컨텐츠 타입과 파일명 지정
	        response.setContentType("ms-vnd/excel");
	        response.setHeader("Content-Disposition", "attachment;filename="+fileName);
	
	        // Excel File Output
	        workbook.write(response.getOutputStream());
	        workbook.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	/**
     * null 체크
     * value가 null이면 빈값으로, 
     * null이 아니면 toString후 return
     * @param value
     * @return String
     */
	public String nullCheck(Object value) {
		
		if(null==value) {
			return "";
		}else {
			return value.toString();
		}
	}
	/**
     * 테이블 명이 존재 하는지 체크
     * where schemaname=dspace 
     * @param tableName
     * @return boolean
     */
	public boolean checkTableName(String tableName) {
		return excelMapper.checkTableName(tableName);
	}
	/**
	 * 해당 테이블에 컬럼명이 존재 하는지 체크
	 * @param tableName
	 * @return boolean
	 */
	public boolean checkColumName(Map columnMap) {
		return excelMapper.checkColumName(columnMap);
	}
	
	/**
	 * 받은 param 으로 query 문 형식으로 만듬
	 * @param tableName,getLimit,orderBy,loginIdFirst
	 * @return String
	 */
	public String excelMakeQuery(String tableName ,String getLimit,String orderBy,String loginIdFirst) {
		long limit = 10;
		String query = "select * from ";
		Map map = new HashMap<String,Object>();
		if(null==tableName && tableName.equals("") && tableName.length()==0) {
			return "테이블명을 입력하세요.";
		}else if(checkTableName(tableName)) {
			query += "dspace." + tableName;
			map.put("tableName", tableName);
		}else {
			return "해당 테이블명이 없습니다.";
		}
		if(loginIdFirst!=null && !"".equals(loginIdFirst) && loginIdFirst.length()!=0) {
			query += " where login_id like '" + loginIdFirst + "%'";
		}
		if(orderBy != null ) {
			query += " order by " + orderBy;
			map.put("columnName", orderBy);
			if(!checkColumName(map)) {
				return "해당 칼럼명이 없습니다.";
			}
		}
		if(null!=getLimit && !getLimit.equals("") && getLimit.length()>0) {
			limit = Long.parseLong(getLimit);
			query += " limit " + limit;
		}else {
			query += " limit " + 10;
		}
		return query;
	}
}
