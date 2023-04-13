import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
@ApiModel(value = "프로젝트 조회 VO", description = "프로젝트 조회 VO")
public class ProjectVO {
	@ApiModelProperty(name = "limit", value = "조회 limit", dataType = "Long")
	private Long limit;
	@ApiModelProperty(name = "orderBy", value = "조회 정렬 key", dataType = "String")
	private String orderBy;
	@ApiModelProperty(name = "tableName", value = "테이블 명", dataType = "String")
	private String tableName;
	public Long getLimit() {
		return limit;
	}
	public void setLimit(Long limit) {
		this.limit = limit;
	}
	public String getOrderBy() {
		return orderBy;
	}
	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public ProjectVO(Long limit, String orderBy, String tableName) {
		super();
		this.limit = limit;
		this.orderBy = orderBy;
		this.tableName = tableName;
	}
	@Override
	public String toString() {
		return "ProjectVO [limit=" + limit + ", orderBy=" + orderBy + ", tableName=" + tableName + "]";
	}
}

