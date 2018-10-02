package ca.bc.gov.gwa.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.conn.HttpHostConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.gwa.http.HttpStatusException;
import ca.bc.gov.gwa.http.JsonHttpClient;
import ca.bc.gov.gwa.http.JsonHttpConsumer;
import ca.bc.gov.gwa.http.JsonHttpFunction;
import ca.bc.gov.gwa.servlet.admin.ImportServlet;
import ca.bc.gov.gwa.servlet.authentication.GitHubPrincipal;
import ca.bc.gov.gwa.servlet.authentication.SiteminderPrincipal;
import ca.bc.gov.gwa.util.Json;
import ca.bc.gov.gwa.util.LruMap;

@WebListener
public class ApiService implements ServletContextListener, GwaConstants {

  static String API_SERVICE_NAME = ApiService.class.getName();

  public static final Logger LOG = LoggerFactory.getLogger(ApiService.class);

  @SuppressWarnings("unchecked")
  private static final Comparator<Map<String, Object>> PLUGIN_COMPARATOR = (row1, row2) -> {
    for (final String fieldName : API_SORT_FIELDS) {
      final Comparable<Object> value1 = (Comparable<Object>)row1.get(fieldName);
      final Object value2 = row2.get(fieldName);
      if (value1 == null) {
        if (value2 != null) {
          return 1;
        }
      } else if (value2 == null) {
        return -1;
      } else {
        final int compare = value1.compareTo(value2);
        if (compare != 0) {
          return compare;
        }
      }
    }
    return 0;
  };

  private static final List<String> DEVELOPER_KEY_API_FIELD_NAMES = Arrays.asList(NAME, HOSTS,
    "uris");

  private static final List<String> DEV_API_KEY_FIELD_NAMES = Arrays.asList(ID, KEY, CREATED_AT);

  public static ApiService get(final ServletContext servletContext) {
    return (ApiService)servletContext.getAttribute(API_SERVICE_NAME);
  }

  private int apiKeyExpiryDays = 90;

  private final Map<String, String> apiNameById = new LruMap<>(1000);

  private boolean caching;

  private final Map<String, Object> config = new HashMap<>();

  private String gitHubAccessToken;

  private String gitHubClientId;

  private String gitHubClientSecret;

  private String gitHubOrganizationName;

  private String gitHubOrganizationRole;

  private String kongAdminPassword = null;

  private String kongAdminUrl = "http://localhost:8001";

  private String kongAdminUsername = null;

  private final Map<String, Map<String, Map<String, Object>>> objectByTypeAndId = new HashMap<>();

  private final Map<String, String> usernameByConsumerId = new LruMap<>(1000);

  private String version;

  private ScheduledExecutorService scheduler;

  private boolean useEndpoints = true;

  private void addData(final Map<String, Object> data, final Map<String, Object> requestData,
    final List<String> fieldNames) {
    for (final String key : fieldNames) {
      if (requestData.containsKey(key)) {
        final Object value = requestData.get(key);
        if (value == null) {
          data.remove(key);
        } else {
          data.put(key, value);
        }
      }
    }
  }

  public void addGitHubDeveloperGroup(final BasePrincipal userPrincipal) throws IOException {
    addUserToGroup(userPrincipal, GitHubPrincipal.DEVELOPER_ROLE);
  }

  @SuppressWarnings("unchecked")
  private void addLimits(final Map<String, Object> limits, final Map<String, Object> plugin) {
    final Map<String, Object> config = (Map<String, Object>)plugin.getOrDefault("config",
      Collections.emptyMap());
    for (final String fieldName : Arrays.asList("year", "month", "day", "hour", "minute",
      "second")) {
      final Object fieldValue = config.get(fieldName);
      if (fieldValue != null) {
        limits.put(fieldName, fieldValue);
      }
    }
  }

