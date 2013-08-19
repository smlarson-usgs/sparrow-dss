package gov.usgswim.sparrow;

import gov.usgs.cida.proxy.ProxyServlet;
import gov.usgswim.sparrow.SparrowUtil.UrlFeatures;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;


/**
 * Proxies geoserver requests to: http://130.11.165.190:8080/geonetwork/srv/en/csw
 * (configuration is ignored)
 * 
 * Most responses are returned unmodified, however, some urls (as indicated by
 * extra path) have XSLT applied to return HTML instead of XML.
 * 
 * If HTML is returned, references to 'http://water.usgs.gov/nawqa/' are replaced
 * with 'http://cida.usgs.gov/'  See copyStreams().
 * 
 * @author eeverman
 *
 */
public class GeoNetworkXSLTProxy extends ProxyServlet {
	protected static Logger log = Logger.getLogger(GeoNetworkXSLTProxy.class);
	
	private static TransformerFactory transformerFactory = TransformerFactory.newInstance();
	private static final String HTML_MIME_TYPE = "text/html";
	private static final String UTF_8 = "UTF-8";
	private static final String CSW_QUERY_RESULTS_TO_HTML_XSLT_PATH = "/gov/usgswim/sparrow/landing/xslt/csw_xml-to-html-results.xsl";
	private static final String CSW_ID_RESULT_TO_HTML_XSLT_PATH = "/gov/usgswim/sparrow/landing/xslt/csw_id_to_html.xsl";
	private static final String PUBLISHED_SERVER_NAME = "http://water.usgs.gov/nawqa/sparrow/dss/";
	private static final String ACTUAL_SERVER_NAME = "http://cida.usgs.gov/sparrow/";
	
	
	/**
	 * Total hack to force a different url beyond what we can configure b/c
	 * we are using a global context url as a base, which means that we cannot
	 * configure individual urls.
	 * 
	 * @param baseURL
	 * @return
	 */
	public URL buildRequestURL(HttpServletRequest request, URL baseURL)
			throws MalformedURLException {
		
		return new URL("http://localhost:8080/geonetwork/srv/en/csw");
		
	}

