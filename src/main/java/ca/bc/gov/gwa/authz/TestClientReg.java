/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.bc.gov.gwa.authz;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.token.TokenManager;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.Configuration;
import org.keycloak.authorization.client.resource.ProtectedResource;
import org.keycloak.client.registration.Auth;
import static org.keycloak.client.registration.Auth.token;
import org.keycloak.client.registration.ClientRegistration;
import org.keycloak.client.registration.ClientRegistrationException;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.authorization.PermissionRequest;
import org.keycloak.representations.idm.authorization.PermissionResponse;
import org.keycloak.representations.idm.authorization.ResourceRepresentation;
import org.keycloak.representations.idm.authorization.ScopeRepresentation;

/**
 *
 * https://www.keycloak.org/docs/latest/authorization_services/
 */
public class TestClientReg {
    static public Set<String> toSet (String... list) {
        Set<String> res = new HashSet<>();
        for ( String s : list ) {
            res.add(s);
        }
        return res;
    }
    
    static public void main(String[] args) throws ClientRegistrationException {
        String accessToken;

        AuthzClient authzClient = AuthzClient.create();
        Configuration config = authzClient.getConfiguration();
        
        accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJmNGI3YjJiMS03YjJkLTQ1YWMtOWMxYy0zYWRlOWExYzQ3MDMifQ.eyJleHAiOjAsImlhdCI6MTYwMTA1MzMxNiwianRpIjoiNjJlNTFiZjYtYjQ4Yi00YjBkLWIwMGEtMWVjNTcyOTc4MGNmIiwiaXNzIjoiaHR0cHM6Ly9hdXRoLXF3enJ3Yy1kZXYucGF0aGZpbmRlci5nb3YuYmMuY2EvYXV0aC9yZWFsbXMvYXBzIiwiYXVkIjoiaHR0cHM6Ly9hdXRoLXF3enJ3Yy1kZXYucGF0aGZpbmRlci5nb3YuYmMuY2EvYXV0aC9yZWFsbXMvYXBzIiwidHlwIjoiSW5pdGlhbEFjY2Vzc1Rva2VuIn0.7i7rklRQ16QKq39KgyzQViuKoDLTV06PPRxfb45uuLU";

        ClientRepresentation client = new ClientRepresentation();
        client.setClientId("abc5");
        client.setStandardFlowEnabled(false);
        client.setDefaultRoles(new String[0]);
        client.setOptionalClientScopes(new ArrayList());
        

        System.out.println(config.getAuthServerUrl());
        System.out.println(config.getRealm());
        ClientRegistration reg = ClientRegistration.create()
            .url(config.getAuthServerUrl(), config.getRealm())
            .build();

        reg.auth(Auth.token(accessToken));

        client = reg.create(client);

        String registrationAccessToken = client.getRegistrationAccessToken();
        System.out.println(registrationAccessToken);    

        
    }
}
