/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.bc.gov.gwa.servlet.authentication.oidc;

import org.pac4j.core.exception.TechnicalException;

public class TokenInvalidException extends TechnicalException {
    
    public TokenInvalidException(String message) {
        super(message);
    }
}
