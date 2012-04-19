/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgswim.sparrow;

import gov.usgs.cida.config.DynamicReadOnlyProperties;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author eeverman
 */
public class HealthTest extends HttpServlet {

	protected DynamicReadOnlyProperties props;
					
	/** 
	 * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
					throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		try {

			out.println("<html>");
			out.println("<head>");
			out.println("<title>Servlet HealthTest</title>");  
			out.println("</head>");
			out.println("<body>");
			out.println("<h1>Servlet HealthTest at " + request.getContextPath () + "</h1>");
			out.append("<ul>");
			
			out.append("<li>" + props.getProperty("gov.usgswim.sparrow.HealthTest", "Not Found") + "</li>");
			
			out.append("</ul>");
			out.println("</body>");
			out.println("</html>");

		} finally {			
			out.close();
		}
	}

	@Override
	public void init() throws ServletException {
		super.init();
		
		props = new DynamicReadOnlyProperties();
		props.addJNDIContexts(DynamicReadOnlyProperties.DEFAULT_JNDI_CONTEXTS);

	}

	// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
	/** 
	 * Handles the HTTP <code>GET</code> method.
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
					throws ServletException, IOException {
		processRequest(request, response);
	}

	/** 
	 * Handles the HTTP <code>POST</code> method.
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
					throws ServletException, IOException {
		processRequest(request, response);
	}

	/** 
	 * Returns a short description of the servlet.
	 * @return a String containing servlet description
	 */
	@Override
	public String getServletInfo() {
		return "Short description";
	}// </editor-fold>
	

}
