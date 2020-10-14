/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.bc.gov.gwa.servlet.authentication.oidc;

import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;

@Slf4j
public class LookupUtil {
    static public final String NAMESPACE_CLAIM = "namespace";
            
    static public CommonProfile lookupUserProfile(HttpServletRequest request, HttpServletResponse response) {
        final WebContext webContext = new JEEContext(request, response);

        ProfileManager<CommonProfile> profileManager = new ProfileManager(webContext);
        Optional<CommonProfile> profile = profileManager.get(true);

        if (profile.isPresent()) {
            System.out.println("BEARER = " + profile.get().getAttribute("access_token", BearerAccessToken.class));
            return profile.get();
        } else {
            return null;
        }
    }
    
    static public String getNamespaceClaim (CommonProfile profile) {
        String ns = (String) profile.getAttribute(NAMESPACE_CLAIM);
        log.debug("Lookup With {}", ns);
        String[] parts = ns.split("/");
        log.debug("parts = "+parts.length);
        
        String namespace = parts[2];
        log.debug("Lookup Namespace {} -- {}", ns, parts[2]);
        return namespace;
    }
}
