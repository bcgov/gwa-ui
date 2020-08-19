/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.bc.gov.gwa.servlet.authentication.oidc;

import org.pac4j.core.client.Client;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.http.adapter.JEEHttpActionAdapter;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.jee.filter.AbstractConfigFilter;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import javax.servlet.annotation.WebFilter;

@WebFilter(urlPatterns = {
  "/logout", "/int/logout"
})
public class ForceLoginFilter extends AbstractConfigFilter {

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
    }

    @Override
    protected void internalFilter(final HttpServletRequest request, final HttpServletResponse response,
            final FilterChain chain) throws IOException, ServletException {

        String clientId = request.getParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER);
        if (clientId == null) {
            clientId = "KeycloakOidcClient";
        }
        
        final JEEContext context = new JEEContext(request, response);
        final Client client = Config.INSTANCE.getClients().findClient(clientId).orElseThrow(() -> new TechnicalException("No client found"));
        HttpAction action;
        try {
            action = (HttpAction) client.getRedirectionAction(context).get();
        } catch (final HttpAction e) {
            action = e;
        }
        JEEHttpActionAdapter.INSTANCE.adapt(action, context);
    }
}
