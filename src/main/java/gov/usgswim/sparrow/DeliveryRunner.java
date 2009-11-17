package gov.usgswim.sparrow;

import static gov.usgswim.sparrow.PredictData.FNODE_COL;
import static gov.usgswim.sparrow.PredictData.IFTRAN_COL;
import static gov.usgswim.sparrow.PredictData.INSTREAM_DECAY_COL;
import static gov.usgswim.sparrow.PredictData.TNODE_COL;
import static gov.usgswim.sparrow.PredictData.UPSTREAM_DECAY_COL;
import static gov.usgswim.sparrow.util.PredictDataUtils.getDownstreamNode;
import static gov.usgswim.sparrow.util.PredictDataUtils.getUpstreamNode;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.datatable.impl.StandardNumberColumnDataWritable;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.datatable.PredictResultCompare;
import gov.usgswim.sparrow.datatable.PredictResultImm;
import gov.usgswim.sparrow.navigation.NavigationUtils;
import gov.usgswim.sparrow.parser.Analysis;
import gov.usgswim.sparrow.parser.DataSeriesType;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.service.predict.WeightRunner;
import gov.usgswim.sparrow.service.predict.aggregator.AggregationRunner;

import java.util.Set;

public class DeliveryRunner implements Runner {
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
	protected DataTable deliveryCoefficient;

	/**
	 * The source amount for each reach-source. src[i][k] == the amount added
	 * via source k at reach i
	 */
	protected DataTable sourceValues; // TODO remove NOT USED

	/**
	 * The stream and resevor decay. The values in the array are *actually*
	 * delivery, which is (1 - decay). I.E. the delivery calculation is already
	 * done.
	 *
	 * src[i][0] == the instream decay at reach i. This decay is assumed to be
	 * at mid-reach and already computed as such. That is, it would normally be
	 * the sqr root of the instream decay, and it is assumed that this value
	 * already has the square root taken. src[i][1] == the upstream decay at
	 * reach i. This decay is applied to the load coming from the upstream node.
	 */
	protected DataTable decayCoefficient;
	/**
	 * The number of nodes
	 */
	protected int nodeCount;


	// ============
	// CONSTRUCTORS
	// ============
	public DeliveryRunner(DataTable topo, DataTable coef, DataTable src,
			DataTable decay) {
		this(new PredictDataImm(topo, coef, src, null, decay,
				null, null));
	}

	public DeliveryRunner(PredictData data) {
		this.topo = data.getTopo(); // assign the passed values to the class
		// variables
		//this.deliveryCoefficient = data.getCoef();
		this.sourceValues = data.getSrc();
		this.decayCoefficient = data.getDecay();
		this.deliveryCoefficient = data.getCoef();

		int maxNode = Math.max(topo.getMaxInt(FNODE_COL), topo
				.getMaxInt(TNODE_COL));

		this.predictData = data;
		nodeCount = maxNode + 1;
	}

	public double[] calculateNodeTransportFraction(Set<Long> targetReaches) {
		// TODO cache the result, based on modelID,
		int maxReachRow = NavigationUtils.findMaxReachRow(targetReaches, topo);
		PredictResultStructure prs = PredictResultStructure.analyzePredictResultStructure(maxReachRow, sourceValues);

		// Reach incrementals do not affect delivery coefficient

		// Delivery fraction, one for each node. Source type irrelevant
		double transportFraction[] = new double[nodeCount];

		for (Long reachID : targetReaches) {
			// Initialize node contributions, set = +decay for all fnodes of
			// targetReaches. Note that the alternate approach of setting
			// fraction at tnode = 1 doesn't work, as other streams ending at
			// tnode would then contribute. Also, note that this has to be += to
			// handle the case when the fnode splits into more than one target
			// reaches.
			int reach = topo.getRowForId(reachID);
			Integer upstreamNode = getUpstreamNode(topo, reach);
			if (topo.getInt(reach, IFTRAN_COL) != 0) {
				transportFraction[upstreamNode] += decayCoefficient.getDouble(reach, UPSTREAM_DECAY_COL);
			}
		}

		// Iterate over all reaches in reverse hydrological sequence order
		for (int reach = maxReachRow; reach >=0 ; reach--) {
			Long reachID = topo.getIdForRow(reach);
			// Accumulate at upstream node only if this reach transmits
			// Don't process the target reaches as they've already been processed.
			if (!targetReaches.contains(reachID) && topo.getInt(reach, IFTRAN_COL) != 0) {
				Integer downstreamNode = getDownstreamNode(topo, reach);
				double upstreamContrib = (transportFraction[downstreamNode]
				                                            * decayCoefficient.getDouble(reach, UPSTREAM_DECAY_COL)); /* Just the decayed upstream portion */
				Integer upstreamNode = getUpstreamNode(topo, reach);
				transportFraction[upstreamNode]+= upstreamContrib;
			}
		}
		return transportFraction;
	}

