package gov.usgswim.sparrow.service.predict;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.service.HttpService;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.PredictDataBuilder;
import gov.usgswim.sparrow.datatable.DataColumn;
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

        if (predictionContextID != null) {
            predictContext = SharedApplication.getInstance().getPredictionContext(predictionContextID);
        } else {
            // TODO [IK] Ask whether set predictionContext to null later?
            predictContext = new PredictionContext(req.getModelID(), null, null, null, null, null);
        }
        
        DataTable watershedAreas = SharedApplication.getInstance().getCatchmentAreas(new UnitAreaRequest(predictContext.getModelID(), UnitAreaType.HUC_NONE, true), false);
        DataTable huc8 = SharedApplication.getInstance().getHUCData(new HUCTableRequest(predictContext.getModelID()), false);
        DataColumn adjDataColumn = predictContext.getDataColumn();
    	DataColumn nomDataColumn = null;
    	PredictResult adjPredictResult = null;
    	PredictResult nomPredictResult = null;
    	PredictData adjPredictData = null;
    	PredictData nomPredictData = SharedApplication.getInstance().getPredictData(predictContext.getModelID());
        
        if (predictContext.getAdjustmentGroups() != null) {

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
        
        
        return new  PredictExportSerializer(req,
    			adjDataColumn, nomDataColumn,
    			adjPredictData, nomPredictData,
    			adjPredictResult, nomPredictResult, watershedAreas, huc8);
    }

    public void shutDown() {
    }
}
