import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

/**
 * HttpClientErrorException, HttpServerErrorException 에러 메시지 처리
 * @author USER
 *
 */
public class HttpErrorExceptionDetail
{
	public static ApiException getApiException(Exception exception, String restApiTitle, String restApiUrl)
	{
		if(exception instanceof HttpClientErrorException)
		{
			HttpClientErrorException httpClientErrorException = (HttpClientErrorException)exception;

			HttpStatus status = httpClientErrorException.getStatusCode();
			if(status == HttpStatus.BAD_REQUEST)
			{
				return new ApiException("E0109", new Object[]{restApiUrl, restApiTitle, status.toString()});
			}else if(status == HttpStatus.UNAUTHORIZED)
			{
				return new ApiException("E0110", new Object[]{restApiUrl, restApiTitle, status.toString()});
			}else if(status == HttpStatus.FORBIDDEN) {
				return new ApiException("E0111", new Object[]{restApiUrl, restApiTitle, status.toString()});
			}else if(status == HttpStatus.NOT_FOUND) {
				return new ApiException("E0112", new Object[]{restApiUrl, restApiTitle, status.toString()});
			}else if(status == HttpStatus.METHOD_NOT_ALLOWED)
			{
				return new ApiException("E0113", new Object[]{restApiUrl, restApiTitle, status.toString()});
			}else if(status == HttpStatus.INTERNAL_SERVER_ERROR) {
				return new ApiException("E0114", new Object[]{restApiUrl, restApiTitle, status.toString()});
			}else
			{
				return new ApiException("E0010", new Object[] {restApiUrl, httpClientErrorException.getMessage()});
			}
		}else if(exception instanceof HttpServerErrorException)
		{
			HttpServerErrorException httpServerErrorException = (HttpServerErrorException)exception;
			HttpStatus status = httpServerErrorException.getStatusCode();

			//URL - {0}\r\n{1}, {2} : 서버에 오류가 발생했습니다. 관리자에게 문의하세요.
			return new ApiException("E0114", new Object[]{restApiUrl, restApiTitle, status.toString()});
		}

		//Rest API 호출 실패\\nURL - {0}\\nMessage - {1}
		return new ApiException("E0010", new Object[] {restApiUrl, exception.getMessage()});
	}
}
