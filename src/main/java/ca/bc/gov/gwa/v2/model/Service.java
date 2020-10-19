/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.bc.gov.gwa.v2.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Service implements Comparable<Service> {
    static final boolean TIME_IN_SECONDS = true;
            

    String id;
    String name;
    List<Route> routes;
    List<Plugin> plugins;
    List<String> hosts; // legacy
    List<String> paths; // legacy
    Tags tags;
    
    @JsonProperty("created_at")
    Long createdAt;

    @JsonProperty("updated_at")
    Long updatedAt;
    
    @JsonIgnore
    Map<String, Object> data;
        
    public Service (Object payload) {
        this.data = (Map<String, Object>) payload;
        
        id = (String) data.get("id");
        name = (String) data.get("name");
        tags = new Tags(data);
        createdAt = ( (BigDecimal) data.get("created_at")).longValue() * (TIME_IN_SECONDS ? 1000:1);
        updatedAt = ( (BigDecimal) data.get("updated_at")).longValue() * (TIME_IN_SECONDS ? 1000:1);
        
        routes = new ArrayList<>();
        plugins = new ArrayList<>();
        
        hosts = new ArrayList<>();
        paths = new ArrayList<>();
    }
    
    public void addRoute (Route route) {
        routes.add(route);
        
        if (route.getHosts() != null) {
            for ( String host : route.getHosts() ) {
                if (!hosts.contains(host)) {
                    hosts.add(host);
                }
            }
        }
        if (route.getPaths() != null) {
            for ( String path : route.getPaths() ) {
                if (!paths.contains(path)) {
                    paths.add(path);
                }
            }
        }
    }
    
    public void addPlugin (Plugin plugin) {
        plugins.add(plugin);
    }
    
    public boolean hasPlugin (String name) {
        return plugins.stream().filter(p -> p.getName().equals(name)).count() != 0;
    }

    @Override
    public int compareTo(Service o) {
        return name.compareToIgnoreCase(o.getName());
    }
    
    public boolean isOwner (String ns) {
//        return plugins.stream().filter(p -> p.getName().equals("bcgov-gwa-endpoint"))
//                .map(p -> p.getConfigStringArray("api_owners"))
//                .findFirst().get().stream()
//                .filter(owner -> owner.equals(String.format("#/team/%s", ns)))
//                .findFirst().isPresent();
          return tags.stream().filter(t -> t.equals(String.format("ns.%s", ns)))
                  .findFirst().isPresent();
    }
}
