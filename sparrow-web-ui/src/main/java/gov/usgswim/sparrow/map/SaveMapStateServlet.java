package gov.usgswim.sparrow.map;

import gov.usgswim.sparrow.SparrowProxyServlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SaveMapStateServlet extends SparrowProxyServlet {

@Override
public void doPost(HttpServletRequest request, HttpServletResponse response)  throws ServletException, IOException {
    String filename = request.getParameter("savefileas");
    String ext = request.getParameter("savefileas_extension");
    response.addHeader("Content-Disposition", "attachment; filename=" + filename + "." + ext);
    if ("js".equals(ext)) {
      response.setContentType("text/javascript");
    } else {
      response.setContentType("text/xml");
    }
    response.getOutputStream().print(request.getParameter("ui_XML"));
  }
}
