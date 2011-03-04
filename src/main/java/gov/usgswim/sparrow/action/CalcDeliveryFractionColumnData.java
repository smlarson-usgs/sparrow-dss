package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.impl.SparseDoubleColumnData;
import gov.usgswim.datatable.impl.StandardDoubleColumnData;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.SparrowUnits;
import gov.usgswim.sparrow.datatable.TableProperties;
import gov.usgswim.sparrow.domain.BaseDataSeriesType;
import gov.usgswim.sparrow.domain.DataSeriesType;
import gov.usgswim.sparrow.domain.DeliveryFractionMap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This action creates a ColumnData containing the delivery
 * fractions provided in the passed deliveryFractionHash.
 * 
 * @author eeverman
 *
 */
public class CalcDeliveryFractionColumnData extends Action<ColumnData> {

	protected PredictData predictData;
	protected DeliveryFractionMap deliveryFractionMap;
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
	public void setDeliveryFractionHash(DeliveryFractionMap deliveryFraction) {
		this.deliveryFractionMap = deliveryFraction;
	}
	
	@Override
	protected String getPostMessage() {
		return msg;
	}
	
	@Override
	public ColumnData doAction() throws Exception {
		//Hash containing rows as keys and DeliveryReaches as values.
		DeliveryFractionMap deliveries = deliveryFractionMap;
		int baseRows = predictData.getTopo().getRowCount();
		
		//Props for the returned column
		Map<String, String> props = new HashMap<String, String>();
		props.put(TableProperties.CONSTITUENT.getPublicName(), predictData.getModel().getConstituent());
		props.put(TableProperties.DATA_TYPE.getPublicName(), BaseDataSeriesType.delivered_fraction.name());
		props.put(TableProperties.DATA_SERIES.getPublicName(), DataSeriesType.delivered_fraction.name());
		
		if (deliveries.size() > (baseRows / 9)) {
			double[] vals2d = new double[baseRows];
			
			Iterator<Entry<Integer, Float>> it = deliveries.entrySet().iterator();
			while (it.hasNext()) {
				Entry<Integer, Float> e = it.next();
				vals2d[e.getKey()] = e.getValue();
			}

			
			//Todo:  It would be nice to have a standard property name for the
			//model that this relates to and what the rows are related to.
			
			//TODO:  We have float data but are using double storage
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
			
			Iterator<Entry<Integer, Float>> it = deliveries.entrySet().iterator();
			while (it.hasNext()) {
				Entry<Integer, Float> e = it.next();
				delFracs.put(e.getKey(), e.getValue().doubleValue());
			}
			
			//TODO:  We have float data but are using double storage
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