	/**
	 * Returns the delivery fraction for each reach in an array, in hydseq order
	 * @param targetReaches
	 * @return
	 */
	public double[] calculateReachTransportFraction(Set<Long> targetReaches) {
		int maxReachRow = topo.getRowCount() - 1;
		PredictResultStructure prs = PredictResultStructure.analyzePredictResultStructure(maxReachRow, sourceValues);
		double[] nodeDeliveryFraction = calculateNodeTransportFraction(targetReaches);

		// Delivery fraction, one for each reach and source type
		double[] incReachTransportFraction = new double[prs.reachCount];

		// Iterate over all reaches (order and source irrelevant)
		for (int reach = 0; reach < prs.reachCount; reach++) {
			Integer downstreamNode = getDownstreamNode(topo, reach);
			incReachTransportFraction[reach] = nodeDeliveryFraction[downstreamNode] *
			decayCoefficient.getDouble(reach, INSTREAM_DECAY_COL);
		}

		// target reaches must be calculated separately ( = the instream decay)
		for (Long reachID: targetReaches) {
			int reach = topo.getRowForId(reachID);
			incReachTransportFraction[reach] = decayCoefficient.getDouble(reach, INSTREAM_DECAY_COL);
		}

		// The target reaches calculation is slightly different (use instream decay)
		return incReachTransportFraction;
	}

	/**
	 * Returns the same result as calculateReachTransportFraction(), but in a DataTable with the reachid as the rowID
	 * @param targetReaches
	 * @return
	 */
	public DataTable calculateReachTransportFractionDataTable(Set<Long> targetReaches) {
		int maxReachRow = topo.getRowCount() - 1;
		PredictResultStructure prs = PredictResultStructure.analyzePredictResultStructure(maxReachRow, sourceValues);
		double[] nodeDeliveryFraction = calculateNodeTransportFraction(targetReaches);

		// Delivery fraction, one for each reach and source type
		SimpleDataTableWritable incReachTransportFraction = new SimpleDataTableWritable();
		StandardNumberColumnDataWritable<Double> dataColumn = new StandardNumberColumnDataWritable<Double>("reach_del_fraction", null);
		incReachTransportFraction.addColumn(dataColumn);

		// Iterate over all reaches (order and source irrelevant)
		for (int reach = 0; reach < prs.reachCount; reach++) {
			Integer downstreamNode = getDownstreamNode(topo, reach);

			double value = nodeDeliveryFraction[downstreamNode] * decayCoefficient.getDouble(reach, INSTREAM_DECAY_COL);
			incReachTransportFraction.setValue(value, reach , 0);
			incReachTransportFraction.setRowId(topo.getIdForRow(reach), reach);
		}

		// Target reaches must be calculated separately
		for (Long reachID: targetReaches) {
			int reach = topo.getRowForId(reachID);
			incReachTransportFraction.setValue(decayCoefficient.getDouble(reach, INSTREAM_DECAY_COL), reach , 0);
		}

		return incReachTransportFraction;
	}

