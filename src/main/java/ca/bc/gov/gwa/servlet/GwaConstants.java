package ca.bc.gov.gwa.servlet;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface GwaConstants {

  static final String OAUTH2 = "oauth2";

  static final String JWT = "jwt";

  static final String HMAC_AUTH = "hmac-auth";

  static final String BASIC_AUTH = "basic-auth";

  static String ACL = "acl";

  static final String GROUPS_PATH = "/groups";

  static final String GROUPS_PATH2 = "/groups/";

  static String ACLS_PATH = "/acls";

  static String API_ID = "service_id";

  static String API_OWNERS = "api_owners";

  static String POST = "POST";

  static String DELETE = "DELETE";

  static String GET = "GET";

  static String API = "api";

  static List<String> PLUGIN_FIELD_NAMES = Arrays.asList("id", "name", "config", "enabled");

  static long serialVersionUID = 1L;

  static String PUT = "PUT";

  static String APIS = "apis";

  static String APIS_PATH = "services";

  static String APIS_PATH2 = "/services/";

  static String ROUTES = "routes";

  static String ROUTES_PATH = "/routes";

  static String ROUTES_PATH2 = "/routes/";

  static String SERVICES = "services";

  static String SERVICE_PATH = "/service";

  static String SERVICES_PATH = "/services";

  static String SERVICES_PATH2 = "/services/";

  static String APPLICATION_JSON = "application/json";

  static String BCGOV_GWA_ENDPOINT = "bcgov-gwa-endpoint";

  static String CONFIG = "config";

  static String CONSUMER_ID = "consumer_id";

  static String CONSUMERS = "consumers";

  static String CONSUMERS_PATH = "/consumers";

  static String CONSUMERS_PATH2 = "/consumers/";

  static String CREATED_AT = "created_at";

  static String CUSTOM_ID = "custom_id";

  static String DATA = "data";

  static String DELETED = "deleted";

  static String ENABLED = "enabled";

  static String ERROR = "error";

  static String GROUP = "group";

  static String GROUPS = "groups";

  static String HOSTS = "hosts";

  static String ID = "id";

  static String INDEX = "index";

  static String ITEM = "item";

  static String KEY = "key";

  static String KEY_AUTH = "key-auth";

  static String KONG_SERVER_NOT_AVAILABLE = "Kong server not available";

  static String KONG_SERVER_RETURNED_AN_ERROR = "Kong server returned an error";

  static String LABEL = "label";

  static String NAME = "name";

  static String NEXT = "next";

  static String PATH = "path";

  static String PLUGINS = "plugins";

  static String PLUGINS_PATH = "/plugins";

  static String PLUGINS_PATH2 = "/plugins/";

  static String RESULT = "result";

  static String ROLE_GWA_ADMIN = "gwa_admin";

  static String ROLE_GWA_API_OWNER = "gwa_api_owner";

  static String TOTAL = "total";

  static String TYPE = "type";

  static String UNKNOWN_APPLICATION_ERROR = "Unknown application error";

  static String UPDATED = "updated";

  static String USERNAME = "username";

  static String USERS = "users";

  static String USERS_PATH = "/users";

  static String VERSION = "version";

  static String WHITELIST = "whitelist";

  static List<String> ACL_FIELD_NAMES = Arrays.asList(WHITELIST);

  static List<String> API_SORT_FIELDS = Arrays.asList("api_name", NAME, "consumer_username");

  static List<String> APIS_FIELD_NAMES = Arrays.asList(ID, CREATED_AT, "upstream_url",
    "preserve_host", NAME, HOSTS, "uris", "methods", "strip_uri", "retries",
    "upstream_connect_timeout", "upstream_send_timeout", "upstream_read_timeout", "https_only",
    "http_if_terminated");

  static Map<String, Object> ENDPOINT_DEFAULT_CONFIG = Collections
    .singletonMap("allow_developer_keys", false);

  static List<String> ENDPOINT_FIELD_NAMES = Arrays.asList(API_OWNERS);

  static List<String> ENDPOINT_RATE_LIMIT_FIELD_NAMES = Arrays.asList("second", "hour", "minute",
    "day", "month", "year");

  static List<String> KEY_AUTH_FIELD_NAMES = Arrays.asList("key_names", "hide_credentials",
    "anonymous");

  static List<String> USER_DATA_NAMES = Arrays.asList(BASIC_AUTH, HMAC_AUTH, JWT, KEY_AUTH, OAUTH2);

  static List<String> CONFIG_TYPES = Arrays.asList(APIS, CONSUMERS, PLUGINS);

  static String[] USERNAME_CUSTOM_ID = new String[] {
    USERNAME, CUSTOM_ID
  };

  String API_NAME = "api_name";

  String USER_USERNAME = "user_username";

}
