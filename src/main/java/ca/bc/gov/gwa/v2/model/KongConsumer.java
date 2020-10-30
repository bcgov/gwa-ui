/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.bc.gov.gwa.v2.model;

import static ca.bc.gov.gwa.v2.model.Service.TIME_IN_SECONDS;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class KongConsumer {
    String id;
    String username;
    @JsonProperty("custom_id")
    String customId;
    @JsonProperty("created_at")
    Long createdAt;
    Tags tags;
    List<Plugin> plugins;
    
    Map<String, Object> data;
        
    public KongConsumer (Object payload) {
        this.data = (Map<String, Object>) payload;
        
        id = (String) data.get("id");
        username = (String) data.get("username");
        customId = (String) data.get("custom_id");
        tags = new Tags(data);
        
        plugins = new ArrayList<>();

        createdAt = ( (BigDecimal) data.get("created_at")).longValue() * (TIME_IN_SECONDS ? 1000:1);

    }
    
    public void addPlugin (Plugin plugin) {
        plugins.add(plugin);
    }
    
}
