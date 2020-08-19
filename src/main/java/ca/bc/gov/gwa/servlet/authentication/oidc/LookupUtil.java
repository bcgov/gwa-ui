/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.bc.gov.gwa.servlet.authentication.oidc;

import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;

/**
 *
 * @author aidancope
 */
public class LookupUtil {
    static public UserProfile lookupUserProfile(HttpServletRequest request, HttpServletResponse response) {
        final WebContext webContext = new JEEContext(request, response);

        ProfileManager<CommonProfile> profileManager = new ProfileManager(webContext);
        Optional<CommonProfile> profile = profileManager.get(true);

        if (profile.isPresent()) {
            return profile.get();
        } else {
            return null;
        }
    }
}
