package com.sungchul.etc.sql.controller;




import java.util.List;
import java.util.Map;

import com.sungchul.etc.sql.service.SqlService;
import com.sungchul.etc.sql.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = {"SqlController"})
@RestController
@RequestMapping(value="/sql")
public class SqlController {

    @Autowired
    private SqlService sqlService;

    /**
     *  DB 쿼리 조회
     * @param queryParamVO - Query 파라메터 VO
     * @return
     */
    @ApiOperation(value = " DB 쿼리 조회", notes = " DB 쿼리 조회합니다.")
    @PostMapping(value = "/query")
    @ResponseBody
    public List<Map<String, Object>> executeQuery(@RequestBody QueryParamVO queryParamVO)
    {
        String query = queryParamVO.getSql();
        Map<String, Object> paramsMap = queryParamVO.getParams();

        if(query == null)
        {

        }

        return sqlService.executeQuery(query, paramsMap);
    }

    /**
     *  DB Insert SQL 실행
     * @param insertParamVO - Insert SQL 파라메터 VO
     * @return ResponseEntity<ApiResponseEntity> - 응답 Entity
     */
    @ApiOperation(value = " DB Insert문 실행", notes = " DB Insert SQL 실행합니다.")
    @PostMapping(value = "/insert")
    @ResponseBody
    public int executeInsert(@RequestBody InsertParamVO insertParamVO)
    {
        String insertSql = insertParamVO.getSql();
        Map<String, Object> paramsMap = insertParamVO.getParams();

        if(insertSql == null)
        {

        }

        return sqlService.executeInsert(insertSql, paramsMap);
    }

    /**
     *  DB Update SQL 실행
     * @param updateParamVO - Update SQL 파라메터 VO
     * @return ResponseEntity<ApiResponseEntity> - 응답 Entity
     */
    @ApiOperation(value = " DB Insert문 실행", notes = " DB Update SQL 실행합니다.")
    @PostMapping(value = "/update")
    @ResponseBody
    public int executeUpdate(@RequestBody UpdateParamVO updateParamVO)
    {
        String updateSql = updateParamVO.getSql();
        Map<String, Object> paramsMap = updateParamVO.getParams();

        if(updateSql == null)
        {

        }

        return sqlService.executeUpdate(updateSql, paramsMap);
    }

    /**
     *  DB Delete SQL 실행
     * @param deleteParamVO - Delete SQL 파라메터 VO
     * @return ResponseEntity<ApiResponseEntity> - 응답 Entity
     */
    @ApiOperation(value = " DB Insert문 실행", notes = " DB Delete SQL 실행합니다.")
    @PostMapping(value = "/delete")
    @ResponseBody
    public int executeDelete(@RequestBody DeleteParamVO deleteParamVO)
    {
        String deleteSql = deleteParamVO.getSql();
        Map<String, Object> paramsMap = deleteParamVO.getParams();

        if(deleteSql == null)
        {

        }

        return sqlService.executeDelete(deleteSql, paramsMap);
    }

    @ApiOperation(value = "쿼리 실행", notes = " DB 쿼리 실행합니다.")
    @PostMapping(value = "/excute")
    @ResponseBody
    public ResultVO executeQuerys(@RequestBody ExcuteQueryParamVO excuteQueryParamVO)
    {
        String qryStr = excuteQueryParamVO.getSql();

        if(qryStr == null)
        {

        }

        return sqlService.executeQuerys(excuteQueryParamVO);
    }
}
