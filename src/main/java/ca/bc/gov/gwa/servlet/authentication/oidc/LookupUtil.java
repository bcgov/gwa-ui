/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.bc.gov.gwa.servlet.authentication.oidc;

import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import java.util.Date;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.pac4j.core.client.Client;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.util.Pac4jConstants;

@Slf4j
public class LookupUtil {
    static public final String NAMESPACE_CLAIM = "namespace";
            
    static public CommonProfile lookupUserProfile(HttpServletRequest request, HttpServletResponse response) {
        final WebContext webContext = new JEEContext(request, response);

        ProfileManager<CommonProfile> profileManager = new ProfileManager(webContext);
        Optional<CommonProfile> profile = profileManager.get(true);

        if (profile.isPresent()) {
            log.debug("BEARER = " + profile.get().getAttribute("access_token", BearerAccessToken.class));
            log.debug("IsAdmin? " + isNamespaceAdmin(profile.get()));
            log.debug("IsExpired? " + profile.get().isExpired());
            Date exp = profile.get().getAttribute("exp", Date.class);
            log.debug("Exp = "+exp);
            if (fastExpiry (exp, getClient(request, response))) {
                return renewUserProfile (request, response, profile);
            } else {
                return profile.get();
            }
        } else {
            return null;
        }
    }

    static public boolean isNamespaceAdmin (CommonProfile profile) {
        if (profile == null) {
            return false;
        }
        String groupMatch = String.format("/ns-admins/%s", getNamespaceClaim(profile));
        JSONArray groups = profile.getAttribute("groups", JSONArray.class);
        return groups.contains(groupMatch);
    }
    
    static public String getNamespaceClaim (CommonProfile profile) {
        String ns = (String) profile.getAttribute(NAMESPACE_CLAIM);
        log.debug("Lookup namespace With {}", ns);
        return ns;
    }
    
    static private Client getClient (HttpServletRequest request, HttpServletResponse response) {
        String clientId = request.getParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER);
        if (clientId == null) {
            clientId = "KeycloakOidcClient";
        }
        final JEEContext context = new JEEContext(request, response);
        final Client client = Config.INSTANCE.getClients().findClient(clientId).orElseThrow(() -> new TechnicalException("No client found"));
        return client;
    }
    
    static private CommonProfile renewUserProfile (HttpServletRequest request, HttpServletResponse response, final Optional<CommonProfile> profile) {
        final WebContext webContext = new JEEContext(request, response);
        final Client client = getClient(request, response);
        
        ProfileManager<CommonProfile> profileManager = new ProfileManager(webContext);

        try {
            Optional<CommonProfile> newProfile = client.renewUserProfile(profile.get(), webContext);
            if (profile.isPresent()) {
                log.debug("Refresh worked! Saving in session the updated token");
                profileManager.save(true, profile.get(), false);
                return profile.get();
            } else {
                log.error("Refresh failed, so invalidating session and returning a 403 error");
            }
        } catch (TechnicalException te) {
            log.error("Error renewing token", te);
        }
        request.getSession().invalidate();
        profileManager.logout();
        return null;
    }
    
    
    static private boolean fastExpiry(Date exp, Client client) throws TokenInvalidException {
        long dt = exp.getTime() - new Date().getTime();
        log.debug("MS to expiry? " + dt);
        if (dt < 298000) {
            log.debug("Flagging as expired to expedite the logic!");
            return true;
        } else {
            return false;
        }
    }
}
