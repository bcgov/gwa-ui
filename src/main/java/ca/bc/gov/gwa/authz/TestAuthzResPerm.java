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
import org.apache.http.entity.StringEntity;
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
public class TestAuthzResPerm {
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

        accessToken = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJnQVUzYTNzUXgwcWNfR2JnX0RSLWpUU0xoQWtQMlA5NFJJcHBtUkw5RFJ3In0.eyJleHAiOjE2MDEwNDI3OTEsImlhdCI6MTYwMTAwNzEyNSwiYXV0aF90aW1lIjoxNjAxMDA2NzkxLCJqdGkiOiI3YTBhNWY5MC1kMjQ3LTQ3MTItODMzNC04M2FmOTc4NzI5OGMiLCJpc3MiOiJodHRwczovL2F1dGgtcXd6cndjLWRldi5wYXRoZmluZGVyLmdvdi5iYy5jYS9hdXRoL3JlYWxtcy9hcHMiLCJhdWQiOlsicmVhbG0tbWFuYWdlbWVudCIsImFwaS1hcGlzZXJ2LWFyZ2ctdWkiLCJhY2NvdW50Il0sInN1YiI6ImQ0ZDM1NzRiLTdjMWMtNGVhYS04MTRhLWNhOTEwODhhZTY5OCIsInR5cCI6IkJlYXJlciIsImF6cCI6ImFwcyIsIm5vbmNlIjoiWkt3UW1yZ3lhZVBCOUJaLTlwUzFRUWs0YWI1NzZzQ29tVkxmLVRjQWZhRSIsInNlc3Npb25fc3RhdGUiOiIwNGJkMWQ3Yi03ZjY2LTRmZGItYmIyMS1hY2EwZDFkZGMyMDEiLCJhY3IiOiIwIiwicmVzb3VyY2VfYWNjZXNzIjp7InJlYWxtLW1hbmFnZW1lbnQiOnsicm9sZXMiOlsidmlldy11c2VycyIsInF1ZXJ5LWNsaWVudHMiLCJxdWVyeS1ncm91cHMiLCJxdWVyeS11c2VycyJdfSwiYXBzIjp7InJvbGVzIjpbImFwaS1hZG1pbiJdfSwiYXBpLWFwaXNlcnYtYXJnZy11aSI6eyJyb2xlcyI6WyJhZG1pbiIsIm1hbmFnZS1ndy1jb25maWciXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoib3BlbmlkIHByb2ZpbGUgZW1haWwiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsIm5hbWUiOiJBaWRhbiBDb3BlIiwicHJlZmVycmVkX3VzZXJuYW1lIjoiYWNvcGVAaWRpciIsImdpdmVuX25hbWUiOiJBaWRhbiIsImZhbWlseV9uYW1lIjoiQ29wZSIsImVtYWlsIjoiYWlkYW4uY29wZUBnbWFpbC5jb20ifQ.VnPQ4m1K4v4W4FWTW6CQUxrJBjm4IuMw5oTxFw_G8wlZKQW4IahACvmkqYXyzXFcBBqLi7ldylKNJLYMlKV-oA_w4WfW7p7UVeh_0bk3L5DfKRAo7OisUMcLgWYOpRRE0IqprHxoWFJ4T7iFrP5m-aVcK32INxJS4DpQ4rM05oWip9YYciCoCWvrPZKl7LakZb46a4Amn4vKbAoGCH09LAEAkAbEf5xxT0SgtbPvKA1rYdA3GGalkJG5t5Acxno4eWcpSd8bPtHSIls7Ag-q_37UsfDbJjBQ2Afdv-PRsZ9jZd6weVGwZSR8HrGWMYr53OQ5bjEp1ZS8axe7V0oabQ";

        //accessToken = authzClient.obtainAccessToken().getToken();

        String resId = "2c5a0b17-331c-4f28-8864-20fd58d66034";
        resId = "9a816d2b-a270-481c-b3a3-e910b7bb9854"; // for acope@idir
        resId = "9816cbed-379e-45c9-8379-b723442ada14";
        
        
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(String.format("%s/realms/%s/authz/protection/uma-policy/" + resId, conf.getAuthServerUrl(), conf.getRealm()));

        httpPost.addHeader("Content-Type", "application/json");
        
        String json = "{\"name\":\"Any people manager\",\"description\":\"Allow access to any people manager\",\"scopes\":[\"urn:apimgmt:scopes:view-metrics\"],\"roles\":[\"api:call\"]}";
        StringEntity entity = new StringEntity(json);        
        
        httpPost.setEntity(entity);
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
