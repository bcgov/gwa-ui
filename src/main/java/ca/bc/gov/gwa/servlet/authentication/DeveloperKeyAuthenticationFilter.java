package ca.bc.gov.gwa.servlet.authentication;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ca.bc.gov.gwa.http.JsonHttpClient;
import ca.bc.gov.gwa.servlet.AbstractFilter;
import ca.bc.gov.gwa.servlet.ApiService;
import ca.bc.gov.gwa.util.LruMap;

@WebFilter(urlPatterns = {
  "/", "/login/*", "/git/*", "/logout", "/rest/*", "/ui/*"
})
public class DeveloperKeyAuthenticationFilter extends AbstractFilter {

  private static final int EXPIRY_TIME_MS = 10 * 60 * 1000;

  private static final String GITHUB = "github_";

  private static final String GIT_HUB_PRINCIPAL = "GitHubPrincipal";

  private static final String GIT_HUB_STATE_URL_MAP = "GitHubStateUrlMap";

  protected transient ApiService apiService;

  @SuppressWarnings("unchecked")
  private void addGitHubGroups(final JsonHttpClient client, final String accessToken,
    final Set<String> groups) throws IOException {
    final Object orgResponse = client.get("/user/orgs?access_token=" + accessToken);
    if (orgResponse instanceof List) {
      final List<Map<String, Object>> orgList = (List<Map<String, Object>>)orgResponse;
      for (final Map<String, Object> organization : orgList) {
        final String role = (String)organization.get("login");
        if (role != null) {
          groups.add(GITHUB + role.toLowerCase());
        }
      }
    }
  }

  @Override
  public void doFilter(final ServletRequest request, final ServletResponse response,
    final FilterChain chain) throws IOException, ServletException {
    if (request.getAttribute("gwaDeveloperKeyFiltered") == null) {
      request.setAttribute("gwaDeveloperKeyFiltered", Boolean.TRUE);
      final HttpServletRequest httpRequest = (HttpServletRequest)request;
      final HttpServletResponse httpResponse = (HttpServletResponse)response;
      final String servletPath = httpRequest.getServletPath();
      if ("/logout".equals(servletPath)) {
        doFilterLogout(httpRequest, httpResponse);
      } else {
        doFilterLogin(chain, httpRequest, httpResponse);
      }
    } else {
      chain.doFilter(request, response);
    }
  }

  private void doFilterLogin(final FilterChain chain, final HttpServletRequest httpRequest,
    final HttpServletResponse httpResponse) throws IOException, ServletException {
    final HttpSession session = httpRequest.getSession();

    final String fullPath = httpRequest.getServletPath();
    if ("/git/callback".equals(fullPath)) {
      handleCallback(httpRequest, httpResponse, session);
    } else {
      GitHubPrincipal principal = (GitHubPrincipal)session.getAttribute(GIT_HUB_PRINCIPAL);
      if (principal == null || principal.isExpired(EXPIRY_TIME_MS)) {
        final String userId = httpRequest.getHeader("X-Consumer-Custom-Id");
        final String username = httpRequest.getHeader("X-Consumer-Username");
        if (userId != null && username != null && username.startsWith(GITHUB)) {
          final Set<String> groups = getGroups(httpResponse, userId, username);
          final String login = username.replaceFirst(GITHUB, "");
          principal = new GitHubPrincipal(userId, login, username, groups);
          if (groups.contains(this.apiService.getGitHubOrganizationRole())) {
            principal.addDeveloperRole();
          }
          session.setAttribute(GIT_HUB_PRINCIPAL, principal);
          final HttpServletRequestWrapper requestWrapper = principal
            .newHttpServletRequestWrapper(httpRequest);
          chain.doFilter(requestWrapper, httpResponse);
        } else {
          handleRedirectToGitHub(chain, httpRequest, httpResponse, session);
        }
      } else {
        final HttpServletRequestWrapper requestWrapper = principal
          .newHttpServletRequestWrapper(httpRequest);
        chain.doFilter(requestWrapper, httpResponse);
      }
    }
  }