	public void calculateIncrementalDeliveredFlux(DataTable reachTransportFunction) {
		//
	}
	// delivery analysis sql
//	select sparrow_model_id, count(*) as total,
//	  count(case when (total_delivery > (inc_delivery * inc_delivery + .00001)) then 1 else 0 end) as tooHigh,
//	  count(case when (total_delivery < (inc_delivery * inc_delivery - .00001)) then 1 else 0 end) as tooLow
//	from reach_coef join model_reach on reach_coef.model_reach_id = model_reach.model_reach_id
//	group by sparrow_model_id
//
//	select sparrow_model_id, fnode, tnode,reach_coef. model_reach_id,
//	  total_delivery, inc_delivery, (total_delivery / (inc_delivery * inc_delivery)) as frac,
//	  (total_delivery - (inc_delivery * inc_delivery)) as diff
//	from reach_coef join model_reach on reach_coef.model_reach_id = model_reach.model_reach_id
//	where (total_delivery > (inc_delivery * inc_delivery) + .00001
//	or total_delivery < (inc_delivery * inc_delivery) - .00001)
//	and total_delivery<>inc_delivery
//	and iteration=0
//	order by sparrow_model_id, fnode, model_reach_id
//
//	select sparrow_model_id, identifier, fnode, tnode,reach_coef. model_reach_id, total_delivery
//	from reach_coef join model_reach on reach_coef.model_reach_id = model_reach.model_reach_id
//	where total_delivery=inc_delivery
//	and iteration=0 and sparrow_model_id=34
//	order by sparrow_model_id, fnode, model_reach_id

	public PredictResultImm calculateDeliveredFlux(
			Set<Long> targetReaches, PredictResult pResult, double[] nodeTransportFraction, DataTable reachTransportFraction)
			throws Exception {
		// TODO Currently, this calculates both incremental and total delivered flux. The calulation for the incremental del flux
		// is easier, and can be streamlined using the reachTransportFraction as it involves only a multiplication,
		//		inc_del_flux = inc_contrib * reach_transport_fraction
		// However, the calculation for the total delivered flux
		//		total_del_flux = total_upstream_contrib * (node_transport_function(at downstream node) or 1 if target reach)

		//This calculation requires the nodeTransport
		// May revisit if total delivered flux is not desired. This is because the incremental flux can be more quickly
		// calculated using just the reachTransportFraction, but this improvement may be negligible (fewer multiplications,
		// no extra memory allocation?
		// TODO Create a weightedDataTableBuilder, taking PredictResult & weighing by nodeTransport fraction

		int maxReachRow = topo.getRowCount() - 1;
		PredictResultStructure prs = PredictResultStructure.analyzePredictResultStructure(maxReachRow, sourceValues);
		double deliveredFlux[][] = new double[prs.reachCount][prs.rchValColCount];

		assert(pResult.getRowCount() == prs.reachCount);
		assert(pResult.getColumnCount() == prs.rchValColCount);

		// Iterate over all reaches
		for (int reach = 0; reach < prs.reachCount; reach++) {
			Double reachDeliveryFraction = reachTransportFraction.getDouble(reach, 0);
			Integer downstreamNode = getDownstreamNode(topo, reach);
			double nodeDeliveryFraction = nodeTransportFraction[downstreamNode];

			for (int sourceType = 0; sourceType < prs.sourceCount; sourceType++) {
				int source = sourceType + prs.sourceCount;
				deliveredFlux[reach][sourceType] = pResult.getDouble(reach, sourceType) * reachDeliveryFraction;
				deliveredFlux[reach][source] = pResult.getDouble(reach, source) * nodeDeliveryFraction;
			}

			deliveredFlux[reach][prs.totalIncrementalColOffset] = pResult.getDouble(reach, prs.totalIncrementalColOffset) * reachDeliveryFraction;
			deliveredFlux[reach][prs.grandTotalColOffset] = pResult.getDouble(reach, prs.grandTotalColOffset) * nodeDeliveryFraction;
		}
		return PredictResultImm.buildPredictResult(deliveredFlux, predictData);


	}

