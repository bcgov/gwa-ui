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
import ca.bc.gov.gwa.v2.conf.GwaSettings;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author aidancope
 */
public class KongAdminService {
  
  GwaSettings config;
  
  public KongAdminService (GwaSettings config) {
      this.config = config;
  }
  
  public Map<String, Object> kongPageAll(final HttpServletRequest httpRequest,
    final JsonHttpClient httpClient, final String path) throws IOException {
    final List<Map<String, Object>> rows = new ArrayList<>();

    kongPageAll(httpRequest, httpClient, path, (Consumer<Map<String, Object>>)rows::add);

    return newResponseRows(rows);
  }

    private Map<String, Object> newResponseRows(final List<Map<String, Object>> rows) {
    final Map<String, Object> response = new LinkedHashMap<>();
    response.put(DATA, rows);
    response.put(TOTAL, rows.size());
    return response;
  }
    
  protected void kongPageAll(final HttpServletRequest httpRequest, final JsonHttpClient httpClient,
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

  public Map<String, Object> kongPageAll(final HttpServletRequest httpRequest,
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

  public void kongPageAll(final JsonHttpClient httpClient, final String path,
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
    String urlString = (String)kongResponse.remove(NEXT);
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
    return url.toString();
  }

  private String getKongPageUrl(final String path) {
    final StringBuilder url = new StringBuilder(config.getKongAdminUrl());
    url.append(path);
    return url.toString();
  }
  
  public JsonHttpClient newKongClient() {
    return new JsonHttpClient(config.getKongAdminUrl(), config.getKongAdminUsername(), config.getKongAdminPassword());
  }  
  
  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  public <V> List<V> getList(final Map<String, Object> map, final String key) {
    final Object value = map.get(key);
    if (value == null) {
      return Collections.emptyList();
    } else if (value instanceof List) {
      return (List<V>)value;
    } else if (value instanceof Map) {
      final Map mapValue = (Map)value;
      if (mapValue.isEmpty()) {
        return Collections.emptyList();
      }
    }
    throw new IllegalArgumentException("Expecting a list for " + key + " not " + value);
  }  
}
