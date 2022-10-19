
import org.json.JSONObject;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Component
public class KeyCloakSession
{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(KeyCloakSession.class);
	
    @Value("${swagger.keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${swagger.keycloak.token-path}")
    private String tokenPath;

    @Value("${jira.keycloak.client-id}")
    private String jiraClientId;

    @Value("${jira.keycloak.client-secret}")
    private String jiraClientSecret;
    
	@Value("${jenkins.keycloak.client-id}")
	private String jenkinsClientId;
	
	@Value("${jenkins.keycloak.client-secret}")
	private String jenkinsClientSecret;

    @Autowired
    private RestService restService;

    @Autowired
    private GitlabRestService gitlabRestService;

    /**
     * 현재 로그인된 사용자의 사용자 ID를 반환 
     * @return String - 로그인된 사용자 ID
     */
    public String getLoginId()
    {
        ServletRequestAttributes attr = (ServletRequestAttributes)RequestContextHolder.currentRequestAttributes();
        KeycloakSecurityContext session = (KeycloakSecurityContext) attr.getRequest().getAttribute(KeycloakSecurityContext.class.getName());
        String loginId = null;

        if(session != null)
        {
            loginId = session.getToken().getPreferredUsername();
        }

        return loginId;
    }

    /**
     * 현재 로그인된 사용자의 이메일 주소를 반환 
     * @return String - 로그인된 사용자의 이메일 주소
     * @return
     */
    public String getEmail()
    {
        ServletRequestAttributes attr = (ServletRequestAttributes)RequestContextHolder.currentRequestAttributes();
        KeycloakSecurityContext session = (KeycloakSecurityContext) attr.getRequest().getAttribute(KeycloakSecurityContext.class.getName());
        String email = null;

        if(session != null)
        {
            email = session.getToken().getEmail();
        }

        return email;
    }

    /**
     * 현재 로그인된 사용자 정보 반환
     * @return UserVO - 현재 로그인된 사용자 정보
     */
    public UserVO getUserInfo()
    {
        ServletRequestAttributes attr = (ServletRequestAttributes)RequestContextHolder.currentRequestAttributes();
        KeycloakSecurityContext session = (KeycloakSecurityContext) attr.getRequest().getAttribute(KeycloakSecurityContext.class.getName());
        UserVO uv = null;

        if(session != null)
        {
            AccessToken at = session.getToken();

            //USER ID(사번)
            String loginId = at.getPreferredUsername();

            //first name(userName)
            String givenName = at.getGivenName();

            //last name(Dept)
            String familyName = at.getFamilyName();

            //E-Mail
            String email = at.getEmail();

            uv = new UserVO();
            uv.setLoginId(loginId);
            uv.setName(givenName);
            uv.setDept(familyName);
            uv.setEmail(email);

//            accessToken.getRealmAccess().getRoles();
        }

        return uv;
    }

    /**
     * 현재 로그인된 사용자 ID로부터 Git Lab의 Impersonate Token 을 가져옴.
     * @return String - Git Lab Rest API 호출용 토큰 발행
     */
    public String getGitLabPrivateToken()
    {
        return getGitLabPrivateToken(null);
    }

    /**
     * 현재 로그인된 사용자ID 혹은 Portal 사용자 ID로부터 Git Lab의 Impersonate Token 을 가져옴.
     * Git Lab의 Rest Api시 헤더정보로 사용함.
     * @return String - Git Lab Rest API 호출용 토큰 발행
     */
    public String getGitLabPrivateToken(String loginId)
    {
        String tmpLoginId = null;
        if(loginId == null)
        {
            ServletRequestAttributes attr = (ServletRequestAttributes)RequestContextHolder.currentRequestAttributes();
            KeycloakSecurityContext session = (KeycloakSecurityContext) attr.getRequest().getAttribute(KeycloakSecurityContext.class.getName());
            tmpLoginId = session.getToken().getPreferredUsername();
        }else
        {
        	tmpLoginId = loginId;
        }

        String token = gitlabRestService.getImpersonateToken(tmpLoginId);

        return token;
    }

    /**
     * keycloak의 KeycloakSecurityContext으로부터 access token 문자열을 추출한 뒤 jira용 access token으로 교체함.
     * Authorization Bearer access_token 형식으로 헤더 생성
     * @return HttpHeaders -  jira용 access token을 넣은 HttpHeaders 객체
     */
    public HttpHeaders getJiraAccessTokenToHttpHeaders()
    {
        ServletRequestAttributes attr = (ServletRequestAttributes)RequestContextHolder.currentRequestAttributes();
        KeycloakSecurityContext session = (KeycloakSecurityContext) attr.getRequest().getAttribute(KeycloakSecurityContext.class.getName());
        String subjectToken = session.getTokenString();

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", jiraClientId);
        map.add("client_secret", jiraClientSecret);
        map.add("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange");
        map.add("requested_token_type", "urn:ietf:params:oauth:token-type:refresh_token");
        map.add("subject_token", subjectToken);
        
        LOGGER.debug("Origin Token => {}", subjectToken);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String authTokenPath = authServerUrl + tokenPath;
        ResponseEntity<String> result = restService.postRestApi(authTokenPath, headers, map);

        String resultJsonStr = null;

        if(result.getStatusCodeValue() >= 300)
        {
            resultJsonStr = "{ \"error\" : " + result.getStatusCodeValue() + " }";
        }else
        {
            resultJsonStr = result.getBody();
        }

        HttpHeaders resultHeaders = null;
        if(resultJsonStr != null && !resultJsonStr.contains("error"))
        {
            JSONObject authValues = new JSONObject(resultJsonStr);

            //authValues.getString("refresh_token")
            resultHeaders = new HttpHeaders();
            resultHeaders.setBasicAuth(getLoginId(), authValues.getString("access_token"));
//            resultHeaders.set("Authorization","Bearer " + authValues.getString("access_token"));
            
            LOGGER.debug("Exchange Token => {}", authValues.getString("access_token"));
        }

        return resultHeaders;
    }
    
    public HttpHeaders getJenkinsAccessTokenToHttpHeaders()
    {
        ServletRequestAttributes attr = (ServletRequestAttributes)RequestContextHolder.currentRequestAttributes();
        KeycloakSecurityContext session = (KeycloakSecurityContext) attr.getRequest().getAttribute(KeycloakSecurityContext.class.getName());
        String subjectToken = session.getTokenString();

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", jenkinsClientId);
        map.add("client_secret", jenkinsClientSecret);
        map.add("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange");
        map.add("requested_token_type", "urn:ietf:params:oauth:token-type:refresh_token");
        map.add("subject_token", subjectToken);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String authTokenPath = authServerUrl + tokenPath;
        ResponseEntity<String> result = restService.postRestApi(authTokenPath, headers, map);

        String resultJsonStr = null;

        if(result.getStatusCodeValue() >= 300)
        {
            resultJsonStr = "{ \"error\" : " + result.getStatusCodeValue() + " }";
        }else
        {
            resultJsonStr = result.getBody();
        }

        HttpHeaders resultHeaders = null;
        if(resultJsonStr != null && !resultJsonStr.contains("error"))
        {
//            JSONObject authValues = new JSONObject(resultJsonStr);

            //authValues.getString("refresh_token")
            resultHeaders = new HttpHeaders();
            //resultHeaders.setBearerAuth(authValues.getString("access_token"));
            resultHeaders.setBasicAuth(jenkinsClientId, jenkinsClientSecret);
            //resultHeaders.set("Authorization", "Bearer " + authValues.getString("access_token"));
        }

        return resultHeaders;
    }
}
