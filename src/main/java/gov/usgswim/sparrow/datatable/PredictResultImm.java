package gov.usgswim.sparrow.datatable;

import static gov.usgswim.sparrow.service.predict.aggregator.AggregateType.sum;
import gov.usgswim.Immutable;
import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.impl.SimpleDataTable;
import gov.usgswim.datatable.utils.DataTableUtils;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.action.Action;
import gov.usgswim.sparrow.datatable.TableProperties;
import gov.usgswim.sparrow.parser.BaseDataSeriesType;
import gov.usgswim.sparrow.parser.DataSeriesType;
import gov.usgswim.sparrow.service.predict.aggregator.AggregateType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * An immutable implementation of PredictResult. This is simply an Immutable
 * DataTable with convenience getXXX methods appropriate to the content
 *
 * This will likely be the only implementation unless the PredictRunner code is
 * modified to use a builder instead of arrays.
 *
 * @author eeverman
 */
@Immutable
public class PredictResultImm extends SimpleDataTable implements PredictResult {

	/**
	 * A mapping from a source Identifier to the column number of the column
	 * containing the Incremental value for that source.
	 */
	private final Map<Long, Integer> srcIdIncMap;

	/**
	 * A mapping from a source Identifier to the column number of the column
	 * containing the Total value for that source.
	 */
	private final Map<Long, Integer> srcIdTotalMap;

	/**
	 * Index of the total Incremental column.
	 */
	private final int totalIncCol;

	/**
	 * Index of the total Total column.
	 */
	private final int totalTotalCol;

	/**
	 * The number of sources.
	 */
	private final int sourceCount;


	public PredictResultImm(ColumnData[] columns, long[] rowIds, Map<String, String> properties,
				Map<Long, Integer> srcIdIncMap, Map<Long, Integer> srcIdTotalMap,
				int totalIncCol, int totalTotalCol) {

		super(columns, "Prediction Data", "Prediction Result Data", properties, rowIds);

		{
			Hashtable<Long, Integer> map = new Hashtable<Long, Integer>(srcIdIncMap.size() * 2 + 1);
			map.putAll(srcIdIncMap);
			this.srcIdIncMap = map;
		}

		{
			Hashtable<Long, Integer> map = new Hashtable<Long, Integer>(srcIdTotalMap.size() * 2 + 1);
			map.putAll(srcIdTotalMap);
			this.srcIdTotalMap = map;
		}

		this.totalIncCol = totalIncCol;
		this.totalTotalCol = totalTotalCol;
		this.sourceCount = srcIdIncMap.size();
	}

    /**
     * Adds appropriate metadata to the raw 2x2 array and returns the result as a DataTable
     *
     * @param data
     * @param predictData
     * @return
     * @throws Exception
     */
    public static PredictResultImm buildPredictResult(double[][] data, PredictData predictData) throws Exception {
        return buildPredictResult(data, predictData, null, Collections.<String, String>emptyMap());
    }

    public static PredictResultImm buildPredictResult(double[][] data, PredictData predictData, long[] ids)
    throws Exception {
        return buildPredictResult(data, predictData, ids, Collections.<String, String>emptyMap());
    }

