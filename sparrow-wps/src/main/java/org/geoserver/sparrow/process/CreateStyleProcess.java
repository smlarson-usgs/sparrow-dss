package org.geoserver.sparrow.process;

import gov.usgs.cida.sparrow.service.util.ServiceResponseOperation;
import gov.usgs.cida.sparrow.service.util.ServiceResponseStatus;
import gov.usgs.cida.sparrow.service.util.ServiceResponseWrapper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.geoserver.catalog.Catalog;
import org.geoserver.wps.gs.GeoServerProcess;
import org.geotools.process.ProcessException;
import org.geotools.process.factory.DescribeParameter;
import org.geotools.process.factory.DescribeProcess;
import org.geotools.process.factory.DescribeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Resources;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import org.apache.commons.io.FileUtils;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.catalog.impl.StyleInfoImpl;
import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.platform.GeoServerExtensions;
import org.vfny.geoserver.util.SLDValidator;
		
/**
 * WPS to add a Style to the catalog, optionally replacing an existing style of
 * the same name if present.
 * 
 * This WPS is expecting to shapefiles structured on the server as shown:
 * <pre>
 *	|-sparrow_data (this directory is configured in jndi as 'shapefile-directory')
 *		|-shapefile
 *			|-MRB_1_NHD (this directory passed as 'coverageName' execution arg)
 *				|-flowline
 *					|-coverage.shp
 *					|-coverage.shp.xml
 *					|- ... (other files associated w/ the shapefile)
 *				|-catchment
 *					|-coverage.shp (all shapefiles have the 'coverage' name)
 *					|- ... (other files associated w/ the shapefile)
 *	|-MRB_2_E2RF1...
 * </pre>
 * 
 * For a single execution, a flowline and catchment layer are registered, each
 * in a separate namespace.
 * 
 * The response is an XML document:
 * <pre>
 *	&lt;sparrow-wps-response service-name=\"CreateDatastoreProcess\"&gt;
 *		&lt;status&gt;OK&lt;/status&gt;
 *		&lt;wms-endpoint&gt;
 *			&lt;url&gt;url to access the WMS service at&lt;/url&gt;
 *			&lt;flowline-layer&gt;fully qualified layer name for the flowline layer&lt;/flowline-layer&gt;
 *			&lt;catchment-layer&gt;fully qualified layer name for the catchment layer&lt;/catchment-layer&gt;
 *		&lt;/wms-endpoint&gt;
 *	&lt;sparrow-wps-response/&gt;
 * </pre>
 * @author eeverman
 */
@DescribeProcess(title="CreateStyleProcess", version="1.0.0",
		description="Creates style based on passed style parameters.  OK to re-request styles - they will not be recreated, however.")
public class CreateStyleProcess implements SparrowWps, GeoServerProcess {
	Logger log = LoggerFactory.getLogger(CreateStyleProcess.class);
	
	//Set on construction
	private Catalog catalog;
	
	//Self init
	private GeoServerDataDirectory gsDataDirectory;
	
	//Set per request
	private String styleName;
	private String workspaceName;
	private String sldText;
	private String sldUrl;
	private Boolean overwrite;
	
	//Self init per request
	private WorkspaceInfo workspace;
	
	public CreateStyleProcess(Catalog catalog) {
		this.catalog = catalog;
		
		//Check to see if we can access the base shapefile directory
		try {
			gsDataDirectory = ((GeoServerDataDirectory) GeoServerExtensions.bean("dataDirectory"));
			gsDataDirectory.findDataRoot();
		} catch (Exception e) {
			log.error("Configuration Error.", e);
		}
	}
	
	/**
	 * Requests that the layer be created if it does not already exist.
	 * 
	 * @param styleName
	 * @param workspaceName
	 * @param sldText
	 * @param sldUrl
	 * @param overwrite
	 * @return
	 * @throws Exception 
	 */
	@DescribeResult(name="response", description="Registers a style with GeoServer and makes it permanently available.", type=ServiceResponseWrapper.class)
	public ServiceResponseWrapper execute(
			@DescribeParameter(name="styleName", description="Name to use for the style, not including a workspace name", min = 1, max = 1) String styleName,
			@DescribeParameter(name="workspaceName", description="Name of the workspace to use for the style, which must exist.  Null OK to put in the default namespace", min = 0, max = 1) String workspaceName,
			@DescribeParameter(name="sldText", description="Actual Text of the SLD, which must be valid SLD XML.  If not specified, the sldUrl is required.", min = 0, max = 1) String sldText,
			@DescribeParameter(name="sldUrl", description="URL that should return a valid SLD XML document encoded w/ UTF-8.  If not specified, the sldText is required.", min = 0, max = 1) String sldUrl,
			@DescribeParameter(name="overwrite", description="If true and there is an existing style of the same name/workspace, the new style will replace the old.", min = 0, max = 1) Boolean overwrite
		) throws Exception {

		
		
		this.styleName = styleName;
		this.workspaceName = workspaceName;
		this.sldText = sldText;
		this.sldUrl = sldUrl;
		this.overwrite = overwrite;
		
		if (this.overwrite == null) this.overwrite = Boolean.FALSE;
		
		ServiceResponseWrapper wrap = new ServiceResponseWrapper();
		wrap.setEntityClass(SparrowStyleResponse.class);
		wrap.setOperation(ServiceResponseOperation.CREATE);
		wrap.setStatus(ServiceResponseStatus.OK);
		
		init(wrap);	//Will set wrap status to something other than OK if there is an issue

		if (ServiceResponseStatus.OK.equals(wrap.getStatus())) {
			
			Exception e = null;
			
			try {
				createStyle(wrap);
			} catch (Exception ee) {
				e = ee;
			}
			
			if (! wrap.getStatus().toString().startsWith("OK")) {
				
				//This is a failure handled during creation
				log.error("FAILED to create the new style {} in workspace {}.  Message: {}", new Object[] {styleName, workspaceName, wrap.getMessage()});
				
			} else if (e != null) {
				
				//This is an unhandled error during create
				wrap.setStatus(ServiceResponseStatus.FAIL);
				wrap.setError(e);
				
				String msg = "FAILED:  An unexpected error happened during the creation of create style " + styleName + " in workspace " + workspaceName;
				wrap.setMessage(msg);
				log.error(msg);
			}
		} else {
			log.error("The validation failed with this message: {}", new Object[] {wrap.getMessage()});
		}
		
		return wrap;
	}
	
