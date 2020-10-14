/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.bc.gov.gwa.v2.services;

import ca.bc.gov.gwa.http.HttpStatusException;
import ca.bc.gov.gwa.http.JsonHttpClient;
import static ca.bc.gov.gwa.servlet.GwaConstants.ACLS_PATH;
import static ca.bc.gov.gwa.servlet.GwaConstants.CONSUMERS_PATH;
import static ca.bc.gov.gwa.servlet.GwaConstants.CONSUMERS_PATH2;
import static ca.bc.gov.gwa.servlet.GwaConstants.GROUP;
import static ca.bc.gov.gwa.servlet.GwaConstants.USERNAME;
import ca.bc.gov.gwa.servlet.authentication.oidc.LookupUtil;
import static ca.bc.gov.gwa.v1.ApiService.LOG;
import ca.bc.gov.gwa.v2.conf.GwaSettings;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import static org.ehcache.shadow.org.terracotta.offheapstore.storage.SplitStorageEngine.encoding;
import org.pac4j.core.profile.CommonProfile;

@Slf4j
public class GwaApiService {
    GwaSettings config;

    public GwaApiService(GwaSettings config) {
        this.config = config;
    }
    
    public void createNamespace (CommonProfile profile, String nsName) throws IOException {
        final Map<String, Object> request = Collections.singletonMap("name", nsName);

        JsonHttpClient httpClient = newRestClient();
                
        final String path = String.format("/v1/namespaces");

        Map<String, Object> response = httpClient.post(getToken(profile), path, request);
        log.info("Response = {}", response);
    }

    public void createServiceAccount (CommonProfile profile, String ns, List<String> scopes) throws IOException {

        Map<String, Object> req = new HashMap<>();
        req.put("key", String.format("ns-%s", ns));
        req.put("secret",UUID.randomUUID().toString());
        req.put("scope", scopes);
        
        JsonHttpClient httpClient = newRestClient();

        final String path = String.format("/v1/namespaces/%s/serviceaccounts", ns);
        
        Map<String, Object> response = httpClient.post(getToken(profile), path, req);
        log.info("Response = {}", response);
    }

    public void deleteServiceAccount (CommonProfile profile, String ns, String accountId) throws IOException {

        JsonHttpClient httpClient = newRestClient();

        final String path = String.format("/v1/namespaces/%s/serviceaccounts/%s", ns, accountId);
        
        Map<String, Object> response = httpClient.delete(getToken(profile), path);
        log.info("Response = {}", response);
    }
    
    public List<String> getServiceAccountList (CommonProfile profile, String ns) throws IOException {

        JsonHttpClient httpClient = newRestClient();

        final String path = String.format("/v1/namespaces/%s/serviceaccounts", ns);
        
        List<String> response = httpClient.list(getToken(profile), path);
        log.info("Response = {}", response);
        return response;
    }
    
    private JsonHttpClient newRestClient() {
        return new JsonHttpClient(config.getGwaApiUrl());
    }
    
    private BearerAccessToken getToken (CommonProfile profile) {
        return profile.getAttribute("access_token", BearerAccessToken.class);  
    }
}
