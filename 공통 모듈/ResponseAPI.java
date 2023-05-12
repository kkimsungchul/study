package com.sungchul.camping.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor //매게변수 있는 생성자
@ApiModel("ResponseAPI")
public class ResponseAPI {

    @ApiModelProperty(required = true, name = "status", value = "HTTP 상태코드", notes = "HTTP 상태코드", example = "200")
    private int status;

    @ApiModelProperty(required = true, name = "message", value = "HTTP 상태메시지", notes = "HTTP 상태메시지", example = "200")
    private String message;

    @ApiModelProperty(required = true, name = "data", value = "데이터", notes = "데이터", example = "")
    private Object data;

    @ApiModelProperty(required = true, name = "timestamp", value = "시간", notes = "시간", example = "")
    private LocalDateTime timestamp;

    public ResponseAPI(){
        this(HttpStatus.OK);
    }

    public ResponseAPI(HttpStatus httpStatus){
        this.status = httpStatus.value();
        this.message = httpStatus.getReasonPhrase();
        this.timestamp = LocalDateTime.now();
        this.data = new HashMap<>();
    }

}