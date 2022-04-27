package com.sungchul.etc.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.AuthorizationCodeGrantBuilder;
import springfox.documentation.builders.OAuthBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket api(){
        return new Docket(DocumentationType.SWAGGER_2)
                .tags(
                        new Tag("UserController","User Manager Controller"),
                        new Tag("JwtAuthenticationController","Jwt Manager Controller"),
                        new Tag("FindTextController","FindText Manager Controller")

                )
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.sungchul.etc"))
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build()
                .securityContexts(Arrays.asList(securityContext()))
                .securitySchemes(Arrays.asList(apiKey()));
    }


    private ApiInfo apiInfo() {
        return new ApiInfo(
                "ETC Project Swagger",
                "그냥 이것저것",
                "1.0",
                "http://github.com/kkimsungchul",
                new Contact("kimsungchul", "http://github.com/kkimsungchul", "kimsc1218@gmail.com"),
                "License of API",
                "API license URL",
                Collections.emptyList());
    }

    //스웨거에서 사용할 토큰값에 대한 정보 설정
    // (키종류 , 키이름 , 헤더값이라는 표시)
    //여기서 설정한 키이름으로, JwtTokenUtil 클래스에서 꺼내서 사용함
    private ApiKey apiKey() {
        return new ApiKey("JWT", "jwt", "header");
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder().securityReferences(defaultAuth()).build();
    }

    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return Arrays.asList(new SecurityReference("JWT", authorizationScopes));
    }


}
