package gov.usgswim.sparrow.service.predict;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.service.HttpService;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.PredictDataImm;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.service.predict.aggregator.AggregationRunner;

import javax.xml.stream.XMLStreamReader;

public class PredictExportService implements HttpService<PredictExportRequest> {

    public XMLStreamReader getXMLStreamReader(PredictExportRequest req,
            boolean isNeedsCompleteFirstRow) throws Exception {
        Integer predictionContextID = req.getContextID();
        PredictionContext predictionContext;

        if (predictionContextID != null) {
            predictionContext = SharedApplication.getInstance().getPredictionContext(predictionContextID);
        } else {
            // TODO [IK] Ask whether set predictionContext to null later?
            predictionContext = new PredictionContext(req.getModelID(), null, null, null, null, null);
        }

        DataTable result = SharedApplication.getInstance().
        	getAnalysisResult(predictionContext.getNoComparisonVersion()).getTable();
        PredictData data = SharedApplication.getInstance().getPredictData(predictionContext.getModelID());
        DataTable src = data.getSrc();

        if (predictionContext.getAdjustmentGroups() != null) {
            src = SharedApplication.getInstance().getAdjustedSource(predictionContext.getAdjustmentGroups());
        }
        
        if (predictionContext.getAnalysis().isAggregated()) {
            AggregationRunner aggRunner = new AggregationRunner(predictionContext);
            src = aggRunner.doAggregation(src);
        }
        
        data = new PredictDataImm(data.getTopo(), data.getCoef(), src,
            data.getSrcMetadata(), data.getDelivery(),
            data.getAncil(), data.getModel());

        return new PredictExportSerializer(req, result, data);
    }

    public void shutDown() {
    }
}
