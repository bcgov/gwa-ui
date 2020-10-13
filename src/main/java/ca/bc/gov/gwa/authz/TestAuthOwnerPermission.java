/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.bc.gov.gwa.authz;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.Configuration;
import org.keycloak.authorization.client.resource.PolicyResource;
import org.keycloak.authorization.client.resource.ProtectionResource;
import org.keycloak.representations.idm.authorization.DecisionStrategy;
import org.keycloak.representations.idm.authorization.Logic;
import org.keycloak.representations.idm.authorization.UmaPermissionRepresentation;

/**
 *
 * https://www.keycloak.org/docs/latest/authorization_services/
 */
public class TestAuthOwnerPermission {
    static public Set<String> toSet (String... list) {
        Set<String> res = new HashSet<>();
        for ( String s : list ) {
            res.add(s);
        }
        return res;
    }
    
    static public void main(String[] args) throws UnsupportedEncodingException, IOException {
        
        // https://www.keycloak.org/docs/latest/authorization_services/
        
        
        AuthzClient authzClient = AuthzClient.create();
        
        String accessToken = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJnQVUzYTNzUXgwcWNfR2JnX0RSLWpUU0xoQWtQMlA5NFJJcHBtUkw5RFJ3In0.eyJleHAiOjE2MDExMDg3NzUsImlhdCI6MTYwMTA3Mjc3NiwiYXV0aF90aW1lIjoxNjAxMDcyNzc1LCJqdGkiOiJmNDZkMjg3My0yZTE3LTQ5YTYtOGQ2ZC00ZmExZTQxY2ViYTAiLCJpc3MiOiJodHRwczovL2F1dGgtcXd6cndjLWRldi5wYXRoZmluZGVyLmdvdi5iYy5jYS9hdXRoL3JlYWxtcy9hcHMiLCJhdWQiOlsicmVhbG0tbWFuYWdlbWVudCIsImFwcy1yZXNvdXJjZXMiLCJhcGktYXBpc2Vydi1hcmdnLXVpIiwiYWJjMyIsImFjY291bnQiLCJhYmM0Il0sInN1YiI6ImQ0ZDM1NzRiLTdjMWMtNGVhYS04MTRhLWNhOTEwODhhZTY5OCIsInR5cCI6IkJlYXJlciIsImF6cCI6ImFwcyIsIm5vbmNlIjoienNZOXhIdTFJaUU3LWl4Qk9yOEwwcE15VHh5ZG85ZXlOLXU1dVFVU2xfRSIsInNlc3Npb25fc3RhdGUiOiI5MTIyYWNjNC1jODM2LTQyNGEtODVlNi1mNjg5NjY0Mzg4MDUiLCJhY3IiOiIxIiwicmVzb3VyY2VfYWNjZXNzIjp7InJlYWxtLW1hbmFnZW1lbnQiOnsicm9sZXMiOlsidmlldy11c2VycyIsInF1ZXJ5LWNsaWVudHMiLCJxdWVyeS1ncm91cHMiLCJxdWVyeS11c2VycyJdfSwiYXBzLXJlc291cmNlcyI6eyJyb2xlcyI6WyJ1bWFfcHJvdGVjdGlvbiJdfSwiYXBzIjp7InJvbGVzIjpbInVtYV9wcm90ZWN0aW9uIiwiYXBzOnZpZXctbWV0cmljcyIsImFwaS1hZG1pbiJdfSwiYXBpLWFwaXNlcnYtYXJnZy11aSI6eyJyb2xlcyI6WyJhZG1pbiIsIm1hbmFnZS1ndy1jb25maWciXX0sImFiYzMiOnsicm9sZXMiOlsiYWJjMzptZXRyaWNzIl19LCJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX0sImFiYzQiOnsicm9sZXMiOlsidW1hX3Byb3RlY3Rpb24iLCJ5ZXAtcm9sZXMiXX19LCJzY29wZSI6Im9wZW5pZCBwcm9maWxlIGVtYWlsIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJuYW1lIjoiQWlkYW4gQ29wZSIsInRlYW0iOiIvdGVhbS9hcGlzZXJ2IiwicHJlZmVycmVkX3VzZXJuYW1lIjoiYWNvcGVAaWRpciIsImdpdmVuX25hbWUiOiJBaWRhbiIsImZhbWlseV9uYW1lIjoiQ29wZSIsImVtYWlsIjoiYWlkYW4uY29wZUBnbWFpbC5jb20ifQ.TJB0YXFi6Ixbtk-o3VDcXZlzyQJPs0gB4SU5yAsCBAGUB29L60mpYn99YTKrneWNslOLZ-gerp10wyzAAYz3Odk86KLPHGL0I6TWNra3zS_OdX2qfAn6WzcTSjYMoSTNdFDfUcK4DvkgTmLCpGcMerxjDllN7sEeigRZ9LlbwBFw2BjeUHIoLu8VRxUAJEEf00GpBVpm9PrvZagh3MFXqlNA6iHHfV1y-E5PI0atJM-5ql2oJKISeQaWLf5BnoK0eLz_0iA1iazyEEz4LVZMYILWteKFLZnRGHVDUG6VLboe2cpOfoImM8zx9KwsLs3fUsEdr3SwEMk14VM9Jw-kXA";
        
        ProtectionResource prot = authzClient.protection(accessToken);

        PolicyResource pres = prot.policy("9a816d2b-a270-481c-b3a3-e910b7bb9854");
        //6b3ffa85-12b9-47fb-b1a2-90a481ea074f

        
        Configuration conf = authzClient.getConfiguration();
        /*
        ResourceRepresentation rep = prot.resource().findById("6b3ffa85-12b9-47fb-b1a2-90a481ea074f");
        System.out.println("REp = "+rep);
        System.out.println("rep = "+rep.getOwner().getId());
        
        rep.setOwner("d4d3574b-7c1c-4eaa-814a-ca91088ae698"); // acope@idir
        rep.setOwner(new ResourceOwnerRepresentation("d4d3574b-7c1c-4eaa-814a-ca91088ae698"));

        prot.resource().update(rep);
*/
        UmaPermissionRepresentation umapPermission = new UmaPermissionRepresentation();
        umapPermission.addUser("d4d3574b-7c1c-4eaa-814a-ca91088ae698");
        umapPermission.setName("yeah-yeah-name-6");
        umapPermission.setDecisionStrategy(DecisionStrategy.UNANIMOUS);
        ///umapPermission.setCondition(condition);
        umapPermission.setScopes(toSet());
        umapPermission.setLogic(Logic.POSITIVE);
        
        UmaPermissionRepresentation res = pres.create(umapPermission);
        System.out.println(res);
        
    }
}
