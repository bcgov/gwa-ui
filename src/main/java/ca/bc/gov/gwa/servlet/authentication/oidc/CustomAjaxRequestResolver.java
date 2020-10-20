/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.bc.gov.gwa.servlet.authentication.oidc;

import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.http.ForbiddenAction;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.http.ajax.AjaxRequestResolver;
import org.pac4j.core.http.ajax.DefaultAjaxRequestResolver;
import org.pac4j.core.redirect.RedirectionActionBuilder;

/**
 *
 */
public class CustomAjaxRequestResolver extends DefaultAjaxRequestResolver {

    @Override
    public boolean isAjax(WebContext wc) {
        if (wc.getPath().contains("/rest")) {
            return true;
        }
        return false;
    }
    
    @Override
    public HttpAction buildAjaxResponse(final WebContext context, final RedirectionActionBuilder redirectionActionBuilder) {
        String url = null;
        throw ForbiddenAction.INSTANCE;
    }    
}
