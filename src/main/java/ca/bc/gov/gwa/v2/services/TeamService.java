/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.bc.gov.gwa.v2.services;

import ca.bc.gov.gwa.servlet.TestAuth.GroupModel;
import ca.bc.gov.gwa.v1.ApiService;
import ca.bc.gov.gwa.v2.conf.GwaSettings;
import ca.bc.gov.gwa.v2.model.Team;
import ca.bc.gov.gwa.v2.model.Teams;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public static final Logger LOG = LoggerFactory.getLogger(TeamService.class);
    
    static final String REALM_GW = "gwa";
    static final String ROOT_GROUP_NAME = "team";
    static final String ATTR_FOR_PENDING = "pending";
            
    GwaSettings config;

    public TeamService (GwaSettings config) {
        this.config = config;
    }

    /**
     * Loop through the teams and make sure the group is created and
     * that the membership is updated
     * either in group membership or the group "pending" attribute.
     * @param teams 
     */
    public void sync (Teams teams) {
        long start = new Date().getTime();
        
        Keycloak cl = Keycloak.getInstance(config.getKeycloakUrl(), config.getKeycloakRealm(), config.getKeycloakUsername(), config.getKeycloakPassword(), "admin-cli");

        RealmResource realm = cl.realm(REALM_GW);

        Optional<GroupRepresentation> optRoot = realm.groups().groups().stream().filter(g -> g.getName().equals(ROOT_GROUP_NAME)).findFirst();
        if (!optRoot.isPresent()) {
            throw new IllegalArgumentException("Group not found " + ROOT_GROUP_NAME);
        }

        GroupRepresentation root = optRoot.get();
        
        List<GroupRepresentation> groups = root.getSubGroups();
        
        for (Team team : teams.getTeams()) {
            List<String> membersToRemove = new ArrayList<>();
            
            String groupId;
            
            // does team name exist already?
            Optional<String> optGroupId = groups.stream().filter(g -> g.getName().equals(team.getName())).map(g -> g.getId()).findFirst();
            if (optGroupId.isPresent() == false) {
                // Add the team to the teams group hierarchy
                GroupRepresentation gr = new GroupRepresentation();
                gr.setName(team.getName());
                Map<String, List<String>> attrs = gr.getAttributes();
                Response resp = realm.groups().group(root.getId()).subGroup(gr);

                LOG.info(resp.getStatusInfo().getReasonPhrase());
                if (resp.hasEntity()) {
                    GroupRepresentation addedGroup = (GroupRepresentation) resp.readEntity(GroupRepresentation.class);
                    LOG.info("Group added " + addedGroup.getName());
                    groupId = addedGroup.getId();
                } else {
                    LOG.warn("Group added but don't know the ID!");
                    groupId = null;
                }
                groups = root.getSubGroups();
            } else {
                groupId = optGroupId.get();
            }

            UsersResource users = realm.users();
            
            GroupResource group = realm.groups().group(groupId);
            
            group.members().stream().map(m -> m.getId()).forEach (mid -> membersToRemove.add(mid));
            
            // Check that team membership is synced
            // If the user does not exist, then add to the group's "pending" attribute
            for (String member : team.getMembers()) {
                List<UserRepresentation> user = realm.users().search(member);
                
                if (user.size() == 0) {
                    // Add to pending
                    addToPendingAttribute(group, member);
                } else if (group.members().stream().filter(m -> m.getUsername().equals(member)).count() == 0) {
                    String userId = user.get(0).getId();
                    UserResource userR = users.get(userId);
                    // Add to group if it isn't already there
                    GroupRepresentation groupR = group.toRepresentation();
                    LOG.info("Adding user " + member + " to group "+groupR.getName());
                    userR.joinGroup(groupR.getId());
                    
                    membersToRemove.remove(userId);
                } else {
                    LOG.info("No action.  User " + member + " already in group "+group.toRepresentation().getName());
                    membersToRemove.remove(user.get(0).getId());
                }
            }
            
            GroupRepresentation groupR = group.toRepresentation();
            for ( String userId : membersToRemove ) {
                UserResource user = realm.users().get(userId);
                
                LOG.info("Removing user " + user.toRepresentation().getUsername() + " from group "+groupR.getName());
                user.leaveGroup(groupR.getId());
            }
        }
        
        LOG.info("Elapsed time {}ms", (new Date().getTime() - start));
    }
    
    private void addToPendingAttribute (GroupResource group, String username) {
        GroupRepresentation groupR = group.toRepresentation();
        
        Map<String, List<String>> attrs = groupR.getAttributes();

        if (attrs.containsKey(ATTR_FOR_PENDING)) {
            List<String> pending = attrs.get(ATTR_FOR_PENDING);
            if (!pending.contains(username)) {
                LOG.info("Adding user " + username + " to pending group "+group.toRepresentation().getName());
                pending.add(username);
            }
        } else {
            LOG.info("Adding user " + username + " to pending group "+group.toRepresentation().getName());
            attrs.put(ATTR_FOR_PENDING, Arrays.asList(username));
        }
        group.update(groupR);
    }
}
