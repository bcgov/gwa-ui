package ca.bc.gov.gwa.servlet.developerkey;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.bc.gov.gwa.v1.ApiService;
import ca.bc.gov.gwa.servlet.BaseServlet;
import ca.bc.gov.gwa.servlet.authentication.GitHubPrincipal;

@WebServlet(urlPatterns = "/rest/apis", loadOnStartup = 1)
public class DeveloperApiServlet extends BaseServlet {
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {
    if (GitHubPrincipal.hasDeveloperRole(request, response)) {
      this.apiService.developerApiList(request, response);
    } else {
      response.setContentType(ApiService.APPLICATION_JSON);
      try (
        PrintWriter writer = response.getWriter()) {
        writer.print("{}");
      }
    }
  }
}
