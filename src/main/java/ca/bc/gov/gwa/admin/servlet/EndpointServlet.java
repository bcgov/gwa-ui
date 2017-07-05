package ca.bc.gov.gwa.admin.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.bc.gov.gwa.servlet.BaseServlet;

@WebServlet(urlPatterns = "/int/rest/endpoints/*", loadOnStartup = 1)
public class EndpointServlet extends BaseServlet {
  private static final String USERS = "users";

  private static final String GROUPS = "groups";

  private static final long serialVersionUID = 1L;

  @Override
  protected void doDelete(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {
    final List<String> paths = splitPathInfo(request);
    if (!BaseServlet.apiService.endpointAccessAllowed(request, response, paths)) {
      return;
    }

    if (paths.size() == 5 && GROUPS.equals(paths.get(1)) && USERS.equals(paths.get(3))) {
      final String apiName = paths.get(0);
      doDeleteGroupUser(response, paths, apiName);
    } else {
      sendError(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
  }

  private void doDeleteGroupUser(final HttpServletResponse response, final List<String> paths,
    final String apiName) {
    final String groupName = paths.get(2);
    final String userName = paths.get(4);
    BaseServlet.apiService.apiGroupUserDelete(response, apiName, groupName, userName);
  }

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {
    final List<String> paths = splitPathInfo(request);
    if (!BaseServlet.apiService.endpointAccessAllowed(request, response, paths)) {
      return;
    }
    switch (paths.size()) {
      case 0:
        doGetEndpointList(request, response);
      break;
      case 1:
        doGetEndpoint(response, paths);
      break;

      case 4:
        final String apiName = paths.get(0);
        if (GROUPS.equals(paths.get(1)) && USERS.equals(paths.get(3))) {
          doGetGroupUserList(request, response, paths, apiName);
        } else {
          sendError(response, HttpServletResponse.SC_NOT_FOUND);
        }
      break;

      default:
        sendError(response, HttpServletResponse.SC_NOT_FOUND);
      break;
    }
  }

  private void doGetEndpoint(final HttpServletResponse response, final List<String> paths) {
    final String apiName = paths.get(0);
    BaseServlet.apiService.apiGet(response, apiName);
  }

  private void doGetEndpointList(final HttpServletRequest request,
    final HttpServletResponse response) {
    BaseServlet.apiService.endpointList(request, response);
  }

  private void doGetGroupUserList(final HttpServletRequest request,
    final HttpServletResponse response, final List<String> paths, final String apiName) {
    final String groupName = paths.get(2);
    BaseServlet.apiService.endpointGroupUserList(request, response, apiName, groupName);
  }

  @Override
  protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {
    final List<String> paths = splitPathInfo(request);
    if (!BaseServlet.apiService.endpointAccessAllowed(request, response, paths)) {
      return;
    }
    if (paths.size() == 5 && GROUPS.equals(paths.get(1)) && USERS.equals(paths.get(3))) {
      doPostGroupUserAdd(response, paths);
    } else {
      sendError(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
  }

  private void doPostGroupUserAdd(final HttpServletResponse response, final List<String> paths) {
    final String apiName = paths.get(0);
    final String groupName = paths.get(2);
    final String userName = paths.get(4);
    BaseServlet.apiService.apiGroupUserAdd(response, apiName, groupName, userName);
  }

  @Override
  protected void doPut(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {
    final List<String> paths = splitPathInfo(request);
    if (!BaseServlet.apiService.endpointAccessAllowed(request, response, paths)) {
      return;
    }
    if (paths.size() == 1) {
      doPutEndpointUpdate(request, response);
    } else {
      sendError(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
  }

  private void doPutEndpointUpdate(final HttpServletRequest request,
    final HttpServletResponse response) {
    BaseServlet.apiService.endpointUpdate(request, response);
  }
}
