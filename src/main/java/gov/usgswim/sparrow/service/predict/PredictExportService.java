package gov.usgswim.sparrow.service.predict;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.filter.FilteredDataTable;
import gov.usgswim.datatable.filter.RowFilter;
import gov.usgswim.service.HttpService;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.PredictDataImm;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.service.predict.aggregator.AggregationRunner;
import gov.usgswim.sparrow.service.predict.filter.PredictExportFilter;

import java.util.Map;

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
            predictionContext = new PredictionContext(req.getModelID(), null, null, null, null);
        }

        DataTable result = SharedApplication.getInstance().getAnalysisResult(predictionContext);
        PredictData data = SharedApplication.getInstance().getPredictData(predictionContext.getModelID());
        DataTable src = data.getSrc();

        if (predictionContext.getAdjustmentGroups() != null) {
            src = SharedApplication.getInstance().getAdjustedSource(predictionContext.getAdjustmentGroups());
        }
        
        if (predictionContext.getAnalysis().isAggregated()) {
            AggregationRunner aggRunner = new AggregationRunner(predictionContext);
            src = aggRunner.doAggregation(src);
        }
        
        DataTable topo = data.getTopo();
        if (req.getBbox() != null) {
            
            RowFilter filter = new PredictExportFilter(predictionContext, req.getBbox());
            topo = new FilteredDataTable(topo, filter);
            Map<Integer,Integer> rowMap = ((FilteredDataTable)topo).getRowMap();
            result = new FilteredDataTable(result, rowMap, null);
            src = new FilteredDataTable(src, rowMap, null);
        }
        
        data = new PredictDataImm(topo, data.getCoef(), src,
            data.getSrcMetadata(), data.getDecay(),
            data.getAncil(), data.getModel());

        return new PredictExportSerializer(req, result, data);
    }

    public void shutDown() {
    }
}
