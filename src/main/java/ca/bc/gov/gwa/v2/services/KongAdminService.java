/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.bc.gov.gwa.v2.services;

import ca.bc.gov.gwa.http.JsonHttpClient;
import static ca.bc.gov.gwa.servlet.GwaConstants.DATA;
import static ca.bc.gov.gwa.servlet.GwaConstants.NEXT;
import static ca.bc.gov.gwa.servlet.GwaConstants.TOTAL;
import ca.bc.gov.gwa.servlet.authentication.oidc.LookupUtil;
import ca.bc.gov.gwa.util.Json;
import ca.bc.gov.gwa.v2.conf.GwaSettings;
import ca.bc.gov.gwa.v2.model.ACL;
import ca.bc.gov.gwa.v2.model.Group;
import ca.bc.gov.gwa.v2.model.KongConsumer;
import ca.bc.gov.gwa.v2.model.KongModel;
import ca.bc.gov.gwa.v2.model.Plugin;
import ca.bc.gov.gwa.v2.model.Route;
import ca.bc.gov.gwa.v2.model.Service;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.pac4j.core.profile.UserProfile;

/**
 *
 */
@Slf4j
public class KongAdminService {

    long cacheTime;
    KongModel cachedModel;
            
    GwaSettings config;

    public KongAdminService(GwaSettings config) {
        this.config = config;
    }
    
    public JsonHttpClient newKongClient() {
        return new JsonHttpClient(config.getKongAdminUrl(), config.getKongAdminUsername(), config.getKongAdminPassword());
    }

    public Collection<Group> buildModel () throws IOException {
        long start = new Date().getTime();
        
        Map<String,Group> groups = new HashMap<>();
        
        List<Object> items = (List<Object>) acls().get("data");
        for ( Object item : items ) {
            // Add acls to the groups
            ACL acl = new ACL(item);
            System.out.println("ADD ACL " + acl);
            if (groups.get(acl.getGroup()) == null) {
                Group group = new Group(acl);
                groups.put(group.getGroup(), group);
            } else {
                Group group = groups.get(acl.getGroup());
                group.getMembership().add(acl);
            }
        }
        
        List<Object> plugins = (List<Object>) plugins().get("data");
        for ( Object item : plugins ) {
            Plugin plugin = new Plugin(item);
            if (plugin.getName().equals("acl")) {
                List<String> allow = getList(((Map<String,Object>)plugin.getData().get("config")), "allow");
                for (String groupName : allow) {
                    Group group = groups.get(groupName);
                    if (group == null) {
                        group = new Group(groupName, plugin);
                        groups.put(group.getGroup(), group);
                    } else {
                        group.getPlugins().add(plugin);
                    }
                }
            }
        }
        
        Map<String, KongConsumer> mapConsumers = new HashMap<>();
        List<Object> consumers = (List<Object>) consumers().get("data");
        for ( Object item : consumers ) {
            KongConsumer consumer = new KongConsumer(item);
            mapConsumers.put(consumer.getId(), consumer);
        }

        for (Group group : groups.values()) {
            for (ACL acl : group.getMembership()) {
                if (acl.getConsumerId() != null) {
                    acl.setConsumer(mapConsumers.get(acl.getConsumerId()));
                }
            }
        }

        List<Group> result = groups.values().stream().sorted().collect(Collectors 
                            .toCollection(ArrayList::new));
        
        long end = new Date().getTime();
        System.out.println("Executed in " + (end-start) + " ms");
        
        System.out.println("Groups = "+groups.keySet());
        System.out.println(Json.toString(groups));
        
        return result;
    }

//    public Collection<Plugin> buildPluginModel () throws IOException {
//        long start = new Date().getTime();
//        Collection<Plugin> result = new ArrayList<>();
//        
//        List<Object> plugins = (List<Object>) plugins().get("data");
//        for ( Object item : plugins ) {
//            Plugin plugin = new Plugin(item);
//            result.add(plugin);
//        }
//        long end = new Date().getTime();
//        System.out.println("Executed in " + (end-start) + " ms");
//                
//        return result.stream().collect(Collectors 
//                            .toCollection(ArrayList::new));
//    }

    public KongConsumer buildConsumer (String username) throws IOException {
        final KongConsumer consumer = new KongConsumer (consumer(username));
        KongModel model = buildKongModel();
        model.getPlugins().stream().forEach(plugin -> {
            if (consumer != null && plugin.getConsumerId() != null && plugin.getConsumerId().equals(consumer.getId())) {
                consumer.addPlugin(plugin);
            }
        });
        return consumer;
    }
    
    public void forceRefresh() throws IOException {
        cacheTime = new Date().getTime();
        cachedModel = buildServiceModelCache();
    }
    
