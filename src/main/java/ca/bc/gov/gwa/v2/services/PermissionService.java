/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.bc.gov.gwa.v2.services;

import ca.bc.gov.gwa.v2.conf.GwaSettings;
import ca.bc.gov.gwa.v2.model.Service;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.ws.rs.core.Response;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.resource.ProtectedResource;
import org.keycloak.representations.idm.authorization.ResourceRepresentation;
import org.keycloak.representations.idm.authorization.ScopeRepresentation;


public class PermissionService {
    GwaSettings config;

    public PermissionService(GwaSettings config) {
        this.config = config;
    }
    
    public void syncPermissions() throws IOException {
        KongAdminService admin = new KongAdminService(config);
        
        Collection<Service> services = admin.buildServiceModel();
        
        String owner = "e9b04e48-dc3f-407e-af36-c7d1b197c109"; // acope@idir
        //owner = "e29bb6cb-0fd2-4c6f-a838-1e3a92615ca9"; // ac

        AuthzClient authzClient = AuthzClient.create();

        List<String> exists = new ArrayList<>();
        List<String> serviceNames = new ArrayList<>();
        for ( Service svc : services ) {
            serviceNames.add(String.format("api:%s", svc.getName()));
        }

        ProtectedResource resourceClient = authzClient.protection().resource();
//        String[] allResources = resourceClient.findAll();
//        for ( String r : allResources) {
//            System.out.println(r);
//        }
//        throw new RuntimeException("Fuck");
        
        for ( String serviceName : serviceNames ) {
            ResourceRepresentation newResource = new ResourceRepresentation();
            newResource.setName(serviceName);
            

            try {
                ResourceRepresentation existingResource = resourceClient.findByName(newResource.getName(), owner);

                if (existingResource != null) {
                    exists.add(serviceName);
                }
            } catch (RuntimeException ex) {
                ex.printStackTrace();
                throw ex;
                // Ignore error because prob because it doesn't exist
            }
        }

        for ( String serviceName : serviceNames ) {
            if (exists.contains(serviceName)) {
                System.out.println("Skipping.."+serviceName);
                continue;
            }
            ResourceRepresentation newResource = new ResourceRepresentation();
            newResource.setName(serviceName);
            newResource.setType("urn:hello-world-authz:resources:example");
            newResource.setOwnerManagedAccess(true);

            newResource.setOwner(owner); // User acope@idir

            String[] scopes = new String[] {
                "api:view-metrics",
                "api:view-config",
                "api:pr-config",
                "api:manage-team-access",
                "api:manage-consumer-access"
            };
            for ( String scope : scopes ) {
                newResource.addScope(new ScopeRepresentation(scope));
            }

            //ProtectedResource resourceClient = authzClient.protection().resource();
            /*
            ResourceRepresentation existingResource = resourceClient.findByName(newResource.getName());

            if (existingResource != null) {
                resourceClient.delete(existingResource.getId());
            }
            */
            // create the resource on the server
            ResourceRepresentation response = resourceClient.create(newResource);
            String resourceId = response.getId();
        }
        
//        // query the resource using its newly generated id
//        ResourceRepresentation resource = resourceClient.findById(resourceId);

    }
}
