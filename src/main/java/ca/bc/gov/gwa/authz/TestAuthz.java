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
        authzClient.getConfiguration().setResource("abc4");
//
//        PermissionRequest pr = new PermissionRequest();
//        pr.setResourceId("a94d3eac-d8d9-4221-9d26-e916a1e62071");
//        pr.setScopes(toSet("api:view-metrics"));
//        PermissionResponse resp = authzClient.protection().permission().create(pr);
//
//        System.out.println(resp.getTicket());
//        
//        Configuration conf = authzClient.getConfiguration();
//        
//        System.out.println(String.format("%s,%s,%s,%s", conf.getAuthServerUrl(), conf.getRealm(), conf.getResource(), (String) conf.getCredentials().get("secret")));
//        Keycloak cl = Keycloak.getInstance(conf.getAuthServerUrl(), conf.getRealm(), "realm-admin-principal", "realm-admin-principal", "admin-cli");
//        
//        TokenManager tk = cl.tokenManager();
//        System.out.println(tk.getAccessTokenString());
                
        /* "Request Access"
            curl -X POST \
            http://${host}:${port}/auth/realms/${realm}/protocol/openid-connect/token \
            -H "Authorization: Bearer ${access_token}" \
            --data "grant_type=urn:ietf:params:oauth:grant-type:uma-ticket" \
            --data "ticket=${permission_ticket} \
            --data "submit_request=true"
        */
        

        String accessToken = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJnQVUzYTNzUXgwcWNfR2JnX0RSLWpUU0xoQWtQMlA5NFJJcHBtUkw5RFJ3In0.eyJleHAiOjE2MDEwOTgzNTAsImlhdCI6MTYwMTA2MjM1MCwiYXV0aF90aW1lIjoxNjAxMDYyMzUwLCJqdGkiOiI3ZTgxMmVkMC1hMTM1LTQ1ZmItYmUzYy02MGU0YjhhMDg2NWQiLCJpc3MiOiJodHRwczovL2F1dGgtcXd6cndjLWRldi5wYXRoZmluZGVyLmdvdi5iYy5jYS9hdXRoL3JlYWxtcy9hcHMiLCJhdWQiOlsicmVhbG0tbWFuYWdlbWVudCIsImFwaS1hcGlzZXJ2LWFyZ2ctdWkiLCJhYmMzIiwiYWNjb3VudCIsImFiYzQiXSwic3ViIjoiZDRkMzU3NGItN2MxYy00ZWFhLTgxNGEtY2E5MTA4OGFlNjk4IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiYXBzIiwibm9uY2UiOiJ0RkQ5YTlrVWl5bGRKMURmZzRuRXdrRkZYbTBobXVpM1ZCSXVQMWVpTHpJIiwic2Vzc2lvbl9zdGF0ZSI6IjdkNDUzNzA0LWJlYjAtNDYwMi05MTllLWRiYmNlNTRmZWYwMCIsImFjciI6IjEiLCJyZXNvdXJjZV9hY2Nlc3MiOnsicmVhbG0tbWFuYWdlbWVudCI6eyJyb2xlcyI6WyJ2aWV3LXVzZXJzIiwicXVlcnktY2xpZW50cyIsInF1ZXJ5LWdyb3VwcyIsInF1ZXJ5LXVzZXJzIl19LCJhcHMiOnsicm9sZXMiOlsidW1hX3Byb3RlY3Rpb24iLCJhcHM6dmlldy1tZXRyaWNzIiwiYXBpLWFkbWluIl19LCJhcGktYXBpc2Vydi1hcmdnLXVpIjp7InJvbGVzIjpbImFkbWluIiwibWFuYWdlLWd3LWNvbmZpZyJdfSwiYWJjMyI6eyJyb2xlcyI6WyJhYmMzOm1ldHJpY3MiXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfSwiYWJjNCI6eyJyb2xlcyI6WyJ1bWFfcHJvdGVjdGlvbiIsInllcC1yb2xlcyJdfX0sInNjb3BlIjoib3BlbmlkIHByb2ZpbGUgZW1haWwiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsIm5hbWUiOiJBaWRhbiBDb3BlIiwidGVhbSI6Ii90ZWFtL2FwaXNlcnYiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJhY29wZUBpZGlyIiwiZ2l2ZW5fbmFtZSI6IkFpZGFuIiwiZmFtaWx5X25hbWUiOiJDb3BlIiwiZW1haWwiOiJhaWRhbi5jb3BlQGdtYWlsLmNvbSJ9.MdIUgHN3_7AuJNVlrkDeZfS7ASzl7CMbm5tG-WdBMiHDQwHgUxUFUcE4tctg43MxGddVj_E_EHEc7eGnf53rdmu8pOT8LsKtc7QW9zJIDx7SmC8weDikD47s417riSykSh585SP1bEJaDycYJ2UYAVPJo4XVRVGjue5Sjf_0N0tihIdp1eZCW1sl0NswDblYtXnsr7LwoW4juDw6UoF-AjjPKg-2HaLxXFn_8o7jl5PSO-Kn4WeHnHFBmU7Toe3ojj5kcktF6RJJUCnp3Yz2uy_dNvRWEH07a2h_3HfFMTPBsQNXI920x2Di-O3xoxMsfrQDPSTnSxCogqRwSHsnLQ";
        
        ResourceRepresentation newResource = new ResourceRepresentation();

        newResource.setName("api:bcdcp2-solr-6");
        newResource.setType("urn:hello-world-authz:resources:example");
        newResource.setOwnerManagedAccess(true);
        
        newResource.setOwner("4e69b6db-ff84-452d-b63f-24f435c7f528"); // Team Health Role
        newResource.setOwner("d4d3574b-7c1c-4eaa-814a-ca91088ae698"); // User acope@idir
        //newResource.setOwner((String)null); // client Api-apiserv-kq-ui
        //newResource.setOwner("5e3ec00c-c197-4a9b-ba7d-f60ea08f447b"); // craig

        String[] scopes = new String[] {
            "urn:apimgmt:scopes:view-metrics",
//            "urn:apimgmt:scopes:view-config",
//            "urn:apimgmt:scopes:pr-config",
//            "urn:apimgmt:scopes:manage-team-access",
//            "urn:apimgmt:scopes:manage-consumer-access"
        };
        for ( String scope : scopes ) {
            newResource.addScope(new ScopeRepresentation(scope));
        }
        
        ProtectedResource resourceClient = authzClient.protection(accessToken).resource();

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

    }
}
