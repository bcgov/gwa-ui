package ca.bc.gov.gwa.servlet.admin;

import ca.bc.gov.gwa.http.HttpStatusException;
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
import static ca.bc.gov.gwa.servlet.GwaConstants.UNKNOWN_APPLICATION_ERROR;
import ca.bc.gov.gwa.servlet.authentication.oidc.LookupUtil;
import ca.bc.gov.gwa.util.Json;
import ca.bc.gov.gwa.v1.ApiService;
import ca.bc.gov.gwa.v2.controllers.GwaController;
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
import lombok.extern.slf4j.Slf4j;
import org.pac4j.core.profile.CommonProfile;

@Slf4j
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
    
    String accountId = paths.get(0);
    
    CommonProfile profile = LookupUtil.lookupUserProfile(request, response);
    
    String ns = LookupUtil.getNamespaceClaim(profile);

    GwaController cc = ApiService.getGwaController(request.getServletContext());

    try {
        cc.getGwaApiService().deleteServiceAccount(profile, ns, accountId);
    } catch (HttpStatusException ex) {
        apiService.writeJsonError(response, ex.getMessage(), ex);
    }
    Json.writeJson(response, Collections.emptyMap());
  }
  
  @Override
  protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {
    final List<String> paths = splitPathInfo(request);

    CommonProfile profile = LookupUtil.lookupUserProfile(request, response);
    
    String ns = LookupUtil.getNamespaceClaim(profile);

    GwaController cc = ApiService.getGwaController(request.getServletContext());

    List<String> scopes = Arrays.asList("admin:acl", "admin:gateway", "admin:catalog");
    
    try {
        Map<String, Object> result = cc.getGwaApiService().createServiceAccount(profile, ns, scopes);
        Json.writeJson(response, result);
    } catch (HttpStatusException ex) {
        apiService.writeJsonError(response, ex.getMessage(), ex);
    }
  }

  @Override
  protected void doPut(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {
    final List<String> paths = splitPathInfo(request);

    String accountId = paths.get(0);
    
    CommonProfile profile = LookupUtil.lookupUserProfile(request, response);
    
    String ns = LookupUtil.getNamespaceClaim(profile);

    GwaController cc = ApiService.getGwaController(request.getServletContext());

    try {
        Map<String, Object> apiResponse = cc.getGwaApiService().updateServiceAccountCredentials(profile, ns, accountId);
        Json.writeJson(response, apiResponse);
    } catch (HttpStatusException ex) {
        apiService.writeJsonError(response, ex.getMessage(), ex);
    }
  }
  
//  private void doGetEndpoint(final HttpServletRequest request, final HttpServletResponse response, final List<String> paths) throws IOException {
//    final String apiName = paths.get(0);
//    this.apiService.endpointItem(request, response, apiName);
//  }

  private void doGetServiceAccountsList(final HttpServletRequest request,
    final HttpServletResponse response) throws IOException {
    final Map<String, Object> kongResponse;

    CommonProfile profile = LookupUtil.lookupUserProfile(request, response);
    
    String ns = LookupUtil.getNamespaceClaim(profile);
    
    GwaController cc = ApiService.getGwaController(request.getServletContext());

    try {
        List<String> result = cc.getGwaApiService().getServiceAccountList(profile, ns);
        
        List<Map<String,Object>> data = new ArrayList<>();
        for (String key : result) {
            Map<String, Object> row = new HashMap<>();
            row.put("key", key);
            data.add(row);
        }
        kongResponse = new LinkedHashMap<>();
        kongResponse.put(DATA, data);
        kongResponse.put(TOTAL, result.size());
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), kongResponse);
    } catch (HttpStatusException ex) {
        apiService.writeJsonError(response, ex.getMessage(), ex);
    }
    
  }

}
