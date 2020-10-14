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

@WebServlet(urlPatterns = "/int/rest/namespaces/*", loadOnStartup = 1)
public class NamespacesServlet extends BaseServlet {
  private static final long serialVersionUID = 1L;


  @Override
  protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {

    GwaController cc = ApiService.getGwaController(request.getServletContext());

    ObjectMapper mapper = new ObjectMapper();
    Map<String, String> input = mapper.readValue(request.getInputStream(), Map.class);
    
    String name = input.get("name");
    
    try {
        cc.getGwaApiService().createNamespace(LookupUtil.lookupUserProfile(request, response), name);
    } catch (HttpStatusException ex) {
        apiService.writeJsonError(response, ex.getMessage(), ex);
    }
    
    Json.writeJson(response, Collections.emptyMap());
  }

}