    public static PredictResultImm buildPredictResult(double[][] data, PredictData predictData, long[] ids, Map<String, String> properties)
    throws Exception {
        ColumnData[] columns = new ColumnData[data[0].length];
        int sourceCount = predictData.getSrc().getColumnCount();

        // Same definition as the instance vars
        // Lookup table for key=source_id, value=array index of source contribution data
        Map<Long, Integer> srcIdIncMap = new Hashtable<Long, Integer>(13, 2);
        Map<Long, Integer> srcIdTotalMap = new Hashtable<Long, Integer>(13, 2);
        
        DataTable srcMetadata = predictData.getSrcMetadata();
        String modelUnits = predictData.getModel().getUnits().getUserName();
        String modelConstituent = predictData.getModel().getConstituent();

        // ----------------------------------------------------------------
        // Define the source columns of the DataTable using the PredictData
        // ----------------------------------------------------------------
        for (int srcIndex = 0; srcIndex < sourceCount; srcIndex++) {

            // Get the metadata to be attached to the column definitions
            String srcName = null;
            String srcConstituent = null;
            String precision = null;
            
            
            if (srcMetadata != null) {
                Integer displayNameCol = srcMetadata.getColumnByName("DISPLAY_NAME");
                Integer precisionCol = srcMetadata.getColumnByName("PRECISION");

                // Pull out the metadata for the source
                srcName = srcMetadata.getString(srcIndex, displayNameCol);
                srcConstituent = srcName;
                precision = srcMetadata.getLong(srcIndex, precisionCol).toString();
            }

            //
            int srcIncAddIndex = srcIndex; // index for iterating through the incremental source contributions
            int srcTotalIndex = srcIndex + sourceCount; // index for iterating through the total source contributions

            srcIdIncMap.put(predictData.getSourceIdForSourceIndex(srcIndex), srcIncAddIndex);
            srcIdTotalMap.put(predictData.getSourceIdForSourceIndex(srcIndex), srcTotalIndex);

            // Map of metadata values for inc-add column
            Map<String, String> incProps = new HashMap<String, String>();
            incProps.put(TableProperties.DATA_TYPE.getPublicName(), BaseDataSeriesType.incremental.name());
            incProps.put(TableProperties.DATA_SERIES.getPublicName(),
            		Action.getDataSeriesProperty(DataSeriesType.incremental, false));
            incProps.put(TableProperties.CONSTITUENT.getPublicName(), modelConstituent);
            incProps.put(TableProperties.PRECISION.getPublicName(), precision);
            String incDesc = "Load added at this reach and decayed to the end of the reach. " +
            	"Reported in " + modelUnits + " of " +
            	modelConstituent + " for the " + srcConstituent + " source.";
            
            
            // Map of metadata values for total column
            Map<String, String> totProps = new HashMap<String, String>();
            totProps.put(TableProperties.DATA_TYPE.getPublicName(), BaseDataSeriesType.total.name());
            totProps.put(TableProperties.DATA_SERIES.getPublicName(),
            		Action.getDataSeriesProperty(DataSeriesType.total, false));
            totProps.put(TableProperties.CONSTITUENT.getPublicName(), modelConstituent);
            totProps.put(TableProperties.PRECISION.getPublicName(), precision);
            String totDesc = "Total load decayed from all upstream reaches decayed to the end of this reach. " +
            	"Reported in " + modelUnits + " of " +
        		modelConstituent + " for the " + srcConstituent + " source.";
            
            

            columns[srcIncAddIndex] = new ImmutableDoubleColumn(data, srcIncAddIndex, srcName + " Incremental Load", modelUnits, incDesc, incProps);
            columns[srcTotalIndex] = new ImmutableDoubleColumn(data, srcTotalIndex, srcName + " Total Load", modelUnits, totDesc, totProps);
        }

        // ------------------------------------------
        // Define the total columns of the DataTable
        // ------------------------------------------
        int totalIncCol = 2 * sourceCount;	//The total inc col comes right after the two sets of source columns
        Map<String, String> totalIncProps = new HashMap<String, String>();
        totalIncProps.put(TableProperties.DATA_TYPE.getPublicName(), BaseDataSeriesType.incremental.name());
        totalIncProps.put(TableProperties.DATA_SERIES.getPublicName(),
        		Action.getDataSeriesProperty(DataSeriesType.incremental, false));
        totalIncProps.put(TableProperties.CONSTITUENT.getPublicName(), modelConstituent );
        totalIncProps.put(TableProperties.ROW_AGG_TYPE.getPublicName(), AggregateType.sum.name());

        
        int totalTotalCol = totalIncCol + 1; //The grand total col comes right after the total incremental col
        Map<String, String> grandTotalProps = new HashMap<String, String>();
        grandTotalProps.put(TableProperties.DATA_TYPE.getPublicName(), BaseDataSeriesType.total.name());
        grandTotalProps.put(TableProperties.DATA_SERIES.getPublicName(),
        		Action.getDataSeriesProperty(DataSeriesType.total, false));
        grandTotalProps.put(TableProperties.CONSTITUENT.getPublicName(), modelConstituent);
        grandTotalProps.put(TableProperties.ROW_AGG_TYPE.getPublicName(), AggregateType.sum.name());


        columns[totalIncCol] = new ImmutableDoubleColumn(data, totalIncCol, 
        		Action.getDataSeriesProperty(DataSeriesType.decayed_incremental, false),
        		modelUnits, Action.getDataSeriesProperty(DataSeriesType.decayed_incremental, true), totalIncProps);
        columns[totalTotalCol] = new ImmutableDoubleColumn(data, totalTotalCol,
        		Action.getDataSeriesProperty(DataSeriesType.total, false),
        		modelUnits, Action.getDataSeriesProperty(DataSeriesType.total, true),
        		grandTotalProps);

        // only get the ids if available
        if (ids == null) {
            ids = (predictData.getTopo() != null) ? DataTableUtils.getRowIds(predictData.getTopo()) : null;
        }

        return new PredictResultImm(columns, ids, properties, srcIdIncMap, srcIdTotalMap, totalIncCol, totalTotalCol);
    }

    public int getSourceCount() {
        return sourceCount;
    }

    public Double getIncremental(int row) {
        return getDouble(row, totalIncCol);
    }
    
	@Override
	public Double getDecayedIncremental(int row, PredictData predictData) {
		Double inc = getIncremental(row);
		Double coef = predictData.getDelivery().getDouble(row, PredictData.INSTREAM_DECAY_COL);
		return inc * coef;
	}

    public int getIncrementalCol() {
        return totalIncCol;
    }

    public Double getTotal(int row) {
        return getDouble(row, totalTotalCol);
    }

    public int getTotalCol() {
        return totalTotalCol;
    }

    public int getIncrementalColForSrc(Long srcId) {
        return srcIdIncMap.get(srcId);
    }

    public Double getIncrementalForSrc(int row, Long srcId) {
        return getDouble(row, srcIdIncMap.get(srcId));
    }
    
	@Override
	public Double getDecayedIncrementalForSrc(int row, Long srcId,
			PredictData predictData) {
		Double inc = getIncrementalForSrc(row, srcId);
		Double coef = predictData.getDelivery().getDouble(row, PredictData.INSTREAM_DECAY_COL);
		return inc * coef;
	}

    public int getTotalColForSrc(Long srcId) {
        return srcIdTotalMap.get(srcId);
    }

    public Double getTotalForSrc(int row, Long srcId) {
        return getDouble(row, srcIdTotalMap.get(srcId));
    }



}
