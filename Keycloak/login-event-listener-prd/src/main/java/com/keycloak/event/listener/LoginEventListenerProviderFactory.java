package com.keycloak.event.listener;

import org.keycloak.Config;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class LoginEventListenerProviderFactory implements EventListenerProviderFactory {

    @Override
    public LoginEventListenerProvider create(KeycloakSession keycloakSession) {
        return new LoginEventListenerProvider(keycloakSession);
    }

    @Override
    public void init(Config.Scope scope) {
        // Nothing to if
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
        // Nothing to if
    }

    @Override
    public void close() {
        // Nothing to if
    }

    @Override
    public String getId() {
        return "login_event_listener";
    }

}
