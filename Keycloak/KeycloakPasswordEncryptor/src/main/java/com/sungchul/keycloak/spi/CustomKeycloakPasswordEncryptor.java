package com.sungchul.keycloak.spi;

import com.sungchul.keycloak.spi.helper.EncryptionHelper;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.*;
import org.keycloak.models.credential.PasswordUserCredentialModel;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

public class CustomKeycloakPasswordEncryptor implements Authenticator {

    public void authenticate(AuthenticationFlowContext authenticationFlowContext) {
        String remoteIPAddress = authenticationFlowContext.getConnection().getRemoteAddr();
        String getRemoteHost = authenticationFlowContext.getConnection().getRemoteHost();
        String getLocalAddr = authenticationFlowContext.getConnection().getRemoteAddr();


        //String allowedIPAddress = getAllowedIPAddress(authenticationFlowContext);
        System.out.println("##############################");
        System.out.println("### remoteIPAddress : " + remoteIPAddress);
        System.out.println("### getRemoteHost : " + getRemoteHost);
        System.out.println("### getLocalAddr : " + getLocalAddr);
//        getAllowedIPAddress(authenticationFlowContext);
        System.out.println("##############################");



        // not bringing username
        if(authenticationFlowContext.getHttpRequest().getFormParameters().get("username") == null
                || authenticationFlowContext.getHttpRequest().getFormParameters().get("username").isEmpty()) {

            Response challenge =  Response.status(400)
                    .entity("{\"error\":\"invalid_request\",\"error_description\":\"No Username\"}")
                    .header("Content-Type", "application/json")
                    .build();
            authenticationFlowContext.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
            return;
        }

        // not bringing password
        if(authenticationFlowContext.getHttpRequest().getFormParameters().get("password") == null
                || authenticationFlowContext.getHttpRequest().getFormParameters().get("password").isEmpty()) {

            Response challenge =  Response.status(400)
                    .entity("{\"error\":\"invalid_request\",\"error_description\":\"No Password\"}")
                    .header("Content-Type", "application/json")
                    .build();
            authenticationFlowContext.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
            return;
        }

        // capture username
        String username = authenticationFlowContext.getHttpRequest().getFormParameters().getFirst("username").trim();

        // search for corresponding user
        List<UserModel> userModels = authenticationFlowContext.getSession().users().searchForUser(username, authenticationFlowContext.getRealm());

        // user not exists
        if(userModels.isEmpty()) {
            Response challenge =  Response.status(400)
                    .entity("{\"error\":\"invalid_request\",\"error_description\":\"User Not Found\"}")
                    .header("Content-Type", "application/json")
                    .build();
            authenticationFlowContext.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
            return;
        }

        // capture usermodel, means user is exist
        UserModel userModel = userModels.get(0);

        // capture password and dont forget to html-decode the content (im using a string replacement for this example)
        String password = authenticationFlowContext.getHttpRequest().getFormParameters().getFirst("password").trim();
        password = password.replace("%3D", "=");

        // decrypt the password
        //여기 수정해야함 귀찬아서 다 쓰로우햇음;;
        System.out.println("############### password : "  + password);
        try{
            password = EncryptionHelper.decrypt(password);
        }catch (Exception e){

        }
        System.out.println("############### password : "  + password);

        // password is incorrect
        PasswordUserCredentialModel credentialInput = UserCredentialModel.password(password);
        boolean valid = authenticationFlowContext.getSession().userCredentialManager().isValid(authenticationFlowContext.getRealm(),
                userModel,
                new PasswordUserCredentialModel[]{credentialInput} );
        if( !valid ) {
            Response challenge =  Response.status(400)
                    .entity("{\"error\":\"invalid_request\",\"error_description\":\"User Not Found\"}")
                    .header("Content-Type", "application/json")
                    .build();
            authenticationFlowContext.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
            return;
        }

        // set user
        authenticationFlowContext.setUser(userModel);

        // all validation success
        authenticationFlowContext.success();
    }

    public void action(AuthenticationFlowContext authenticationFlowContext) {
        authenticationFlowContext.success();
    }

    public boolean requiresUser() {
        return false;
    }

    public boolean configuredFor(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
        return false;
    }

    public void setRequiredActions(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {

    }

    public void close() {

    }

    private void getAllowedIPAddress(AuthenticationFlowContext context) {
        AuthenticatorConfigModel configModel = context.getAuthenticatorConfig();
        Map<String, String> config = configModel.getConfig();
        config.forEach((strKey, strValue)->{
            System.out.println( "### " + strKey +" :"+ strValue );
        });

    }
}