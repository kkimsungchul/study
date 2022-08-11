package com.keycloak.event.listener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RealmProvider;
import org.keycloak.models.UserModel;

public class LoginEventListenerProvider implements EventListenerProvider {

    private static final Logger log = Logger.getLogger(LoginEventListenerProvider.class);

    private final KeycloakSession session;
    private final RealmProvider model;

    public LoginEventListenerProvider(KeycloakSession session) {
        this.session = session;
        this.model = session.realms();
    }

    @Override
    public void onEvent(Event event){

        System.out.println("### event.getid : " +event.getIpAddress());

        if(EventType.LOGIN_ERROR == event.getType()){


            String errorStr = event.getError();
            RealmModel realmModel = this.model.getRealm(event.getRealmId());
            String realmName = realmModel.getName();

//            UserModel user = session.userStorageManager().getUserById(realmModel, event.getUserId());
//            UserModel user = this.session.users().getUserById(realmModel, event.getUserId());
            UserModel user=null;
//            boolean isTempDisabled = session.getProvider(BruteForceProtector.class).isTemporarilyDisabled(session, realmModel, user);


            try{
                user = this.session.users().getUserById(realmModel, event.getUserId());
                /*
                 * 사용자가 여러차례 로그인 실패로 인해 임시로 disable된 상태가 되면 통계 수집용 Rest API을 호출
                 */
                if(user != null && "user_temporarily_disabled".equals(errorStr)){
                    log.info("-----------------------------------------------------------");
                    log.info("## LOGIN_ERROR User_Temporarily_Disabled EVENT");
                    event.getDetails().forEach((key, value) -> log.info(key + ": " + value));
                    log.info("-----------------------------------------------------------");
                }
            }catch (Exception e){
                log.info("-------------------------------------------------");
                log.info("## USER LOGIN ERROR , USER INFO NOT FOUND");
                log.info("-------------------------------------------------");
            }
        }else if(EventType.LOGIN == event.getType()){
            log.info("-----------------------------------------------------------");
            log.info("## LOGIN EVENT");

            event.getDetails().forEach((key, value) -> System.out.println("### " + key + ": " + value));

            RealmModel realmModel = this.model.getRealm(event.getRealmId());
            String realmName = realmModel.getName();
            UserModel user = this.session.users().getUserById(realmModel, event.getUserId());



            /*
             * 중복 로그인 세션을 제거하고 마지막 로그인을 시도한 세션 하나만 유지
             */
//            InMemoryUserAdapter userInMemory = new InMemoryUserAdapter(this.session, realmModel, event.getUserId());

            System.out.println("### this.session.sessions().getUserSessionsStream(realmModel, user) : " + this.session.sessions().getUserSessionsStream(realmModel, user).count());

            this.session.sessions().getUserSessionsStream(realmModel, user).forEach(userSession ->{
                // remove all existing user sessions but the current one (last one wins)
                // this is HIGHLANDER MODE - there must only be one!
                if(!userSession.getId().equals(event.getSessionId())){
                    this.session.sessions().removeUserSession(realmModel, userSession);
                }else{
                    this.session.sessions().removeUserSession(realmModel, userSession);
                }

            });
            log.info("-----------------------------------------------------------");
        }
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean b) {
//        log.info("## NEW ADMIN EVENT");
//        log.info("-----------------------------------------------------------");
//        log.info("Resource path" + ": " + adminEvent.getResourcePath());
//        log.info("Resource type" + ": " + adminEvent.getResourceType());
//        log.info("Operation type" + ": " + adminEvent.getOperationType());

        if (ResourceType.USER.equals(adminEvent.getResourceType())
                && OperationType.CREATE.equals(adminEvent.getOperationType())) {
//            log.info("A new user has been created");
        }

//        log.info("-----------------------------------------------------------");
    }

    @Override
    public void close() {
        // Nothing to close
    }

    public void get(String strUrl){
        BufferedReader br = null;
        try{
            URL url = new URL(strUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(5000); //서버에 연결되는 Timeout 시간 설정
            con.setReadTimeout(5000); // InputStream 읽어 오는 Timeout 시간 설정
            con.setRequestMethod("GET");
//            con.setRequestProperty("Content-Type", "application/json");

            /*
             * URLConnection에 대한 doOutput 필드값을 지정된 값으로 설정한다.
             * URL 연결은 입출력에 사용될 수 있다. URL 연결을 출력용으로 사용하려는 경우 DoOutput 플래그를 true로 설정하고,
             * 그렇지 않은 경우는 false로 설정해야 한다. 기본값은 false이다.
             */
            con.setDoOutput(false);

            StringBuffer sb = new StringBuffer();
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK){
                br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));

                String line = null;
                while ((line = br.readLine()) != null){
                    sb.append(line).append("\n");
                }

                log.info("GET Rest API Url="+strUrl+", responseStr=" + sb.toString());
            }else{
                log.info("GET Rest API Url="+strUrl+", responseCode="+con.getResponseCode()+", responseMessage=" + con.getResponseMessage());
            }
        }catch(IOException ioe){
            log.error("GET Rest API Url="+strUrl+", exceptionMessage="+ioe.getMessage());
        }finally{
            if(br != null){try{br.close();}catch(IOException e){}}
        }
    }

    public void post(String strUrl, String jsonMessage){
        OutputStreamWriter wr = null;
        BufferedReader br = null;

        try{
            URL url = new URL(strUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(5000); //서버에 연결되는 Timeout 시간 설정
            con.setReadTimeout(5000); // InputStream 읽어 오는 Timeout 시간 설정
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
//            con.setDoInput(true);
            con.setDoOutput(true); //POST 데이터를 OutputStream으로 넘겨 주겠다는 설정
//            con.setUseCaches(false);
//            con.setDefaultUseCaches(false);

            wr = new OutputStreamWriter(con.getOutputStream());
            wr.write(jsonMessage); //json 형식의 message 전달
            wr.flush();

            StringBuffer sb = new StringBuffer();
            if(con.getResponseCode() == HttpURLConnection.HTTP_OK){
                br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));

                String line = null;
                while((line = br.readLine()) != null)
                {
                    sb.append(line).append("\n");
                }

                log.info("POST Rest API Url="+strUrl+", responseStr=" + sb.toString());
            }else{
                log.info("POST Rest API Url="+strUrl+", responseCode="+con.getResponseCode()+", responseMessage=" + con.getResponseMessage());
            }
        }catch(IOException ioe){
            log.error("POST Rest API Url="+strUrl+", exceptionMessage="+ioe.getMessage());
        }finally{
            if(br != null){try{br.close();}catch(IOException e){}}
            if(wr != null){try{wr.close();}catch(IOException e){}}
        }
    }
}