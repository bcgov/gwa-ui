/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.bc.gov.gwa.servlet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.util.Strings;

/**
 *
 */
public class TestAuth {
    @Setter
    @Getter
    @AllArgsConstructor
    static public class GroupModel {
        String id;
        String name;
        String parentId;
        GroupModel parent;
    }
    
    static public void main(String[] args) {
        List<GroupModel> groupSet = new ArrayList<>();
        
        GroupModel teams = new GroupModel("000-1", "teams", null, null);
        
        groupSet.add(new GroupModel("000-2", "apiserv", teams.getId(), teams));
        groupSet.add(new GroupModel("000-3", "team-apiserv", null, null));
        
        for (Iterator<GroupModel> it = groupSet.iterator(); it.hasNext(); ) {
            GroupModel g = it.next();

            List<String> path = new ArrayList<>();
            GroupModel aNode = g;
            while (aNode != null) {
                path.add(0, aNode.getName());
                if (aNode.getParentId() != null) {
                    aNode = aNode.getParent();
                } else {
                    aNode = null;
                }
            }
            
            
            String fullPath = path.stream().collect(Collectors.joining("/"));
            
            String fullGroupName = path.size() == 1 ? path.get(0) : String.format("/%s", fullPath);
            System.out.println("FullGroupName = " + fullGroupName);
        }
        
        
    }
}
