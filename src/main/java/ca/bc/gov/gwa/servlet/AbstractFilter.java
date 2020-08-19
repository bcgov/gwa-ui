package ca.bc.gov.gwa.servlet;

import ca.bc.gov.gwa.v1.ApiService;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.conn.HttpHostConnectException;
import org.slf4j.LoggerFactory;

public abstract class AbstractFilter implements Filter {

  protected ApiService apiService;

  @Override
  public void destroy() {
    this.apiService = null;
  }

  protected Set<String> getGroups(final HttpServletResponse httpResponse, final String userId,
    final String username) {
    try {
      return this.apiService.userGroups(userId, username);
    } catch (final HttpHostConnectException e) {
      LoggerFactory.getLogger(getClass()).error("Unable to connect to KONG", e);
      sendError(httpResponse, HttpServletResponse.SC_SERVICE_UNAVAILABLE);
      return Collections.emptySet();
    } catch (final Exception e) {
      LoggerFactory.getLogger(getClass()).error("Error getting user's roles", e);
      sendError(httpResponse, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return Collections.emptySet();
    }
  }

  @Override
  public void init(final FilterConfig filterConfig) throws ServletException {
    final ServletContext servletContext = filterConfig.getServletContext();
    this.apiService = ApiService.get(servletContext);
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
}
