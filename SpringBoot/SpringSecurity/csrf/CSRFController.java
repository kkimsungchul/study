package com.sungchul.stock.config.csrf;

import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;


//swagger 사용시 http://localhost:/csrf 404 오류메시지가 출력되어서 구현함
//https://stackoverflow.com/questions/55582023/when-loading-the-swagger-ui-html-page-a-request-is-made-to-hostport-and-host
@Controller
public class CSRFController {
    @RequestMapping(value = "/csrf", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public ResponseEntity<CsrfToken> getToken(final HttpServletRequest request) {
        return ResponseEntity.ok().body(new HttpSessionCsrfTokenRepository().generateToken(request));
    }
}