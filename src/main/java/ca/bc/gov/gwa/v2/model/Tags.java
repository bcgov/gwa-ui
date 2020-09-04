/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.bc.gov.gwa.v2.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class Tags extends ArrayList<String> {
    
    public Tags (Map<String, Object> record) {
        if (record.containsKey("tags") && record.get("tags") != null) {
            this.addAll((List) record.get("tags"));
        }
    }
}
