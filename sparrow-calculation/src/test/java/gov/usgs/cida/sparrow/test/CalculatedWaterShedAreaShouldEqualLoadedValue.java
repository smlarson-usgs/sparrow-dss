package gov.usgs.cida.sparrow.test;



import gov.usgs.cida.datatable.DataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.TopoData;
import gov.usgswim.sparrow.action.*;
import gov.usgswim.sparrow.domain.*;
import gov.usgswim.sparrow.request.FractionedWatershedAreaRequest;
import gov.usgswim.sparrow.request.ReachAreaFractionMapRequest;
import gov.usgswim.sparrow.request.ReachID;
import gov.usgswim.sparrow.request.UnitAreaRequest;
import gov.usgswim.sparrow.service.ConfiguredCache;
import gov.usgswim.sparrow.service.SharedApplication;
import java.util.List;
import net.sf.ehcache.Element;


/**
 * Compares the db value for cumulative watershed area to the aggregated value,
 * built by adding up all the catchments upstream of the reach.
 *
 * @author eeverman
 */
public class CalculatedWaterShedAreaShouldEqualLoadedValue extends Action {

	/**
	 * Runs QA checks against the data.
	 * @param modelId
	 * @return
	 * @throws Exception
	 */
	public void testModelBasedOnFractionedAreas(Long modelId) throws Exception {

		DataTable cumulativeAreasFromDb = SharedApplication.getInstance().getCatchmentAreas(new UnitAreaRequest(modelId, AggregationLevel.REACH, true));
		DataTable incrementalAreasFromDb = SharedApplication.getInstance().getCatchmentAreas(new UnitAreaRequest(modelId, AggregationLevel.REACH, false));
		PredictData predictData = SharedApplication.getInstance().getPredictData(modelId);
		TopoData topo = predictData.getTopo();
		//ModelReachAreaRelations reachToHuc2Relation = SharedApplication.getInstance().getModelReachAreaRelations(new ModelAggregationRequest(modelId, AggregationLevel.HUC2));

		//All the HUC2s in this model, with the HUC id as the row ID.
		//DataTable regionDetail = SharedApplication.getInstance().getHucsForModel(new ModelHucsRequest(modelId, HucLevel.HUC2));

		int rowCompleteCnt = 0;

		for (int row = 0; row < topo.getRowCount(); row++) {
			Long reachId = predictData.getIdForRow(row);
			Double dbArea = cumulativeAreasFromDb.getDouble(row, 1);
			Double calculatedFractionalWatershedArea = null;
			ReachID reachUId = new ReachID(modelId, reachId);
			Boolean ifTran = topo.isIfTran(row);

			//Do Fractioned watershed area calc

			FractionedWatershedAreaRequest areaReq = new AreaRequest(
					reachUId, );
			calculatedFractionalWatershedArea =  SharedApplication.getInstance().getFractionedWatershedArea(areaReq);

		}


	}

	@Override
	public Object doAction() throws Exception {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}


}

