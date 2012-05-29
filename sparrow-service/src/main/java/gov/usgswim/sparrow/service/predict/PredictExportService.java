package gov.usgswim.sparrow.service.predict;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.impl.ColumnDataFromTable;
import gov.usgswim.datatable.impl.SimpleDataTable;
import gov.usgswim.service.HttpService;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.PredictDataBuilder;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.domain.AdjustmentGroups;
import gov.usgswim.sparrow.domain.PredictionContext;
import gov.usgswim.sparrow.domain.SparrowModel;
import gov.usgswim.sparrow.domain.AggregationLevel;
import gov.usgswim.sparrow.request.HUCTableRequest;
import gov.usgswim.sparrow.request.ModelRequestCacheKey;
import gov.usgswim.sparrow.request.UnitAreaRequest;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.SparrowResourceUtils;

import javax.xml.stream.XMLStreamReader;

public class PredictExportService implements HttpService<PredictExportRequest> {

    public XMLStreamReader getXMLStreamReader(PredictExportRequest req,
            boolean isNeedsCompleteFirstRow) throws Exception {
    	
    	SharedApplication sharedApp = SharedApplication.getInstance();
    	
        Integer predictionContextID = req.getContextID();
        PredictionContext adjPredictContext = req.getContext();
        Long modelId = null;

        if (adjPredictContext != null) {
        	//The context was supplied w/ the request
        	modelId = adjPredictContext.getModelID();
        } else if (predictionContextID != null) {
        	//The context was passed by ID
            adjPredictContext = sharedApp.getPredictionContext(predictionContextID);
            modelId = adjPredictContext.getModelID();
        } else {
            adjPredictContext = new PredictionContext(req.getModelID(), null, null, null, null, null);
            modelId = req.getModelID();
        }
        
        
        
        DataTable watershedAreas = sharedApp.getCatchmentAreas(new UnitAreaRequest(modelId, AggregationLevel.NONE, true));
        //DataTable huc8 = sharedApp.getHUCData(new HUCTableRequest(modelId), false);
        SparrowColumnSpecifier adjDataColumn = adjPredictContext.getDataColumn();
    	SparrowColumnSpecifier orgDataColumn = null;
    	PredictResult adjPredictResult = null;
    	PredictResult orgPredictResult = null;
    	PredictData adjPredictData = null;
    	PredictData orgPredictData = null;
    	DataTable reachIdAttribs = null;
    	DataTable huc8 = null;
    	DataTable reachStatsTable = null;
    	
		//Get the readme text
    	SparrowModel model = sharedApp.getModelMetadata(new ModelRequestCacheKey(modelId, false, false, false)).get(0);
		String networkName = model.getEnhNetworkName();
		String networkUrl = model.getEnhNetworkUrl();
		String networkIdColumn = model.getEnhNetworkIdColumn();
		
		String readmeText = SparrowResourceUtils.lookupMergedHelp(
				model.getId().toString(),
				"CommonTerms.Export Readme",
				null,
				new Object[] {"networkName", networkName, "networkUrl", networkUrl, "networkIdColumn", networkIdColumn});
		
    	
		//shortcut ref
		AdjustmentGroups adjGroups = adjPredictContext.getAdjustmentGroups();
		
		if (req.isIncludeAdjSource()) {
	        if (adjGroups != null && adjGroups.hasAdjustments()) {
	
	    		DataTable adjustedSources =
	    			sharedApp.getAdjustedSource(adjGroups);
	
	    		
	    		//Assembling the predict data in this way does not actually create
	    		//a new copy of the data.
	    		PredictDataBuilder mutable = sharedApp.getPredictData(modelId).getBuilder();
	    		mutable.setSrc(adjustedSources);
	        	
	        	adjPredictData = mutable.toImmutable();
	        	
	        } else {
	        	adjPredictData = sharedApp.getPredictData(modelId);
	        }
		}
		
		if (req.isIncludeOrgSource()) {
			orgPredictData = sharedApp.getPredictData(modelId);
		}
        
        if (req.isIncludeOrgPredict()) {
        	orgPredictResult = sharedApp.getPredictResult(adjPredictContext.getNoAdjustmentVersion().getAdjustmentGroups());
        	orgDataColumn = adjPredictContext.getNoAdjustmentVersion().getDataColumn();
        }
        
        if (req.isIncludeAdjPredict()) {
        	if (adjGroups != null && adjGroups.hasAdjustments()) {
        		adjPredictResult = sharedApp.getPredictResult(adjPredictContext.getAdjustmentGroups());
        	} else {
        		adjPredictResult = sharedApp.getPredictResult(adjPredictContext.getNoAdjustmentVersion().getAdjustmentGroups());
        	}
        }
        
        //Include optional identification information
        if (req.isIncludeReachIdAttribs()) {
        	reachIdAttribs = sharedApp.getModelReachIdentificationAttributes(modelId);
        	huc8 = sharedApp.getHUCData(new HUCTableRequest(modelId), false);
        }
        
        //Include optional stats information
        if (req.isIncludeReachStatAttribs()) {
        	
        	ColumnData[] statColumns = new ColumnData[2];
        	
        	
        	SparrowColumnSpecifier scs = sharedApp.getStreamFlow(modelId);
        	statColumns[0] = new ColumnDataFromTable(scs.getTable(), scs.getColumn());
        	
			UnitAreaRequest catchAreaReq = new UnitAreaRequest(modelId, AggregationLevel.NONE, false);
			DataTable catchmentAreaTab = sharedApp.getCatchmentAreas(catchAreaReq);
			statColumns[1] = new ColumnDataFromTable(catchmentAreaTab, 1);
			
			//Merge the columns into a single table
			reachStatsTable = new SimpleDataTable(statColumns, "Reach Statistics", null, null);
        }
        
        
        return new  PredictExportSerializer(
        		req, adjPredictContext, 
        		adjDataColumn, orgDataColumn,
    			adjPredictData, orgPredictData,
    			adjPredictResult, orgPredictResult,
    			watershedAreas, huc8, reachIdAttribs, reachStatsTable, readmeText);
    }

    public void shutDown() {
    }
}
