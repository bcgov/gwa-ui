package ca.bc.gov.gwa.servlet.developerkey;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pac4j.core.context.JEEContext;

import ca.bc.gov.gwa.v1.ApiService;
import ca.bc.gov.gwa.servlet.BaseServlet;
import ca.bc.gov.gwa.servlet.authentication.oidc.OidcCallbackFilter;
import java.util.Optional;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.Pac4JPrincipal;
import org.pac4j.core.profile.ProfileManager;

@WebServlet(urlPatterns = "/me", loadOnStartup = 1)
public class ProtectServlet extends BaseServlet {
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {
      response.setContentType(ApiService.APPLICATION_JSON);
      System.out.println(request.getUserPrincipal());
      Pac4JPrincipal principal = (Pac4JPrincipal) request.getUserPrincipal();
      
      //CommonProfile profile = (CommonProfile) request.getAttribute(OidcCallbackFilter.OIDC_PROFILE);
      
 
      //System.out.println("EMAIL = " + profile.getEmail());
      try (
              
        PrintWriter writer = response.getWriter()) {
        writer.print("{}");
      }
  }
}
