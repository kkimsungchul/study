package com.sungchul.etc.config.jwt.util;

import com.sungchul.etc.user.vo.UserVO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenUtil implements Serializable {

    private static final long serialVersionUID = -2550185165626007488L;

    public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60;

    @Value("${spring.jwt.secret}")
    private String secret;


    /**
     * jwt token으로부터 user사용자 정보를 획득한다.
     * @return Map
     */
    public Map getUserInfo() {
        final Claims claims = getAllClaimsFromToken(getToken());
        return claims.get("USER_INFO", Map.class);
    }



    /**
     * retrieve username from jwt token
     * jwt token으로부터 username을 획득한다.
     * @return String
     */
    public String getUsernameFromToken() {
        return getClaimFromToken(getToken(), Claims::getSubject);
    }



    /**
     * retrieve username from jwt token
     * jwt token으로부터 username을 획득한다.
     * @param token
     * @return String
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }



    /**
     * retrieve expiration date from jwt token
     * jwt token으로부터 만료일자를 알려준다.
     * @param token
     * @return Date
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     *
     * @param token
     * @param claimsResolver
     * @param <T>
     * @return
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }
    /**
     * for retrieveing any information from token we will need the secret key
     * @param token
     * @return
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    /**
     * check if the token has expired
     * 토큰이 만료되었는지 확인한다.
     * @param token
     * @return Boolean
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }



    /**
     * generate token for user
     * 유저를 위한 토큰을 발급해준다.
     * @param userDetails
     * @return String
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, userDetails.getUsername());
    }



    /**
     * generate token for user
     * 유저를 위한 토큰을 발급해준다.
     * 발급시 사용자의 아이디를 사용함
     * @param userDetails
     * @param userVO
     * @return String
     */
    public String generateToken(UserDetails userDetails , UserVO userVO) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("USER_INFO" , userVO);
        return doGenerateToken(claims, userDetails.getUsername());
    }



    /**
     * while creating the token -
     * 1. Define  claims of the token, like Issuer, Expiration, Subject, and the ID
     * 2. Sign the JWT using the HS512 algorithm and secret key.
     * 3. According to JWS Compact Serialization(https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
     *    compaction of the JWT to a URL-safe string
     * @param claims
     * @param subject
     * @return
     */
    private String doGenerateToken(Map<String, Object> claims, String subject) {
        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
                .signWith(SignatureAlgorithm.HS512, secret).compact();
    }



    /**
     * 토큰 유효성 체크
     * validate token
     * @param token
     * @param userDetails
     * @return Boolean
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * header에 있는 토큰을 가져옴
     * @return String
     */
    public String getToken(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        return request.getHeader("jwt");
    }
}