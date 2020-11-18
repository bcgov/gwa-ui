/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.bc.gov.gwa.servlet.authentication.oidc;

import static ca.bc.gov.gwa.servlet.authentication.oidc.LookupUtil.isNamespaceAdmin;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.pac4j.core.authorization.authorizer.Authorizer;
import org.pac4j.core.authorization.checker.AuthorizationChecker;
import org.pac4j.core.authorization.checker.DefaultAuthorizationChecker;
import org.pac4j.core.client.Client;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;

/**
 *
 */
public class CustomDefaultAuthorizationChecker extends DefaultAuthorizationChecker {

    @Override
    public boolean isAuthorized(WebContext wc, List<UserProfile> profiles, String authorizersValue, Map<String, Authorizer> authorizersMap) {
//        ProfileManager<CommonProfile> profileManager = new ProfileManager(wc);
//        Optional<CommonProfile> profile = profileManager.get(true);
//
//        if (profile.isPresent()) {
//            System.out.println("Authz IsAdmin? " + isNamespaceAdmin(profile.get()));
//            System.out.println("Authz IsExpired? " + profile.get().isExpired());
//            Date exp = profile.get().getAttribute("exp", Date.class);
//            System.out.println("Authz Exp = "+exp);
//            long dt = exp.getTime() - new Date().getTime();
//            System.out.println("Authz Exp = "+dt);
//            if (dt < 298000) {
//                System.out.println("Authz EXPIRED!");
//                return true;
//            }
//        }
        return  true;
    }
    
}
