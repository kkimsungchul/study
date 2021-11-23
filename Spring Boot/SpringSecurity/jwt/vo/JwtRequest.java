package com.sungchul.stock.config.jwt.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter @Setter
@NoArgsConstructor //need default constructor for JSON Parsing
@AllArgsConstructor
public class JwtRequest implements Serializable {

    private static final long serialVersionUID = 5926468583005150707L;



    @ApiModelProperty(name = "user_id", value = "관리자ID", notes = "필수", example = "admin")
    @JsonProperty("user_id")
    private String userId;

    @ApiModelProperty(name = "password", value = "비밀번호", notes = "필수", example = "admin")
    @JsonProperty("password")
    private String password;
}