  private void doFilterLogout(final HttpServletRequest httpRequest,
    final HttpServletResponse httpResponse) {
    final HttpSession session = httpRequest.getSession(false);
    if (session != null) {
      session.invalidate();
    }

    sendRedirect(httpResponse, "https://github.com/logout");
  }

  @SuppressWarnings("unchecked")
  private void handleCallback(final HttpServletRequest httpRequest,
    final HttpServletResponse httpResponse, final HttpSession session) throws IOException {
    final LruMap<String, String> stateUrlMap = (LruMap<String, String>)session
      .getAttribute(GIT_HUB_STATE_URL_MAP);
    if (stateUrlMap != null) {
      final String state = httpRequest.getParameter("state");
      final String code = httpRequest.getParameter("code");
      final String redirectUrl = stateUrlMap.get(state);
      if (redirectUrl != null) {
        final String accessToken = this.apiService.getGitHubAccessToken(state, code);
        try (
          JsonHttpClient client = new JsonHttpClient("https://api.github.com")) {
          final Map<String, Object> userResponse = client.get("/user?access_token=" + accessToken);
          final Number id = (Number)userResponse.get("id");
          final String login = (String)userResponse.get("login");
          if (id != null && login != null) {
            final String userId = GITHUB + id;
            final String username = GITHUB + login.toLowerCase();
            final Set<String> groups = getGroups(httpResponse, userId, username);
            if (groups == Collections.<String> emptySet()) {
              return;

            } else {
              addGitHubGroups(client, accessToken, groups);
              final Set<String> kongGroups = this.apiService.userGroups(userId, username);
              final GitHubPrincipal principal = new GitHubPrincipal(userId, login, username,
                groups);
              if (groups.contains(this.apiService.getGitHubOrganizationRole())) {
                if (!kongGroups.contains(GitHubPrincipal.DEVELOPER_ROLE)) {
                  this.apiService.addGitHubDeveloperGroup(principal);
                }
                principal.addDeveloperRole();
              }
              session.setAttribute(GIT_HUB_PRINCIPAL, principal);
              sendRedirect(httpResponse, redirectUrl);
              return;
            }
          }
        }
      }
    }
    sendError(httpResponse, HttpServletResponse.SC_FORBIDDEN);
  }

  @SuppressWarnings("unchecked")
  public void handleRedirectToGitHub(final FilterChain chain, final HttpServletRequest httpRequest,
    final HttpServletResponse httpResponse, final HttpSession session)
    throws IOException, ServletException {
    final String servletPath = httpRequest.getServletPath();
    if (servletPath.startsWith("/rest")) {
      sendError(httpResponse, HttpServletResponse.SC_FORBIDDEN);
    } else if (!servletPath.startsWith("/ui")
      || !"true".equals(httpRequest.getParameter("contentOnly"))) {
      final String state = UUID.randomUUID().toString();
      LruMap<String, String> stateUrlMap = (LruMap<String, String>)session
        .getAttribute(GIT_HUB_STATE_URL_MAP);
      if (stateUrlMap == null) {
        stateUrlMap = new LruMap<>(10);
        session.setAttribute(GIT_HUB_STATE_URL_MAP, stateUrlMap);
      }

      final StringBuffer redirectBuilder = httpRequest.getRequestURL();
      final String queryString = httpRequest.getQueryString();
      if (queryString != null) {
        redirectBuilder.append('?');
        redirectBuilder.append(queryString);
      }
      final String redirectUrl = redirectBuilder.toString();
      stateUrlMap.put(state, redirectUrl);
      final String gitHubClientId = this.apiService.getGitHubClientId();
      final String authorizeUrl = "https://github.com/login/oauth/authorize?client_id="
        + gitHubClientId + "&scope=read:org&state=" + state;
      sendRedirect(httpResponse, authorizeUrl);
    } else {
      chain.doFilter(httpRequest, httpResponse);
    }
  }

  @Override
  public void init(final FilterConfig filterConfig) throws ServletException {
    super.init(filterConfig);
    final ServletContext servletContext = filterConfig.getServletContext();
    this.apiService = ApiService.get(servletContext);
  }

}
