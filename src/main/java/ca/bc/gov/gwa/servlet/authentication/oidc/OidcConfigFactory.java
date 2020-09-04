/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.bc.gov.gwa.servlet.authentication.oidc;

import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.core.config.ConfigFactory;
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.oidc.client.KeycloakOidcClient;
import org.pac4j.oidc.config.KeycloakOidcConfiguration;
import ca.bc.gov.gwa.v2.conf.GwaSettings;

public class OidcConfigFactory implements ConfigFactory {

    @Override
    public Config build(final Object... parameters) {
        final GwaSettings settings = new GwaSettings();
        
        final KeycloakOidcConfiguration oidcConfiguration = new KeycloakOidcConfiguration();
        oidcConfiguration.setRealm(settings.getOidcRealm());
        oidcConfiguration.setBaseUri(settings.getOidcBaseUri());
        oidcConfiguration.setClientId(settings.getOidcClientId());
        oidcConfiguration.setSecret(settings.getOidcClientSecret());
        oidcConfiguration.setUseNonce(true);
        
        //oidcClient.setPreferredJwsAlgorithm(JWSAlgorithm.RS256);
        //oidcConfiguration.addCustomParam("prompt", "consent");

        final KeycloakOidcClient kcClient = new KeycloakOidcClient(oidcConfiguration);

        final Clients clients = new Clients(settings.getOidcCallbackUrl(), kcClient);

        clients.setAjaxRequestResolver(new CustomAjaxRequestResolver());
        final Config config = new Config(clients);

        DefaultSecurityLogic.INSTANCE.setAuthorizationChecker(new CustomDefaultAuthorizationChecker());

        return config;
    }
}