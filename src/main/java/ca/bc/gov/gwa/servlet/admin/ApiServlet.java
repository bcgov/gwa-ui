package ca.bc.gov.gwa.servlet.admin;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.bc.gov.gwa.servlet.ApiService;
import ca.bc.gov.gwa.servlet.GwaConstants;
import ca.bc.gov.gwa.util.Json;

@WebServlet(urlPatterns = "/int/rest/apis/*", loadOnStartup = 1)
public class ApiServlet extends BaseAdminServlet implements GwaConstants {

  private int apiRecord(final HttpServletRequest request, final HttpServletResponse response,
    final String method, final String apiIdOrName) {
    if (GET.equals(method)) {
      apiRecordGet(response, apiIdOrName);
    } else if (DELETE.equals(method)) {
      apiRecordDelete(response, apiIdOrName);
    } else if (PUT.equals(method)) {
      apiRecordUpdate(request, response, apiIdOrName);
    } else {
      return HttpServletResponse.SC_METHOD_NOT_ALLOWED;
    }
    return 0;
  }

  private void apiRecordDelete(final HttpServletResponse response, final String apiName) {
    final String path = APIS_PATH2 + apiName;
    this.apiService.handleDelete(response, path);
    this.apiService.clearCachedObject(API, apiName);
  }

  private void apiRecordGet(final HttpServletResponse response, final String apiId) {
    this.apiService.apiGet(response, apiId);
  }

  private void apiRecordUpdate(final HttpServletRequest request, final HttpServletResponse response,
    final String apiName) {
    this.apiService.apiUpdate(request, response, apiName);
  }

  private int apis(final HttpServletRequest request, final HttpServletResponse response,
    final String method) {
    if (GET.equals(method)) {
      apisList(request, response);
    } else if (POST.equals(method)) {
      return apisAdd(request, response);
    } else {
      return HttpServletResponse.SC_METHOD_NOT_ALLOWED;
    }
    return 0;
  }

  private int apisAdd(final HttpServletRequest request, final HttpServletResponse response) {
    final Map<String, Object> requestData = Json.readJsonMap(request);
    if (requestData == null) {
      return HttpServletResponse.SC_BAD_REQUEST;
    } else {
      this.apiService.handleRequest(response, httpClient -> {
        final Map<String, Object> apiRequest = this.apiService.getMap(requestData,
          ApiService.APIS_FIELD_NAMES);
        final Map<String, Object> apiResponse = httpClient.post(ApiService.APIS_PATH, apiRequest);
        final String apiId = (String)apiResponse.get(ApiService.ID);
        if (apiId != null) {
          this.apiService.apiAddPlugin(httpClient, requestData, apiId,
            ApiService.BCGOV_GWA_ENDPOINT, ApiService.ENDPOINT_FIELD_NAMES,
            ApiService.ENDPOINT_DEFAULT_CONFIG, false);
          this.apiService.apiAddPlugin(httpClient, requestData, apiId, ApiService.KEY_AUTH,
            ApiService.KEY_AUTH_FIELD_NAMES, null, true);
          this.apiService.apiAddPlugin(httpClient, requestData, apiId, ApiService.ACL,
            ApiService.ACL_FIELD_NAMES, null, true);
        }
        Json.writeJson(response, apiResponse);
      });
      return 0;
    }
  }

  private void apisList(final HttpServletRequest request, final HttpServletResponse response) {
    this.apiService.handleListAll(request, response, "/apis");
  }

  @Override
  protected void doService(final HttpServletRequest request, final HttpServletResponse response,
    final String method) {
    int statusCode = HttpServletResponse.SC_NOT_FOUND;
    final List<String> paths = splitPathInfo(request);
    final int pathCount = paths.size();
    if (pathCount == 0) {
      statusCode = apis(request, response, method);
    } else {
      final String apiIdOrName = paths.get(0);
      if (pathCount == 1) {
        statusCode = apiRecord(request, response, method, apiIdOrName);
      } else {
        final String path2 = paths.get(1);
        if (pathCount == 2) {
          if (PLUGINS.equals(path2)) {
            statusCode = plugins(request, response, method, apiIdOrName);
          }
        } else {
          final String path3 = paths.get(2);
          if (pathCount == 3) {
            if (PLUGINS.equals(path2)) {
              statusCode = pluginRecord(request, response, method, apiIdOrName, path3);
            }
          } else {
            final String path4 = paths.get(3);
            if (pathCount == 4) {
              if (GET.equals(method)) {
                if (GROUPS.equals(path2) && USERS.equals(path4)) {
                  statusCode = groupUsersList(request, response, apiIdOrName, path3);
                } else if (PLUGINS.equals(path2) && USERS.equals(path4)) {
                  statusCode = pluginUsersList(request, response, apiIdOrName, path3);
                }
              }
            } else {
              if (pathCount == 5) {
                final String path5 = paths.get(4);
                if (GROUPS.equals(path2) && USERS.equals(path4)) {
                  statusCode = groupUserRecord(response, method, apiIdOrName, path3, path5);
                } else if (PLUGINS.equals(path2) && USERS.equals(path4)) {
                  statusCode = pluginUserRecord(request, response, method, apiIdOrName, path3,
                    path5);
                }
              }
            }
          }
        }
      }
    }
    if (statusCode != 0) {
      sendError(response, statusCode);
    }
  }

