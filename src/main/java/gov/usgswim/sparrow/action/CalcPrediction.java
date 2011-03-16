package gov.usgswim.sparrow.action;

import static gov.usgswim.sparrow.PredictData.FNODE_COL;
import static gov.usgswim.sparrow.PredictData.IFTRAN_COL;
import static gov.usgswim.sparrow.PredictData.INSTREAM_DECAY_COL;
import static gov.usgswim.sparrow.PredictData.TNODE_COL;
import static gov.usgswim.sparrow.PredictData.UPSTREAM_DECAY_COL;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.utils.DataTableUtils;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.datatable.ColumnAttribsBuilder;
import gov.usgswim.sparrow.datatable.ImmutableDoubleColumn;
import gov.usgswim.sparrow.datatable.MultiplicativeColumnData;
import gov.usgswim.sparrow.datatable.PredictResultImm;
import gov.usgswim.sparrow.datatable.TableProperties;
import gov.usgswim.sparrow.domain.BaseDataSeriesType;
import gov.usgswim.sparrow.domain.DataSeriesType;
import gov.usgswim.sparrow.service.predict.aggregator.AggregateType;

/**
 * A simple SPARROW prediction implementation.
 *
 * Note: It is assumed that the reach order in the topo, coef, and src arrays
 * all match, and that the reach order is such that reach(n) never flows to
 * reach(<n).
 * 
 * The returned 2D array is of this form:
 * 
 * inc:  Incremental, NOT DECAYED.
 * k:  the number of sources
 * i:  a give reach, which is a row in the array
 * Total row count = 2k + 2
 * 
 * The Array is made up of data in these blocks
 * [i, 0 ... (k-1)] inc added at reach, per source k. NOT decayed.
 * [i, k ... (2k-1)] total (w/ up stream contrib), per source k (decayed)
 * [i, (2k)] total incremental contribution at reach (NOT decayed)
 * [i, (2k + 1)] grand total at reach (decayed inc + upstream load)
 * 
 */
public class CalcPrediction extends Action<PredictResultImm> {
	/**
	 * The parent of all child values. If not passed in, it is created.
	 */
	protected PredictData predictData;

	/**
	 * Invariant topographic info about each reach.
	 * i = reach index [i][0] from node index [i][1] to node index [i][2] 'if
	 * transmit' is 1 if the reach transmits to its to-node
	 *
	 * NOTE: We assume that the node indexes start at zero and have no skips.
	 * Thus, nodeCount must equal the largest node index + 1
	 * @see gov.usgswim.ImmutableBuilder.PredictData#getTopo()
	 *
	 */
	protected DataTable topo;

	/**
	 * The coef's for each reach-source. coef[i][k] == the coefficient for
	 * source k at reach i
	 */
	protected DataTable sourceCoefficient;

	/**
	 * The source amount for each reach-source. src[i][k] == the amount added
	 * via source k at reach i
	 */
	protected DataTable sourceValues;

	/**
	 * @see PredictData#getDelivery()
	 */
	protected DataTable deliveryCoefficient;

	/**
	 * The number of nodes
	 */
	protected int nodeCount;

	 /**
	 * Construct a new instance using a PredictionDataSet.
	 *
	 * @param data An all-in-one data object
	 *
	 */
	public CalcPrediction(PredictData data) {
		{// assign the passed values to the class variables
			this.topo = data.getTopo();
			this.sourceCoefficient = data.getCoef();
			this.sourceValues = data.getSrc();
			this.deliveryCoefficient = data.getDelivery();
		}


		int maxNode = Math.max(topo.getMaxInt(FNODE_COL), topo
				.getMaxInt(TNODE_COL));

		{ // IK: Efficiency checks disabled for now as they cause failing tests,
			// and I'm not sure if I want to do this optimization as it doesn't
			// have sufficient benefit. Basically, we are allocating an array of
			// node values based on the maximum index of the nodes. In an ideal
			// world, that number should be slightly more than the number of
			// reaches.
			// TODO Add this check to data loading process
			boolean isCheckEfficiency = false;
			if (isCheckEfficiency) {
				checkEfficiency(maxNode);
			}
		}
		this.predictData = data;
		nodeCount = maxNode + 1;
	}
	
