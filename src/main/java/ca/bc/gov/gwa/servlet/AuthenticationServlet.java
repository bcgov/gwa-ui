package ca.bc.gov.gwa.servlet;

import static ca.bc.gov.gwa.servlet.authentication.GitHubPrincipal.ADMIN_ROLE;
import static ca.bc.gov.gwa.servlet.authentication.GitHubPrincipal.DEVELOPER_ROLE;
import static ca.bc.gov.gwa.servlet.authentication.GitHubPrincipal.NS_ADMIN_ROLE;
import ca.bc.gov.gwa.servlet.authentication.oidc.LookupUtil;
import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pac4j.core.profile.Pac4JPrincipal;

import ca.bc.gov.gwa.util.Json;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@WebServlet(urlPatterns = "/rest/authentication", loadOnStartup = 1)
public class AuthenticationServlet extends BaseServlet {
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {
    final Principal principal = request.getUserPrincipal();
    final Map<String, Object> data;
    System.out.println("Principal " + principal.getClass().getName());
    if (principal instanceof BasePrincipal) {
      final BasePrincipal gwaPrincipal = (BasePrincipal)principal;
      data = gwaPrincipal.toMap();
    } else if (principal instanceof Pac4JPrincipal) {
      final Pac4JPrincipal gwaPrincipal = (Pac4JPrincipal)principal;
      data = new HashMap<String, Object>();
      data.put("id", gwaPrincipal.getName());
      data.put("name", gwaPrincipal.getName());
      Set<String> roles = new HashSet();
      roles.add(DEVELOPER_ROLE);
      
      boolean isNsAdmin = LookupUtil.isNamespaceAdmin(LookupUtil.lookupUserProfile(request, response));
      if (isNsAdmin) {
          roles.add(NS_ADMIN_ROLE);
      }
      
      data.put("roles", roles);
      
      Map<String, Object> config = new HashMap<>();
      config.put("grafanaBaseUrl", this.apiService.getGrafanaUrl());
      data.put("config", config);
      
    } else {
      data = Collections.emptyMap();
    }
    Json.writeJson(response, data);
  }
}