	public PredictResultImm doPredict() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}


	public PredictResult calculateDeliveredFlux(PredictionContext context) throws Exception{

		PredictResult adjResult = SharedApplication.getInstance().getPredictResult(context.getTargetContextOnly());

		// apply delivery fraction weights
		assert(context.getTerminalReaches() != null) : "shouldn't reach this point if no terminal reaches";
		Set<Long> targetReaches = context.getTerminalReaches().asSet();
		double[] nodeTransportFraction = calculateNodeTransportFraction(targetReaches);
		DataTable reachTransportFraction = calculateReachTransportFractionDataTable(targetReaches);
		PredictResult deliveryResult = calculateDeliveredFlux(targetReaches, adjResult, nodeTransportFraction, reachTransportFraction);

		{
			Analysis analysis = context.getAnalysis();
			if (analysis.isAggregated()) {
				AggregationRunner aggRunner = new AggregationRunner(context);
				deliveryResult = aggRunner.doAggregation(deliveryResult);
				// Aggregation can handle weighting underneath
			} else if (analysis.isWeighted()) {
				deliveryResult = WeightRunner.doWeighting(context, deliveryResult);
			}
		}
		return deliveryResult;
	}

	public Object createEntry(Object predictContext) throws Exception {
		PredictionContext context = (PredictionContext) predictContext;

		AggregationRunner aggRunner = new AggregationRunner(context);

		PredictResult adjResult = SharedApplication.getInstance().getPredictResult(context.getTargetContextOnly());

		// apply delivery fraction weights
		assert(context.getTerminalReaches() != null) : "shouldn't reach this point if no terminal reaches";
		Set<Long> targetReaches = context.getTerminalReaches().asSet();
		double[] nodeTransportFraction = calculateNodeTransportFraction(targetReaches);
		DataTable reachTransportFraction = calculateReachTransportFractionDataTable(targetReaches);
		PredictResultImm deliveryResult = calculateDeliveredFlux(targetReaches, adjResult, nodeTransportFraction, reachTransportFraction);


		// Perform transformations called for by the Analysis section
		Analysis analysis = context.getAnalysis();
		DataSeriesType dataSeries = analysis.getDataSeries();
		if (analysis.isAggregated()) {
			adjResult = aggRunner.doAggregation(adjResult);
			// Aggregation can handle weighting underneath
		} else if (analysis.isWeighted()) {
			adjResult = WeightRunner.doWeighting(context, adjResult);
		}

		PredictResult result = null;
		switch (analysis.getNominalComparison()) {
			case none: {
				result = adjResult;
				break;
			}
			case percent: {

				PredictionContext nomContext = new PredictionContext(context.getModelID(), null, null, null, null);
				PredictResult nomResult = SharedApplication.getInstance().getPredictResult(nomContext);

				// Check for aggregation and run if necessary
				if (analysis.isAggregated()) {
					nomResult = aggRunner.doAggregation(nomResult);
				} else if (analysis.isWeighted()) {
					nomResult = WeightRunner.doWeighting(nomContext, nomResult);
				}

				result = new PredictResultCompare(nomResult, adjResult, false);

				break;
			}
			case absolute: {

				PredictionContext nomContext = new PredictionContext(context.getModelID(), null, null, null, null);
				PredictResult nomResult = SharedApplication.getInstance().getPredictResult(nomContext);

				// Check for aggregation and run if necessary
				if (analysis.isAggregated()) {
					nomResult = aggRunner.doAggregation(nomResult);
				} else if (analysis.isWeighted()) {
					nomResult = WeightRunner.doWeighting(nomContext, nomResult);
				}

				result = new PredictResultCompare(nomResult, adjResult, true);

				break;
			}
			default: {
				throw new Exception("Should never be in here...");
			}


		}

		return result;
	}
}
