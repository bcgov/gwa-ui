/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.bc.gov.gwa.authz;

import ca.bc.gov.gwa.servlet.authentication.oidc.LookupUtil;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.token.TokenManager;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.Configuration;
import org.keycloak.authorization.client.resource.ProtectedResource;
import org.keycloak.authorization.client.resource.ProtectionResource;
import org.keycloak.representations.idm.authorization.AuthorizationResponse;
import org.keycloak.representations.idm.authorization.PermissionRequest;
import org.keycloak.representations.idm.authorization.PermissionResponse;
import org.keycloak.representations.idm.authorization.ResourceRepresentation;
import org.keycloak.representations.idm.authorization.ScopeRepresentation;
import org.pac4j.core.profile.CommonProfile;

/**
 *
 * https://www.keycloak.org/docs/latest/authorization_services/
 */
public class TestAuthzRequest {
    static public Set<String> toSet (String... list) {
        Set<String> res = new HashSet<>();
        for ( String s : list ) {
            res.add(s);
        }
        return res;
    }
    
    static public void main(String[] args) throws UnsupportedEncodingException, IOException {
        
        // https://www.keycloak.org/docs/latest/authorization_services/
        
        
        AuthzClient authzClient = AuthzClient.create();
        
        String accessToken;

        accessToken = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJnQVUzYTNzUXgwcWNfR2JnX0RSLWpUU0xoQWtQMlA5NFJJcHBtUkw5RFJ3In0.eyJleHAiOjE2MDEwMjY3NTQsImlhdCI6MTYwMDk5MTA5OSwianRpIjoiMDdmMjU0N2MtYTViYi00MDRiLWIxOTYtNWYyZjFiZmNiZGNkIiwiaXNzIjoiaHR0cHM6Ly9hdXRoLXF3enJ3Yy1kZXYucGF0aGZpbmRlci5nb3YuYmMuY2EvYXV0aC9yZWFsbXMvYXBzIiwiYXVkIjpbInJlYWxtLW1hbmFnZW1lbnQiLCJhcGktYXBpc2Vydi1hcmdnLXVpIiwiYWNjb3VudCJdLCJzdWIiOiJkNGQzNTc0Yi03YzFjLTRlYWEtODE0YS1jYTkxMDg4YWU2OTgiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJhcHMiLCJub25jZSI6IjhGX19pVnA3M1dUbzZZZklUaEd1aDlMTGd3X2Y2cWRqQVBEaVhVd1poWWsiLCJzZXNzaW9uX3N0YXRlIjoiY2UzZDA4MDgtZmNiMi00OGRhLWI3NTctNTY2MmNjNzA1MDM4IiwiYWNyIjoiMCIsInJlc291cmNlX2FjY2VzcyI6eyJyZWFsbS1tYW5hZ2VtZW50Ijp7InJvbGVzIjpbInZpZXctdXNlcnMiLCJxdWVyeS1jbGllbnRzIiwicXVlcnktZ3JvdXBzIiwicXVlcnktdXNlcnMiXX0sImFwcyI6eyJyb2xlcyI6WyJhcGktYWRtaW4iXX0sImFwaS1hcGlzZXJ2LWFyZ2ctdWkiOnsicm9sZXMiOlsiYWRtaW4iLCJtYW5hZ2UtZ3ctY29uZmlnIl19LCJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6Im9wZW5pZCBwcm9maWxlIGVtYWlsIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJuYW1lIjoiQWlkYW4gQ29wZSIsInByZWZlcnJlZF91c2VybmFtZSI6ImFjb3BlQGlkaXIiLCJnaXZlbl9uYW1lIjoiQWlkYW4iLCJmYW1pbHlfbmFtZSI6IkNvcGUiLCJlbWFpbCI6ImFpZGFuLmNvcGVAZ21haWwuY29tIn0.hpR9iSJNlxXAMX-wxISTnA2eD_F-GnBLtc4LEmR0y1IfCZDnb2t0AiZZB_5XVB3KuJQnsf2r-KsSPH4W_wTkQXnDthsd_i1l_cWjS6pQE7iatBd161pDoi4oGuwdsz7UKcTpoWPhH9faBi4ORD1l-Jj1UpJ61X8UX6gJwZu3VVQjOm9_yT0otDUSVfDaDafCH5L-MipR2a0CvsAi26ZSLFPyJr9R45yaZD8RWyz0xa9G5XFdVV-EHKqERf1JbgCPU6QTqC3NWCPxiIWQC90AKscwnaWVWsBlnSqqHskWcYDJ4aYmG1UB5ssN1EVpalDRj0AkqTZiF2gvbo7KWaju-g";
        
        //AuthorizationResponse response = authzClient.authorization(accessToken).authorize();
//        System.out.println(response.getOtherClaims());

//        authzClient.getConfiguration().setResource("app-2");
        

        ProtectionResource prot = authzClient.protection();

        PermissionRequest pr = new PermissionRequest();
        pr.setResourceId("a94d3eac-d8d9-4221-9d26-e916a1e62071"); // api:adecat-dlv-webade-oauth-webade
        pr.setResourceId("2d885ebf-6bc8-4633-9b10-eec17fe9810c"); // api:ocicat-dlv-apps
        //pr.setResourceId("8c3bb793-2618-407a-b519-946c218d16f8"); // api:test in app-2
        pr.setResourceId("9a816d2b-a270-481c-b3a3-e910b7bb9854");
        //pr.setResourceId("488410b3-48ae-405a-b321-7086906668b4");
        pr.setResourceId("e99a5a70-e716-46c7-84d7-0838a36bb5e9"); // a resource owned by 'craig'
        
        pr.setScopes(toSet("urn:apimgmt:scopes:view-metrics"));
        PermissionResponse resp = prot.permission().create(pr);

        // Get a 500 error when the resource belongs to a different client than the one
        // the AuthzClient is configured for
        
        System.out.println("TICKET = "+resp.getTicket());
//        if (true) return;
        
        Configuration conf = authzClient.getConfiguration();
        
        //conf.setResource("app-2");
        
        System.out.println(String.format("%s,%s,%s,%s", conf.getAuthServerUrl(), conf.getRealm(), conf.getResource(), (String) conf.getCredentials().get("secret")));
        //Keycloak cl = Keycloak.getInstance(conf.getAuthServerUrl(), conf.getRealm(), "realm-admin-principal", "realm-admin-principal", "admin-cli");
        
        // User the currently logged in user's token
        //CommonProfile up = LookupUtil.lookupUserProfile(request, response);
        
//        String accessToken = up.getAttribute("access_token", BearerAccessToken.class).getValue();

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(String.format("%s/realms/%s/protocol/openid-connect/token", conf.getAuthServerUrl(), conf.getRealm()));

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("grant_type", "urn:ietf:params:oauth:grant-type:uma-ticket"));
        params.add(new BasicNameValuePair("ticket", resp.getTicket()));
        //params.add(new BasicNameValuePair("permission", "api:bcdcp2-solr"));
        
        params.add(new BasicNameValuePair("submit_request", "false"));
        params.add(new BasicNameValuePair("response_mode", "permissions"));
        params.add(new BasicNameValuePair("audience", "aps-resources"));
        httpPost.setEntity(new UrlEncodedFormEntity(params));
        httpPost.addHeader("Authorization",  "Bearer " + accessToken);
        CloseableHttpResponse answer = client.execute(httpPost);
        System.out.println("Answer = " + answer.getStatusLine());
        String result = IOUtils.toString(answer.getEntity().getContent());
        System.out.println("Result = "+result);
        client.close();

        /*
        401 - Unauthorized : Could be because of an expired token?
        403 - Forbidden : Means that a ticket was created to request access
        200 - User has permission to the resource already
        */
        
    }
}