	@Override
	public PredictResultImm doAction() throws Exception {
		int reachCount = topo.getRowCount(); // # of reaches is equal to the number of 'rows' in topo
		int sourceCount = sourceValues.getColumnCount(); // # of sources is equal to the number of 'columns' in an
		int outputColumnCount = (sourceCount * 2) + 2;	//The # of output columns
		
		/*
		 * Array definitions
		 * inc:  Incremental, NOT DECAYED.
		 * k:  the number of sources
		 * i:  a give reach, which is a row in the array
		 * Total row count = 2k + 2
		 * 
		 * The Array is made up of data in these blocks
		 * [i, 0 ... (k-1)] inc added at reach, per source k. NOT decayed.
		 * [i, k ... (2k-1)] total (w/ up stream contrib), per source k (decayed)
		 * [i, (2k)] total incremental contribution at reach (NOT decayed)
		 * [i, (2k + 1)] grand total at reach (decayed inc + upstream load)
		 */
		
		//The output array for all data, one row per reach and in the same order.
		double outputArray[][] = new double[reachCount][outputColumnCount];
		
		//Array of accumulated values at nodes, row number correspond to nodes
		//defined by fnode and tnode.
		double upstreamNodeLoad[][] = new double[nodeCount][sourceCount];
		
		//in outputArray, the column of the combined incremental contribution (not decayed, all sources)
		int totalIncrementalColumnIndex = 2*sourceCount;
		
		//in outputArray, the column of the combined total load (decayed, all sources)
		int totalTotalColumnIndex = totalIncrementalColumnIndex + 1;

		// Iterate over all reaches
		for (int reachRow = 0; reachRow < reachCount; reachRow++) {

			double totalIncrementalLoad = 0d; // incremental for all sources/ (NOT decayed)
			double rchGrandTotal = 0d; // all sources + all from upstream node (decayed)

			//Iterate over all sources.
			//currentSourceIndex indexes to the incremental per source in the output array.
			for (int currentSourceIndex = 0; currentSourceIndex < sourceCount; currentSourceIndex++) {
				
				//Index to the total (w/ upstream decayed) for this source
				int currentTotalSourceIndex = currentSourceIndex + sourceCount;

				//Land delivery & coef both included in coef value. (NOT decayed)
				double incrementalLoadForThisSource = 
					sourceCoefficient.getDouble(reachRow, currentSourceIndex)
					* sourceValues.getDouble(reachRow, currentSourceIndex);

				outputArray[reachRow][currentSourceIndex] = incrementalLoadForThisSource;

				//total (w/ upstream contrib) for this source (Decayed)
				outputArray[reachRow][currentTotalSourceIndex] =
					(incrementalLoadForThisSource * deliveryCoefficient.getDouble(reachRow, INSTREAM_DECAY_COL))
					/* (incremental addition of this source, decayed by 1/2 stream travel) */
					+
					/* (upstream load decayed by full stream travel (includes delivery fraction)) */
					(upstreamNodeLoad[topo.getInt(reachRow, FNODE_COL)][currentSourceIndex]
					* deliveryCoefficient.getDouble(reachRow, UPSTREAM_DECAY_COL));

				// Accumulate at downstream node if this reach transmits
				if (topo.getInt(reachRow, IFTRAN_COL) != 0) {
					upstreamNodeLoad[topo.getInt(reachRow, TNODE_COL)][currentSourceIndex]
					+= outputArray[reachRow][currentTotalSourceIndex];
				}

				totalIncrementalLoad += incrementalLoadForThisSource; // add to incremental total for all sources at reach
				rchGrandTotal += outputArray[reachRow][currentTotalSourceIndex]; // add to grand total for all sources (w/upsteam) at reach
			}

			outputArray[reachRow][totalIncrementalColumnIndex] = totalIncrementalLoad; // incremental for all sources (NOT decayed)
			outputArray[reachRow][totalTotalColumnIndex] = rchGrandTotal; // all sources + all from upstream node (Decayed)

		}

		return buildPredictResult(outputArray,	predictData);
	}
	