  private int groupUserRecord(final HttpServletResponse response, final String method,
    final String apiName, final String groupName, final String userName) {
    if (DELETE.equals(method)) {
      groupUserRecordDelete(response, apiName, groupName, userName);
    } else if (POST.equals(method)) {
      groupUserRecordAdd(response, apiName, groupName, userName);
    } else {
      return HttpServletResponse.SC_METHOD_NOT_ALLOWED;
    }
    return 0;
  }

  private void groupUserRecordAdd(final HttpServletResponse response, final String apiName,
    final String groupName, final String userName) {
    this.apiService.apiGroupUserAdd(response, apiName, groupName, userName);
  }

  private void groupUserRecordDelete(final HttpServletResponse response, final String apiName,
    final String groupName, final String userName) {
    this.apiService.apiGroupUserDelete(response, apiName, groupName, userName);
  }

  private int groupUsersList(final HttpServletRequest request, final HttpServletResponse response,
    final String apiName, final String groupName) {
    this.apiService.endpointGroupUserList(request, response, apiName, groupName);
    return 0;
  }

  private int pluginRecord(final HttpServletRequest request, final HttpServletResponse response,
    final String method, final String apiIdOrName, final String pluginName) {
    if (DELETE.equals(method)) {
      pluginRecordDelete(response, apiIdOrName, pluginName);
    } else if (PUT.equals(method)) {
      pluginRecordUpdate(request, response, apiIdOrName, pluginName);
    } else {
      return HttpServletResponse.SC_METHOD_NOT_ALLOWED;
    }
    return 0;
  }

  private void pluginRecordDelete(final HttpServletResponse response, final String apiName,
    final String pluginName) {
    final String path = APIS_PATH2 + apiName + PLUGINS_PATH2 + pluginName;
    this.apiService.handleDelete(response, path);
    this.apiService.clearCachedObject(API, apiName);
  }

  private void pluginRecordUpdate(final HttpServletRequest request,
    final HttpServletResponse response, final String apiName, final String pluginName) {
    final String updatePath = APIS_PATH2 + apiName + PLUGINS_PATH2 + pluginName;
    this.apiService.handleUpdatePatch(request, response, updatePath, ApiServlet.PLUGIN_FIELD_NAMES);
    this.apiService.clearCachedObject(API, apiName);
  }

  private int plugins(final HttpServletRequest request, final HttpServletResponse response,
    final String method, final String apiName) {
    if (GET.equals(method)) {
      pluginsList(request, response, apiName);
    } else if (POST.equals(method)) {
      pluginsAdd(request, response, apiName);
    } else {
      return HttpServletResponse.SC_METHOD_NOT_ALLOWED;
    }
    return 0;
  }

  private void pluginsAdd(final HttpServletRequest request, final HttpServletResponse response,
    final String apiName) {
    final String pluginAddPath = APIS_PATH2 + apiName + "/plugins";
    this.apiService.handleAdd(request, response, pluginAddPath);
  }

  private void pluginsList(final HttpServletRequest request, final HttpServletResponse response,
    final String apiId) {
    this.apiService.pluginList(request, response, "/plugins?api_id=" + apiId,
      row -> row.get("consumer_id") == null);
  }

  private int pluginUserRecord(final HttpServletRequest request, final HttpServletResponse response,
    final String method, final String apiName, final String pluginName, final String userName) {
    if (GET.equals(method)) {
      pluginUserRecordGet(request, response, apiName, pluginName, userName);
    } else if (DELETE.equals(method)) {
      pluginUserRecordDelete(response, apiName, pluginName, userName);
    } else if (POST.equals(method)) {
      pluginRecordUpdate(request, response, apiName, pluginName);
    } else {
      return HttpServletResponse.SC_METHOD_NOT_ALLOWED;
    }
    return 0;
  }

  private void pluginUserRecordDelete(final HttpServletResponse response, final String apiName,
    final String pluginName, final String userName) {
    this.apiService.apiGroupUserDelete(response, apiName, pluginName, userName);
  }

  private void pluginUserRecordGet(final HttpServletRequest httpRequest,
    final HttpServletResponse httpResponse, final String apiId, final String pluginName,
    final String username) {
    this.apiService.handleRequest(httpResponse, httpClient -> {
      final String userId = this.apiService.userIdGetByUsername(httpClient, username);

      final String path = "/plugins?api_id=" + apiId + "&name=" + pluginName + "&consumer_id="
        + userId;
      final Map<String, Object> kongResponse = this.apiService.kongPageAll(httpRequest, httpClient,
        path);
      final List<Map<String, Object>> rows = this.apiService.getList(kongResponse, DATA);
      Map<String, Object> plugin;
      if (rows.isEmpty()) {
        plugin = new LinkedHashMap<>();
        plugin.put(NAME, pluginName);
        plugin.put(API_ID, apiId);
        plugin.put(CONSUMER_ID, userId);
      } else {
        plugin = rows.get(0);
      }
      this.apiService.pluginAddData(httpClient, plugin);
      Json.writeJson(httpResponse, plugin);
    });

  }

  private int pluginUsersList(final HttpServletRequest request, final HttpServletResponse response,
    final String apiId, final String pluginName) {
    final String path = "/plugins?api_id=" + apiId + "&name=" + pluginName;
    this.apiService.pluginList(request, response, path, row -> {
      return row.get("consumer_id") != null;
    });
    return 0;
  }
}
