package ca.bc.gov.gwa.servlet.admin;

import static ca.bc.gov.gwa.authz.TestAuthz.toSet;
import ca.bc.gov.gwa.servlet.BaseServlet;
import ca.bc.gov.gwa.servlet.authentication.oidc.LookupUtil;
import ca.bc.gov.gwa.v2.controllers.GwaController;
import static com.google.common.base.Predicates.equalTo;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.pac4j.core.profile.CommonProfile;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.keycloak.AuthorizationContext;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.token.TokenManager;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.Configuration;
import org.keycloak.authorization.client.resource.ProtectionResource;
import org.keycloak.representations.idm.authorization.Permission;
import org.keycloak.representations.idm.authorization.PermissionRequest;
import org.keycloak.representations.idm.authorization.PermissionResponse;

@WebServlet(urlPatterns = "/int/authz/request", loadOnStartup = 1)
public class AuthzRequestServlet extends BaseServlet {

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        GwaController gwa = getGwaController(request.getServletContext());

//        Configuration con = new Configuration();
//        con.setAuthServerUrl("https://auth-qwzrwc-dev.pathfinder.gov.bc.ca/auth");
       
        AuthzClient authzClient = AuthzClient.create();
//        authzClient.getConfiguration().setResource("app-2");
        
        ProtectionResource prot = authzClient.protection("realm-admin-principal", "realm-admin-principal");

        PermissionRequest pr = new PermissionRequest();
        pr.setResourceId("a94d3eac-d8d9-4221-9d26-e916a1e62071"); // api:adecat-dlv-webade-oauth-webade
        pr.setResourceId("2d885ebf-6bc8-4633-9b10-eec17fe9810c"); // api:ocicat-dlv-apps
        //pr.setResourceId("8c3bb793-2618-407a-b519-946c218d16f8"); // api:test in app-2
        pr.setResourceId("027738f1-8d00-430f-bc37-a2f7824043f9");
        //pr.setScopes(toSet("api:view-metrics"));
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
        CommonProfile up = LookupUtil.lookupUserProfile(request, response);
        
        String accessToken = up.getAttribute("access_token", BearerAccessToken.class).getValue();

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(String.format("%s/realms/%s/protocol/openid-connect/token", conf.getAuthServerUrl(), conf.getRealm()));

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("grant_type", "urn:ietf:params:oauth:grant-type:uma-ticket"));
        params.add(new BasicNameValuePair("ticket", resp.getTicket()));
        params.add(new BasicNameValuePair("submit_request", "true"));
        httpPost.setEntity(new UrlEncodedFormEntity(params));
        httpPost.addHeader("Authorization",  "Bearer " + accessToken);
        CloseableHttpResponse answer = client.execute(httpPost);
        client.close();
        System.out.println("Answer = " + answer.getStatusLine());
        
        /*
        401 - Unauthorized : Could be because of an expired token?
        403 - Forbidden : Means that a ticket was created to request access
        200 - User has permission to the resource already
        */
        
        /* "Request Access"
            curl -X POST \
            http://${host}:${port}/auth/realms/${realm}/protocol/openid-connect/token \
            -H "Authorization: Bearer ${access_token}" \
            --data "grant_type=urn:ietf:params:oauth:grant-type:uma-ticket" \
            --data "ticket=${permission_ticket} \
            --data "submit_request=true"
        */

        
        response.getWriter().println("Decision");
        
    }

    public void a(HttpServletRequest request) {
        KeycloakSecurityContext keycloakSecurityContext = (KeycloakSecurityContext) request.getAttribute(KeycloakSecurityContext.class.getName());
        AuthorizationContext authzContext = keycloakSecurityContext.getAuthorizationContext();     
        List<Permission> perms = authzContext.getPermissions();
        System.out.println(perms);
    }

    public static GwaController getGwaController(final ServletContext servletContext) {
        return (GwaController) servletContext.getAttribute(GwaController.GWA_CONTROLLER);
    }

}