	/**
	 * Handles a POST request submitted to this proxy servlet.  The request is
	 * forwarded on to the URL configured in this servlet's init parameter.
	 * 
	 * @param request Request object forwarded by this servlet.
	 * @param response Response object returned by the forwarded URL.
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		OutputStreamWriter targetConnWriter = null; // Close when done
		InputStream targetIs = null; // Close when done
		HttpURLConnection targetConn = null; //Do not disconnect when done
		String requestURI = request.getRequestURI();
		String forwardURL = strategy.lookup(requestURI);
		URL targetURL = buildRequestURL(request, new URL(forwardURL));

		OutputStream responseOutputStream = null;

		try {

			if (log.isDebugEnabled()) {
				log.debug("Proxying POST '" + request.getRequestURL()
						+ "' to POST '" + targetURL.toExternalForm() + "'");
			}

			targetConn = getConnection(targetURL, "POST", strategy.getConnectTimeout(requestURI), strategy.getReadTimeout(requestURI));

			// Copy headers from the incoming request to the forwarded request
			copyHeaders(request, targetConn);

			if (targetURL.getPort() > 0) {
				targetConn.setRequestProperty("HOST", targetURL.getHost() + ":" + targetURL.getPort());
			}
			else {
				targetConn.setRequestProperty("HOST", targetURL.getHost());
			}

			// Applications can override the next method to addStrategy app specific properties
			addCustomRequestHeaders(request, targetConn);

			//TODO:  This type of parameter patching will not work for multi-
			//part POSTs.
			Map<String, String> custParams = getCustomParameters(request);
			if (custParams != null && custParams.size() > 0) {

				// We have custom params, so copy the initial params and addStrategy the custom ones
				StringBuffer params = requestToQueryString(request);
				if (params.length() > 0) {
					params.append("&");
				}

				params.append(mapToQueryString(custParams));
				
				if (log.isTraceEnabled()) {
					log.trace("Sending this POST content to '" + targetURL.toExternalForm()  + "': " + params.toString());
				}

				// Overwrite the content length to reflect the added parameters
				targetConn.setRequestProperty("CONTENT-LENGTH", Integer.toString(params.length()));

				// Establish connection to forwarded server
				// This method could throw an immediate error if the requested
				// resource does not exist.
				targetConn.connect();

				targetConnWriter = new OutputStreamWriter(targetConn.getOutputStream(), DEFAULT_ENCODING);
				targetConnWriter.write(params.toString());
				targetConnWriter.flush();

			} else if (log.isTraceEnabled()) {
				//Grab the content as a string so that we can write the entire
				//content to the log.  DO NOT USE FOR PRODUCTION.

				targetConn.connect();
				String sentContent = 
					copyStreamsWithTrace(request.getInputStream(), targetConn.getOutputStream(), request.getCharacterEncoding());
				log.trace("Sending this POST content to '" + targetURL.toExternalForm()  + "': " + sentContent);
				
			} else {
				// Establish connection to forwarded server
				targetConn.connect();

				// No custom params are defined, so we can just copy the body of
				// the request from the incoming request to the forwarded request
				copyStreams(request.getInputStream(), targetConn.getOutputStream());
			}

			// Copy header back from the forwarded response to the servlet response
			copyHeaders(targetConn, response);

			response.setStatus(targetConn.getResponseCode());

			if (log.isTraceEnabled()) {
				dumpHeadersToTrace(targetConn);
			}

			// Copy the response content from the forward response to the
			// servlet response
			try {

				String extraPath =  request.getPathInfo();
				targetIs = targetConn.getInputStream();
				
				UrlFeatures urlFeatures = SparrowUtil.getRequestUrlFeatures(request);
				
				String requestServerName = urlFeatures.serverName;
				String requestServerPort = urlFeatures.serverPort;
				String contextPath = urlFeatures.contextPath;
				
				if ("/query".equals(extraPath)) {
					
					
					if (HTML_MIME_TYPE.equals(request.getHeader("Accept"))) {
						log.debug("POST to '" + targetURL.toExternalForm()  + "' is a /query for HTML content");
						InputStream xsltStream = this.getClass().getClassLoader().getResourceAsStream(CSW_QUERY_RESULTS_TO_HTML_XSLT_PATH);
						
						copyStreams(targetIs, response, xsltStream, requestServerName, requestServerPort, contextPath);
					} else {
						//It should be asking for XML otherwise, but we have nothing else to offer
						
						log.debug("POST to '" + targetURL.toExternalForm()  + "' is a /query for XML content");
						responseOutputStream = response.getOutputStream();
						
						if (log.isTraceEnabled()) {
							copyStreamsWithTrace(targetIs, responseOutputStream, request.getCharacterEncoding());
						} else {
							copyStreams(targetIs, responseOutputStream);
						}
					}
				} else if ("/byid".equals(extraPath)) {
					if (HTML_MIME_TYPE.equals(request.getHeader("Accept"))) {
						log.debug("POST to '" + targetURL.toExternalForm()  + "' is a /byid for HTML content");
						InputStream xsltStream = this.getClass().getClassLoader().getResourceAsStream(CSW_ID_RESULT_TO_HTML_XSLT_PATH);
						copyStreams(targetIs, response, xsltStream, requestServerName, requestServerPort, contextPath);;
					} else {
						//It should be asking for XML otherwise, but we have nothing else to offer
						log.debug("POST to '" + targetURL.toExternalForm()  + "' is a /byid for XML content");
						responseOutputStream = response.getOutputStream();
						
						if (log.isTraceEnabled()) {
							copyStreamsWithTrace(targetIs, responseOutputStream, request.getCharacterEncoding());
						} else {
							copyStreams(targetIs, responseOutputStream);
						}
						
					}
				} else {
					log.debug("POST to '" + targetURL.toExternalForm()  + "' is a general request (no special handling)");
					responseOutputStream = response.getOutputStream();
					copyStreams(targetIs, responseOutputStream);
				}

			}
			catch (IOException e) {
				log.warn("An exception was thrown by the Proxy Servlet "
						+ "while copying streams", e);
				if (log.isDebugEnabled()) {
					dumpRequest(request);

					//attempting to access these seems to cause another error
					//dumpURLConnProperties(fwdConn);
				}

				//This error caught after we may have already sent some bits, 
				//so the best we can do is dump the error contents so the 
				//connection can be re-pooled.
				handleErrorStream(targetConn, null);
			}

		}
		catch (Exception e) {
			log.warn("An exception was thrown in the Proxy Servlet", e);
			if (log.isDebugEnabled()) {
				dumpRequest(request);
                //attempting to access these seems to cause another error
				//dumpURLConnProperties(targetConn);
			}

			//This err is caught before writing to the output stream, so send err content
			response.setStatus(targetConn.getResponseCode());
			handleErrorStream(targetConn, responseOutputStream);
		}
		finally {
			if (targetConnWriter != null) {
				try {
					targetConnWriter.close();
				}
				catch (Exception e) {
					//This is likely not an err at all - could already be closed.
				}
			}

			if (targetIs != null) {
				try {
					targetIs.close();
				}
				catch (Exception e) {
					//This is likely not an err at all - could already be closed.
				}
			}

		}
	}
	


	/**
	 * Copies the input stream to the response and does an xslt transformation.
	 * 
	 * This method will also replace production app urls with urls that match
	 * the server and port of the request.
	 * 
	 * @param src
	 * @param response
	 * @param xsltStream
	 * @param requestedServerName
	 * @param requestedServerPort
	 * @throws IOException
	 */
	public static void copyStreams(InputStream src, HttpServletResponse response,
			InputStream xsltStream, String requestedServerName, String requestedServerPort, String contextPath)
			throws IOException {

		

		Source styleSource = new StreamSource(xsltStream);

		Source xmlSource = new StreamSource(src);

		// Ok... Transform the xml:
		try {
			Transformer transformer = transformerFactory.newTransformer(styleSource);
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, UTF_8);
			transformer.setOutputProperty(OutputKeys.METHOD, "html");
			
			CharArrayWriter caw = new CharArrayWriter();
			StreamResult result = new StreamResult(caw);
			transformer.transform(xmlSource, result);

			String strResponse = caw.toString();
			
			//The application links in GeoNetwork link to the production version
			//of the app.  If we are testing or running a special purpose server,
			//the links need to point to the same server and port, not to the prod
			//link.  Also, the 'official' and journal article published url for
			//the app is http://water.usgs.gov/nawqa/sparrow/dss/
			//However, this redirects to cida.  Rather than redirect the user
			//after they are already at the cida url, we just send them the
			//actual cida url instead.  (Redirecting will result in the user
			//killing their session cookies)
			

			String serverToUse = "http://";
			if (requestedServerPort != null && requestedServerPort.length() > 0 && ! requestedServerPort.equals("80")) {
				serverToUse = serverToUse + requestedServerName + ":" + requestedServerPort + contextPath + "/";
			} else {
				serverToUse = serverToUse + requestedServerName + contextPath + "/";
			}
			
			
			//Replace any refs to the published name w/ the current location
			strResponse = strResponse.replace(PUBLISHED_SERVER_NAME, serverToUse);
			
			//Also replace any refs to the actual cida location if we are not
			//on production.
			if (! ACTUAL_SERVER_NAME.equals(requestedServerName)) {
				strResponse = strResponse.replace(ACTUAL_SERVER_NAME, serverToUse);
			}
			
			response.setContentType(HTML_MIME_TYPE);
			response.setCharacterEncoding(UTF_8);
			response.setContentLength(strResponse.length());
			
			response.getWriter().write(strResponse);
		} catch (Exception ex) {
			try {
				PrintWriter pw = response.getWriter();
				pw.println(ex.toString());
			} catch (Exception ee) {
				
			} finally {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "some sort of error");
			}

		}
	}
	
	/**
	 * Copies the streams and returns a String copy of what the copied content was.
	 * 
	 * @param src
	 * @param dest
	 * @param traceCharsetName The character set to use when converting to a string.
	 * @return
	 * @throws IOException
	 */
	public static String copyStreamsWithTrace(InputStream src, OutputStream dest, String traceCharsetName)
			throws IOException {

		long bytesRead = 0L;
		ByteArrayOutputStream traceOut = new ByteArrayOutputStream();
		
		try {
			byte[] buf = new byte[BLKSIZ];
			int size;
		
			while ((size = src.read(buf)) > -1) {
				dest.write(buf, 0, size);
				traceOut.write(buf, 0, size);
				bytesRead += size;
			}
		} finally {
		    try {
		        if (src != null) { src.close(); }
		    } catch (IOException e) { /* do nothing */ }
		    try {
		        if (dest != null) { dest.close(); }
		    } catch (IOException e) {/* do nothing */ }
		}
		
		if (traceCharsetName != null) {
			return traceOut.toString(traceCharsetName);
		} else {
			return traceOut.toString("UTF-8");
		}
	}

}
