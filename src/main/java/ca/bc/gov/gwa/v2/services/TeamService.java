/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.bc.gov.gwa.v2.services;

import ca.bc.gov.gwa.v2.conf.GwaSettings;

/**
 * This class deals with syncing the users team membership
 * in Keycloak if there are is membership that is pending
 * 
 * Pending membership is captured as an attribute in the Group
 * in Keycloak.
 * 
 * Examples on when these methods might be called?
 * 
 * - updateTeamMembershipForUser() can be called on Login
 * - updateTeamMembership() called when the team configuration is updated
 * 
 */
public class TeamService {
    GwaSettings config;

    public TeamService (GwaSettings config) {
        this.config = config;
    }

    /**
     * Get all the Keycloak 'team' groups, and check to see if
     * the 'pending' attribute has the user identified.
     * If so, add the user to the group, and take the user off the
     * group 'pending' attribute.
     * 
     * @param user 
     */
    public void updateTeamMembershipForUser (String user) {
        
    }

    /**
     * Create a group in keycloak if the 'team' group doesn't already exist.
     * Go through the members and either add them to the Keycloak group,
     * or add them to a 'pending' attribute.
     * 
     * Consider the members[] as an incremental only.  So this will not
     * delete members that are not in the list.
     * 
     * @param team
     * @param members 
     */
    public void updateTeamMembership (String team, String[] members) {
        
    }
}
