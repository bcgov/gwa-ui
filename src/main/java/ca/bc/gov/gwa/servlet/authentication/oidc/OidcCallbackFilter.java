/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.bc.gov.gwa.servlet.authentication.oidc;

import ca.bc.gov.gwa.servlet.authentication.GitHubPrincipal;
import ca.bc.gov.gwa.util.Json;
import ca.bc.gov.gwa.v1.ApiService;
import java.io.IOException;
import java.util.Set;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.jee.filter.CallbackFilter;

@WebFilter(urlPatterns = {
    "/callback"
},
        initParams = {
            @WebInitParam(name = "defaultUrl", value = "/"),
            @WebInitParam(name = "renewSession", value = "true"),
            @WebInitParam(name = "multiProfile", value = "true")
        }
)
public class OidcCallbackFilter extends CallbackFilter {
    protected ApiService apiService;

    static public final String OIDC_PROFILE = "oidc.profile";

    protected void internalFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        super.internalFilter(request, response, chain);

        UserProfile profile = LookupUtil.lookupUserProfile(request, response);

        if (profile != null) {
            // Auto-register the Consumer on Kong
            System.out.println("Auto Reg " + profile.getId() + " : " + profile.getUsername());
            System.out.println("IDIR GUID = "+profile.getAttribute("idir_userid"));
            Json.toString(profile);
            
            String userId = profile.getId();
            String username = profile.getUsername();
            Set<String> groups = this.apiService.userGroups(userId, username);
            if (!groups.contains(GitHubPrincipal.DEVELOPER_ROLE)) {
                this.apiService.addConsumerToGroup(username, GitHubPrincipal.DEVELOPER_ROLE);
            }
        }
    }

    @Override
    public void destroy() {
        this.apiService = null;
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        final ServletContext servletContext = filterConfig.getServletContext();
        this.apiService = ApiService.get(servletContext);
    }
}