  public void addUserToGroup(final BasePrincipal userPrincipal, final String groupName)
    throws IOException {
    try (
      JsonHttpClient httpClient = newKongClient()) {
      final Set<String> groups = userGroups(httpClient, userPrincipal);
      if (!groups.contains(groupName)) {
        final Map<String, Object> aclRequest = Collections.singletonMap(GROUP, groupName);
        final String userName = userPrincipal.getName();
        final String aclPath = ApiService.CONSUMERS_PATH2 + userName + ApiService.ACLS_PATH;
        httpClient.post(aclPath, aclRequest);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public void apiAddPlugin(final JsonHttpClient client, final Map<String, Object> requestData,
    final String apiId, final String pluginName, final List<String> fieldNames,
    final Map<String, Object> defaultConfig, final boolean ignoreDisabled) {
    final Map<String, Object> pluginAdd = pluginGet(requestData, pluginName);
    try {
      Map<String, Object> config;
      if (pluginAdd == null) {
        if (ignoreDisabled || defaultConfig == null) {
          return;
        } else {
          config = defaultConfig;
        }
      } else {
        if (ignoreDisabled && Boolean.TRUE != pluginAdd.get(ENABLED)) {
          return;
        } else {
          final Map<String, Object> configAdd = (Map<String, Object>)pluginAdd.get(CONFIG);
          config = new LinkedHashMap<>();
          addData(config, configAdd, fieldNames);
        }
      }

      final String path = APIS_PATH2 + apiId + PLUGINS_PATH;
      final Map<String, Object> plugin = new LinkedHashMap<>();
      plugin.put(NAME, pluginName);
      plugin.put(CONFIG, config);
      client.post(path, plugin);
    } catch (final Exception e) {
      logError("Error adding api " + apiId + " plugin " + pluginName + ":\n" + pluginAdd, e);
    }
  }

  public void apiGet(final HttpServletResponse httpResponse, final String apiName) {
    final Map<String, Object> api = apiGet(apiName);
    if (api == null) {
      sendError(httpResponse, HttpServletResponse.SC_NOT_FOUND);
    } else {
      Json.writeJson(httpResponse, api);
    }
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> apiGet(final String apiName) {
    try {
      return getCachedObject("api", apiName, httpClient -> {
        final Map<String, Object> apiResponse = httpClient.get(APIS_PATH2 + apiName);
        final String apiId = (String)apiResponse.get(ID);
        if (apiId == null) {
          return null;
        } else {
          fixHostsMapToList(apiResponse);
          final Map<String, Object> pluginsResponse = httpClient
            .get(APIS_PATH2 + apiId + PLUGINS_PATH);
          final List<Map<String, Object>> plugins = getList(pluginsResponse, DATA);
          final Map<String, Map<String, Object>> pluginByName = new TreeMap<>();
          for (final Map<String, Object> plugin : plugins) {
            if (plugin.get(CONSUMER_ID) == null) {
              final String name = (String)plugin.get(NAME);
              final Map<String, Object> config = (Map<String, Object>)plugin.get(CONFIG);
              fixMapToList(config);
              pluginByName.put(name, plugin);
            }
          }
          apiResponse.put(PLUGINS, pluginByName);
          return apiResponse;
        }
      });
    } catch (final Exception e) {
      LOG.debug("Unable to get API:" + apiName, e);
      throw new IllegalStateException("Error getting API: " + apiName, e);
    }
  }

  public String apiGetId(final String apiName) {
    final Map<String, Object> api = apiGet(apiName);
    if (api == null) {
      return null;
    } else {
      return (String)api.get("id");
    }
  }

  private String apiGetName(final JsonHttpClient httpClient, final String apiId) {
    String apiName = this.apiNameById.get(apiId);
    if (apiName == null) {
      apiName = apiId;
      try {
        final Map<String, Object> api = httpClient.get(APIS_PATH2 + apiId);
        apiName = (String)api.get(NAME);
        if (apiName != null && this.caching) {
          this.apiNameById.put(apiId, apiName);
        }
      } catch (final Exception e) {
        LOG.error("Unable to get API:" + apiId, e);
        return "";
      }
    }
    return apiName;
  }

  public void apiGroupUserAdd(final HttpServletResponse httpResponse, final String apiName,
    final String groupName, final String userName) {
    final boolean hasGroup = endpointHasGroupEdit(apiName, groupName);
    if (hasGroup) {
      groupUserAdd(httpResponse, userName, groupName);
    } else {
      sendError(httpResponse, HttpServletResponse.SC_NOT_FOUND);
    }
  }

  public void apiGroupUserDelete(final HttpServletResponse httpResponse, final String apiName,
    final String groupName, final String userName) {
    final boolean hasGroup = endpointHasGroupEdit(apiName, groupName);
    if (hasGroup) {
      groupUserDelete(httpResponse, userName, groupName);
    } else {
      writeJsonResponse(httpResponse, DELETED);
    }
  }

  public void apiUpdate(final HttpServletRequest httpRequest,
    final HttpServletResponse httpResponse, final String apiId) {
    final Map<String, Object> requestData = Json.readJsonMap(httpRequest);
    if (requestData == null) {
      sendError(httpResponse, HttpServletResponse.SC_BAD_REQUEST);
    } else {
      handleRequest(httpResponse, httpClient -> {
        String oldApiName = this.apiNameById.get(apiId);
        if (oldApiName == null) {
          oldApiName = (String)requestData.get(NAME);
        }
        try {
          final Map<String, Object> apiRequest = getMap(requestData, APIS_FIELD_NAMES);
          final Map<String, Object> apiResponse = httpClient.patch(APIS_PATH2 + apiId, apiRequest);
          if (apiId != null) {
            if (this.useEndpoints) {
              if (pluginGet(requestData, BCGOV_GWA_ENDPOINT) == null) {
                apiAddPlugin(httpClient, requestData, apiId, BCGOV_GWA_ENDPOINT,
                  ENDPOINT_FIELD_NAMES, ENDPOINT_DEFAULT_CONFIG, false);
              } else {
                apiUpdatePlugin(httpClient, requestData, apiId, BCGOV_GWA_ENDPOINT,
                  ENDPOINT_FIELD_NAMES);
              }
            }
            apiUpdatePlugin(httpClient, requestData, apiId, KEY_AUTH, KEY_AUTH_FIELD_NAMES);
            apiUpdatePlugin(httpClient, requestData, apiId, ACL, ACL_FIELD_NAMES);
          }
          Json.writeJson(httpResponse, apiResponse);
        } finally {
          clearCachedObject("api", oldApiName);
        }
      });
    }
  }

  @SuppressWarnings("unchecked")
  private void apiUpdatePlugin(final JsonHttpClient client, final Map<String, Object> requestData,
    final String apiId, final String pluginName, final List<String> fieldNames) throws IOException {
    final Map<String, Object> pluginUpdate = pluginGet(requestData, pluginName);
    if (pluginUpdate != null) {

      final Map<String, Object> plugin = new LinkedHashMap<>();
      final String pluginId = (String)pluginUpdate.get(ID);

      final boolean enabled = Boolean.TRUE == pluginUpdate.get(ENABLED);
      plugin.put(ENABLED, enabled);

      plugin.put(NAME, pluginName);

      final Map<String, Object> configAdd = (Map<String, Object>)pluginUpdate.get(CONFIG);
      final Map<String, Object> config = new LinkedHashMap<>();
      addData(config, configAdd, fieldNames);
      plugin.put(CONFIG, config);

      if (pluginId == null) {
        if (enabled) {
          final String path = APIS_PATH2 + apiId + PLUGINS_PATH;
          client.post(path, plugin);
        }
      } else {
        if (enabled) {
          final String path = APIS_PATH2 + apiId + PLUGINS_PATH2 + pluginId;
          client.patch(path, plugin);
        } else {
          final String path = APIS_PATH2 + apiId + PLUGINS_PATH2 + pluginId;
          client.delete(path);
        }
      }
    }

  }

  private void cleanExpiringKeys() {
    try (
      JsonHttpClient httpClient = newKongClient()) {
      final Map<String, String> usernameById = new HashMap<>();
      final Calendar oldestDate = new GregorianCalendar();
      oldestDate.add(Calendar.DAY_OF_MONTH, -this.apiKeyExpiryDays);
      final long oldestTime = oldestDate.getTimeInMillis();

      oldestDate.add(Calendar.DAY_OF_MONTH, (int)(this.apiKeyExpiryDays * 0.1));
      final long createNewTime = oldestDate.getTimeInMillis();

      final Map<String, Long> keyAgeByConsumerId = new HashMap<>();
      final Set<String> createKeyConsumerIds = new HashSet<>();
      try {
        String urlString = this.kongAdminUrl + "/key-auths";
        do {
          final Map<String, Object> kongResponse = httpClient.getByUrl(urlString);
          final List<Map<String, Object>> keyAuths = getList(kongResponse, DATA);
          for (final Map<String, Object> keyAuth : keyAuths) {
            final String consumerId = (String)keyAuth.get(CONSUMER_ID);
            final long createdAt = ((Number)keyAuth.get(CREATED_AT)).longValue();
            final long mostRecentKeyAge = keyAgeByConsumerId.getOrDefault(consumerId, 0L);
            if (createdAt > mostRecentKeyAge) {
              keyAgeByConsumerId.put(consumerId, createdAt);
            }
            if (createdAt < createNewTime) {
              final String id = (String)keyAuth.get(ID);
              String username = usernameById.get(consumerId);
              if (username == null) {
                username = userGetUsername(httpClient, consumerId);
                usernameById.put(consumerId, username);
              }
              if (username.startsWith("github_")) {
                createKeyConsumerIds.add(consumerId);

                if (createdAt < oldestTime) {
                  // DELETE expired key
                  final String deletePath = CONSUMERS_PATH2 + consumerId + "/key-auth/" + id;
                  try {
                    httpClient.delete(deletePath);
                  } catch (final Exception e) {
                    LOG.error("Cannot delete:" + deletePath, e);
                  }
                }
              }
            }
          }
          urlString = getNextUrl(kongResponse);
        } while (urlString != null);
      } catch (final NoSuchElementException e) {
      }

      for (final String consumerId : createKeyConsumerIds) {
        final long mostRecentKeyAge = keyAgeByConsumerId.getOrDefault(consumerId, Long.MAX_VALUE);
        if (mostRecentKeyAge < createNewTime) {

          // Create a new key
          final String keyAuthPath = CONSUMERS_PATH2 + consumerId + "/" + KEY_AUTH;
          try {
            httpClient.post(keyAuthPath, Collections.emptyMap());

          } catch (final Exception e) {
            LOG.error("Cannot created:" + keyAuthPath, e);
          }
        }
      }
    } catch (final HttpStatusException e) {
      final int statusCode = e.getCode();
      if (statusCode == 503) {
        LOG.error(KONG_SERVER_NOT_AVAILABLE, e);
      } else {
        final String body = e.getBody();
        LOG.error(e.toString() + "\n" + body, e);
      }
    } catch (final HttpHostConnectException e) {
      LOG.error("Kong not available", e);
    } catch (final Exception e) {
      LOG.error("Unexpected kong error", e);
    }
  }

  public void clearCachedObject(final String type, final String id) {
    final Map<String, Map<String, Object>> objectById = this.objectByTypeAndId.get(type);
    if (objectById != null) {
      objectById.remove(id);
    }
  }

  /**
   * True if list2 contains any value from list 1.
   *
   * @param list1
   * @param list2
   * @return
   */
  private boolean containsAny(final List<String> list1, final Set<String> list2) {
    if (list1 != null) {
      for (final String value : list1) {
        if (list2.contains(value)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public void contextDestroyed(final ServletContextEvent event) {
    this.apiNameById.clear();
    this.scheduler.shutdownNow();
  }

  @Override
  public void contextInitialized(final ServletContextEvent event) {
    try {
      readProperties();
      this.kongAdminUrl = getConfig("gwaKongAdminUrl", this.kongAdminUrl);
      this.kongAdminUsername = getConfig("gwaKongAdminUsername", this.kongAdminUsername);
      this.kongAdminPassword = getConfig("gwaKongAdminPassword", this.kongAdminPassword);
      this.gitHubOrganizationName = getConfig("gwaGitHubOrganization", "gwa-qa");
      this.gitHubOrganizationRole = "github_" + this.gitHubOrganizationName.toLowerCase();
      this.gitHubAccessToken = getConfig("gwaGitHubAccessToken");
      this.gitHubClientId = getConfig("gwaGitHubClientId");
      this.gitHubClientSecret = getConfig("gwaGitHubClientSecret");
      this.apiKeyExpiryDays = Integer.parseInt(getConfig("gwaApiKeyExpiryDays", "90"));
      this.useEndpoints = !"false".equals(getConfig("gwaUseEndpoints", "true"));

      if (this.gitHubClientId == null || this.gitHubClientSecret == null) {
        LoggerFactory.getLogger(getClass())
          .error("Missing gitHubClientId or gitHubClientSecret configuration");
      }
      final ServletContext servletContext = event.getServletContext();
      servletContext.setAttribute(API_SERVICE_NAME, this);
    } catch (final Exception e) {
      LOG.error("Unable to initialize service", e);
      throw e;
    }
    {
      this.scheduler = Executors.newScheduledThreadPool(1);
      this.scheduler.schedule(this::cleanExpiringKeys, 10, TimeUnit.SECONDS);

      final LocalDateTime localNow = LocalDateTime.now();
      final ZoneId currentZone = ZoneId.systemDefault();
      final ZonedDateTime zonedNow = ZonedDateTime.of(localNow, currentZone);
      final ZonedDateTime zonedNextTarget = zonedNow.withHour(5).withMinute(0).withSecond(0);

      final Duration duration = Duration.between(zonedNow, zonedNextTarget);
      final long delaySeconds = duration.getSeconds();
      this.scheduler.scheduleAtFixedRate(this::cleanExpiringKeys, delaySeconds, 24 * 60 * 60,
        TimeUnit.SECONDS);
    }
  }

  /**
   * Delete all existing API keys and create a new one.
   *
   * @param httpRequest
   * @param httpResponse
   *
   */
  public void developerApiKeyAdd(final HttpServletRequest httpRequest,
    final HttpServletResponse httpResponse) {
    handleRequest(httpResponse, httpClient -> {
      final String userId = httpRequest.getRemoteUser();
      final String keyAuthPath = CONSUMERS_PATH2 + userId + "/" + KEY_AUTH;
      final Map<String, Object> apiKeyResponse = httpClient.post(keyAuthPath,
        Collections.emptyMap());
      developerApiKeyRecordValues(apiKeyResponse);
      Json.writeJson(httpResponse, apiKeyResponse);
    });

  }

  /**
   * Delete all existing API keys and create a new one.
   *
   * @param httpRequest
   * @param httpResponse
   *
   */
  public void developerApiKeyDelete(final HttpServletRequest httpRequest,
    final HttpServletResponse httpResponse, final String apiKey) {
    final String userId = httpRequest.getRemoteUser();
    final String path = CONSUMERS_PATH2 + userId + "/key-auth/" + apiKey;
    handleDelete(httpResponse, path);
  }

  /**
   * Delete all existing API keys and create a new one.
   *
   * @param httpRequest
   * @param httpResponse
   *
   */
  public void developerApiKeyDeleteAll(final HttpServletRequest httpRequest,
    final HttpServletResponse httpResponse) {
    handleRequest(httpResponse, httpClient -> {
      final String userId = httpRequest.getRemoteUser();
      final String keyAuthPath = CONSUMERS_PATH2 + userId + "/" + KEY_AUTH;
      kongPageAll(httpRequest, httpClient, keyAuthPath, apiKey -> {
        final String id = (String)apiKey.get(ID);
        try {
          httpClient.delete(keyAuthPath + "/" + id);
        } catch (final Exception e) {
          final String message = "Unable to delete " + keyAuthPath + "/" + id;
          logError(message, e);
        }
      });
      writeJsonResponse(httpResponse, "deleted");
    });

  }

  public void developerApiKeyList(final HttpServletRequest httpRequest,
    final HttpServletResponse httpResponse) {
    handleRequest(httpResponse, httpClient -> {

      final String username = httpRequest.getRemoteUser();
      final String keyAuthPath = CONSUMERS_PATH2 + username + "/" + KEY_AUTH;
      final Map<String, Object> keyAuthResponse = httpClient.get(keyAuthPath);
      final List<Map<String, Object>> keyAuthList = getList(keyAuthResponse, DATA);

      for (final Map<String, Object> keyAuth : keyAuthList) {
        developerApiKeyRecordValues(keyAuth);
      }
      keyAuthList.sort((a, b) -> {
        final BigDecimal time1 = (BigDecimal)a.get(CREATED_AT);
        final BigDecimal time2 = (BigDecimal)b.get(CREATED_AT);
        final int compare = time1.compareTo(time2);
        return -compare; // Largest first
      });
      Json.writeJson(httpResponse, keyAuthResponse);
    });
  }

  private void developerApiKeyRecordValues(final Map<String, Object> keyAuth) {
    keyAuth.keySet().retainAll(DEV_API_KEY_FIELD_NAMES);
    keyAuth.put("maxAgeDays", this.apiKeyExpiryDays);
  }

  @SuppressWarnings("unchecked")
  public void developerApiList(final HttpServletRequest httpRequest,
    final HttpServletResponse httpResponse) {
    handleRequest(httpResponse, httpClient -> {

      final BasePrincipal userPrincipal = (BasePrincipal)httpRequest.getUserPrincipal();
      final Set<String> groups = userGroups(httpClient, userPrincipal);

      final Map<String, Map<String, Object>> apiByName = new TreeMap<>();

      final LinkedList<String> apiIds = new LinkedList<>();
      final String path = "/plugins?name=acl";
      kongPageAll(httpRequest, httpClient, path, acl -> {
        if (acl.get(CONSUMER_ID) == null) {
          final Map<String, Object> config = (Map<String, Object>)acl.get(CONFIG);
          final List<String> blacklist = getList(config, "blacklist");
          final List<String> whitelist = getList(config, WHITELIST);
          if (containsAny(blacklist, groups)) {
            // Ignore blacklist
          } else if (whitelist.isEmpty() || containsAny(whitelist, groups)) {
            final String apiId = (String)acl.get(API_ID);
            apiIds.add(apiId);
          }
        }
      });

      kongPageAll(httpRequest, httpClient, APIS_PATH, api -> {
        final String apiId = (String)api.get(ID);
        if (apiIds.remove(apiId)) {
          final String name = (String)api.get(NAME);
          final Map<String, Object> devkKeyApi = new LinkedHashMap<>();
          for (final String fieldName : DEVELOPER_KEY_API_FIELD_NAMES) {
            final Object fieldValue = api.get(fieldName);
            if (fieldValue != null) {
              devkKeyApi.put(fieldName, fieldValue);
            }
          }
          apiByName.put(name, devkKeyApi);
          if (apiIds.isEmpty()) {
            throw new NoSuchElementException();
          }
        }
      });

      final Map<String, Object> kongResponse = new LinkedHashMap<>();
      kongResponse.put(DATA, apiByName.values());
      Json.writeJson(httpResponse, kongResponse);
    });
  }

  @SuppressWarnings("unchecked")
  public void developerApiRateLimitGet(final HttpServletRequest httpRequest,
    final HttpServletResponse httpResponse, final String apiName) throws IOException {
    final String apiId = apiGetId(apiName);

    final BasePrincipal principal = (BasePrincipal)httpRequest.getUserPrincipal();
    final String username = principal.getName();
    handleRequest(httpResponse, httpClient -> {
      final String consumerId = userIdGetByUsername(httpClient, username);
      final String consumerPath = APIS_PATH2 + apiName + PLUGINS_PATH
        + "?name=rate-limiting&api_id=" + apiId + "&consumer_id=" + consumerId;
      final Map<String, Object> limits = new LinkedHashMap<>();
      final Map<String, Object> kongResponse = httpClient.get(consumerPath);
      final Number total = (Number)kongResponse.getOrDefault("total", 0);
      if (total.intValue() > 0) {
        final List<Map<String, Object>> plugins = (List<Map<String, Object>>)kongResponse
          .getOrDefault("data", Collections.emptyList());
        for (final Map<String, Object> plugin : plugins) {
          addLimits(limits, plugin);
        }
      } else {
        final Map<String, Object> api = apiGet(apiName);
        final Map<String, Object> plugin = pluginGet(api, "rate-limiting");
        if (plugin != null) {
          addLimits(limits, plugin);
        }
      }

      Json.writeJson(httpResponse, limits);
    });
  }

  public boolean endpointAccessAllowed(final HttpServletRequest httpRequest,
    final HttpServletResponse httpResponse, final List<String> paths) {
    if (paths.isEmpty()) {
      return true;
    } else {
      final SiteminderPrincipal principal = (SiteminderPrincipal)httpRequest.getUserPrincipal();
      if (principal.isUserInRole(ROLE_GWA_ADMIN)) {
        return true;
      } else {
        return endpointAccessAllowedApiOwner(httpResponse, paths, principal);
      }

    }
  }

  @SuppressWarnings("unchecked")
  private boolean endpointAccessAllowedApiOwner(final HttpServletResponse httpResponse,
    final List<String> paths, final SiteminderPrincipal principal) {
    if (this.useEndpoints) {
      final String endpointName = paths.get(0);
      final Map<String, Object> api = apiGet(endpointName);
      if (api != null) {
        final String username = principal.getName();
        final Map<String, Object> endPoint = pluginGet(api, BCGOV_GWA_ENDPOINT);
        if (endPoint != null) {
          final Map<String, Object> config = (Map<String, Object>)endPoint.get(CONFIG);
          final List<String> apiOwners = getList(config, API_OWNERS);
          if (apiOwners.contains(username)) {
            return true;
          }
        }
      }
      sendError(httpResponse, HttpServletResponse.SC_NOT_FOUND);
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public void endpointDelete(final HttpServletRequest httpRequest,
    final HttpServletResponse httpResponse, final String endpointId) {
    handleRequest(httpResponse, httpClient -> {
      final String endpointPath = PLUGINS_PATH + endpointId;
      try {
        final Map<String, Object> endpoint = httpClient.get(endpointPath);
        final String apiId = (String)endpoint.get(API_ID);
        final Map<String, Object> endpointConfig = (Map<String, Object>)endpoint.get(CONFIG);
        final BasePrincipal principal = (BasePrincipal)httpRequest.getUserPrincipal();
        final String userId = principal.getName();
        final String endpointUserId = (String)endpointConfig.get("created_by");
        if (principal.isUserInRole(ROLE_GWA_ADMIN) || userId.equals(endpointUserId)) {
          final String apiDeletePath = APIS_PATH2 + apiId;
          httpClient.delete(apiDeletePath);
          writeJsonResponse(httpResponse, DELETED);
        } else {
          writeJsonError(httpResponse, "You do not have permission to delete endpoint");
        }
      } catch (final HttpStatusException e) {
        if (e.getCode() == 404) {
          writeJsonResponse(httpResponse, DELETED);
        } else {
          throw e;
        }
      }
    });
  }

  public void endpointGroupUserList(final HttpServletRequest httpRequest,
    final HttpServletResponse httpResponse, final String apiName, final String groupName) {
    final boolean hasGroup = endpointHasGroup(apiName, groupName);
    if (hasGroup) {
      final String groupPath = GROUPS_PATH2 + groupName + USERS_PATH;
      groupUserList(httpRequest, httpResponse, groupPath);
    } else {
      sendError(httpResponse, HttpServletResponse.SC_NOT_FOUND);
    }
  }

  /**
   * Check if the endpoint has the group in the api_groups. Ignore groups that
   * start with github_ or idir_.
   *
   * @param apiName
   * @param groupName
   * @return
   *
   */
  @SuppressWarnings("unchecked")
  private boolean endpointHasGroup(final String apiName, final String groupName) {
    try {
      final Map<String, Object> api = apiGet(apiName);
      if (api != null) {
        final Map<String, Object> aclPlugin = pluginGet(api, ACL);
        if (aclPlugin != null) {
          final Map<String, Object> config = (Map<String, Object>)aclPlugin.getOrDefault(CONFIG,
            Collections.emptyMap());
          final List<String> apiGroups = getList(config, WHITELIST);
          return apiGroups.contains(groupName);
        }
      }
      return false;
    } catch (final Exception e) {
      LOG.debug("Unable to find group:" + groupName, e);
      return false;
    }
  }

  /**
   * Check if the endpoint has the group in the api_groups. Ignore groups that
   * start with github_ or idir_.
   *
   * @param apiName
   * @param groupName
   * @return
   *
   */
  private boolean endpointHasGroupEdit(final String apiName, final String groupName) {
    if (endpointHasGroup(apiName, groupName)) {
      return !(groupName.startsWith("github") || groupName.startsWith("idir"));
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public void endpointList(final HttpServletRequest httpRequest,
    final HttpServletResponse httpResponse) {
    handleRequest(httpResponse, httpClient -> {
      final SiteminderPrincipal principal = (SiteminderPrincipal)httpRequest.getUserPrincipal();
      final String path = "/plugins?name=bcgov-gwa-endpoint";
      final Map<String, Object> kongResponse;
      final boolean adminUser = principal.isUserInRole(ROLE_GWA_ADMIN);
      final String username = principal.getName();
      final List<Map<String, Object>> allEndpoints = new ArrayList<>();
      try {
        final StringBuilder urlBuilder = new StringBuilder(this.kongAdminUrl);
        urlBuilder.append(path);
        if (path.indexOf('?') == -1) {
          urlBuilder.append('?');
        } else {
          urlBuilder.append('&');
        }
        final String limit = httpRequest.getParameter("limit");
        if (limit != null) {
          urlBuilder.append("size=");
          urlBuilder.append(limit);
        }

        Predicate<Map<String, Object>> filter = record -> true;
        final String filterFieldName = httpRequest.getParameter("filterFieldName");
        String filterValue = httpRequest.getParameter("filterValue");
        if (filterFieldName != null && filterValue != null) {
          filterValue = filterValue.trim();
          if (filterValue.length() > 0) {
            final String expectedValue = filterValue;
            filter = endpoint -> {
              final Object value = endpoint.get(filterFieldName);
              if (value == null) {
                return false;
              } else if ("hosts".equals(filterFieldName) || "uris".equals(filterFieldName)) {
                final List<?> values = (List<?>)value;
                return values.contains(expectedValue);
              } else {
                return expectedValue.equals(value);
              }
            };
          }
        }
        String urlString = urlBuilder.toString();
        do {
          final Map<String, Object> pageKongResponse = httpClient.getByUrl(urlString);
          final List<Map<String, Object>> pageRows = getList(pageKongResponse, DATA);
          for (final Map<String, Object> plugin : pageRows) {
            final String apiId = (String)plugin.get(API_ID);
            final String apiName = apiGetName(httpClient, apiId);
            final Map<String, Object> api = apiGet(apiName);
            if (api != null) {
              final Map<String, Object> endpoint = new LinkedHashMap<>();
              for (final String fieldName : Arrays.asList(ID, NAME, CREATED_AT, HOSTS, "uris")) {
                final Object values = api.get(fieldName);
                endpoint.put(fieldName, values);
              }
              if (filter.test(endpoint)) {
                if (adminUser) {
                  allEndpoints.add(endpoint);
                } else {
                  final Map<String, Object> config = (Map<String, Object>)plugin.get(CONFIG);
                  final List<String> apiOwners = getList(config, API_OWNERS);
                  if (apiOwners.contains(username)) {
                    allEndpoints.add(endpoint);
                  }
                }
              }
            }

          }

          urlString = getNextUrl(pageKongResponse);
        } while (urlString != null);
      } catch (final NoSuchElementException e) {
      }
      kongResponse = new LinkedHashMap<>();
      final int offsetPage = getPageOffset(httpRequest);
      if (offsetPage > 0) {
        final List<Map<String, Object>> rows = allEndpoints.subList(offsetPage,
          allEndpoints.size());
        kongResponse.put(DATA, rows);
      } else {
        kongResponse.put(DATA, allEndpoints);
      }
      kongResponse.put(TOTAL, allEndpoints.size());
      Json.writeJson(httpResponse, kongResponse);
    });
  }

  @SuppressWarnings("unchecked")
  private boolean endPointSetKongParameters(final HttpServletRequest httpRequest,
    final HttpServletResponse httpResponse, final Map<String, Object> apiRequest,
    final List<Map<String, Object>> pluginRequests) {
    final Map<String, Object> requestData = Json.readJsonMap(httpRequest);
    if (requestData == null) {
      writeJsonError(httpResponse, "Missing JSON request body");
      return false;
    } else {
      addData(apiRequest, requestData, APIS_FIELD_NAMES);
      final Map<String, Map<String, Object>> plugins = (Map<String, Map<String, Object>>)requestData
        .get(PLUGINS);
      for (final Map<String, Object> plugin : plugins.values()) {
        final String pluginName = (String)plugin.get(NAME);
        Map<String, Object> pluginConfig = null;
        if ("rate-limiting".equals(pluginName)) {
          pluginConfig = endPointSetPluginRateLimiting(plugin);
        }
        if (pluginConfig != null) {
          final Map<String, Object> pluginRequest = new LinkedHashMap<>();
          final String id = (String)plugin.get(ID);
          pluginRequest.put(ID, id);
          pluginRequest.put(NAME, pluginName);
          pluginRequest.put(CONFIG, pluginConfig);
          pluginRequests.add(pluginRequest);
        }
      }
      return true;
    }
  }

  protected Map<String, Object> endPointSetPluginRateLimiting(
    final Map<String, Object> pluginUpdate) {
    @SuppressWarnings("unchecked")
    final Map<String, Object> configUpdate = (Map<String, Object>)pluginUpdate.get(CONFIG);

    final Map<String, Object> pluginConfig = new LinkedHashMap<>();
    addData(pluginConfig, configUpdate, ENDPOINT_RATE_LIMIT_FIELD_NAMES);
    pluginConfig.put("limit_by", "consumer");
    return pluginConfig;
  }

  public void endpointUpdate(final HttpServletRequest httpRequest,
    final HttpServletResponse httpResponse) {
    handleRequest(httpResponse, httpClient -> {
      final Map<String, Object> apiRequest = new LinkedHashMap<>();
      final List<Map<String, Object>> pluginRequests = new ArrayList<>();

      if (endPointSetKongParameters(httpRequest, httpResponse, apiRequest, pluginRequests)) {
        final String apiId = (String)apiRequest.remove(ID);
        final String apiPath = APIS_PATH2 + apiId;
        final Map<String, Object> apiResponse = httpClient.patch(apiPath, apiRequest);
        if (apiId != null) {
          for (final Map<String, Object> pluginRequest : pluginRequests) {
            final String pluginId = (String)pluginRequest.remove(ID);
            if (pluginId == null) {
              final String pluginPath = apiPath + PLUGINS_PATH;
              httpClient.post(pluginPath, pluginRequest);
            } else {
              final String pluginPath = apiPath + PLUGINS_PATH2 + pluginId;
              httpClient.patch(pluginPath, pluginRequest);
            }
          }
        }
        Json.writeJson(httpResponse, apiResponse);
      }
    });
  }

  private void fixHostsMapToList(final Map<String, Object> apiResponse) {
    for (final String fieldName : Arrays.asList("uris", HOSTS)) {
      final Object value = apiResponse.get(fieldName);
      if (value instanceof Map) {
        final Map<?, ?> mapValue = (Map<?, ?>)value;
        if (mapValue.isEmpty()) {
          apiResponse.put(fieldName, new ArrayList<>());
        }
      }
    }
  }

  private void fixMapToList(final Map<String, Object> config) {
    for (final Entry<String, Object> entry : config.entrySet()) {
      final Object value = entry.getValue();
      if (value instanceof Map) {
        final Map<?, ?> mapValue = (Map<?, ?>)value;
        if (mapValue.isEmpty()) {
          entry.setValue(new ArrayList<>());
        }
      }
    }
  }

  public int getApiKeyExpiryDays() {
    return this.apiKeyExpiryDays;
  }

  private Map<String, Object> getCachedObject(final String type, final String id,
    final JsonHttpFunction action) throws IOException {
    final Map<String, Map<String, Object>> objectById = getObjectById(type);
    Map<String, Object> object = objectById.get(id);
    if (object == null) {
      try (
        JsonHttpClient httpClient = newKongClient()) {
        object = action.apply(httpClient);
      }
      if (this.caching) {
        objectById.put(id, object);
      }
    }
    return object;
  }

  public String getConfig(final String name) {
    return getConfig(name, null);
  }

  public String getConfig(final String name, final String defaultValue) {
    String value = null;
    value = System.getProperty(name);
    if (value == null) {
      value = (String)this.config.get(name);
    }
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  public String getGitHubAccessToken(final String state, final String code) throws IOException {
    final String accessToken;
    try (
      JsonHttpClient client = new JsonHttpClient("https://github.com")) {
      final Map<String, Object> accessResponse = client
        .get("/login/oauth/access_token?client_id=" + this.gitHubClientId + "&client_secret="
          + this.gitHubClientSecret + "&code=" + code + "&state=" + state);
      accessToken = (String)accessResponse.get("access_token");
    }
    return accessToken;
  }

  public String getGitHubClientId() {
    return this.gitHubClientId;
  }

  public String getGitHubOrganizationRole() {
    return this.gitHubOrganizationRole;
  }

  private String getKongPageUrl(final HttpServletRequest httpRequest, final String path) {
    final StringBuilder url = new StringBuilder(this.kongAdminUrl);
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
    final StringBuilder url = new StringBuilder(this.kongAdminUrl);
    url.append(path);
    return url.toString();
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

  public Map<String, Object> getMap(final Map<String, Object> requestData,
    final List<String> fieldNames) {
    final Map<String, Object> data = new LinkedHashMap<>();
    addData(data, requestData, fieldNames);
    ImportServlet.removeEmptyValues(data);
    return data;
  }

  private String getNextUrl(final Map<String, Object> kongResponse) {
    String urlString = (String)kongResponse.remove(NEXT);
    if (urlString != null && !urlString.startsWith("http")) {
      urlString = this.kongAdminUrl + urlString;
    }
    return urlString;
  }

  private synchronized Map<String, Map<String, Object>> getObjectById(final String type) {
    Map<String, Map<String, Object>> objectById = this.objectByTypeAndId.get(type);
    if (objectById == null) {
      objectById = new HashMap<>();
      this.objectByTypeAndId.put(type, objectById);
    }
    return objectById;
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

  public String getVersion() {
    if (this.version == null) {
      try (
        JsonHttpClient httpClient = newKongClient()) {
        final Map<String, Object> kongResponse = httpClient.get("");
        this.version = (String)kongResponse.get(VERSION);

      } catch (final Exception e) {
        throw new IllegalStateException("Unable to get kong version", e);
      }
    }
    return this.version;
  }

  public void getVersion(final HttpServletResponse httpResponse) {
    final String version = getVersion();
    final Map<String, Object> response = Collections.singletonMap(VERSION, version);
    Json.writeJson(httpResponse, response);
  }

  public void gitHubAddOrganizationMember(final HttpServletRequest request) throws IOException {
    final Principal userPrincipal = request.getUserPrincipal();
    if (userPrincipal instanceof GitHubPrincipal) {
      final GitHubPrincipal gitHubPrincipal = (GitHubPrincipal)userPrincipal;
      final String username = gitHubPrincipal.getLogin();
      try (
        JsonHttpClient client = new JsonHttpClient("https://api.github.com")) {
        final String path = "/orgs/" + this.gitHubOrganizationName + "/memberships/" + username
          + "?access_token=" + this.gitHubAccessToken;
        client.put(path);
        addGitHubDeveloperGroup(gitHubPrincipal);
        gitHubPrincipal.addDeveloperRole();
      }
    }
  }

  public void groupUserAdd(final HttpServletResponse httpResponse, final String username,
    final String groupName) {
    handleRequest(httpResponse, httpClient -> {
      final Map<String, Object> aclRequest = Collections.singletonMap(GROUP, groupName);

      final String aclPath = CONSUMERS_PATH2 + username + ACLS_PATH;
      Map<String, Object> apiResponse = Collections.emptyMap();
      try {
        apiResponse = httpClient.post(aclPath, aclRequest);
      } catch (final HttpStatusException e) {
        LOG.debug("Error adding group", e);
        if (e.getCode() == 404) {
          final Map<String, Object> consumer = Collections.singletonMap(USERNAME, username);
          httpClient.post(CONSUMERS_PATH, consumer);
          apiResponse = httpClient.post(aclPath, aclRequest);
        }
      }
      Json.writeJson(httpResponse, apiResponse);
    });
  }

  public void groupUserDelete(final HttpServletResponse httpResponse, final String username,
    final String groupName) {
    final String path = CONSUMERS_PATH2 + username + "/acls/" + groupName;
    handleDelete(httpResponse, path);
  }

  public void groupUserList(final HttpServletRequest httpRequest,
    final HttpServletResponse httpResponse, final String path) {
    handleRequest(httpResponse, httpClient -> {
      final Predicate<Map<String, Object>> action = acl -> {
        final String consumerId = (String)acl.get(CONSUMER_ID);
        final String username = userGetUsername(httpClient, consumerId);
        acl.put(USERNAME, username);
        return true;
      };
      final Map<String, Object> kongResponse = kongPageAll(httpRequest, httpClient, path, action);
      Json.writeJson(httpResponse, kongResponse);
    });
  }

  public void handleAdd(final HttpServletRequest httpRequest,
    final HttpServletResponse httpResponse, final String path) {
    final Map<String, Object> requestData = Json.readJsonMap(httpRequest);
    if (requestData == null) {
      sendError(httpResponse, HttpServletResponse.SC_BAD_REQUEST);
    } else {
      ImportServlet.removeEmptyValues(requestData);
      handleRequest(httpResponse, httpClient -> {
        final Map<String, Object> apiResponse = httpClient.post(path, requestData);
        Json.writeJson(httpResponse, apiResponse);
      });
    }
  }

  public void handleAdd(final HttpServletRequest httpRequest,
    final HttpServletResponse httpResponse, final String path, final List<String> fieldNames) {
    final Map<String, Object> requestData = Json.readJsonMap(httpRequest);
    if (requestData == null) {
      sendError(httpResponse, HttpServletResponse.SC_BAD_REQUEST);
    } else {
      handleRequest(httpResponse, httpClient -> {
        final Map<String, Object> insertData = getMap(requestData, fieldNames);
        final Map<String, Object> apiResponse = httpClient.post(path, insertData);
        Json.writeJson(httpResponse, apiResponse);
      });
    }
  }

  public void handleDelete(final HttpServletResponse httpResponse, final String path) {
    handleRequest(httpResponse, httpClient -> {
      httpClient.delete(path);
      writeJsonResponse(httpResponse, DELETED);
    });
  }

  public void handleGet(final HttpServletResponse httpResponse, final String path) {
    handleRequest(httpResponse, httpClient -> {
      final Map<String, Object> kongResponse = httpClient.get(path);
      Json.writeJson(httpResponse, kongResponse);
    });
  }

  public void handleListAll(final HttpServletRequest httpRequest,
    final HttpServletResponse httpResponse, final String path) {
    handleRequest(httpResponse, httpClient -> {
      final Map<String, Object> response = kongPageAll(httpRequest, httpClient, path);
      Json.writeJson(httpResponse, response);
    });
  }

  public void handleListAll(final HttpServletRequest httpRequest,
    final HttpServletResponse httpResponse, final String path,
    final Predicate<Map<String, Object>> filter) {
    handleRequest(httpResponse, httpClient -> {
      final Map<String, Object> response = kongPageAll(httpRequest, httpClient, path, filter);
      Json.writeJson(httpResponse, response);
    });
  }

  public void handleRequest(final HttpServletResponse httpResponse, final JsonHttpConsumer action) {
    final JsonHttpFunction function = action;
    handleRequest(httpResponse, function);
  }

  public Map<String, Object> handleRequest(final HttpServletResponse httpResponse,
    final JsonHttpFunction action) {
    try (
      JsonHttpClient httpClient = newKongClient()) {
      return action.apply(httpClient);
    } catch (final HttpStatusException e) {
      final int statusCode = e.getCode();
      if (statusCode == 503) {
        writeJsonError(httpResponse, KONG_SERVER_NOT_AVAILABLE, e);
      } else {
        final String body = e.getBody();
        LOG.error(e.toString() + "\n" + body, e);
        if (statusCode == 400) {
          writeJsonError(httpResponse, KONG_SERVER_RETURNED_AN_ERROR, body);
        } else {
          writeJsonError(httpResponse, KONG_SERVER_RETURNED_AN_ERROR);
        }
      }
    } catch (final HttpHostConnectException e) {
      LOG.debug("Kong not available", e);
      writeJsonError(httpResponse, KONG_SERVER_NOT_AVAILABLE);
    } catch (final Exception e) {
      LOG.debug("Unexpected kong error", e);
      writeJsonError(httpResponse, UNKNOWN_APPLICATION_ERROR, e);
    }
    return null;
  }

  public void handleUpdatePatch(final HttpServletRequest httpRequest,
    final HttpServletResponse httpResponse, final String path, final List<String> fieldNames) {
    handleRequest(httpResponse, httpClient -> {

      final Map<String, Object> requestData = Json.readJsonMap(httpRequest);
      if (requestData == null) {
        sendError(httpResponse, HttpServletResponse.SC_BAD_REQUEST);
      } else {
        final Map<String, Object> updateData = getMap(requestData, fieldNames);
        httpClient.patch(path, updateData);
        writeJsonResponse(httpResponse, UPDATED);
      }
    });
  }

  public Map<String, Object> kongGet(final JsonHttpClient httpClient, final String path)
    throws IOException {
    try {
      return httpClient.get(path);
    } catch (final HttpStatusException e) {
      if (e.getCode() == 404) {
        return null;
      } else {
        throw e;
      }
    }
  }

  protected Map<String, Object> kongPage(final HttpServletRequest httpRequest,
    final JsonHttpClient httpClient, final String path) throws IOException {
    int offsetPage = getPageOffset(httpRequest);
    String urlString = getKongPageUrl(httpRequest, path);
    Map<String, Object> kongResponse;
    do {
      kongResponse = httpClient.getByUrl(urlString);
      urlString = getNextUrl(kongResponse);
    } while (urlString != null && offsetPage-- > 0);
    return kongResponse;
  }

  public Map<String, Object> kongPageAll(final HttpServletRequest httpRequest,
    final JsonHttpClient httpClient, final String path) throws IOException {
    final List<Map<String, Object>> rows = new ArrayList<>();

    kongPageAll(httpRequest, httpClient, path, (Consumer<Map<String, Object>>)rows::add);

    return newResponseRows(rows);
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

  private void logError(final String message, final Throwable e) {
    if (e instanceof HttpStatusException) {
      final HttpStatusException statusE = (HttpStatusException)e;
      if (statusE.getCode() == 503) {
        LOG.error("{}\nKong not available", message);
      } else {
        final String logMessage = message + "\n" + e.toString() + "\n" + statusE.getBody();
        LOG.error(logMessage, statusE);
      }
    } else if (e instanceof HttpHostConnectException) {
      LOG.error("{}\nKong not available", message);
    } else {
      final String logMessage = message + "\n" + e.getMessage();
      LOG.error(logMessage, e);
    }
  }

  public JsonHttpClient newKongClient() {
    return new JsonHttpClient(this.kongAdminUrl, this.kongAdminUsername, this.kongAdminPassword);
  }

  private Map<String, Object> newResponseRows(final List<Map<String, Object>> rows) {
    final Map<String, Object> response = new LinkedHashMap<>();
    response.put(DATA, rows);
    response.put(TOTAL, rows.size());
    return response;
  }

  public void pluginAddData(final JsonHttpClient httpClient, final Map<String, Object> plugin) {
    final String apiId = (String)plugin.get(API_ID);
    final String apiName = apiGetName(httpClient, apiId);
    plugin.put(API_NAME, apiName);

    final String consumerId = (String)plugin.get(CONSUMER_ID);
    final String username = userGetUsername(httpClient, consumerId);
    plugin.put(USER_USERNAME, username);
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> pluginGet(final Map<String, Object> api, final String pluginName) {
    final Map<String, Map<String, Object>> plugins = (Map<String, Map<String, Object>>)api
      .get(PLUGINS);
    if (plugins == null) {
      return null;
    } else {
      return plugins.get(pluginName);
    }
  }

  public void pluginList(final HttpServletRequest httpRequest,
    final HttpServletResponse httpResponse) {
    handleRequest(httpResponse, httpClient -> {
      final String path = PLUGINS_PATH;
      final Map<String, Object> kongResponse = kongPage(httpRequest, httpClient, path);
      pluginListAddData(httpResponse, httpClient, kongResponse);
    });
  }

  public void pluginList(final HttpServletRequest httpRequest,
    final HttpServletResponse httpResponse, final String path,
    final Predicate<Map<String, Object>> filter) {
    handleRequest(httpResponse, httpClient -> {
      final Map<String, Object> kongResponse = kongPageAll(httpRequest, httpClient, path, filter);
      pluginListAddData(httpResponse, httpClient, kongResponse);
      final List<Map<String, Object>> rows = getList(kongResponse, DATA);
      rows.sort(PLUGIN_COMPARATOR);
    });
  }

  public void pluginListAddData(final HttpServletResponse httpResponse,
    final JsonHttpClient httpClient, final Map<String, Object> kongResponse) {
    final List<Map<String, Object>> data = getList(kongResponse, DATA);
    for (final Map<String, Object> plugin : data) {
      pluginAddData(httpClient, plugin);
    }
    Json.writeJson(httpResponse, kongResponse);
  }

  private void readProperties() {
    final String catalinaBase = System.getProperty("catalina.base");
    for (final String dir : Arrays.asList(catalinaBase, ".", "..", "/apps")) {
      final String fileName = dir + "/config/gwa.properties";
      final File propertiesFile = new File(fileName);
      try {
        if (propertiesFile.exists()) {
          final Properties properties = new Properties();
          try (
            FileInputStream in = new FileInputStream(propertiesFile)) {
            properties.load(in);
            final Enumeration<?> propertyNames = properties.propertyNames();
            while (propertyNames.hasMoreElements()) {
              final String propertyName = (String)propertyNames.nextElement();
              final String value = properties.getProperty(propertyName);
              this.config.put(propertyName, value);
            }
          }
          return;
        }
      } catch (final Exception e) {
        LOG.error("Unable to read config from: " + propertiesFile, e);
      }
    }
  }

  public void sendError(final HttpServletResponse response, final int statusCode) {
    try {
      response.sendError(statusCode);
    } catch (final IOException e) {
      LOG.debug("Unable to send status:" + statusCode, e);
    }
  }

  public void setCaching(final boolean caching) {
    this.caching = caching;
  }

  private Map<String, Object> userGetByCustomId(final JsonHttpClient httpClient,
    final String customId, final boolean createMissing) throws IOException {
    final String customIdFilter = "/consumers/?custom_id=" + customId;

    final Map<String, Object> consumerResponse = httpClient.get(customIdFilter);
    final List<Map<String, Object>> consumers = getList(consumerResponse, DATA);
    if (consumers.isEmpty()) {
      if (createMissing) {
        Map<String, Object> consumer = new HashMap<>();
        consumer.put(CUSTOM_ID, customId);
        consumer = httpClient.put(CONSUMERS_PATH, consumer);
        return consumer;
      } else {
        return new LinkedHashMap<>();
      }
    } else {
      return consumers.get(0);
    }
  }

  public Map<String, Object> userGetByUsername(final JsonHttpClient httpClient,
    final String username, final boolean createMissing) throws IOException {
    final String usernameFilter = "/consumers/" + username;
    try {
      final Map<String, Object> consumer = httpClient.get(usernameFilter);
      return consumer;
    } catch (final HttpStatusException e) {
      if (e.getCode() == 404) {
        if (createMissing) {
          Map<String, Object> consumer = new HashMap<>();
          consumer.put(USERNAME, username);
          consumer = httpClient.put(CONSUMERS_PATH, consumer);
          return consumer;
        } else {
          return new LinkedHashMap<>();
        }
      } else {
        throw e;
      }
    }
  }

  private String userGetUsername(final JsonHttpClient httpClient, final String consumerId) {
    String username = this.usernameByConsumerId.get(consumerId);
    if (username == null) {
      username = consumerId;
      try {
        final Map<String, Object> consumerResponse = httpClient.get(CONSUMERS_PATH2 + consumerId);
        username = (String)consumerResponse.get(USERNAME);
        if (username != null) {
          this.usernameByConsumerId.put(consumerId, username);
        }
      } catch (final Exception e) {
        LOG.debug("Unable to find user:" + consumerId, e);
        return null;
      }
    }
    return username;
  }

  public Set<String> userGroups(final JsonHttpClient httpClient, final BasePrincipal userPrincipal)
    throws IOException {
    final String customId = userPrincipal.getId();
    final String username = userPrincipal.getName();
    return userGroups(httpClient, customId, username);
  }

  public Set<String> userGroups(final JsonHttpClient httpClient, final String customId,
    final String username) throws IOException {
    final Set<String> roles = new TreeSet<>();
    Map<String, Object> consumer;
    if (customId == null) {
      consumer = Collections.emptyMap();
    } else {
      consumer = userGetByCustomId(httpClient, customId, false);
    }
    if (consumer.isEmpty()) {
      consumer = userGetByUsername(httpClient, username, false);
    } else {
      // Update if username changed
      if (!username.equals(consumer.get(USERNAME))) {
        final String id = (String)consumer.get(ID);
        consumer.put(USERNAME, username);
        httpClient.patch(CONSUMERS_PATH2 + id, consumer);
      }
    }
    if (consumer.isEmpty()) {
      // Create if consumer doesn't exist
      consumer = new HashMap<>();
      if (customId != null) {
        consumer.put(CUSTOM_ID, customId);
      }
      consumer.put(USERNAME, username);
      consumer = httpClient.put(CONSUMERS_PATH, consumer);
    } else {
      // Update if custom_id changed
      if (customId != null && !customId.equals(consumer.get(CUSTOM_ID))) {
        final String id = (String)consumer.get(ID);
        consumer.put(CUSTOM_ID, customId);
        httpClient.patch(CONSUMERS_PATH2 + id, consumer);
      }
    }
    final String id = (String)consumer.get(ID);

    this.usernameByConsumerId.put(id, username);
    final String groupsPath = CONSUMERS_PATH2 + id + ACLS_PATH;
    final Map<String, Object> groupsResponse = httpClient.get(groupsPath);
    final List<Map<String, Object>> groupList = getList(groupsResponse, DATA);
    for (final Map<String, Object> groupRecord : groupList) {
      final String group = (String)groupRecord.get(GROUP);
      roles.add(group);
    }
    return roles;
  }

  public Set<String> userGroups(final String customId, final String username) throws IOException {
    try (
      JsonHttpClient httpClient = newKongClient()) {
      return userGroups(httpClient, customId, username);
    }
  }

  public String userIdGetByUsername(final JsonHttpClient httpClient, final String username)
    throws IOException {
    final Map<String, Object> consumer = userGetByUsername(httpClient, username, true);
    return (String)consumer.get(ID);
  }

  public void writeInserted(final HttpServletResponse httpResponse, final String id)
    throws IOException {
    httpResponse.setContentType(APPLICATION_JSON);
    try (
      PrintWriter writer = httpResponse.getWriter()) {
      writer.print("{\"data\":{\"inserted\": true,\"id\":\"");
      writer.print(id);
      writer.println("\"}}");
    }
  }

  public void writeJsonError(final HttpServletResponse httpResponse, final String message) {
    httpResponse.setContentType(APPLICATION_JSON);
    try (
      PrintWriter writer = httpResponse.getWriter()) {
      writer.print("{\"error\":\"");
      writer.print(message);
      writer.println("\"}");
    } catch (final IOException ioe) {
      LOG.debug("Unable to write error: " + message, ioe);
    }
  }

  public void writeJsonError(final HttpServletResponse httpResponse, final String message,
    final String body) {
    httpResponse.setContentType(APPLICATION_JSON);
    final Map<String, Object> error = new LinkedHashMap<>();
    error.put("error", message);
    error.put("body", body);
    Json.writeJson(httpResponse, error);
  }

  public void writeJsonError(final HttpServletResponse httpResponse, final String message,
    final Throwable e) {
    LOG.error(message, e);
    httpResponse.setContentType(APPLICATION_JSON);
    try (
      PrintWriter writer = httpResponse.getWriter()) {
      writer.print("{\"error\":\"");
      writer.print(message);
      writer.println("\"}");
    } catch (final IOException ioe) {
      LOG.debug("Unable to write error: " + message, ioe);
    }
  }

  public void writeJsonResponse(final HttpServletResponse httpResponse, final String field) {
    httpResponse.setContentType(APPLICATION_JSON);
    try (
      PrintWriter writer = httpResponse.getWriter()) {
      writer.print("{\"");
      writer.print(field);
      writer.println("\": true}");
    } catch (final IOException ioe) {
      LOG.debug("Unable to write status: " + field, ioe);
    }
  }

  public void writeJsonRows(final HttpServletResponse httpResponse,
    final List<Map<String, Object>> rows) {
    final Map<String, Object> response = new LinkedHashMap<>();
    response.put(DATA, rows);
    response.put(TOTAL, rows.size());
    Json.writeJson(httpResponse, response);
  }
}
