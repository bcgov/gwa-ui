/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.bc.gov.gwa.v2.model;

import static ca.bc.gov.gwa.v2.model.Service.TIME_IN_SECONDS;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Map;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ACL {

    String group;
    String consumerId;
    KongConsumer consumer;
    String username;
    Map<String, Object> data;

    @JsonProperty("created_at")
    Long createdAt;
    
    public ACL (Object payload) {
        this.data = (Map<String, Object>) payload;
        
        createdAt = ( (BigDecimal) data.get("created_at")).longValue() * (TIME_IN_SECONDS ? 1000:1);
        
        group = (String) this.data.get("group");
        if (this.data.get("consumer") != null) {
            consumerId = (String) ((Map<String, Object>)this.data.get("consumer")).get("id");
        }
    }
    
    public void setConsumer (KongConsumer kongConsumer) {
        this.consumer = kongConsumer;
        this.username = consumer.getUsername(); // only needed for UI! :(
    }
}
