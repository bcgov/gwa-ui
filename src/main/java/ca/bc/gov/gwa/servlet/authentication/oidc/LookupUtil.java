/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.bc.gov.gwa.servlet.authentication.oidc;

import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.pac4j.core.client.Client;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.engine.DefaultLogoutLogic;
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.exception.http.ForbiddenAction;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.exception.http.UnauthorizedAction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.core.util.Pac4jConstants;

@Slf4j
public class LookupUtil {
    static public final String NAMESPACE_CLAIM = "namespace";
            
    static public CommonProfile lookupUserProfile(HttpServletRequest request, HttpServletResponse response) {
        final WebContext webContext = new JEEContext(request, response);

        ProfileManager<CommonProfile> profileManager = new ProfileManager(webContext);
        Optional<CommonProfile> profile = profileManager.get(true);

        if (profile.isPresent()) {
            System.out.println("BEARER = " + profile.get().getAttribute("access_token", BearerAccessToken.class));
            System.out.println("IsAdmin? " + isNamespaceAdmin(profile.get()));
            System.out.println("IsExpired? " + profile.get().isExpired());
            Date exp = profile.get().getAttribute("exp", Date.class);
            System.out.println("Exp = "+exp);
            long dt = exp.getTime() - new Date().getTime();
            System.out.println("Exp = "+dt);
            if (dt < 298000) {
                System.out.println("EXPIRED!");
                String clientId = request.getParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER);
                if (clientId == null) {
                    clientId = "KeycloakOidcClient";
                }
                final JEEContext context = new JEEContext(request, response);
                final Client client = Config.INSTANCE.getClients().findClient(clientId).orElseThrow(() -> new TechnicalException("No client found"));
                
                try {
                    profile = client.renewUserProfile(profile.get(), webContext);
                    if (profile.isPresent()) {
                        System.out.println("Refresh worked! Saving in session the updated token");
                        profileManager.save(true, profile.get(), false);
                    } else {
                        request.getSession().invalidate();
                    }
                } catch (TechnicalException te) {
                    log.error("Error renewing token", te);
                    try {
                        request.getSession().invalidate();
                        profileManager.logout();
                        response.sendError(403, "{\"error\":\"\"}");
                    } catch (IOException ex) {
                        Logger.getLogger(LookupUtil.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    throw new TokenInvalidException("");
                }
                //throw ForbiddenAction.INSTANCE;
                //return null;
                //return null;
//
//                throw UnauthorizedAction.INSTANCE;
            }
            //throw new RuntimeException("Invalid Token");
          return profile.get();
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
}
