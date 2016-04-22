package gov.usgswim.sparrow.service.maplayer;

import static gov.usgs.cida.sparrow.service.util.ServiceResponseMimeType.XML;
import gov.usgs.cida.sparrow.service.util.ServiceResponseOperation;
import static gov.usgs.cida.sparrow.service.util.ServiceResponseStatus.FAIL;
import gov.usgs.cida.sparrow.service.util.ServiceResponseWrapper;
import gov.usgswim.sparrow.action.CreateGeoserverLayer;
import gov.usgswim.sparrow.action.WriteDbfFileForContext;
import gov.usgswim.sparrow.domain.PredictionContext;
import gov.usgswim.sparrow.postgres.action.CreateViewForLayer;
import gov.usgswim.sparrow.service.AbstractSparrowServlet;
import gov.usgswim.sparrow.service.SharedApplication;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RegisterMapLayerService extends AbstractSparrowServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doActualGet(HttpServletRequest httpReq, HttpServletResponse resp)
			throws ServletException, IOException {

		//Since the WPS returns its own wrapper in all cases except an error,
		//this wrapper is only used for errors.
		ServiceResponseWrapper wrap = new ServiceResponseWrapper(
				String.class, ServiceResponseOperation.CREATE);
		wrap.setStatus(FAIL); // pessimistic...
		wrap.setMimeType(XML);

		Map params = httpReq.getParameterMap();

        Integer contextId = getInteger(params, "context-id");
		String projectedSrs = getClean(params, "projected-srs");
		
		log.trace("Received a request to register a map layer for contextid: " + contextId + " projection: " + projectedSrs);
		
		if (projectedSrs == null) {
			log.debug("The projectedSrs is not specified - the default from GeoServer will be used.");
		}
			
		try {
			
			if (contextId == null) {
				throw new Exception("The context ID cannot be empty");
			}

					
			PredictionContext context = SharedApplication.getInstance().getPredictionContext(contextId);
			
			if (context == null) {
				throw new Exception("The context for the id '" + contextId + "' cannot be found");
			}
			             
			//Write the data column of the context  if it does not yet exist
			WriteDbfFileForContext writeDbfFile = new WriteDbfFileForContext(context); //TODO SPDSSII-28 write the row to the postgres table model_output
                        HashMap dbfValuesMap = writeDbfFile.run();
//			File dbfFile = writeDbfFile.getDbfFile(); //remove after test
//			if (!dbfFile.exists()) {
//				dbfFile = writeDbfFile.run();
//			}
			
                        // replacing the dbf joiner...
                      // HashMap dbfValuesMap = getModelOutputValues(context);
                       // HashMap dbfValuesMap = new HashMap();
                        CreateViewForLayer cvlAction = new CreateViewForLayer(context, dbfValuesMap);  //this will need the name of the layers or view names returned for the CreateGeoserverLayer??                    
                        List viewNames = cvlAction.run();


                File dbfFile = new File("Remove after ","dbf file writer test");  //fake remove
			//Register the data plus the shapefile w/ GeoServer as a layer
			CreateGeoserverLayer cglAction = new CreateGeoserverLayer(context, viewNames, projectedSrs);
			String wpsResponse = cglAction.run();
			
			if (cglAction.getException() != null) {
				throw cglAction.getException();	//caught below and handled
			} else if (wpsResponse == null || wpsResponse.length() == 0) {
				throw new Exception("The map server returned an empty response");
			} else {
				sendResponse(resp, wpsResponse, XML);	//The action/WPS response is itself an XML wrapper
				return;
			}
			
		} catch (Throwable e) {
			log.error("Register map layer request for contextid: " + contextId + " caused an Exception", e);
			wrap.setError(e);
		}
		
		//This only happens if there is an error
		sendResponse(resp, wrap);

	}

	@Override
	protected void doActualPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doActualGet(req, resp);
	}
	
}
