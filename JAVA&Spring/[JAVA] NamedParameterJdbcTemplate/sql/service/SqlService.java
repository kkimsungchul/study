package com.sungchul.etc.sql.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sungchul.etc.sql.vo.ExcuteQueryParamVO;
import com.sungchul.etc.sql.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;


@Service
public class SqlService {
//    private Logger logger = LoggerFactory.getLogger(SqlService.class);

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public List<Map<String, Object>> executeQuery(String query, Map<String, Object> params) {

        List<Map<String, Object>> resultList = namedParameterJdbcTemplate.queryForList(query, params);


        return resultList;
    }

    public int executeUpdate(String sql, Map<String, Object> params) {
        int updateCnt = namedParameterJdbcTemplate.update(sql, params);


        return updateCnt;
    }

    public int executeInsert(String sql, Map<String, Object> params) {

        int insertCnt = namedParameterJdbcTemplate.update(sql, params);


        return insertCnt;
    }

    public int executeDelete(String sql, Map<String, Object> params) {
        int deleteCnt = namedParameterJdbcTemplate.update(sql, params);

        return deleteCnt;
    }

    public ResultVO executeQuerys(ExcuteQueryParamVO excuteQueryParamVO) {

        String qryType = excuteQueryParamVO.getSqlType();
        String query = excuteQueryParamVO.getSql();

        ResultVO resultVO = new ResultVO();

        if ("SELECT".equals(qryType)) {
            resultVO.setQryRtnType("SELECT");
            List<Map<String, Object>> resultList = namedParameterJdbcTemplate.queryForList(query, new HashMap<>());

            if (!ObjectUtils.isEmpty(resultList)) {

                List<String> columnList = new ArrayList<>();
                resultList.stream().findFirst().get().forEach((k, v) -> columnList.add(k));
                resultVO.setColumnList(columnList);

                List<List<Object>> valueList = new ArrayList<>();

                for (Map<String, Object> map : resultList) {
                    List<Object> objList = new ArrayList<>();
                    map.forEach((k, v) -> objList.add(v));
                    valueList.add(objList);
                }

                resultVO.setValueList(valueList);
            }
        } else {
            resultVO.setQryRtnType("ETC");
            int resCnt = namedParameterJdbcTemplate.update(query, new HashMap<>());
            resultVO.setEtcQryCnt(resCnt);
        }
        return resultVO;
    }
}
