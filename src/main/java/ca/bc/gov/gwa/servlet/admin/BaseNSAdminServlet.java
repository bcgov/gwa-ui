package ca.bc.gov.gwa.servlet.admin;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.bc.gov.gwa.v1.ApiService;
import ca.bc.gov.gwa.servlet.BaseServlet;
import ca.bc.gov.gwa.servlet.authentication.oidc.LookupUtil;
import org.pac4j.core.profile.CommonProfile;

public abstract class BaseNSAdminServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void service(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        if (hasNamespaceAdminRole(request, response)) {
            super.service(request, response);
        }
    }

    private boolean hasNamespaceAdminRole(final HttpServletRequest request, final HttpServletResponse response) {
        CommonProfile profile = LookupUtil.lookupUserProfile(request, response);

        if (LookupUtil.isNamespaceAdmin(profile)) {
            return true;
        }
        sendError(response, HttpServletResponse.SC_FORBIDDEN);
        return false;
    }

}
