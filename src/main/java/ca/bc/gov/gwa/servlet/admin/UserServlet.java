package ca.bc.gov.gwa.servlet.admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.bc.gov.gwa.v1.ApiService;
import ca.bc.gov.gwa.servlet.GwaConstants;
import ca.bc.gov.gwa.util.Json;

@WebServlet(urlPatterns = "/int/rest/users/*", loadOnStartup = 1)
public class UserServlet extends BaseAdminServlet implements GwaConstants {

  private static final long serialVersionUID = 1L;

  private static final List<String> CONSUMER_FIELD_NAMES = Arrays.asList("id", "username",
    "custom_id", "created_at");

  private static final Map<String, Map<String, Object>> USER_DATA_SCHEMA_BY_NAME = new HashMap<>();

  private static final Map<String, String> USER_DATA_KEY_BY_NAME = new HashMap<>();

  static {
    for (final String dataName : USER_DATA_NAMES) {
      try (
        InputStream in = UserServlet.class
          .getResourceAsStream("/ca/bc/gov/gwa/kong/user/data/" + dataName + ".json")) {
        Map<String, Object> customSchema;
        if (in == null) {
          customSchema = Collections.emptyMap();
        } else {
          customSchema = Json.read(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
        USER_DATA_SCHEMA_BY_NAME.put(dataName, customSchema);
        final String primaryKey = (String)customSchema.getOrDefault("primaryKey", "id");
        USER_DATA_KEY_BY_NAME.put(dataName, primaryKey);
      } catch (final IOException e) {
        ApiService.LOG.error("Unable to read custom schema:" + dataName, e);
      }
    }
  }

  private static String getUserDataKey(final String dataName) {
    return USER_DATA_KEY_BY_NAME.getOrDefault(dataName, "id");
  }

  private static Map<String, Object> getUserDataSchema(final String dataName) {
    return USER_DATA_SCHEMA_BY_NAME.get(dataName);
  }

  @Override
  protected void doDelete(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {
    final List<String> paths = splitPathInfo(request);
    final int pathCount = paths.size();
    if (pathCount == 0) {
      response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    } else {
      final String username = paths.get(0);
      if (pathCount == 1) {
        userRecordDelete(response, username);
      } else {
        final String path2 = paths.get(1);
        if (pathCount == 2) {
          response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        } else {
          final String path3 = paths.get(2);
          if (pathCount == 3) {
            if (GROUPS.equals(path2)) {
              userGroupDelete(response, username, path3);
            } else {
              response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
          } else if (pathCount == 4) {
            final String path4 = paths.get(3);
            if (DATA.equals(path2)) {
              userDataDelete(response, username, path3, path4);
            } else {
              response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
          } else if (pathCount <= 4) {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
          } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
          }
        }
      }
    }
  }

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {
    final List<String> paths = splitPathInfo(request);
    final int pathCount = paths.size();
    if (pathCount == 0) {
      userRecordList(request, response);
    } else {
      final String username = paths.get(0);
      if (pathCount == 1) {
        userRecordGet(response, username);
      } else {
        final String path2 = paths.get(1);
        if (pathCount == 2) {
          if (GROUPS.equals(path2)) {
            userGroupList(request, response, username);
          } else if (PLUGINS.equals(path2)) {
            userPluginList(request, response, username);
          } else if (DATA.equals(path2)) {
            userDataNames(response);
          } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
          }
        } else {
          final String path3 = paths.get(2);
          if (pathCount == 3) {
            if (DATA.equals(path2)) {
              userDataList(request, response, username, path3);
            } else {
              response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
          } else if (pathCount == 4) {
            final String path4 = paths.get(3);
            if (DATA.equals(path2)) {
              if ("_schema".equals(path4)) {
                userDataSchemaGet(response, path3);
              } else {
                userDataGet(response, username, path3, path4);
              }
            }
          } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
          }
        }
      }
    }
  }

  @Override
  protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {
    final List<String> paths = splitPathInfo(request);
    final int pathCount = paths.size();
    if (pathCount == 0) {
      userRecordAdd(request, response);
    } else {
      final String username = paths.get(0);
      if (pathCount == 1) {
        sendError(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
      } else {
        final String path2 = paths.get(1);
        if (pathCount == 2) {
          if (GROUPS.equals(path2)) {
            userGroupAdd(request, response, username);
          } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
          }
        } else {
          if (pathCount == 3) {
            final String path3 = paths.get(2);
            if (DATA.equals(path2)) {
              userDataAdd(request, response, username, path3);
            } else {
              response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
          } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
          }
        }
      }
    }
  }

  @Override
  protected void doPut(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {
    final List<String> paths = splitPathInfo(request);
    final int pathCount = paths.size();
    if (pathCount == 0) {
      sendError(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    } else {
      final String username = paths.get(0);
      if (pathCount == 1) {
        userRecordUpdate(request, response, username);
      } else {
        final String path2 = paths.get(1);
        if (pathCount == 2) {
          if (GROUPS.equals(path2)) {
            sendError(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
          } else if (PLUGINS.equals(path2)) {
            sendError(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
          } else if (DATA.equals(path2)) {
            sendError(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
          } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
          }
        } else {
          final String path3 = paths.get(2);
          if (pathCount == 3) {
            if (DATA.equals(path2)) {
              sendError(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            } else {
              response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
          } else if (pathCount == 4) {
            final String path4 = paths.get(3);
            if (DATA.equals(path2)) {
              if ("_schema".equals(path4)) {
                sendError(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
              } else {
                userDataUpdate(request, response, username, path3, path4);
              }
            } else {
              sendError(response, HttpServletResponse.SC_NOT_FOUND);
            }
          } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
          }
        }
      }

    }
    switch (pathCount) {
      case 0:

      break;

      case 1:

      break;

      default:
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      break;
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> getUserData(final HttpServletRequest request,
    final HttpServletResponse response) {
    final Map<String, Object> requestData = Json.readJsonMap(request);
    if (requestData == null) {
      this.apiService.sendError(response, HttpServletResponse.SC_BAD_REQUEST);
      return null;
    } else {
      final Map<String, Object> data = this.apiService.getMap(requestData,
        Arrays.asList(CONFIG, ID));
      final Map<String, Object> userData = (Map<String, Object>)data.get(CONFIG);
      final String id = (String)data.get(ID);
      userData.put(ID, id);
      ImportServlet.removeEmptyValues(userData);
      return userData;
    }
  }

  private void userDataAdd(final HttpServletRequest httpRequest,
    final HttpServletResponse httpResponse, final String username, final String dataName) {
    final Map<String, Object> userData = getUserData(httpRequest, httpResponse);
    if (userData != null) {
      this.apiService.handleRequest(httpResponse, httpClient -> {
        final String path = CONSUMERS_PATH2 + username + "/" + dataName;
        final Map<String, Object> apiResponse = httpClient.post(path, userData);
        final Map<String, Object> userDataResponse = userDataSetConfig(dataName, apiResponse);
        Json.writeJson(httpResponse, userDataResponse);
      });
    }
  }

  private void userDataDelete(final HttpServletResponse response, final String username,
    final String dataName, final String id) {
    final String consumerPath = CONSUMERS_PATH2 + username + "/" + dataName + "/" + id;
    this.apiService.handleDelete(response, consumerPath);
  }

  private void userDataGet(final HttpServletResponse httpResponse, final String username,
    final String dataName, final String id) {
    this.apiService.handleRequest(httpResponse, httpClient -> {
      final String path = CONSUMERS_PATH2 + username + "/" + dataName + "/" + id;
      final Map<String, Object> response = this.apiService.kongGet(httpClient, path);
      if (response == null) {
        httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
      } else {
        final Map<String, Object> userDataResponse = userDataSetConfig(dataName, response);
        Json.writeJson(httpResponse, userDataResponse);

      }
    });
  }

  private void userDataList(final HttpServletRequest httpRequest,
    final HttpServletResponse htpResponse, final String username, final String dataName) {
    this.apiService.handleRequest(htpResponse, httpClient -> {
      final Map<String, Object> response = this.apiService.kongPageAll(httpRequest, httpClient,
        CONSUMERS_PATH2 + username + "/" + dataName, data -> {
          final Map<String, Object> userDataResponse = userDataSetConfig(dataName, data);
          data.clear();
          data.putAll(userDataResponse);
          return true;
        });
      Json.writeJson(htpResponse, response);
    });
  }

  private void userDataNames(final HttpServletResponse httpResponse) {
    final List<Map<String, Object>> rows = new ArrayList<>();
    for (final String pluginName : Arrays.asList("basic-auth", "hmac-auth", "jwt", "key-auth",
      "oauth2")) {
      final Map<String, Object> row = Collections.singletonMap(ApiService.NAME, pluginName);
      rows.add(row);
    }
    this.apiService.writeJsonRows(httpResponse, rows);
  }

  private void userDataSchemaGet(final HttpServletResponse response, final String dataName)
    throws IOException {
    final Map<String, Object> userDataSchema = getUserDataSchema(dataName);
    if (userDataSchema == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    } else {
      Json.writeJson(response, userDataSchema);
    }
  }

  private Map<String, Object> userDataSetConfig(final String dataName,
    final Map<String, Object> data) {
    if (data != null && !data.containsKey(ERROR)) {
      final String primaryKeyName = getUserDataKey(dataName);
      final String key = (String)data.get(primaryKeyName);
      final Map<String, Object> config = new TreeMap<>(data);
      final Map<String, Object> userData = new LinkedHashMap<>();
      for (final String fieldName : Arrays.asList(ID, CONSUMER_ID, CREATED_AT)) {
        final Object value = config.remove(fieldName);
        userData.put(fieldName, value);
      }

      userData.put(CONFIG, config);
      userData.put(NAME, dataName);
      userData.put(LABEL, key);
      return userData;
    } else {
      return data;
    }
  }

  private void userDataUpdate(final HttpServletRequest httpRequest,
    final HttpServletResponse httpResponse, final String username, final String dataName,
    final String id) {
    final Map<String, Object> userData = getUserData(httpRequest, httpResponse);
    if (userData != null) {

      this.apiService.handleRequest(httpResponse, httpClient -> {
        final String path = CONSUMERS_PATH2 + username + "/" + dataName + "/" + id;
        final Map<String, Object> apiResponse = httpClient.patch(path, userData);
        final Map<String, Object> userDataResponse = userDataSetConfig(dataName, apiResponse);
        Json.writeJson(httpResponse, userDataResponse);

      });
    }
  }

  private void userGroupAdd(final HttpServletRequest request, final HttpServletResponse response,
    final String username) {
    final Map<String, Object> requestData = Json.readJsonMap(request);
    if (requestData == null) {
      this.apiService.sendError(response, HttpServletResponse.SC_BAD_REQUEST);
    } else {
      final String groupName = (String)requestData.get(ApiService.GROUP);
      this.apiService.groupUserAdd(response, username, groupName);
    }
  }

  private void userGroupDelete(final HttpServletResponse response, final String username,
    final String groupId) {
    final String groupPath = CONSUMERS_PATH2 + username + "/acls/" + groupId;
    this.apiService.handleDelete(response, groupPath);
  }

  private void userGroupList(final HttpServletRequest request, final HttpServletResponse response,
    final String username) {
    final String groupsPath = CONSUMERS_PATH2 + username + "/acls";
    this.apiService.handleListAll(request, response, groupsPath);
  }

  private void userPluginList(final HttpServletRequest request, final HttpServletResponse response,
    final String username) {
    this.apiService.pluginList(request, response, "/plugins?consumer_id=" + username, null);
  }

  private void userRecordAdd(final HttpServletRequest request, final HttpServletResponse response) {
    this.apiService.handleAdd(request, response, CONSUMERS_PATH2, CONSUMER_FIELD_NAMES);
  }

  private void userRecordDelete(final HttpServletResponse response, final String username) {
    final String consumerPath = CONSUMERS_PATH2 + username;
    this.apiService.handleDelete(response, consumerPath);
  }

  private void userRecordGet(final HttpServletResponse response, final String username) {
    final String consumerPath = CONSUMERS_PATH2 + username;
    this.apiService.handleGet(response, consumerPath);
  }

  private void userRecordList(final HttpServletRequest request,
    final HttpServletResponse response) {
    this.apiService.handleListAll(request, response, "/consumers");
  }

  private void userRecordUpdate(final HttpServletRequest request,
    final HttpServletResponse response, final String username) {
    final String path = CONSUMERS_PATH2 + username;
    this.apiService.handleUpdatePatch(request, response, path, CONSUMER_FIELD_NAMES);
  }

}
