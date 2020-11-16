package ca.bc.gov.gwa.servlet.authentication;

import java.security.Principal;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import ca.bc.gov.gwa.servlet.BasePrincipal;
import ca.bc.gov.gwa.servlet.authentication.oidc.LookupUtil;
import javax.servlet.http.HttpServletResponse;
import org.pac4j.core.profile.Pac4JPrincipal;
import org.pac4j.core.profile.UserProfile;

public class GitHubPrincipal extends BasePrincipal {
  public static final String ADMIN_ROLE = "gwa_admin";
  public static final String NS_ADMIN_ROLE = "gwa_ns_admin";
  public static final String DEVELOPER_ROLE = "gwa_github_developer";

  private static final long serialVersionUID = 1L;

  public static boolean hasDeveloperRole(final HttpServletRequest request, final HttpServletResponse response) {
    final Principal userPrincipal = request.getUserPrincipal();
    if (userPrincipal instanceof GitHubPrincipal) {
      final GitHubPrincipal principal = (GitHubPrincipal)userPrincipal;
      return principal.isUserInRole(DEVELOPER_ROLE);
    } else if (userPrincipal instanceof Pac4JPrincipal) {
      UserProfile user = LookupUtil.lookupUserProfile(request, response);
      return true;
    }
    return false;
  }

  public static boolean hasAdminRole(final HttpServletRequest request) {
    final Principal userPrincipal = request.getUserPrincipal();
    if (userPrincipal instanceof GitHubPrincipal) {
      final GitHubPrincipal principal = (GitHubPrincipal)userPrincipal;
      return principal.isUserInRole(ADMIN_ROLE);
    }
    return false;
  }

  private final String login;

  public GitHubPrincipal(final String id, final String login, final String name,
    final Set<String> roles) {
    super(id, name, roles);
    this.login = login;
    throw new IllegalArgumentException("GitHubPrincipal no longer supported!");
  }

  public void addDeveloperRole() {
    addRole(DEVELOPER_ROLE);
  }

  public void addAdminRole() {
    addRole(ADMIN_ROLE);
  }

  @Override
  public boolean equals(final Object object) {
    if (this == object) {
      return true;
    } else if (object instanceof GitHubPrincipal) {
      return super.equals(object);
    } else {
      return false;
    }
  }

  public String getLogin() {
    return this.login;
  }

  public boolean hasDeveloperRole() {
    return isUserInRole(DEVELOPER_ROLE);
  }

  public boolean hasAdminRole() {
    return isUserInRole(ADMIN_ROLE);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
