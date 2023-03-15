import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;



@Mapper
public interface ExcelMapper {
	public List<Map<String,Object>> findProject(ProjectVO projectVO);
	
	public boolean checkTableName(String tableName);
	public boolean checkColumName(Map columnMap);
	public List<Map<String,Object>> exportExcelPortalUser();
	public List<Map<String,Object>> exportExcelMember();
}

