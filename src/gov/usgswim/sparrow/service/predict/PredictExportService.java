package gov.usgswim.sparrow.service.predict;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.service.HttpService;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.PredictDataImm;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.parser.Analysis;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.service.predict.aggregator.AggregationRunner;

import javax.xml.stream.XMLStreamReader;

public class PredictExportService implements HttpService<PredictExportRequest> {

    // private PropertyLoaderHelper props = new PropertyLoaderHelper(
    // "gov/usgswim/sparrow/service/predictcontext/PredictContextServiceTemplate.properties"
    // );

    public XMLStreamReader getXMLStreamReader(PredictExportRequest o,
            boolean isNeedsCompleteFirstRow) throws Exception {
        Integer predictionContextID = o.getContextID();
        PredictionContext nominalPredictionContext;
        PredictionContext predictionContext;

        if (predictionContextID != null) {
            // use prediction context to get the predicted results from
            // cache if available
            predictionContext = SharedApplication.getInstance().getPredictionContext(predictionContextID);
            nominalPredictionContext = new PredictionContext(predictionContext.getModelID(), null, null, null, null);
        } else {
            // TODO [IK] Ask whether set predictionContext to null later?
            predictionContext = new PredictionContext(o.getModelID(), null, null, null, null);
            nominalPredictionContext = new PredictionContext(o.getModelID(), null, null, null, null);
        }

        PredictResult result = SharedApplication.getInstance().getAnalysisResult(predictionContext);
        PredictData data = SharedApplication.getInstance().getPredictData(predictionContext.getModelID());
        Analysis analysis = predictionContext.getAnalysis();

        if (predictionContext.getAdjustmentGroups() != null) {

            DataTable adjSrc = SharedApplication.getInstance().getAdjustedSource(predictionContext.getAdjustmentGroups());
            // Check for aggregation and run if necessary
            if (analysis.isAggregated()) {
                AggregationRunner aggRunner = new AggregationRunner(predictionContext);
                adjSrc = aggRunner.doAggregation(adjSrc);
            }

            data = new PredictDataImm(data.getTopo(), data.getCoef(), adjSrc,
                    data.getSrcMetadata(), data.getDecay(), data.getSys(), data
                            .getAncil(), data.getModel());
            
        } else if (analysis.isAggregated()) {
            // Check for aggregation and run if necessary
            AggregationRunner aggRunner = new AggregationRunner(predictionContext);
            DataTable aggSrc = aggRunner.doAggregation(data.getSrc());
            data = new PredictDataImm(data.getTopo(), data.getCoef(), aggSrc,
                    data.getSrcMetadata(), data.getDecay(), data.getSys(), data
                            .getAncil(), data.getModel());
        }

        PredictExportSerializer ser = new PredictExportSerializer(o, result, data);

        return ser;
        // PredictionContext context =
        // SharedApplication.getInstance().getPredictionContext
        // (o.getContextID());
        // PredictResult result =
        // SharedApplication.getInstance().getAnalysisResult(context);
        // PredictData data =
        // SharedApplication.getInstance().getPredictData(context.getModelID());
        //		
        // if (context.getAdjustmentGroups() != null) {
        // DataTable adjSrc =
        // SharedApplication.getInstance().getAdjustedSource(context
        // .getAdjustmentGroups());
        // data = new PredictDataImm(data.getTopo(), data.getCoef(), adjSrc,
        // data.getSrcMetadata(), data.getDecay(),
        // data.getSys(), data.getAncil(), data.getModel());
        //			
        // }
        //		
        // PredictExportSerializer ser = new PredictExportSerializer(o, result,
        // data);
        //		
        // return ser;
    }

    public void shutDown() {
        // TODO Auto-generated method stub
    }
}
