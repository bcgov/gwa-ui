/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.bc.gov.gwa.v2.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Plugin {

    String id;
    String name;
    String consumerId;
    String routeId;
    String serviceId;
    Map<String, Object> config;
    Tags tags;
    
    @JsonIgnore
    Map<String, Object> data;
    
    public Plugin (String name) {
        this.name = name;
        config = new HashMap<>();
    }
    
    public Plugin (Object payload) {
        this.data = (Map<String, Object>) payload;
        
        id = (String) data.get("id");
        name = (String) data.get("name");
        tags = new Tags(data);
        
        if (data.get("consumer") != null) {
            consumerId = (String) ((Map<String, Object>)data.get("consumer")).get("id");
        }
        if (data.get("route") != null) {
            routeId = (String) ((Map<String, Object>)data.get("route")).get("id");
        }
        if (data.get("service") != null) {
            serviceId = (String) ((Map<String, Object>)data.get("service")).get("id");
        }
        config = (Map<String, Object>) data.get("config");
        
        mask_config (config);
        
    }
    
    public List<String> getConfigStringArray (String attrName) {
        Object value = config.getOrDefault(attrName, new ArrayList<>());
        if (value instanceof List) {
            return (List) value;
        } else if (value == null) {
            return new ArrayList<>();
        } else {
            return Arrays.asList((String[]) value);
        }
    }
    
    private void mask_config (Map<String, Object> conf) {
        for (String key : conf.keySet()) {
            if (key.startsWith("redis_")) {
                conf.put(key, "****");
            }
        }
    }
}
