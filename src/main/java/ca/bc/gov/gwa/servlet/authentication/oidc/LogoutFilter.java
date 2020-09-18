/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.bc.gov.gwa.servlet.authentication.oidc;

import org.pac4j.core.config.Config;
import org.pac4j.core.http.adapter.JEEHttpActionAdapter;
import org.pac4j.jee.filter.AbstractConfigFilter;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpSession;
import org.pac4j.core.client.Client;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.util.Pac4jConstants;

@WebFilter(urlPatterns = {
  "/logout", "/int/logout"
})
public class LogoutFilter extends AbstractConfigFilter {

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
    }

    @Override
    protected void internalFilter(final HttpServletRequest request, final HttpServletResponse response,
            final FilterChain chain) throws IOException, ServletException {

//       final Config config = getSharedConfig();
//
//        final SessionStore<JEEContext> bestSessionStore = FindBest.sessionStore(null, config, JEESessionStore.INSTANCE);
//        final HttpActionAdapter<Object, JEEContext> bestAdapter = FindBest.httpActionAdapter(null, config, JEEHttpActionAdapter.INSTANCE);
//        final LogoutLogic<Object, JEEContext> bestLogic = FindBest.logoutLogic(null, config, DefaultLogoutLogic.INSTANCE);
//
//        final JEEContext context = new JEEContext(request, response, bestSessionStore);
//        bestLogic.perform(context, config, bestAdapter, this.defaultUrl, "/int/logout", this.localLogout, this.destroySession, this.centralLogout);

        String clientId = request.getParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER);
        if (clientId == null) {
            clientId = "KeycloakOidcClient";
        }

        
        String targetUrl = request.getRequestURL().toString().replace("/logout", "");
        logger.debug(targetUrl);
        
        final JEEContext context = new JEEContext(request, response);
        
        final Client client = Config.INSTANCE.getClients().findClient(clientId).orElseThrow(() -> new TechnicalException("No client found"));
        HttpAction action;
        try {
            Optional<HttpAction> optAction = client.getLogoutAction(context, LookupUtil.lookupUserProfile(request, response), targetUrl);
                //action = (HttpAction) client.getRedirectionAction(context).get();
                action = optAction.get();
//            if (optAction.isPresent()) {
//                System.out.println("Action " + optAction.get());
//                action = optAction.get();
//            } else {
//                action = (HttpAction) client.getRedirectionAction(context).get();
//            }
        } catch (final HttpAction e) {
            e.printStackTrace();
            action = e;
        }

        final HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        JEEHttpActionAdapter.INSTANCE.adapt(action, context);

    }
}
