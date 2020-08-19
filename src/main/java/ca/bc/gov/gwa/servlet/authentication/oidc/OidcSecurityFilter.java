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
import org.pac4j.jee.filter.SecurityFilter;

@WebFilter(urlPatterns = {
        "/me",
        "/git/*", "/logout", "/rest/*", "/ui/*",
        "/int/ui/*", "/int/rest/*", "/int/logout", "/int/login/*"
    },
    initParams = {
        @WebInitParam(name = "configFactory", value = "ca.bc.gov.gwa.servlet.authentication.oidc.OidcConfigFactory"),
        @WebInitParam(name = "clients", value = "KeycloakOidcClient")
    }
)
public class OidcSecurityFilter extends SecurityFilter {
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        super.doFilter(request, response, chain);
    }    
}
