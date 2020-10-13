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
public class TestAuthzPEP {
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
        
        Configuration conf = authzClient.getConfiguration();

        String accessToken;

        accessToken = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJnQVUzYTNzUXgwcWNfR2JnX0RSLWpUU0xoQWtQMlA5NFJJcHBtUkw5RFJ3In0.eyJleHAiOjE2MDEwOTY1NDUsImlhdCI6MTYwMTA2MDYxOCwiYXV0aF90aW1lIjoxNjAxMDYwNTQ1LCJqdGkiOiJmNGVmMTc5OS1mOTkzLTQ2NzAtODZlZC01YWY1MmFjM2Y1NTMiLCJpc3MiOiJodHRwczovL2F1dGgtcXd6cndjLWRldi5wYXRoZmluZGVyLmdvdi5iYy5jYS9hdXRoL3JlYWxtcy9hcHMiLCJhdWQiOlsicmVhbG0tbWFuYWdlbWVudCIsImFwaS1hcGlzZXJ2LWFyZ2ctdWkiLCJhYmMzIiwiYWNjb3VudCIsImFiYzQiXSwic3ViIjoiZDRkMzU3NGItN2MxYy00ZWFhLTgxNGEtY2E5MTA4OGFlNjk4IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiYXBzIiwibm9uY2UiOiJaZkhPQlFTVTJfSFZ4YlFCWnJJVzRXUzNMRUp4cWZSbUxhaFg3c0VUdXNRIiwic2Vzc2lvbl9zdGF0ZSI6ImRmNDhkOWQ2LWM0ZTgtNDI4My1hZjFlLTQ2OTVkYzUwMDI1YiIsImFjciI6IjAiLCJyZXNvdXJjZV9hY2Nlc3MiOnsicmVhbG0tbWFuYWdlbWVudCI6eyJyb2xlcyI6WyJ2aWV3LXVzZXJzIiwicXVlcnktY2xpZW50cyIsInF1ZXJ5LWdyb3VwcyIsInF1ZXJ5LXVzZXJzIl19LCJhcHMiOnsicm9sZXMiOlsiYXBzOnZpZXctbWV0cmljcyIsImFwaS1hZG1pbiJdfSwiYXBpLWFwaXNlcnYtYXJnZy11aSI6eyJyb2xlcyI6WyJhZG1pbiIsIm1hbmFnZS1ndy1jb25maWciXX0sImFiYzMiOnsicm9sZXMiOlsiYWJjMzptZXRyaWNzIl19LCJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX0sImFiYzQiOnsicm9sZXMiOlsieWVwLXJvbGVzIl19fSwic2NvcGUiOiJvcGVuaWQgcHJvZmlsZSBlbWFpbCIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwibmFtZSI6IkFpZGFuIENvcGUiLCJ0ZWFtIjoiL3RlYW0vYXBpc2VydiIsInByZWZlcnJlZF91c2VybmFtZSI6ImFjb3BlQGlkaXIiLCJnaXZlbl9uYW1lIjoiQWlkYW4iLCJmYW1pbHlfbmFtZSI6IkNvcGUiLCJlbWFpbCI6ImFpZGFuLmNvcGVAZ21haWwuY29tIn0.TTpv7lg1D6rkP8Ct7v9e0-jYIxJvr2z6HYtGhZdYVmRYkdJoqLW_B1qDFp59CWLn9VjVzPRxuO9CdEqxKnLFN8P1_AOT2032ctV_PPsBD5t4_K-4lOfBywOgt6YD7yB-_RwvihuThOcJGdyWiOA4xspEXJBWn7e5DOpu72YQQ-kEQsmCMlGD5zJ4zha0yEWDxsmIX_LCX1cZrYj65KoUlNvj0kQf-qMhdysXcaSmxWyYNXTxXiR3nK7ydemAInD9if5Ym78GjrrODYicYuAhiGxqeK52Nq-3C8alpa8LNV7HCuVoamBjAI9w7jNCTIsjWns-DgGMhZxjC-B60mcaxQ";
        

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(String.format("%s/realms/%s/protocol/openid-connect/token", conf.getAuthServerUrl(), conf.getRealm()));

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("grant_type", "urn:ietf:params:oauth:grant-type:uma-ticket"));
        //params.add(new BasicNameValuePair("permission", "api:bcdcp2-solr#urn:apimgmt:scopes:pr-config"));
        
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
