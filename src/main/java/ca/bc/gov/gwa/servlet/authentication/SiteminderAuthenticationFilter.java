package ca.bc.gov.gwa.servlet.authentication;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.LoggerFactory;

import ca.bc.gov.gwa.servlet.AbstractFilter;
import ca.bc.gov.gwa.servlet.ApiService;

// @WebFilter(urlPatterns = {
//   "/int/ui/*", "/int/rest/*", "/int/logout", "/int/login/*"
// }, servletNames = {
//   "AdminUiServlet"
// })
public class SiteminderAuthenticationFilter extends AbstractFilter {

  private static final int EXPIRY_TIME_MS = 10 * 60 * 1000;

  private static final String SITEMINDER_PRINCIPAL = "SiteminderPrincipal";

  @Override
  public void doFilter(final ServletRequest request, final ServletResponse response,
    final FilterChain chain) throws IOException, ServletException {
    final HttpServletRequest httpRequest = (HttpServletRequest)request;
    final HttpServletResponse httpResponse = (HttpServletResponse)response;
    try {
      if (request.getAttribute("siteminderFiltered") == null) {
        request.setAttribute("siteminderFiltered", Boolean.TRUE);
        final String servletPath = httpRequest.getServletPath();
        if ("/int/logout".equals(servletPath)) {
          doFilterLogout(httpRequest, httpResponse);
        } else {
          doFilterLogin(chain, httpRequest, httpResponse);
        }
      } else {
        chain.doFilter(request, response);
      }
    } catch (ServletException | IOException e) {
      throw e;
    } catch (final Exception e) {
      LoggerFactory.getLogger(getClass()).error("Unknown error", e);
      sendError(httpResponse, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  protected void doFilterLogin(final FilterChain chain, final HttpServletRequest httpRequest,
    final HttpServletResponse httpResponse) throws IOException, ServletException {
    final HttpSession session = httpRequest.getSession();
    SiteminderPrincipal principal = (SiteminderPrincipal)session.getAttribute(SITEMINDER_PRINCIPAL);
    final String userGuid = httpRequest.getHeader("smgov_userguid");
    final String universalid = httpRequest.getHeader("sm_universalid");
    final String type = httpRequest.getHeader("smgov_usertype");
    final String userDir = httpRequest.getHeader("sm_authdirname");
    if (userGuid == null || universalid == null || userDir == null) {
      sendError(httpResponse, HttpServletResponse.SC_FORBIDDEN);
    } else {
      final String userId = (userDir + "_" + userGuid).toLowerCase();
      if (principal == null || principal.isInvalid(userId, EXPIRY_TIME_MS)) {
        final String username = (userDir + "_" + universalid).toLowerCase();

        final Set<String> groups = getGroups(httpResponse, userId, username);
        if (groups == Collections.<String> emptySet()) {
          return;
        } else {
          principal = new SiteminderPrincipal(userId, type, username, groups);
          session.setAttribute(SITEMINDER_PRINCIPAL, principal);
        }
      }
      if (principal.isUserInRole(ApiService.ROLE_GWA_ADMIN)
        || principal.isUserInRole(ApiService.ROLE_GWA_API_OWNER)) {
        final HttpServletRequestWrapper requestWrapper = principal
          .newHttpServletRequestWrapper(httpRequest);
        chain.doFilter(requestWrapper, httpResponse);
      } else {
        sendError(httpResponse, HttpServletResponse.SC_FORBIDDEN);
      }
    }
  }

  protected void doFilterLogout(final HttpServletRequest httpRequest,
    final HttpServletResponse httpResponse) {
    final HttpSession session = httpRequest.getSession(false);
    if (session != null) {
      session.invalidate();
    }
    sendRedirect(httpResponse, "https://logon.gov.bc.ca/clp-cgi/logoff.cgi");
  }
}
