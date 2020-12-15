package ca.bc.gov.gwa.servlet;

import ca.bc.gov.gwa.servlet.authentication.oidc.TokenInvalidException;
import ca.bc.gov.gwa.v1.ApiService;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  protected static List<String> splitPath(final String path) {
    if (path == null || path.length() == 1) {
      return Collections.emptyList();
    } else {
      final List<String> paths = new ArrayList<>();
      int startIndex = 1;
      for (int endIndex = path.indexOf('/', startIndex); endIndex != -1; endIndex = path
        .indexOf('/', startIndex)) {
        if (endIndex - startIndex > 0) {
          final String part = path.substring(startIndex, endIndex);
          paths.add(part);
        }
        startIndex = endIndex + 1;
      }
      if (startIndex < path.length()) {
        final String part = path.substring(startIndex);
        paths.add(part);
      }
      return paths;
    }
  }

  protected static List<String> splitPathInfo(final HttpServletRequest request) {
    final String path = request.getPathInfo();
    return splitPath(path);
  }

  protected transient ApiService apiService;

  public BaseServlet() {
    super();
  }

  @Override
  public void destroy() {
    super.destroy();
    this.apiService = null;
  }

  protected void doService(final HttpServletRequest request, final HttpServletResponse response,
    final String method) {

      
    try {
      if ("DELETE".equals(method)) {
        doDelete(request, response);
      } else {
        super.service(request, response);
      }
    } catch (final TokenInvalidException e) {
      final Class<?> clazz = getClass();
      final Logger logger = LoggerFactory.getLogger(clazz);
      logger.error("Token Invalid", e);
        ///sendError (response, 403);
    } catch (final Exception e) {
      final Class<?> clazz = getClass();
      final Logger logger = LoggerFactory.getLogger(clazz);
      logger.error("Error handling request", e);
    } catch (final Throwable e) {
      final Class<?> clazz = getClass();
      final Logger logger = LoggerFactory.getLogger(clazz);
      logger.error("THROWABLE handling request", e);
    }
  }

  protected boolean hasRole(final HttpServletRequest request, final HttpServletResponse response,
    final String roleName) {
    final Principal userPrincipal = request.getUserPrincipal();
    if (userPrincipal instanceof BasePrincipal) {
      final BasePrincipal principal = (BasePrincipal)userPrincipal;
      if (principal.isUserInRole(roleName)) {
        return true;
      }
    }
    sendError(response, HttpServletResponse.SC_FORBIDDEN);
    return false;
  }

  @Override
  public void init() throws ServletException {
    final ServletContext servletContext = getServletContext();
    this.apiService = ApiService.get(servletContext);
  }

  public boolean isPathEmpty(final String pathInfo) {
    return pathInfo == null || "/".equals(pathInfo);
  }

  protected void sendError(final HttpServletResponse response, final int statusCode) {
    try {
      response.sendError(statusCode);
    } catch (final IOException e) {
      LoggerFactory.getLogger(getClass()).debug("Unable to send status:" + statusCode, e);
    }
  }

  protected void sendRedirect(final HttpServletResponse response, final String url) {
    try {
      response.sendRedirect(url);
    } catch (final IOException e) {
      LoggerFactory.getLogger(getClass()).debug("Unable to send redirect: " + url, e);
    }
  }

  @Override
  protected void service(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {
    try {
      String method = request.getMethod();
      final String methodOverride = request.getHeader("X-HTTP-Method-Override");
      if ("DELETE".equals(methodOverride) && "POST".equals(request.getMethod())) {
        method = methodOverride;
      }
      doService(request, response, method);
    } catch (final Exception e) {
      final Class<?> clazz = getClass();
      final Logger logger = LoggerFactory.getLogger(clazz);
      logger.error("Error handling request", e);
    }
  }

}
