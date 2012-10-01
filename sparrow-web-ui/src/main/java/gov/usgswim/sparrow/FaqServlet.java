/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgswim.sparrow;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.Response.ResponseType;
import org.apache.abdera.protocol.client.AbderaClient;
import org.apache.abdera.protocol.client.ClientResponse;

/**
 *
 * @author eeverman
 */
public class FaqServlet extends HttpServlet {

	String atomFeedUrl;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		atomFeedUrl = config.getInitParameter("AtomFeedUrl");
		
		if (atomFeedUrl == null)
			throw new ServletException("The servlet init parameter 'AtomFeedUrl' must be specified.");
	}

	/**
	 * Processes requests for both HTTP
	 * <code>GET</code> and
	 * <code>POST</code> methods.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
//	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
//			throws ServletException, IOException {
//		response.setContentType("text/html;charset=UTF-8");
//		PrintWriter out = response.getWriter();
//		try {
//			/*
//			 * TODO output your page here. You may use following sample code.
//			 */
//			out.println("<html>");
//			out.println("<head>");
//			out.println("<title>Servlet FaqServlet</title>");			
//			out.println("</head>");
//			out.println("<body>");
//			out.println("<h1>Servlet FaqServlet at " + request.getContextPath() + "</h1>");
//			out.println("</body>");
//			out.println("</html>");
//		} finally {			
//			out.close();
//		}
//	}
	
	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		try {

			Abdera abdera = new Abdera();
			AbderaClient client = new AbderaClient(abdera);
			ClientResponse resp = client.get(atomFeedUrl);
			if (resp.getType() == ResponseType.SUCCESS) {
				Document<Feed> doc = resp.getDocument();
			} else {
				// there was an error
			}
			
		} finally {			
			out.close();
		}
	}

	// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
	/**
	 * Handles the HTTP
	 * <code>GET</code> method.
	 *
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
	 * Handles the HTTP
	 * <code>POST</code> method.
	 *
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
	 *
	 * @return a String containing servlet description
	 */
	@Override
	public String getServletInfo() {
		return "Short description";
	}// </editor-fold>
}
