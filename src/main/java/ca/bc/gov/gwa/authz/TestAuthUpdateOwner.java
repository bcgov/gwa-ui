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
import org.keycloak.representations.idm.authorization.ResourceOwnerRepresentation;
import org.keycloak.representations.idm.authorization.ResourceRepresentation;
import org.keycloak.representations.idm.authorization.ScopeRepresentation;
import org.pac4j.core.profile.CommonProfile;

/**
 *
 * https://www.keycloak.org/docs/latest/authorization_services/
 */
public class TestAuthUpdateOwner {
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
        
        ProtectionResource prot = authzClient.protection();
        
        Configuration conf = authzClient.getConfiguration();
        
        ResourceRepresentation rep = prot.resource().findById("6b3ffa85-12b9-47fb-b1a2-90a481ea074f");
        System.out.println("REp = "+rep);
        System.out.println("rep = "+rep.getOwner().getId());
        
        rep.setOwner("d4d3574b-7c1c-4eaa-814a-ca91088ae698"); // acope@idir
        rep.setOwner(new ResourceOwnerRepresentation("d4d3574b-7c1c-4eaa-814a-ca91088ae698"));

        prot.resource().update(rep);
    }
}
