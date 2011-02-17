package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.impl.SparseDoubleColumnData;
import gov.usgswim.datatable.impl.StandardDoubleColumnData;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.SparrowUnits;
import gov.usgswim.sparrow.datatable.TableProperties;
import gov.usgswim.sparrow.parser.BaseDataSeriesType;
import gov.usgswim.sparrow.parser.DataSeriesType;

import java.util.HashMap;
import java.util.Map;

/**
 * This action creates a ColumnData containing the delivery
 * fractions provided in the passed deliveryFractionHash.
 * 
 * @author eeverman
 *
 */
public class CalcDeliveryFractionDataColumn extends Action<ColumnData> {

	protected PredictData predictData;
	protected HashMap<Integer, DeliveryReach> deliveryFractionHash;
	protected String msg = null;
	
	/**
	 * Sets the predictData used to calc the delivery fraction.
	 * @param predictData
	 */
	public void setPredictData(PredictData predictData) {
		this.predictData = predictData;
	}

	/**
	 * Delivery hash, as described in the Action CalcDeliveryFractionHash.
	 * @param targetReachIds
	 */
	public void setDeliveryFractionHash(HashMap<Integer, DeliveryReach> deliveryFractionHash) {
		this.deliveryFractionHash = deliveryFractionHash;
	}
	
	@Override
	protected String getPostMessage() {
		return msg;
	}
	
	@Override
	public ColumnData doAction() throws Exception {
		//Hash containing rows as keys and DeliveryReaches as values.
		HashMap<Integer, DeliveryReach> deliveries = deliveryFractionHash;
		int baseRows = predictData.getTopo().getRowCount();
		
		//Props for the returned column
		Map<String, String> props = new HashMap<String, String>();
		props.put(TableProperties.CONSTITUENT.getPublicName(), predictData.getModel().getConstituent());
		props.put(TableProperties.DATA_TYPE.getPublicName(), BaseDataSeriesType.delivered_fraction.name());
		props.put(TableProperties.DATA_SERIES.getPublicName(), DataSeriesType.delivered_fraction.name());
		
		if (deliveries.size() > (baseRows / 9)) {
			double[] vals2d = new double[baseRows];
			
			for (DeliveryReach dr : deliveries.values()) {
				vals2d[dr.getRow()] = dr.getDelivery();
			}
			

			
			//Todo:  It would be nice to have a standard property name for the
			//model that this relates to and what the rows are related to.
			StandardDoubleColumnData column = new StandardDoubleColumnData(
					vals2d,
					getDataSeriesProperty(DataSeriesType.delivered_fraction, false),
					SparrowUnits.FRACTION.getUserName(),
					getDataSeriesProperty(DataSeriesType.delivered_fraction, true),
					props, false);
			
			return column;
		} else {
			int hashSize = baseRows * 3 / 2;
			if ((hashSize / 2) == ((double)hashSize / 2d)) hashSize++;
			
			HashMap<Integer, Double> delFracs = new HashMap<Integer, Double>(hashSize, 1);
			
			for (DeliveryReach dr : deliveries.values()) {
				delFracs.put(dr.getRow(), dr.getDelivery());
			}
			
			SparseDoubleColumnData column = new SparseDoubleColumnData(
					delFracs,
					getDataSeriesProperty(DataSeriesType.delivered_fraction, false),
					SparrowUnits.FRACTION.getUserName(),
					getDataSeriesProperty(DataSeriesType.delivered_fraction, true),
					props, null, predictData.getTopo().getRowCount(), 0d);
			
			return column;
		}
	}
	
}
