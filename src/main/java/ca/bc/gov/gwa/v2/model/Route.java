/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.bc.gov.gwa.v2.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Route {

    String id;
    String name;
    String serviceId;
    List<Plugin> plugins;
    List<String> hosts;
    List<String> paths;
    Map<String, Object> data;
    Tags tags;
        
    public Route (Object payload) {
        this.data = (Map<String, Object>) payload;
        
        id = (String) data.get("id");
        name = (String) data.get("name");
        
        if (this.data.get("service") != null) {
            serviceId = (String) ((Map<String, Object>)this.data.get("service")).get("id");
        }
        hosts = (List<String>) data.get("hosts");
        paths = (List<String>) data.get("paths");
        tags = new Tags(data);
        
        plugins = new ArrayList<>();
        
//        List<String> hostPaths = new ArrayList<>();
//
//        if (paths == null || paths.size() == 0) {
//            hostPaths.addAll(hosts);
//        } else {
//            if (hosts != null) {
//                hosts.stream().forEach(h -> {
//                   paths.stream().forEach(p -> {
//                       hostPaths.add(String.format("%s%s", h, p));
//                   });
//                });
//            }
//        }
//        hosts = hostPaths;
//        
//        if (hosts != null) {
//            hosts.clear();
//        }
//        if (paths != null) {
//            paths.clear();
//        }
        
    }

    public boolean hasPlugin (String name) {
        return plugins.stream().filter(p -> p.getName().equals(name)).count() != 0;
    }

    public Optional<Plugin> getPlugin (String name) {
        return plugins.stream().filter(p -> p.getName().equals(name)).findFirst();
    }
    
    public void addPlugin (Plugin plugin) {
        plugins.add(plugin);
    }    
}
