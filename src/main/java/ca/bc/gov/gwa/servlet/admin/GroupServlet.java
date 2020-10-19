package ca.bc.gov.gwa.servlet.admin;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.bc.gov.gwa.servlet.BaseServlet;
import ca.bc.gov.gwa.servlet.GwaConstants;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(urlPatterns = "/int/rest/groups/*", loadOnStartup = 1)
public class GroupServlet extends BaseServlet implements GwaConstants {

  private static final long serialVersionUID = 1L;

  @Override
  protected void doService(final HttpServletRequest httpRequest,
    final HttpServletResponse httpResponse, final String method) {
    int statusCode = HttpServletResponse.SC_NOT_FOUND;
    final List<String> paths = splitPathInfo(httpRequest);
    final int pathCount = paths.size();
    if (pathCount == 0) {
        try {
            statusCode = groups(httpRequest, httpResponse, method);
        } catch (IOException ex) {
            Logger.getLogger(GroupServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    } else if (pathCount == 1) {
      final String groupName = paths.get(0);
        try {
            statusCode = groupRecord(httpRequest, httpResponse, method, groupName);
        } catch (IOException ex) {
            Logger.getLogger(GroupServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    } else {
      statusCode = HttpServletResponse.SC_NOT_FOUND;
    }
    if (statusCode != 0) {
      sendError(httpResponse, statusCode);
    }
  }

  private int groupRecord(final HttpServletRequest httpRequest,
    final HttpServletResponse httpResponse, final String method, final String groupName) throws IOException {
    if (GET.equals(method)) {
      this.apiService.groupUserList(httpRequest, httpResponse, groupName);
    } else if (DELETE.equals(method)) {
        final String groupPath = GROUPS_PATH2 + groupName + USERS_PATH;
      this.apiService.handleDelete(httpResponse, groupPath);
    } else {
      return HttpServletResponse.SC_METHOD_NOT_ALLOWED;
    }
    return 0;
  }

  private int groups(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse,
    final String method) throws IOException {
    if (GET.equals(method)) {
      String groupName = httpRequest.getParameter("groupName");
      this.apiService.groupsList(httpRequest, httpResponse);
//      if (groupName == null) {
//        this.apiService.handleListAll(httpRequest, httpResponse, GROUPS_PATH);
//      } else {
//        Predicate<Map<String, Object>> filter = group -> {
//          String name = (String)group.get("group");
//          return name.contains(groupName);
//        };
//        this.apiService.handleListAll(httpRequest, httpResponse, GROUPS_PATH, filter);
//      }
    } else if (POST.equals(method)) {
      this.apiService.handleAdd(httpRequest, httpResponse, GROUPS_PATH,
        Collections.singletonList(GROUP));
    } else {
      return HttpServletResponse.SC_METHOD_NOT_ALLOWED;
    }
    return 0;
  }
}
