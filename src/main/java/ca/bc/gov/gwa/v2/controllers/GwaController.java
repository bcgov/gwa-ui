/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.bc.gov.gwa.v2.controllers;

import ca.bc.gov.gwa.http.HttpStatusException;
import ca.bc.gov.gwa.http.JsonHttpClient;
import ca.bc.gov.gwa.http.JsonHttpConsumer;
import ca.bc.gov.gwa.http.JsonHttpFunction;
import ca.bc.gov.gwa.servlet.GwaConstants;
import static ca.bc.gov.gwa.servlet.GwaConstants.APPLICATION_JSON;
import static ca.bc.gov.gwa.servlet.GwaConstants.DATA;
import static ca.bc.gov.gwa.servlet.GwaConstants.DELETED;
import static ca.bc.gov.gwa.servlet.GwaConstants.KONG_SERVER_NOT_AVAILABLE;
import static ca.bc.gov.gwa.servlet.GwaConstants.KONG_SERVER_RETURNED_AN_ERROR;
import static ca.bc.gov.gwa.servlet.GwaConstants.TOTAL;
import static ca.bc.gov.gwa.servlet.GwaConstants.UNKNOWN_APPLICATION_ERROR;
import static ca.bc.gov.gwa.servlet.GwaConstants.UPDATED;
import ca.bc.gov.gwa.servlet.admin.ImportServlet;
import ca.bc.gov.gwa.util.Json;
import ca.bc.gov.gwa.util.LruMap;
import ca.bc.gov.gwa.v2.conf.GwaSettings;
import ca.bc.gov.gwa.v2.services.KongAdminService;
import ca.bc.gov.gwa.v2.services.PermissionService;
import ca.bc.gov.gwa.v2.services.TeamService;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

@WebListener
public class GwaController implements ServletContextListener, GwaConstants {

    private final Map<String, String> apiNameById = new LruMap<>(1000);

    public static String GWA_CONTROLLER = GwaController.class.getName();

    public static final Logger LOG = LoggerFactory.getLogger(GwaController.class);

    final GwaSettings config = new GwaSettings();

    KongAdminService kongAdminService = new KongAdminService(config);
    PermissionService permissionService = new PermissionService(config);
    TeamService teamService = new TeamService(config);

    public KongAdminService getKongAdminService() {
        return kongAdminService;
    }

    public PermissionService getPermissionService() {
        return permissionService;
    }

    public TeamService getTeamService() {
        return teamService;
    }

    @Override
    public void contextDestroyed(final ServletContextEvent event) {
        //this.apiNameById.clear();
    }

    @Override
    public void contextInitialized(final ServletContextEvent event) {
        //
        try {
            final ServletContext servletContext = event.getServletContext();
            servletContext.setAttribute(GWA_CONTROLLER, this);
        } catch (final Exception e) {
            LOG.error("Unable to initialize service", e);
            throw e;
        }
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
            final Map<String, Object> response = kongAdminService.kongPageAll(httpRequest, httpClient, path);
            Json.writeJson(httpResponse, response);
        });
    }

    /*
  public void handleListAll(final HttpServletRequest httpRequest,
    final HttpServletResponse httpResponse, final String path,
    final Predicate<Map<String, Object>> filter) {
    handleRequest(httpResponse, httpClient -> {
      final Map<String, Object> response = kongAdminService.kongPageAll(httpRequest, httpClient, path, filter);
      Json.writeJson(httpResponse, response);
    });
  }
     */

    public void handleRequest(final HttpServletResponse httpResponse, final JsonHttpConsumer action) {
        final JsonHttpFunction function = action;
        handleRequest(httpResponse, function);
    }

    public Map<String, Object> handleRequest(final HttpServletResponse httpResponse,
            final JsonHttpFunction action) {
        try (
                JsonHttpClient httpClient = kongAdminService.newKongClient()) {
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

    public void sendError(final HttpServletResponse response, final int statusCode) {
        try {
            response.sendError(statusCode);
        } catch (final IOException e) {
            LOG.debug("Unable to send status:" + statusCode, e);
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

    public Map<String, Object> getMap(final Map<String, Object> requestData,
            final List<String> fieldNames) {
        final Map<String, Object> data = new LinkedHashMap<>();
        addData(data, requestData, fieldNames);
        ImportServlet.removeEmptyValues(data);
        return data;
    }

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

}