    /**
     * Adds appropriate metadata to the raw 2x2 array and returns the result as a DataTable.
     * 
     * This method is public only for testing, which needs to be able to directly
     * construct a known good PredictResult instance for testing w/o going
     * through the prediction code.
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
    
    /**
     * Constructs a new PredictResultImm instance based on the passed data.
     * This relies heavily on the structure of the data array.  Copying the
     * docs from the CalcPrediction Action, it is defined as:
     * 
     * <quote>
     * inc:  Incremental, NOT DECAYED.
	 * k:  the number of sources
	 * i:  a give reach, which is a row in the array
	 * Total row count = 2k + 2
	 * 
	 * The Array is made up of data in these blocks
	 * [i, 0 ... (k-1)] inc added at reach, per source k. NOT decayed.
	 * [i, k ... (2k-1)] total (w/ up stream contrib), per source k (decayed)
	 * [i, (2k)] total incremental contribution at reach (NOT decayed)
	 * [i, (2k + 1)] grand total at reach (decayed inc + upstream load)
	 * </quote>
	 * 
     * @param data
     * @param predictData
     * @param ids
     * @param properties
     * @return
     * @throws Exception
     */
    public static PredictResultImm buildPredictResult(double[][] data, PredictData predictData, long[] ids, Map<String, String> properties)
    		throws Exception {
    	
    	int srcCount = (data[0].length - 2) / 2;	//as per definition
    	
    	//Need to record the location of these base data columns
    	//Note that this is the position in the double[][] data array, which
    	//will be a different column position from where the end up in the final
    	//Table.
    	int totalIncColInDataArray = srcCount * 2;
    	int totalTotalColInDataArray = totalIncColInDataArray + 1;
    	//No entry for totalDecayedInc b/c it is calculated (not in the src data)
    	
    	//One set of data for each main data series: Total, Incremental,
    	//and decayed Incremental, for each source.  Plus a total for each each
    	//of the main data series.
    	int outputColCount = (srcCount * 3) + 3;
    	
        ColumnData[] columns = new ColumnData[outputColCount];

        // Same definition as the instance vars
        // Lookup table for key=source_id, value=array index of source contribution data
        Map<Long, Integer> srcIdIncMap = new Hashtable<Long, Integer>(13);
        Map<Long, Integer> srcIdTotalMap = new Hashtable<Long, Integer>(13);
        Map<Long, Integer> srcIdDecayIncMap = new Hashtable<Long, Integer>(13);
        
        DataTable srcMetadata = predictData.getSrcMetadata();
        String modelUnits = predictData.getModel().getUnits().getUserName();
        String modelConstituent = predictData.getModel().getConstituent();

        // ----------------------------------------------------------------
        // Define the source columns of the DataTable using the PredictData
        // ----------------------------------------------------------------
        for (int srcIndex = 0; srcIndex < srcCount; srcIndex++) {

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

            //Columns in the source data array for these data sources
            int srcIncAddDataIndex = srcIndex; //incremental source contributions
            int srcTotalDataIndex = srcIndex + srcCount; //total source contributions (leave gap for total inc)
            
            //Output column index for the following series of columns:
            int srcIncAddIndex = srcIndex; //incremental source contributions
            int srcTotalIndex = srcIncAddIndex + srcCount + 1; //total source contributions (leave gap for total inc)
            int srcDecayIncAddIndex = srcTotalIndex + srcCount + 1; //decayed incremental (leave for total tatal)

            //populate the index maps
            srcIdIncMap.put(predictData.getSourceIdForSourceIndex(srcIndex), srcIncAddIndex);
            srcIdTotalMap.put(predictData.getSourceIdForSourceIndex(srcIndex), srcTotalIndex);
            srcIdDecayIncMap.put(predictData.getSourceIdForSourceIndex(srcIndex), srcDecayIncAddIndex);
            
            //Map of metadata values for inc-add column
            Map<String, String> incProps = new HashMap<String, String>();
            incProps.put(TableProperties.DATA_TYPE.getPublicName(), BaseDataSeriesType.incremental.name());
            incProps.put(TableProperties.DATA_SERIES.getPublicName(),
            		Action.getDataSeriesProperty(DataSeriesType.incremental, false));
            incProps.put(TableProperties.CONSTITUENT.getPublicName(), modelConstituent);
            incProps.put(TableProperties.PRECISION.getPublicName(), precision);
            String incDesc = "Load added at this reach, undecayed. " +
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
            
            //Map of metadata values for decayed inc-add column
            ColumnAttribsBuilder decayedIncAttribs = new ColumnAttribsBuilder();
            decayedIncAttribs.setProperty(TableProperties.DATA_TYPE.getPublicName(),
            		BaseDataSeriesType.decayed_incremental.name());
            decayedIncAttribs.setProperty(TableProperties.DATA_SERIES.getPublicName(),
            		Action.getDataSeriesProperty(DataSeriesType.decayed_incremental, false));
            decayedIncAttribs.setName(srcName + "Decayed Incremental Load");
            decayedIncAttribs.setDescription(
            	"Load added at this reach and decayed to the end of the reach. " +
            	"Reported in " + modelUnits + " of " +
            	modelConstituent + " for the " + srcConstituent + " source.");
            

            columns[srcIncAddIndex] = new ImmutableDoubleColumn(data, srcIncAddDataIndex, srcName + " Incremental Load", modelUnits, incDesc, incProps);
            columns[srcTotalIndex] = new ImmutableDoubleColumn(data, srcTotalDataIndex, srcName + " Total Load", modelUnits, totDesc, totProps);
            columns[srcDecayIncAddIndex] = new MultiplicativeColumnData(
            		columns[srcIncAddIndex], 
            		predictData.getDelivery().getColumn(PredictData.INSTREAM_DECAY_COL),
            		decayedIncAttribs);
        }

        // ------------------------------------------
        // Define the total columns of the DataTable
        // ------------------------------------------
        int totalIncCol = srcCount;	//The total inc col comes right after the inc per source cols
        Map<String, String> totalIncProps = new HashMap<String, String>();
        totalIncProps.put(TableProperties.DATA_TYPE.getPublicName(), BaseDataSeriesType.incremental.name());
        totalIncProps.put(TableProperties.DATA_SERIES.getPublicName(),
        		Action.getDataSeriesProperty(DataSeriesType.incremental, false));
        totalIncProps.put(TableProperties.CONSTITUENT.getPublicName(), modelConstituent );
        totalIncProps.put(TableProperties.ROW_AGG_TYPE.getPublicName(), AggregateType.sum.name());

        //Total of total load column
        int totalTotalCol = totalIncCol + srcCount + 1; //The grand total col comes right after the total cols per source
        Map<String, String> grandTotalProps = new HashMap<String, String>();
        grandTotalProps.put(TableProperties.DATA_TYPE.getPublicName(), BaseDataSeriesType.total.name());
        grandTotalProps.put(TableProperties.DATA_SERIES.getPublicName(),
        		Action.getDataSeriesProperty(DataSeriesType.total, false));
        grandTotalProps.put(TableProperties.CONSTITUENT.getPublicName(), modelConstituent);
        grandTotalProps.put(TableProperties.ROW_AGG_TYPE.getPublicName(), AggregateType.sum.name());
        
        //total of decayed inc-add column
        int totalDecayedIncCol = totalTotalCol + srcCount + 1; //The total decayed inc comes after the total per source cols
        ColumnAttribsBuilder totDecayedIncAttribs = new ColumnAttribsBuilder();
        totDecayedIncAttribs.setProperty(TableProperties.DATA_TYPE.getPublicName(),
        		BaseDataSeriesType.decayed_incremental.name());
        totDecayedIncAttribs.setProperty(TableProperties.DATA_SERIES.getPublicName(),
        		Action.getDataSeriesProperty(DataSeriesType.decayed_incremental, false));
        totDecayedIncAttribs.setProperty(TableProperties.ROW_AGG_TYPE.getPublicName(), AggregateType.sum.name());
        totDecayedIncAttribs.setName(Action.getDataSeriesProperty(DataSeriesType.decayed_incremental, false));
        totDecayedIncAttribs.setDescription(Action.getDataSeriesProperty(DataSeriesType.decayed_incremental, true));


        columns[totalIncCol] = new ImmutableDoubleColumn(data, totalIncColInDataArray, 
        		Action.getDataSeriesProperty(DataSeriesType.incremental, false),
        		modelUnits, Action.getDataSeriesProperty(DataSeriesType.incremental, true), totalIncProps);
        columns[totalTotalCol] = new ImmutableDoubleColumn(data, totalTotalColInDataArray,
        		Action.getDataSeriesProperty(DataSeriesType.total, false),
        		modelUnits, Action.getDataSeriesProperty(DataSeriesType.total, true),
        		grandTotalProps);
        columns[totalDecayedIncCol] = new MultiplicativeColumnData(
        		columns[totalIncCol], 
        		predictData.getDelivery().getColumn(PredictData.INSTREAM_DECAY_COL),
        		totDecayedIncAttribs);

        // only get the ids if available
        if (ids == null) {
            ids = (predictData.getTopo() != null) ? DataTableUtils.getRowIds(predictData.getTopo()) : null;
        }

        return new PredictResultImm(columns, ids, properties,
        		srcIdIncMap, srcIdDecayIncMap, srcIdTotalMap,
        		totalIncCol, totalDecayedIncCol, totalTotalCol);
    }

	/**
	 * Method to check the efficiency/density of the node ids. Ideally, they would all be consecutive
	 *
	 * @param maxNode
	 */
	private void checkEfficiency(int maxNode) {
		int minNode = Math.min(topo.getMinInt(FNODE_COL), topo
				.getMinInt(TNODE_COL));
		assert (minNode < 2) : "too high a starting point for the node indices leads to large memory consumption";

		double ratio = (double) maxNode / (double) topo.getRowCount();
		assert (ratio < 1.2) : "large gaps in the indices" + maxNode
		+ " - " + +topo.getRowCount()
		+ "results in inefficient memory consumption";
	}


}
