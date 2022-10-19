
import io.swagger.annotations.ApiModel;

@ApiModel(value = "공통 Contents", description = "공통 컨텐츠")
public class Contents {
	public static final String STATUS_NM_TODO = "Todo";
	public static final String STATUS_NM_IN_PROGRESS = "In Progress";
	public static final String STATUS_NM_DONE = "Done";

	public static final String STATUS_CATEGORY_NM_TODO = "new";
	public static final String STATUS_CATEGORY_NM_IN_PROGRESS = "indeterminate";
	public static final String STATUS_CATEGORY_NM_DONE = "done";

	public static final int SPARROW_VIEW_MAX_LEN = 10;
}
