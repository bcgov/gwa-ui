/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.bc.gov.gwa.v2.model;

import java.util.Map;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ACL {

    String group;
    String consumerId;
    KongConsumer consumer;
    Map<String, Object> data;
    
    public ACL (Object payload) {
        this.data = (Map<String, Object>) payload;
        
        group = (String) this.data.get("group");
        if (this.data.get("consumer") != null) {
            consumerId = (String) ((Map<String, Object>)this.data.get("consumer")).get("id");
        }
    }
    
    public void setConsumer (KongConsumer kongConsumer) {
        this.consumer = kongConsumer;
    }
}
