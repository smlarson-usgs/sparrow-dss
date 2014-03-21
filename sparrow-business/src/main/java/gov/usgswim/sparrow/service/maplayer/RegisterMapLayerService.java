package gov.usgswim.sparrow.service.maplayer;

import static gov.usgswim.sparrow.service.ServiceResponseStatus.FAIL;
import static gov.usgswim.sparrow.service.ServiceResponseStatus.OK;
import gov.usgswim.sparrow.action.CreateGeoserverLayer;
import gov.usgswim.sparrow.action.WriteDbfFileForContext;
import gov.usgswim.sparrow.domain.PredictionContext;
import gov.usgswim.sparrow.service.AbstractSparrowServlet;
import gov.usgswim.sparrow.service.ServiceResponseOperation;
import gov.usgswim.sparrow.service.ServiceResponseWrapper;
import gov.usgswim.sparrow.service.SharedApplication;
import java.io.File;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RegisterMapLayerService extends AbstractSparrowServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doActualGet(HttpServletRequest httpReq, HttpServletResponse resp)
			throws ServletException, IOException {

		ServiceResponseWrapper wrap = new ServiceResponseWrapper(
				String.class, ServiceResponseOperation.REGISTER);
		wrap.setStatus(FAIL); // pessimistic...
		wrap.setMimeType(parseMime(httpReq));

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
			
			//Write the data column of the context to disk
			WriteDbfFileForContext writeDbfFile = new WriteDbfFileForContext(context);
			File dbfFile = writeDbfFile.run();
			
			//Register the data plus the shapefile w/ GeoServer as a layer
			CreateGeoserverLayer cglAction = new CreateGeoserverLayer(context, dbfFile, projectedSrs);
			String layerName = cglAction.run();
			
			Throwable actionExp = cglAction.getException();
			
			if (actionExp == null) {
				wrap.setStatus(OK);
				wrap.addEntity(layerName);
			} else {
				wrap.setStatus(FAIL);
				wrap.setMessage(actionExp.getMessage());
			}
			
		} catch (Exception e) {
			log.error("Register map layer request for contextid: " + contextId + " caused an Exception", e);
			wrap.setError(e);
		}

		sendResponse(resp, wrap);
	}

	@Override
	protected void doActualPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doActualGet(req, resp);
	}
	
}
