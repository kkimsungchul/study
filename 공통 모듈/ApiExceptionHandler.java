
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;



@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(annotations = RestController.class)
public class ApiExceptionHandler
{
    private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(value = { ApiException.class })
    public ResponseEntity<ApiResponseEntity> exceptionHandler(HttpServletRequest request, final ApiException e)
    {
    	//(String status, Object response, String errorCode, String errorMessage)
        e.printStackTrace();
        logger.info(request.toString());
        return ResponseEntity
                .status(e.getStatus())
                .body(new ApiResponseEntity(DbMessageManager.getMessage("I0002", "ko"), null, e.getMsgCode(), e.getMsgDesc()));
    }

    @ExceptionHandler(value = { PersistenceException.class })
    public ResponseEntity<ApiResponseEntity> exceptionHandler(HttpServletRequest request, final PersistenceException e)
    {
        String errorMessage = null;

        try
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            errorMessage = sw.toString();
            sw.close();
            pw.close();
        }catch(IOException e1)
        {
            logger.error("ApiExceptionHandler exceptionHandler ERROR: ", e);
        }

        logger.info(request.toString());
        e.printStackTrace();

        //E0005 - Mybatis(org.apache.ibatis.exceptions.PersistenceException)의 printStackTrace 내용으로 메시지 대체함.
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseEntity(DbMessageManager.getMessage("I0002", "ko"), null, "E0005", errorMessage));
    }

    @ExceptionHandler(value = { TooManyResultsException.class })
    public ResponseEntity<ApiResponseEntity> exceptionHandler(HttpServletRequest request, final TooManyResultsException e)
    {
        String errorMessage = null;

        try
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            errorMessage = sw.toString();
            sw.close();
            pw.close();
        }catch(IOException e1)
        {
            logger.error("ApiExceptionHandler exceptionHandler ERROR: ", e);
        }

        logger.info(request.toString());
        e.printStackTrace();

        //E0006 - Mybatis(org.apache.ibatis.exceptions.TooManyResultsException)의 printStackTrace 내용으로 메시지 대체함.
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseEntity(DbMessageManager.getMessage("I0002", "ko"), null, "E0006", errorMessage));
    }

    @ExceptionHandler(value = { HttpClientErrorException.class })
    public ResponseEntity<ApiResponseEntity> exceptionHandler(HttpServletRequest request, final HttpClientErrorException e)
    {
        logger.info(request.toString());
        e.printStackTrace();
        String errorMessage = e.getStatusText();

        return ResponseEntity
                .status(e.getStatusCode())
                .body(new ApiResponseEntity(DbMessageManager.getMessage("I0002", "ko"), null, "E0019", DbMessageManager.getMessage("E0019", "\n"+errorMessage, "ko")));
    }

    /*
     * org.postgresql.util.PSQLException: 오류: "git_lab_project" 이름의 릴레이션(relation)이 없습니다
     */
    
    
//    @ExceptionHandler(value = { RuntimeException.class })
//    public ResponseEntity<ApiResponseEntity> exceptionHandler(HttpServletRequest request, final RuntimeException e)
//    {
//        e.printStackTrace();
//        return ResponseEntity
//                .status(HttpStatus.BAD_REQUEST)
//                .body(ApiResponseEntity.builder()
//                        .status(DbMessageManager.getMessage("I0002", "ko"))
//                        .errorCode("E0001")
//                        .errorMessage(DbMessageManager.getMessage("E0001", "ko"))
//                        .build());
//    }

    @ExceptionHandler(value = { AccessDeniedException.class })
    public ResponseEntity<ApiResponseEntity> exceptionHandler(HttpServletRequest request, final AccessDeniedException e)
    {
        logger.info(request.toString());
        e.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponseEntity(DbMessageManager.getMessage("I0002", "ko"), null, "E0002", DbMessageManager.getMessage("E0002", "ko")));
    }

    @ExceptionHandler(value = { Exception.class })
    public ResponseEntity<ApiResponseEntity> exceptionHandler(HttpServletRequest request, final Exception e)
    {
        /*
        String errorMessage = null;

        try
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            errorMessage = sw.toString();
            sw.close();
            pw.close();
        }catch(IOException e1)
        {
            //TODO: Exception not checked
        }
        */

        //e.printStackTrace();
        logger.info(request.toString());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseEntity(DbMessageManager.getMessage("I0002", "ko"), null, "E0004", e.getMessage()));
    }
}
