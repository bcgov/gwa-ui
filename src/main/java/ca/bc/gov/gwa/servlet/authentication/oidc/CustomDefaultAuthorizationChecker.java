/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.bc.gov.gwa.servlet.authentication.oidc;

import java.util.List;
import java.util.Map;
import org.pac4j.core.authorization.authorizer.Authorizer;
import org.pac4j.core.authorization.checker.AuthorizationChecker;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.UserProfile;

/**
 *
 * @author aidancope
 */
public class CustomDefaultAuthorizationChecker implements AuthorizationChecker {

    @Override
    public boolean isAuthorized(WebContext wc, List<UserProfile> list, String string, Map<String, Authorizer> map) {
        return  true;
    }
    
}
