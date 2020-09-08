/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.bc.gov.gwa.servlet;

import ca.bc.gov.gwa.v1.ApiService;
import ca.bc.gov.gwa.v2.controllers.GwaController;
import ca.bc.gov.gwa.v2.model.Teams;
import ca.bc.gov.gwa.v2.services.TeamService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.nimbusds.jose.util.IOUtils;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet(urlPatterns = "/int/api/groups/sync", loadOnStartup = 1)
public class GroupSyncServlet extends BaseServlet {
     @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        GwaController gwa = ApiService.getGwaController(request.getServletContext());

        TeamService teamService = gwa.getTeamService();
        
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Teams teams = mapper.readValue(request.getInputStream(), Teams.class);
        
        teamService.sync(teams);
        
        // Read a list of 
        response.getWriter().println("Done");
    }
}
