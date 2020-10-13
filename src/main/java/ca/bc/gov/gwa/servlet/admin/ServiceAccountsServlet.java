package ca.bc.gov.gwa.servlet.admin;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.bc.gov.gwa.servlet.BaseServlet;
import static ca.bc.gov.gwa.servlet.GwaConstants.DATA;
import static ca.bc.gov.gwa.servlet.GwaConstants.DELETED;
import static ca.bc.gov.gwa.servlet.GwaConstants.TOTAL;
import ca.bc.gov.gwa.servlet.authentication.oidc.LookupUtil;
import ca.bc.gov.gwa.util.Json;
import ca.bc.gov.gwa.v2.model.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@WebServlet(urlPatterns = "/int/rest/serviceAccounts/*", loadOnStartup = 1)
public class ServiceAccountsServlet extends BaseServlet {
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {
    final List<String> paths = splitPathInfo(request);
    
    if (!this.apiService.endpointAccessAllowed(request, response, paths)) {
      return;
    }
    switch (paths.size()) {
      case 0:
        doGetServiceAccountsList(request, response);
        break;
      case 1:
        //doGetEndpoint(request, response, paths);
        break;
      default:
        sendError(response, HttpServletResponse.SC_NOT_FOUND);
      break;
    }
  }

  @Override
  protected void doDelete(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {
    final List<String> paths = splitPathInfo(request);

    System.out.println("D = "+paths.get(0));
    Map<String, Object> apiResponse = new HashMap<>();
   
    apiService.writeJsonResponse(response, DELETED);
//
//    Json.writeJson(response, apiResponse);
  }
  
  @Override
  protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {
    final List<String> paths = splitPathInfo(request);

    Map<String, Object> apiResponse = new HashMap<>();
    apiResponse.put("key","ns-sampler");
    apiResponse.put("secret",UUID.randomUUID().toString());
    apiResponse.put("scope", Arrays.asList("admin:acl", "admin:gateway", "admin:catalog"));
   
    Json.writeJson(response, apiResponse);
    
  }

  @Override
  protected void doPut(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {
    final List<String> paths = splitPathInfo(request);

    Map<String, Object> apiResponse = new HashMap<>();
    apiResponse.put("key","ns-sampler");
    apiResponse.put("secret",UUID.randomUUID().toString());
    apiResponse.put("scope", Arrays.asList("admin:acl", "admin:gateway", "admin:catalog"));
   
    Json.writeJson(response, apiResponse);
    
  }
  
//  private void doGetEndpoint(final HttpServletRequest request, final HttpServletResponse response, final List<String> paths) throws IOException {
//    final String apiName = paths.get(0);
//    this.apiService.endpointItem(request, response, apiName);
//  }

  private void doGetServiceAccountsList(final HttpServletRequest request,
    final HttpServletResponse response) throws IOException {
    final Map<String, Object> kongResponse;

    List<Map<String, Object>> result = new ArrayList<>();
    Map<String, Object> apiResponse = new HashMap<>();
    apiResponse.put("id","123");
    apiResponse.put("key","ns-sampler");
    apiResponse.put("scope", Arrays.asList("admin:acl", "admin:gateway", "admin:catalog"));
    result.add(apiResponse);
    
    kongResponse = new LinkedHashMap<>();
    kongResponse.put(DATA, result);
    kongResponse.put(TOTAL, 1);
    ObjectMapper mapper = new ObjectMapper();
    mapper.writeValue(response.getOutputStream(), kongResponse);
  }

}
