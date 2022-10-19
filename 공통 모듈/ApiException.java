
import org.springframework.http.HttpStatus;


public class ApiException extends RuntimeException
{
	private static final long serialVersionUID = 1L;
	private  HttpStatus status;
    private  String msgCode;
    private String msgDesc;

    /**
     * 메시지 코드로부터 메시지 본문 가져오기
     * @param msgCode - 메시지 코드
     */
    public ApiException(String msgCode)
    {
        this(HttpStatus.INTERNAL_SERVER_ERROR, msgCode);
    }

    /**
     * 메시지 코드로부터 메시지 본문 가져오기(단일 메시지 파라메터 전달)
     * @param msgCode - 메시지 코드
     * @param msgParam - 가변메시지 파라메터값
     */
    public ApiException(String msgCode, Object msgParam) {
        this(HttpStatus.INTERNAL_SERVER_ERROR, msgCode, msgParam);
    }

    /**
     * 메시지 코드로부터 가변 메시지 본문 가져오기(복수 메시지 파라메터 전달)
     * @param msgCode - 메시지 코드
     * @param msgParams - 가변메시지 파라메터값 배열
     */
    public ApiException(String msgCode, Object[] msgParams) {
        this(HttpStatus.INTERNAL_SERVER_ERROR, msgCode, msgParams);
    }

    /**
     * 메시지 코드로부터 메시지 본문 가져오기
     * @param status - HttpStatus 상태 코드 객체
     * @param msgCode - 메시지 코드
     */
    public ApiException(HttpStatus status, String msgCode) {
        this.status = status;
        this.msgCode = msgCode;
        this.msgDesc = DbMessageManager.getMessage(msgCode, "ko");
    }

    /**
     * 메시지 코드로부터 메시지 본문 가져오기(단일 메시지 파라메터 전달)
     * @param status - HttpStatus 상태 코드 객체
     * @param msgCode - 메시지 코드
     * @param msgParam - 가변메시지 파라메터값
     */
    public ApiException(HttpStatus status, String msgCode, Object msgParam) {
        this.status = status;
        this.msgCode = msgCode;
        if(msgCode != null) {
            this.msgDesc = DbMessageManager.getMessage(msgCode, msgParam, "ko");
        } else {
            this.msgDesc = (String) msgParam;
        }
    }

    /**
     * 메시지 코드로부터 가변 메시지 본문 가져오기(복수 메시지 파라메터 전달)
     * @param status - HttpStatus 상태 코드 객체
     * @param msgCode - 메시지 코드
     * @param msgParams - 가변메시지 파라메터값 배열
     */
    public ApiException(HttpStatus status, String msgCode, Object[] msgParams) {
        this.status = status;
        this.msgCode = msgCode;
        this.msgDesc = DbMessageManager.getMessage(msgCode, msgParams, "ko");
    }

    public String getMsgDesc()
    {
        return msgDesc;
    }

    public void setMsgDesc(String msgDesc)
    {
        this.msgDesc = msgDesc;
    }

    public HttpStatus getStatus()
    {
        return status;
    }

    public String getMsgCode()
    {
        return msgCode;
    }
}
