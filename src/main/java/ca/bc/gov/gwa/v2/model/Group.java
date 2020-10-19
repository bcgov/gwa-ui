/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.bc.gov.gwa.v2.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Group implements Comparable<Group> {

    String group;
    List<Plugin> plugins; // Filtered acl plugins - Grouped Service/Route Resources
    List<ACL> membership; // Consumer Group Membership
    
    public Group (ACL firstMember) {
        group = firstMember.getGroup();
        membership = new ArrayList<>();
        plugins = new ArrayList<>();
        membership.add(firstMember);
    }

    public Group (String groupName, Plugin firstPlugin) {
        group = groupName;
        membership = new ArrayList<>();
        plugins = new ArrayList<>();
        plugins.add(firstPlugin);
    }
    
    @Override
    public int compareTo(Group o) {
        return group.compareToIgnoreCase(o.getGroup());
    }
    
}
