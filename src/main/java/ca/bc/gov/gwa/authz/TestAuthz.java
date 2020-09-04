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
import org.keycloak.representations.idm.authorization.PermissionRequest;
import org.keycloak.representations.idm.authorization.PermissionResponse;
import org.keycloak.representations.idm.authorization.ResourceRepresentation;
import org.keycloak.representations.idm.authorization.ScopeRepresentation;

/**
 *
 * https://www.keycloak.org/docs/latest/authorization_services/
 */
public class TestAuthz {
    static public Set<String> toSet (String... list) {
        Set<String> res = new HashSet<>();
        for ( String s : list ) {
            res.add(s);
        }
        return res;
    }
    
    static public void main(String[] args) {
        
        
        AuthzClient authzClient = AuthzClient.create();

        PermissionRequest pr = new PermissionRequest();
        pr.setResourceId("a94d3eac-d8d9-4221-9d26-e916a1e62071");
        pr.setScopes(toSet("api:view-metrics"));
        PermissionResponse resp = authzClient.protection().permission().create(pr);

        System.out.println(resp.getTicket());
        
        Configuration conf = authzClient.getConfiguration();
        
        System.out.println(String.format("%s,%s,%s,%s", conf.getAuthServerUrl(), conf.getRealm(), conf.getResource(), (String) conf.getCredentials().get("secret")));
        Keycloak cl = Keycloak.getInstance(conf.getAuthServerUrl(), conf.getRealm(), "realm-admin-principal", "realm-admin-principal", "admin-cli");
        
        TokenManager tk = cl.tokenManager();
        System.out.println(tk.getAccessTokenString());
                
        /* "Request Access"
            curl -X POST \
            http://${host}:${port}/auth/realms/${realm}/protocol/openid-connect/token \
            -H "Authorization: Bearer ${access_token}" \
            --data "grant_type=urn:ietf:params:oauth:grant-type:uma-ticket" \
            --data "ticket=${permission_ticket} \
            --data "submit_request=true"
        */
        

        /*
        ResourceRepresentation newResource = new ResourceRepresentation();

        newResource.setName("api:bcdcp2-solr");
        newResource.setType("urn:hello-world-authz:resources:example");
        newResource.setOwnerManagedAccess(true);
        
        newResource.setOwner("4e69b6db-ff84-452d-b63f-24f435c7f528"); // Team Health Role
        newResource.setOwner("e29bb6cb-0fd2-4c6f-a838-1e3a92615ca9"); // User ac

        String[] scopes = new String[] {
            "urn:apimgmt:scopes:view-metrics",
            "urn:apimgmt:scopes:view-config",
            "urn:apimgmt:scopes:pr-config",
            "urn:apimgmt:scopes:manage-team-access",
            "urn:apimgmt:scopes:manage-consumer-access"
        };
        for ( String scope : scopes ) {
            newResource.addScope(new ScopeRepresentation(scope));
        }
        
        ProtectedResource resourceClient = authzClient.protection().resource();

        ResourceRepresentation existingResource = resourceClient.findByName(newResource.getName());

        if (existingResource != null) {
            resourceClient.delete(existingResource.getId());
        }

        // create the resource on the server
        ResourceRepresentation response = resourceClient.create(newResource);
        String resourceId = response.getId();

        // query the resource using its newly generated id
        ResourceRepresentation resource = resourceClient.findById(resourceId);

        System.out.println(resource);    
*/
    }
}
