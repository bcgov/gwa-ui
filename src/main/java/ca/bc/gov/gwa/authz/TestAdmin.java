/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.bc.gov.gwa.authz;

import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RealmsResource;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.Configuration;
import org.keycloak.authorization.client.resource.ProtectedResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.authorization.ResourceRepresentation;
import org.keycloak.representations.idm.authorization.ScopeRepresentation;
import sun.security.krb5.Realm;

/**
 *
 * https://www.keycloak.org/docs/latest/authorization_services/
 */
public class TestAdmin {
    static public void main(String[] args) {
        AuthzClient authzClient = AuthzClient.create();
        
        Configuration conf = authzClient.getConfiguration();
        
        System.out.println(String.format("%s,%s,%s,%s", conf.getAuthServerUrl(), conf.getRealm(), conf.getResource(), (String) conf.getCredentials().get("secret")));
        Keycloak cl = Keycloak.getInstance(conf.getAuthServerUrl(), conf.getRealm(), "realm-admin-principal", "realm-admin-principal", "admin-cli");

        RealmResource realm = cl.realm("gwa");

        for ( GroupRepresentation grp : realm.groups().groups()) {
            System.out.println(grp.getName());
        }
        GroupRepresentation team = realm.groups().groups().stream().filter(g -> g.getName().equals("team")).findFirst().get();
        
        System.out.println(realm.groups());
        String[] groups = new String[] {
            "abc",
            "def"
        };
        GroupRepresentation gr = new GroupRepresentation();
        gr.setName("abce");
        Map<String, List<String>> attrs = gr.getAttributes();
        

//        team.getSubGroups().add(gr);
        
        Response resp = realm.groups().group(team.getId()).subGroup(gr);
        System.out.println(resp.getStatusInfo());
        


//        RealmsResource realms = cl.realms();
//        for ( RealmRepresentation realm : realms.findAll()) {
//            System.out.println("R = " + realm);
//            
//        }
        
    }
}
