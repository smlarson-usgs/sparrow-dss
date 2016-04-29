package gov.usgswim.sparrow.service.maplayer;

import static gov.usgs.cida.sparrow.service.util.ServiceResponseMimeType.XML;
import gov.usgs.cida.sparrow.service.util.ServiceResponseOperation;
import static gov.usgs.cida.sparrow.service.util.ServiceResponseStatus.FAIL;
import gov.usgs.cida.sparrow.service.util.ServiceResponseWrapper;
import gov.usgswim.sparrow.action.CreateGeoserverLayer;
import gov.usgswim.sparrow.action.WriteDbfFileForContext;
import gov.usgswim.sparrow.domain.PredictionContext;
import gov.usgswim.sparrow.postgres.action.CreateViewForLayer;
import gov.usgswim.sparrow.postgres.action.DeleteModelOutput;
import gov.usgswim.sparrow.postgres.action.LoadInitialViews;
import gov.usgswim.sparrow.service.AbstractSparrowServlet;
import gov.usgswim.sparrow.service.SharedApplication;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RegisterMapLayerService extends AbstractSparrowServlet {

	private static final long serialVersionUID = 1L;
        private static boolean hasLoadedInitialViews = false;

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
			
                        if (!hasLoadedInitialViews) {
                        // LoadInitialViews script that will identify any model_output rows that should have views created and create them.
                        // Typically, this will only be done when the application is bounced to bring in a new model. 
                        // Requires that the model output rows have been inserted via a script (from a shape dbf file).
                        // Part two is to expose the views as layers on geoserver via the cglAction below.
                            LoadInitialViews loadViews = new LoadInitialViews();
                            loadViews.doAction();
                            hasLoadedInitialViews = true;
                        }
          
			//Write the data column of the context if it does not yet exist
			WriteDbfFileForContext getOutputValues = new WriteDbfFileForContext(context); //TODO SPDSSII-28 write the row to the postgres table model_output
                        HashMap dbfValuesMap = getOutputValues.run();

                        CreateViewForLayer cvlAction = new CreateViewForLayer(context, dbfValuesMap);                    
                        List viewNames = cvlAction.run();
                        

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
