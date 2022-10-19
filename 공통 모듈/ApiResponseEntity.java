
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Rest API 응답 객체", description = "Rest API 응답 객체 VO")
public class ApiResponseEntity
{
	/**
	 * 처리 상태
	 */
	@ApiModelProperty(position = 1, name = "status", value = "처리 상태", dataType = "String", example = "success or fail")
	private String status;

	/**
	 * 응답 내용
	 */
	@ApiModelProperty(position = 2, name = "response", value = "응답 내용", dataType = "String", example = "null or boolean or or string or json")
	private Object response;

	/**
	 * 에러 메시지 코드
	 */
	@ApiModelProperty(position = 3, name = "errorCode", value = "에러 메시지 코드", dataType = "String", example = "E0012")
	private String errorCode;

	/**
	 * 에러 메시지 내용
	 */
	@ApiModelProperty(position = 4, name = "errorMessage", value = "에러 메시지 내용", dataType = "String", example = "데이터 입력 실패")
	private String errorMessage;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Object getResponse() {
		return response;
	}

	public void setResponse(Object response) {
		this.response = response;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public ApiResponseEntity(String status, Object response, String errorCode, String errorMessage) {
		this.status = status;
		this.response = response;
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}
}