    public Collection<Service> buildServiceModel () throws IOException {
        return c.getServices();
    }

    public KongModel buildKongModel () throws IOException {
        long ms = new Date().getTime();
        
        if ((ms - cacheTime) > (20 * 60 * 1000)) {
            log.debug("Refreshing cache on this request.");
            cachedModel = buildServiceModelCache();
            cacheTime = ms;
        }
        return cachedModel;
    }

    
    private KongModel buildServiceModelCache () throws IOException {
        long start = new Date().getTime();
        
        KongModel model = new KongModel();
        
        Map<String, Service> services = new HashMap<>();
        Map<String, Route> routes = new HashMap<>();
        List<Object> serviceObjects = (List<Object>) services().get("data");
        for ( Object item : serviceObjects ) {
            Service service = new Service(item);
            services.put(service.getId(), service);
        }

        List<Object> items = (List<Object>) routes().get("data");
        for ( Object item : items ) {
            Route route = new Route(item);
            if (route.getServiceId() != null) {
                Service service = services.get(route.getServiceId());
                service.addRoute(route);
            }
            routes.put(route.getId(), route);
        }

        List<Plugin> pluginData = new ArrayList<>();
        
        List<Object> plugins = (List<Object>) plugins().get("data");
        for ( Object item : plugins ) {
            Plugin plugin = new Plugin(item);
            if (plugin.getServiceId() != null) {
                Service service = services.get(plugin.getServiceId());
                service.addPlugin(plugin);
            }
            if (plugin.getRouteId() != null) {
                Route route = routes.get(plugin.getRouteId());
                route.addPlugin(plugin);
            }
            
            pluginData.add(plugin);
        }

        Collection<Service> serviceValues = services.values();
        
        // Need to backfill this plugin to get the UI to work
        // Should remove ASAP as its silly to put it in, but want to limit
        // the UI work.
        for ( Service service : serviceValues ) {
            if (!service.hasPlugin("bcgov-gwa-endpoint")) {
                Plugin plugin = new Plugin("bcgov-gwa-endpoint");
                plugin.getConfig().put("api_owners", new String[0]);
                service.addPlugin(plugin);
            }
        }
        
        long end = new Date().getTime();
        System.out.println("Executed in " + (end-start) + " ms");
                
        model.setServices(services.values().stream().sorted().collect(Collectors 
                            .toCollection(ArrayList::new)));
        model.setPlugins (pluginData);
        return model;
    }

    
    public List<Service> filterServicesByPermissions (Collection<Service> services, UserProfile profile) {
        String ns = String.format("%s", profile.getAttribute(LookupUtil.NAMESPACE_CLAIM));
        
        log.debug("FILTER BY {} using namespace {}", services.size(), ns);
        if (profile.getAttribute(LookupUtil.NAMESPACE_CLAIM) == null) {
            return new ArrayList<>();
        } else {
            return services.stream()
                    .filter(s -> {
                        return s.isOwner(ns);
                    })
                    .collect(Collectors 
                        .toCollection(ArrayList::new));
        }
    }
    
    public Map<String, Object> acls() throws IOException {
        JsonHttpClient httpClient = newKongClient();
        return kongPageAll(null, httpClient, "acls");
    }

    public Map<String, Object> services() throws IOException {
        JsonHttpClient httpClient = newKongClient();
        return kongPageAll(null, httpClient, "services");
    }

    public Map<String, Object> routes() throws IOException {
        JsonHttpClient httpClient = newKongClient();
        return kongPageAll(null, httpClient, "routes");
    }

    public Map<String, Object> consumers() throws IOException {
        JsonHttpClient httpClient = newKongClient();
        return kongPageAll(null, httpClient, "consumers");
    }

    
    public Map<String, Object> consumer(String username) throws IOException {
        JsonHttpClient httpClient = newKongClient();
        return kongRecord(httpClient, String.format("consumers/%s", username));
    }

    public Map<String, Object> plugins() throws IOException {
        JsonHttpClient httpClient = newKongClient();
        return kongPageAll(null, httpClient, "plugins");
    }

    public Map<String, Object> kongPageAll(final HttpServletRequest httpRequest,
            final JsonHttpClient httpClient, final String path) throws IOException {
        final List<Map<String, Object>> rows = new ArrayList<>();

        kongPageAll(httpRequest, httpClient, path, (Consumer<Map<String, Object>>) rows::add);

        return newResponseRows(rows);
    }

    private Map<String, Object> newResponseRows(final List<Map<String, Object>> rows) {
        final Map<String, Object> response = new LinkedHashMap<>();
        response.put(DATA, rows);
        response.put(TOTAL, rows.size());
        return response;
    }

