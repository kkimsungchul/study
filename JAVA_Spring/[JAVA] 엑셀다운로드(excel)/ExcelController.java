import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;


@Api(tags = "ExcelController")
@RestController
@RequestMapping("/excel")
public class ExcelController {
	@Autowired
	private ExcelService excelService;
	
	
    /**
     * project 조회 후 엑셀로 다운로드
     * 추후 검색조건 추가
     * @param response,id,projectName,limit
     * @return
     */
	@ApiOperation(value = "프로젝트 엑셀 다운로드", notes = "프로젝트 엑셀 다운로드")
	@GetMapping(value = "/exportExcel/project")
	public String exportExcelProject(HttpServletResponse response,String getLimit,String orderBy) {
		return excelService.exportExcel(response,"project", getLimit,orderBy,null);
	}
	
    /**
     * project 조회 후 엑셀로 다운로드
     * 추후 검색조건 추가
     * @param response,id,projectName,limit
     * @return
     */
	@ApiOperation(value = "멤버 엑셀 다운로드", notes = "멤버 엑셀 다운로드")
	@GetMapping(value = "/exportExcel/member")
	public void exportExcelMember(HttpServletResponse response) {
		excelService.exportExcelMember(response,"member");
	}
    /**
     * project 조회 후 엑셀로 다운로드
     * @param response
     * @return
     */
	@ApiOperation(value = "포탈유저 엑셀 다운로드", notes = "포탈유저 엑셀 다운로드")
	@GetMapping(value = "/exportExcel/portalUser")
	public void exportExcelPortalUser(HttpServletResponse response) {
		excelService.exportExcelPortalUser(response,"portal_user");
	}
	
    /**
     * 엑셀 다운로드(사용자 지정 테이블)
     * 추후 검색조건 추가
     * @param response,id,projectName,limit
     * @return
     */
	@ApiOperation(value = "엑셀 다운로드(사용자 지정 테이블)", notes = "엑셀 다운로드(사용자 지정 테이블)")
	@GetMapping(value = "/exportExcel/{tableName}")
	public String exportExcel(HttpServletResponse response,@PathVariable String tableName,String getLimit,String orderBy) {
		return excelService.exportExcel(response,tableName,getLimit,orderBy,null);
	}
	
	
	
    /**
     * 엑셀 다운로드(사용자 지정 쿼리)
     * 추후 검색조건 추가
     * @param response,id,projectName,limit
     * @return
     */
	@ApiOperation(value = "엑셀 다운로드(사용자 지정 쿼리)", notes = "엑셀 다운로드(사용자 지정 쿼리)")
	@PostMapping(value = "/exportExcel")
	public void exportExcelQuery(HttpServletResponse response,@RequestBody QueryParamVO queryParamVO) {
		excelService.exportExcelQuery(response,queryParamVO);
		
	}
}

