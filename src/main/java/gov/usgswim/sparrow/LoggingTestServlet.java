package gov.usgswim.sparrow;

import gov.usgswim.sparrow.action.Action;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Dumps an entry into the log file for each level of logging.
 * Intended to give a way to verify logging is working and the format is what
 * was intended.
 * 
 * @author eeverman
 */
public class LoggingTestServlet extends HttpServlet {
	protected static Logger log =
		Logger.getLogger(Action.class); //logging for this class
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LoggingTestServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Exception e = new Exception("Test exception.  Not a real error, just simulated to show log output.");
		
		log.trace("Debug level log example, with fake exception", e);
		log.debug("Debug level log example, with fake exception", e);
		log.warn("Warn level log example, with fake exception", e);
		log.error("Error level log example, with fake exception", e);
		log.fatal("Debug level log example, with fake exception", e);
		
		response.setStatus(200);
		response.setContentType("text");
		response.getWriter().print("An example of each log level has been written to the logs.");
	}

}
