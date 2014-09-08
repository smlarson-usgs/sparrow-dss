package gov.usgs.cida.sparrow.service.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.jndi.JndiTemplate;

public class GeoServerConnection {
	protected static Logger log =
			Logger.getLogger(GeoServerConnection.class); //logging for this class
	
	private static final int BUFFER_SIZE = 4096;
	
	private static final String DEFAULT_GEOSERVER_HOST = "cida-eros-sparrowdev.er.usgs.gov";
	private static final String DEFAULT_GEOSERVER_PATH = "/geoserver";
	private static final int DEFAULT_GEOSERVER_PORT = 8081;
	
	private String geoserverHost;
	private int geoserverPort;
	private String geoserverPath;
	
	private String fullURL;
	
	/*
	 * INSTANCE
	 */
	private static GeoServerConnection INSTANCE = null;
	
	private GeoServerConnection() {
		JndiTemplate template = new JndiTemplate();
		
		try {
			geoserverHost = (String)template.lookup("java:comp/env/geoserver-host");
		} catch(Exception e){
			log.error("GeoServerConnection Constructor Exception: Unable to load "
					+ " configuration from context.xml for the GeoServer Host.  Using "
					+ "[" + GeoServerConnection.DEFAULT_GEOSERVER_HOST + "] as default.");
			geoserverHost = GeoServerConnection.DEFAULT_GEOSERVER_HOST;
		}
		
		try {
			geoserverPort = (Integer)template.lookup("java:comp/env/geoserver-port");			
		} catch(Exception e){
			log.error("GeoServerConnection Constructor Exception: Unable to load "
					+ " configuration from context.xml for the GeoServer Port.  Using "
					+ "[" + GeoServerConnection.DEFAULT_GEOSERVER_PORT + "] as default.");
			geoserverPort = GeoServerConnection.DEFAULT_GEOSERVER_PORT;
		}
		
		try {
			geoserverPath = (String)template.lookup("java:comp/env/geoserver-path");			
		} catch(Exception e){
			log.error("GeoServerConnection Constructor Exception: Unable to load "
					+ " configuration from context.xml for the GeoServer Path.  Using "
					+ "[" + GeoServerConnection.DEFAULT_GEOSERVER_PATH + "] as default.");
			geoserverPath = GeoServerConnection.DEFAULT_GEOSERVER_PATH;
		}
		
		this.fullURL = "http://" + geoserverHost + ":" + geoserverPort + geoserverPath + "/";
	}
	
	public static GeoServerConnection getInstance() {
		if(GeoServerConnection.INSTANCE == null) {
			synchronized (GeoServerConnection.class) {
				if(GeoServerConnection.INSTANCE == null) {
					GeoServerConnection.INSTANCE = new GeoServerConnection();
				}
			}
		}
        return GeoServerConnection.INSTANCE;
    }
	
	/**
	 * 
	 * @param request
	 * @return GeoServerResponse object containing the raw response from GeoServer.
	 * <br/><br/>
	 * It is the clients responsibility to parse the byte[] content from GeoServer
	 * by inspecting the headers contained in the response object.
	 * @throws Exception 
	 */
	public GeoServerResponse doRequest(String request) throws Exception {
		GeoServerResponse result = null;
		
		URL url = null;
		try {
			url = new URL(this.fullURL + request);
		} catch (MalformedURLException e) {
			String msg = "GeoServerResponse.doRequest() MalformedURLException : unable to create URL from request [" + request +"].  Exception: [" + e.getMessage() +"]";
			log.error(msg);
			throw new Exception(msg);
		}
		
		InputStream responseStream = null;
    	try {
    		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
    		responseStream = connection.getInputStream();
	
	        // Get the response code
    		int status = connection.getResponseCode();
    		result = new GeoServerResponse(url, status);
    		
    		if(status == 200) {
	    		result.setResponseHeaders(connection.getHeaderFields());
	    		result.setContentEncoding(connection.getContentEncoding());
	    		result.setContentType(connection.getContentType());
	    		result.setContentLength(connection.getContentLength());
	    		
	    		/**
	    		 * Create a file and stream the content to it as we read it from
	    		 * the stream.  The reason we do this is two-fold:
	    		 * 		1) The resulting content could be very large (GML from GeoServer)
	    		 * 		2) We only need a little bit of data out of it and its GML so
	    		 * 		   we will be using a SAX parser which reads from a file more
	    		 * 		   efficiently than a String.
	    		 */
	    		String uuid = UUID.randomUUID().toString().replaceAll("-", "");
	    		Path tempFile = Paths.get(System.getProperty("java.io.tmpdir"), uuid);
	    		long pureLength = Files.copy(responseStream, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);	    		
	    		result.setContentsRead(pureLength);
	    		result.setFilename(tempFile.toString());
    		} else {
    			String msg = "GeoServerResponse.doRequest() HTTP Result Code != 200 : [" + status + "] for request [" + request + "]";
    			log.error(msg);
    			throw new Exception(msg);
    		}
    	} catch (Exception e) {
    		String msg = "GeoServerResponse.doRequest() Exception : " + e.getMessage();
    		log.error(msg);
    		throw new Exception(msg);
    		
		} finally {
			try {
				if(responseStream != null) {
					responseStream.close();
				}
			} catch (IOException e) {
				log.warn("GeoServerResponse.doRequest() Closing response inputstream exception : " + e.getMessage());
			}
		}
		
		return result;
	}
	
	
	
	/**
	 * Class to encapsulate a raw response from GeoServer
	 */
	public class GeoServerResponse {
		private URL originalURL;
		private int status;
		private String contentEncoding;
		private int contentLength;
		private String contentType;
		private Map<String, String> contentTypes;
		private Map<String, List<String>> responseHeaders;
		private String filename;
		private long contentsRead;
		
		public GeoServerResponse(URL url, int responseStatus) {
			this.originalURL = url;
			this.status = responseStatus;
			
			this.contentEncoding = "";
			this.contentLength = 0;
			this.contentType = "";
			this.filename = "";
			this.contentTypes = new HashMap<String, String>();
			this.responseHeaders = new HashMap<String, List<String>>();
		}

		public String getContentEncoding() {
			return contentEncoding;
		}

		public void setContentEncoding(String contentEncoding) {
			this.contentEncoding = contentEncoding;
		}

		public int getContentLength() {
			return contentLength;
		}

		public void setContentLength(int contentLength) {
			this.contentLength = contentLength;
		}

		public String getContentType() {
			return contentType;
		}
		
		public Map<String, String> getContentTypes() {
			return contentTypes;
		}

		public void setContentType(String contentType) {
			int mainTypeCount = 1;
			for (String param : contentType.replace(" ", "").split(";")) {
				if(param.contains("=")) {
					String[] parts = param.split("=", 2);
					this.contentTypes.put(parts[0], parts[1]);
				} else {
					this.contentTypes.put("type" + mainTypeCount, param);
					mainTypeCount++;
				}
    		}
			
			this.contentType = contentType;
		}

		public Map<String, List<String>> getResponseHeaders() {
			return responseHeaders;
		}

		public void setResponseHeaders(Map<String, List<String>> responseHeaders) {
			this.responseHeaders = responseHeaders;
		}

		public String getFilename() {
			return filename;
		}

		public void setFilename(String filename) {
			this.filename = filename;
		}

		public URL getOriginalURL() {
			return originalURL;
		}

		public int getStatus() {
			return status;
		}

		public long getContentsRead() {
			return contentsRead;
		}

		public void setContentsRead(long pureLength) {
			this.contentsRead = pureLength;
		}
	}
	
	
}
