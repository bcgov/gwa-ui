package ca.bc.gov.gwa.servlet.admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.bc.gov.gwa.v1.ApiService;
import ca.bc.gov.gwa.servlet.PluginFieldLayout;
import ca.bc.gov.gwa.util.CaseConverter;
import ca.bc.gov.gwa.util.Json;

@WebServlet(urlPatterns = "/int/rest/plugins/*", loadOnStartup = 1)
public class PluginServlet extends BaseAdminServlet {
  private static final long serialVersionUID = 1L;

  private static final Map<String, Map<String, Object>> PLUGIN_SCHEMA_BY_NAME = new HashMap<>();

  private static final String FIELDS = "fields";

  private static final String VALUES = "values";

  private static final String NAME = "name";

  private static final String TYPE = "type";

  private static Map<String, Object> getCustomSchema(final String pluginName) {
    if (PLUGIN_SCHEMA_BY_NAME.containsKey(pluginName)) {
      return PLUGIN_SCHEMA_BY_NAME.get(pluginName);
    } else {
      try (
        InputStream in = PluginServlet.class
          .getResourceAsStream("/ca/bc/gov/gwa/kong/plugins/" + pluginName + ".json")) {
        Map<String, Object> customSchema;
        if (in == null) {
          customSchema = Collections.emptyMap();
        } else {
          customSchema = Json.read(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
        PLUGIN_SCHEMA_BY_NAME.put(pluginName, customSchema);
        return customSchema;
      } catch (final IOException e) {
        ApiService.LOG.error("Unable to read custom schema:" + pluginName, e);
        return Collections.emptyMap();
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static <V> V setProperty(final Map<String, Object> target, final String targetName,
    final Map<String, Object> source, final String sourceName) {
    final Object value = source.get(sourceName);
    if (value != null) {
      target.put(targetName, value);
    }
    return (V)value;
  }

  @SuppressWarnings("unchecked")
  private static <V> V setProperty(final Map<String, Object> target, final String targetName,
    final Map<String, Object> source, final String sourceName, final Object defaultValue) {
    Object value = source.get(sourceName);
    if (value == null) {
      value = defaultValue;
    }
    if (value != null) {
      target.put(targetName, value);
    }
    return (V)value;
  }

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {
    final List<String> paths = splitPathInfo(request);
    final int pathCount = paths.size();
    if (pathCount == 0) {
      this.apiService.handleRequest(response, httpClient -> {
        final Map<String, Object> enabledResponse = httpClient.get("/plugins/enabled");
        final List<String> pluginNames = this.apiService.getList(enabledResponse,
          "enabled_plugins");
        Collections.sort(pluginNames);
        final List<Map<String, Object>> rows = new ArrayList<>();
        for (final String pluginName : pluginNames) {
          final Map<String, Object> row = Collections.singletonMap(ApiService.NAME, pluginName);
          rows.add(row);
        }
        this.apiService.writeJsonRows(response, rows);
      });
      return;
    } else {
      final String pluginName = paths.get(0);
      if (pathCount == 1) {
        if ("_names".equals(pluginName)) {
          pluginNames(response);
        } else {
          pluginList(request, response, pluginName);
        }
        return;
      } else if (pathCount == 2) {
        final String type = paths.get(1);
        if ("_schema".equals(type)) {
          pluginSchemaGet(response, pluginName);
          return;
        }
      }
      sendError(response, HttpServletResponse.SC_NOT_FOUND);
    }
  }

  private void pluginSchemaGet(final HttpServletResponse response, final String pluginName) {
    final String schemaPath = "/plugins/schema/" + pluginName;
    final Map<String, Object> customSchema = getCustomSchema(pluginName);
    this.apiService.handleRequest(response, httpClient -> {
      final Map<String, Object> kongPluginSchema = httpClient.get(schemaPath);
      final Map<String, Object> pluginSchema = new LinkedHashMap<>();
      final List<String> allFieldNames = new ArrayList<>();

      pluginSchemaAddFields("", allFieldNames, pluginSchema, kongPluginSchema, customSchema);
      new PluginFieldLayout(pluginName, pluginSchema,
        this.apiService.getList(customSchema, "layout"));

      Json.writeJson(response, pluginSchema);
    });
  }

  private void pluginList(final HttpServletRequest request, final HttpServletResponse response,
    final String pluginName) {
    final String path = "/plugins?name=" + pluginName;
    this.apiService.pluginList(request, response, path, null);
  }

  private void pluginNames(final HttpServletResponse response) {
    this.apiService.handleRequest(response, httpClient -> {
      final Map<String, Object> enabledResponse = httpClient.get("/plugins/enabled");
      final List<String> pluginNames = this.apiService.getList(enabledResponse, "enabled_plugins");
      Collections.sort(pluginNames);
      Json.writeJson(response, enabledResponse);
    });
  }

  @SuppressWarnings("unchecked")
  private void pluginSchemaAddFields(final String prefix, final List<String> allFieldNames,
    final Map<String, Object> pluginSchema, final Map<String, Object> kongPluginSchema,
    final Map<String, Object> customSchema) {
    final Map<String, Map<String, Object>> pluginFieldMap = new TreeMap<>();
    pluginSchema.put(FIELDS, pluginFieldMap);
    final Map<String, Map<String, Object>> kongFieldMap = (Map<String, Map<String, Object>>)kongPluginSchema
      .getOrDefault(FIELDS, Collections.emptyMap());
    final Map<String, Map<String, Object>> customFieldMap = (Map<String, Map<String, Object>>)customSchema
      .getOrDefault(FIELDS, Collections.emptyMap());
    for (final Entry<String, Map<String, Object>> fieldEntry : kongFieldMap.entrySet()) {
      final String fieldName = fieldEntry.getKey();
      final Map<String, Object> kongField = fieldEntry.getValue();

      final Map<String, Object> pluginField = new LinkedHashMap<>();
      pluginFieldMap.put(fieldName, pluginField);
      final Map<String, Object> customField = customFieldMap.getOrDefault(fieldName,
        Collections.emptyMap());
      pluginField.put(NAME, fieldName);

      String fieldType = (String)kongField.get(TYPE);
      if ("boolean".equals(fieldType)) {
        fieldType = "checkbox";
      } else if (kongField.containsKey("enum") || customField.containsKey(VALUES)) {
        fieldType = "select";
      }
      pluginField.put("fieldType", fieldType);

      String title = (String)customField.get("title");
      if (title == null) {
        title = CaseConverter.toCapitalizedWords(fieldName);
      }
      pluginField.put("title", title);
      setProperty(pluginField, "required", kongField, "required");
      setProperty(pluginField, "readOnly", kongField, "immutable");
      setProperty(pluginField, VALUES, kongField, "enum", customField.get(VALUES));
      Object defaultValue = kongField.get("default");
      if ("array".equals(fieldType)) {
        if (defaultValue == null || defaultValue instanceof Map) {
          defaultValue = Collections.emptyList();
        }
      } else if ("table".equals(fieldType)) {
        defaultValue = pluginSchemaAddFieldsTable(prefix, allFieldNames, fieldName, kongField,
          pluginField, customField, defaultValue);
      }
      pluginField.put("defaultValue", defaultValue);
    }
  }

  @SuppressWarnings("unchecked")
  private Object pluginSchemaAddFieldsTable(final String prefix, final List<String> allFieldNames,
    final String fieldName, final Map<String, Object> kongField,
    final Map<String, Object> pluginField, final Map<String, Object> customField,
    final Object defaultValue) {
    final Map<String, Object> kongPluginChildSchema = (Map<String, Object>)kongField
      .getOrDefault("schema", Collections.emptyMap());
    pluginSchemaAddFields(prefix + fieldName + ".", allFieldNames, pluginField,
      kongPluginChildSchema, customField);
    if (defaultValue == null) {
      return Collections.emptyMap();
    } else {
      return defaultValue;
    }
  }
}
