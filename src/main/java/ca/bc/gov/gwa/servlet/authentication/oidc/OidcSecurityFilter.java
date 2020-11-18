/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.bc.gov.gwa.servlet.authentication.oidc;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.pac4j.jee.filter.SecurityFilter;

@Slf4j
@WebFilter(
    urlPatterns = {
      "/git/*", "/logout", "/rest/*", "/ui/*", "/login/*",
      "/int/ui/*", "/int/rest/*", "/int/logout", "/int/login/*", "/int/authz/request"
    },
    initParams = {
        @WebInitParam(name = "configFactory", value = "ca.bc.gov.gwa.servlet.authentication.oidc.OidcConfigFactory"),
        @WebInitParam(name = "clients", value = "KeycloakOidcClient")
    }
)
public class OidcSecurityFilter extends SecurityFilter {

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final String servletPath = ((HttpServletRequest) request).getServletPath();

        log.info("Security Filter " + servletPath);
        final HttpSession session = ((HttpServletRequest)request).getSession(false);
//        if (session == null) {
//            HttpServletResponse resp = ((HttpServletResponse)response);
//            resp.sendError(401);
//        }
        //if ("/logout".equals(servletPath)) {
        //} else {
        
            super.doFilter(request, response, chain);
        //}
    }

//    private void doFilterLogout(final HttpServletRequest httpRequest,
//            final HttpServletResponse httpResponse) throws IOException {
//        final HttpSession session = httpRequest.getSession(false);
//        if (session != null) {
//            session.invalidate();
//        }
//
//        String url = "";
//        httpResponse.sendRedirect(url);
//    }
}