    private void kongPageAll(final HttpServletRequest httpRequest, final JsonHttpClient httpClient,
            final String path, final Consumer<Map<String, Object>> action) throws IOException {
        try {
            String urlString = getKongPageUrl(httpRequest, path);
            do {
                final Map<String, Object> kongResponse = httpClient.getByUrl(urlString);
                final List<Map<String, Object>> rows = getList(kongResponse, DATA);
                for (final Map<String, Object> row : rows) {
                    action.accept(row);

                }
                urlString = getNextUrl(kongResponse);
            } while (urlString != null);
        } catch (final NoSuchElementException e) {
        }
    }

    private Map<String, Object> kongPageAll(final HttpServletRequest httpRequest,
            final JsonHttpClient httpClient, final String path, final Predicate<Map<String, Object>> filter)
            throws IOException {
        final List<Map<String, Object>> allRows = new ArrayList<>();
        kongPageAll(httpRequest, httpClient, path, row -> {
            if (filter == null || filter.test(row)) {
                allRows.add(row);
            }
        });
        final Map<String, Object> response = new LinkedHashMap<>();
        final int offsetPage = getPageOffset(httpRequest);
        if (offsetPage > 0) {
            final List<Map<String, Object>> rows = allRows.subList(offsetPage, allRows.size());
            response.put(DATA, rows);
        } else {
            response.put(DATA, allRows);
        }
        response.put(TOTAL, allRows.size());
        return response;
    }

    private void kongPageAll(final JsonHttpClient httpClient, final String path,
            final Consumer<Map<String, Object>> action) throws IOException {
        String urlString = getKongPageUrl(path);
        do {
            final Map<String, Object> kongResponse = httpClient.getByUrl(urlString);
            final List<Map<String, Object>> rows = getList(kongResponse, DATA);
            for (final Map<String, Object> row : rows) {
                action.accept(row);
            }
            urlString = getNextUrl(kongResponse);
        } while (urlString != null);
    }

    private String getNextUrl(final Map<String, Object> kongResponse) {
        String urlString = (String) kongResponse.remove(NEXT);
        if (urlString != null && !urlString.startsWith("http")) {
            urlString = config.getKongAdminUrl() + urlString;
        }
        return urlString;
    }

    private int getPageOffset(final HttpServletRequest httpRequest) {
        final String offset = httpRequest.getParameter("offset");
        if (offset != null && offset.length() > 0) {
            try {
                return Integer.parseInt(offset);
            } catch (final Exception e) {
                throw new IllegalArgumentException("Offset must be an integer number:" + offset, e);
            }
        }
        return 0;
    }

    private String getKongPageUrl(final HttpServletRequest httpRequest, final String path) {
        final StringBuilder url = new StringBuilder(config.getKongAdminUrl());
        url.append(path);
        if (path.indexOf('?') == -1) {
            url.append('?');
        } else {
            url.append('&');
        }
        if (httpRequest != null) {
            final String limit = httpRequest.getParameter("limit");
            if (limit != null) {
                url.append("size=");
                url.append(limit);
            }

            final String[] filterFieldNames = httpRequest.getParameterValues("filterFieldName");
            final String[] filterValues = httpRequest.getParameterValues("filterValue");
            if (filterFieldNames != null && filterValues != null) {
                for (int i = 0; i < filterFieldNames.length; i++) {
                    final String filterFieldName = filterFieldNames[i];
                    String filterValue = filterValues[i];
                    if (filterValue != null) {
                        filterValue = filterValue.trim();
                        if (filterValue.length() > 0) {
                            url.append('&');
                            url.append(filterFieldName);
                            url.append('=');
                            url.append(filterValue);
                        }
                    }

                }
            }
        }
        return url.toString();
    }

    private String getKongPageUrl(final String path) {
        final StringBuilder url = new StringBuilder(config.getKongAdminUrl());
        url.append(path);
        return url.toString();
    }

    @SuppressWarnings({
        "unchecked", "rawtypes"
    })
    private <V> List<V> getList(final Map<String, Object> map, final String key) {
        final Object value = map.get(key);
        if (value == null) {
            return Collections.emptyList();
        } else if (value instanceof List) {
            return (List<V>) value;
        } else if (value instanceof Map) {
            final Map mapValue = (Map) value;
            if (mapValue.isEmpty()) {
                return Collections.emptyList();
            }
        }
        throw new IllegalArgumentException("Expecting a list for " + key + " not " + value);
    }
    
    private Map<String, Object> kongRecord(final JsonHttpClient httpClient, final String path) throws IOException {
        String urlString = getKongPageUrl(path);
        final Map<String, Object> kongResponse = httpClient.getByUrl(urlString);
        return kongResponse;
    }
    
}
