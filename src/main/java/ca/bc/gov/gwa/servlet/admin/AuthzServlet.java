package ca.bc.gov.gwa.servlet.admin;

import ca.bc.gov.gwa.servlet.BaseServlet;
import ca.bc.gov.gwa.v2.controllers.GwaController;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.keycloak.AuthorizationContext;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.idm.authorization.Permission;

@WebServlet(urlPatterns = "/int/authz/decision", loadOnStartup = 1)
public class AuthzServlet extends BaseServlet {

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        GwaController gwa = getGwaController(request.getServletContext());
        //gwa.getPermissionService().decision(request.getParameter("user"), request.getParameter("services"));

                a(request);

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
