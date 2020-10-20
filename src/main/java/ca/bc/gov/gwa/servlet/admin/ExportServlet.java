package ca.bc.gov.gwa.servlet.admin;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.bc.gov.gwa.http.JsonHttpClient;
import ca.bc.gov.gwa.v1.ApiService;
import ca.bc.gov.gwa.servlet.GwaConstants;
import ca.bc.gov.gwa.util.JsonWriter;

@WebServlet(urlPatterns = "/int/rest/config/export", loadOnStartup = 1)
public class ExportServlet extends BaseAdminServlet implements GwaConstants {

  private class BooleanHolder {
    private boolean value;

    public BooleanHolder(final boolean value) {
      this.setValue(value);
    }

    public boolean isValue() {
      return this.value;
    }

    public void setValue(final boolean value) {
      this.value = value;
    }
  }

  private static final long serialVersionUID = 1L;

  public void configExport(final Writer writer) throws IOException {
    final Date date = new Date(System.currentTimeMillis());
    configExport(writer, date);
  }

  private void configExport(final Writer writer, final Date date) throws IOException {
    final boolean indent = true;
    try (
      JsonHttpClient httpClient = this.apiService.newKongClient();
      JsonWriter json = new JsonWriter(writer, indent)) {
      final Map<String, Object> serverInfo = httpClient.get("/");
      json.startObject();

      final String version = (String)serverInfo.get(ApiService.VERSION);
      json.label(ApiService.VERSION);
      json.value(version);
      json.endAttribute();

      final String dateString = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(date);
      json.label("date");
      json.value(dateString);

      for (final String type : CONFIG_TYPES) {
        json.endAttribute();
        if (CONSUMERS.equals(type)) {
          json.label(USERS);
        } else {
          json.label(type);
        }
        json.startList();
        final BooleanHolder firstRecord = new BooleanHolder(true);
        final Consumer<Map<String, Object>> baseAction = record -> {
          try {
            if (firstRecord.isValue()) {
              firstRecord.setValue(false);
            } else {
              json.endAttribute();
            }
            ImportServlet.removeEmptyValues(record);
            json.write(record);
          } catch (final IOException e) {
            throw new IllegalStateException(e);
          }
        };
        Consumer<Map<String, Object>> action = baseAction;
        if (CONSUMERS.equals(type)) {
          action = userRecord -> {
            final String userId = (String)userRecord.get("id");
            getUserData(httpClient, userRecord, userId);
            getGroups(httpClient, userRecord, userId);
            baseAction.accept(userRecord);
          };
        }
        this.apiService.kongPageAll(httpClient, "/" + type, action);
        json.endList();
      }
      json.endObject();
    }
  }

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {
    final Date date = new Date(System.currentTimeMillis());
    final String dateString = new SimpleDateFormat("yyyyMMDD-HHmmss").format(date);
    response.setContentType(ApiService.APPLICATION_JSON);
    if (!"false".equals(request.getParameter("download"))) {
      response.setHeader("Content-Disposition",
        "attachment; filename=kong-export-" + dateString + ".json");
    }
    try (
      Writer writer = response.getWriter()) {
      configExport(writer, date);
    }
  }

  private void getGroups(final JsonHttpClient httpClient, final Map<String, Object> userRecord,
    final String userId) {
    final List<String> groupNames = new ArrayList<>();
    try {
      this.apiService.kongPageAll(httpClient, "/consumers/" + userId + "/acls", acl -> {
        final String groupName = (String)acl.get(GROUP);
        groupNames.add(groupName);
      });
    } catch (final IOException e) {
      throw new IllegalStateException(e);
    }
    if (!groupNames.isEmpty()) {
      userRecord.put(GROUPS, groupNames);
    }
  }

  private void getUserData(final JsonHttpClient httpClient, final Map<String, Object> userRecord,
    final String userId) {
    final Map<String, Object> dataByName = new TreeMap<>();
    for (final String dataName : USER_DATA_NAMES) {
      final List<Map<String, Object>> dataList = new ArrayList<>();
      try {
        this.apiService.kongPageAll(httpClient, "/consumers/" + userId + "/" + dataName, record -> {
          ImportServlet.removeEmptyValues(record);
          dataList.add(record);
        });
      } catch (final IOException e) {
        throw new IllegalStateException(e);
      }
      if (!dataList.isEmpty()) {
        dataByName.put(dataName, dataList);
      }
    }
    if (!dataByName.isEmpty()) {
      userRecord.put(GwaConstants.DATA, dataByName);
    }
  }

}
