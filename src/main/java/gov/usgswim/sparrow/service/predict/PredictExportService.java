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
import gov.usgswim.sparrow.domain.PredictionContext;
import gov.usgswim.sparrow.domain.UnitAreaType;
import gov.usgswim.sparrow.request.HUCTableRequest;
import gov.usgswim.sparrow.request.UnitAreaRequest;
import gov.usgswim.sparrow.service.SharedApplication;

import javax.xml.stream.XMLStreamReader;

public class PredictExportService implements HttpService<PredictExportRequest> {

    public XMLStreamReader getXMLStreamReader(PredictExportRequest req,
            boolean isNeedsCompleteFirstRow) throws Exception {
        Integer predictionContextID = req.getContextID();
        PredictionContext predictContext;
        Long modelId = null;

        if (predictionContextID != null) {
            predictContext = SharedApplication.getInstance().getPredictionContext(predictionContextID);
            modelId = predictContext.getModelID();
        } else {
            // TODO [IK] Ask whether set predictionContext to null later?
            predictContext = new PredictionContext(req.getModelID(), null, null, null, null, null);
            modelId = req.getModelID();
        }
        
        
        
        DataTable watershedAreas = SharedApplication.getInstance().getCatchmentAreas(new UnitAreaRequest(modelId, UnitAreaType.HUC_NONE, true));
        DataTable huc8 = SharedApplication.getInstance().getHUCData(new HUCTableRequest(modelId), false);
        SparrowColumnSpecifier adjDataColumn = predictContext.getDataColumn();
    	SparrowColumnSpecifier nomDataColumn = null;
    	PredictResult adjPredictResult = null;
    	PredictResult nomPredictResult = null;
    	PredictData adjPredictData = null;
    	PredictData nomPredictData = SharedApplication.getInstance().getPredictData(modelId);
    	DataTable reachIdAttribs = null;
    	DataTable reachStatsTable = null;
    	
        boolean hasAdjustments = false;
        if (predictContext.getAdjustmentGroups() != null) {
        	hasAdjustments = predictContext.getAdjustmentGroups().hasAdjustments();
        	
    		DataTable adjustedSources =
    			SharedApplication.getInstance().getAdjustedSource(predictContext.getAdjustmentGroups());

    		PredictDataBuilder mutable = nomPredictData.getBuilder();
    		mutable.setSrc(adjustedSources);
        	
        	adjPredictData = mutable.toImmutable();
        	
        	//Data columns
        	nomDataColumn = predictContext.getNoAdjustmentVersion().getDataColumn();
        } else {
        	adjPredictData = nomPredictData;
        	nomPredictData = null;
        	
        	//Data columns
        	nomDataColumn = null;
        }
        
        if (req.isIncludePredict()) {
        	if (predictContext.getAdjustmentGroups() != null) {
        		adjPredictResult = SharedApplication.getInstance().getPredictResult(predictContext.getAdjustmentGroups());
        		nomPredictResult = SharedApplication.getInstance().getPredictResult(predictContext.getNoAdjustmentVersion().getAdjustmentGroups());
        	} else {
        		adjPredictResult = SharedApplication.getInstance().getPredictResult(predictContext.getNoAdjustmentVersion().getAdjustmentGroups());
        	}
        }
        
        //Include optional identification information
        if (req.isIncludeReachIdAttribs()) {
        	reachIdAttribs = SharedApplication.getInstance().getModelReachIdentificationAttributes(modelId);
        }
        
        //Include optional stats information
        if (req.isIncludeReachStatAttribs()) {
        	
        	ColumnData[] statColumns = new ColumnData[2];
        	
        	
        	SparrowColumnSpecifier scs = SharedApplication.getInstance().getStreamFlow(modelId);
        	statColumns[0] = new ColumnDataFromTable(scs.getTable(), scs.getColumn());
        	
			UnitAreaRequest catchAreaReq = new UnitAreaRequest(modelId, UnitAreaType.HUC_NONE, false);
			DataTable catchmentAreaTab = SharedApplication.getInstance().getCatchmentAreas(catchAreaReq);
			statColumns[1] = new ColumnDataFromTable(catchmentAreaTab, 1);
			
			//Merge the columns into a single table
			reachStatsTable = new SimpleDataTable(statColumns, "Reach Statistics", null, null, null);
        }
        
        
        return new  PredictExportSerializer(
        		req,
        		adjDataColumn, nomDataColumn,
    			adjPredictData, nomPredictData,
    			adjPredictResult, nomPredictResult,
    			watershedAreas, huc8, reachIdAttribs, reachStatsTable, hasAdjustments);
    }

    public void shutDown() {
    }
}