	/**
	 * Initiate the self-initialized params from the user params and does validation.
	 */
	protected void init(ServiceResponseWrapper wrap) {
		
		if (sldText == null && sldUrl == null) {
			wrap.setMessage("Both the sldText and the sldUrl are set to null.  Exactly one must be specified.");
			wrap.setStatus(ServiceResponseStatus.FAIL);
			return;
			
		} else if (sldText != null && sldUrl != null) {
			wrap.setMessage("Both the sldText and the sldUrl are specified.  Only one may be specified.");
			wrap.setStatus(ServiceResponseStatus.FAIL);
			return;
			
		} else if (sldUrl != null) {
			try {
				URL url = new URL(sldUrl);
				sldText = Resources.toString(url, Charset.forName("UTF-8"));
				
			} catch (Exception e) {
				wrap.setMessage("Unable to load or read the SLD from the url: " + sldUrl);
				wrap.setStatus(ServiceResponseStatus.FAIL);
				return;
			}
		}
		
		if (workspaceName != null) {
			
			workspace = catalog.getWorkspaceByName(workspaceName);
			if (workspace == null) {
				wrap.setMessage("The workspace " + workspaceName + " does not exist.");
				wrap.setStatus(ServiceResponseStatus.FAIL);
				return;
			}
		}
		
		//Validate the sld text
		InputStream stream = new ByteArrayInputStream(sldText.getBytes());
		SLDValidator sldValidator = new SLDValidator();
		List errors = sldValidator.validateSLD(stream);

		if (errors.size() > 0) {
			wrap.setMessage("Invalid SLD XML Content: " + SLDValidator.getErrorMessage(stream, errors));
			wrap.setStatus(ServiceResponseStatus.FAIL);
		}
	}
	
	
	
	protected void createStyle(ServiceResponseWrapper wrap) throws Exception {
		
		SparrowStyleResponse resp = new SparrowStyleResponse();
		
		StyleInfo existingStyle = catalog.getStyleByName(workspaceName, styleName);
		
		if (existingStyle != null && !overwrite) {
			
			log.debug("Request to create style {} in workspace {} OK.  Style already existed and the overwrite flag is false, so no action taken.", new Object[] {styleName, workspaceName});
			
			resp.setStyleName(existingStyle.getName());
			resp.setWorkspaceName((existingStyle.getWorkspace() != null)?existingStyle.getWorkspace().getName():null);
			wrap.setStatus(ServiceResponseStatus.OK_ALREADY_EXISTS);
			wrap.addEntity(resp);
			
			return;
		}
		
		//In all other cases, delete the existing layer if it exists.
		if (existingStyle != null) {
			catalog.remove(existingStyle);
		}


		//
		//Create the new layer
		
		
		StyleInfoImpl newStyleInfo = new StyleInfoImpl(catalog);
		newStyleInfo.setFilename(styleName + ".sld");
		newStyleInfo.setName(styleName);
		newStyleInfo.setWorkspace(workspace);
		newStyleInfo.setSLDVersion(new org.geotools.util.Version("1.0.0"));
		
		InputStream stream = new ByteArrayInputStream(sldText.getBytes());
		
		File styleSldFile = gsDataDirectory.findOrCreateStyleSldFile(newStyleInfo);
		
		//Now deleting any associated sld xml file.  AT this point, if its present it needs to go.
		if (styleSldFile.exists()) {
			styleSldFile.delete();
		}
		

		//put in correct workspace dir (if spec'ed, or put into the global styles.
		FileUtils.copyInputStreamToFile(stream, styleSldFile);

		//Check for errors in the setup of the new style
		List<RuntimeException> errors = catalog.validate(newStyleInfo, true);
		
		if (errors.size() > 0) {
			
			if (existingStyle != null) {
				String msg = "Request to create style " + styleName + " in workspace " + workspaceName + " FAILED.  Unfortunately, there was an existing style of the same name, which was deleted and the new one could not be created.";
				wrap.setMessage(msg);
			} else {
				String msg = "Request to create style " + styleName + " in workspace " + workspaceName + " FAILED.";
				wrap.setMessage(msg);
			}
			
			wrap.setStatus(ServiceResponseStatus.FAIL);
			return;
		}
		
		
		catalog.add(newStyleInfo);
		
		resp.setStyleName(newStyleInfo.getName());
		resp.setWorkspaceName(workspaceName);
		wrap.addEntity(resp);

	}
	
}
