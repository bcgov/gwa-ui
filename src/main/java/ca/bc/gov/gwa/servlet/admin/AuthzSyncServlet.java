package ca.bc.gov.gwa.servlet.admin;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.bc.gov.gwa.servlet.BaseServlet;
import ca.bc.gov.gwa.v2.controllers.GwaController;
import javax.servlet.ServletContext;

@WebServlet(urlPatterns = "/int/authz/sync", loadOnStartup = 1)
public class AuthzSyncServlet extends BaseServlet {

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        GwaController gwa = getGwaController(request.getServletContext());
        gwa.getPermissionService().syncPermissions();

        response.getWriter().println("Synced");
    }

    public static GwaController getGwaController(final ServletContext servletContext) {
        return (GwaController) servletContext.getAttribute(GwaController.GWA_CONTROLLER);
    }

}
