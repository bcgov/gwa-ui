/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.bc.gov.gwa.v1;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

public class KongDataService {
   
    public String getContent (String id) {
        return "Great";
    }
    
    public static void main(String[] args) {
        
        KongDataService ds = new KongDataService();
        String c = ds.getContent("1");
        System.out.println(c);
//        CacheManager cacheManager
//            = CacheManagerBuilder.newCacheManagerBuilder() 
//            .withCache("preConfigured",
//                CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class, ResourcePoolsBuilder.heap(10))) 
//            .build(); 
//        cacheManager.init(); 
//
//        Cache<Long, String> preConfigured =
//            cacheManager.getCache("preConfigured", Long.class, String.class); 
//
//        Cache<Long, String> myCache = cacheManager.createCache("myCache", 
//            CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class, ResourcePoolsBuilder.heap(10)).build());
//
//        myCache.put(1L, "da one!"); 
//        String value = myCache.get(1L); 
//
//        cacheManager.removeCache("preConfigured"); 
//
//        cacheManager.close(); 
    }

    public String find(String isbn, boolean checkWarehouse) {
        return "Searched";
    }
}
