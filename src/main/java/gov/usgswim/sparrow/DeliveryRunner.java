package gov.usgswim.sparrow;

import static gov.usgswim.sparrow.PredictData.FNODE_COL;
import static gov.usgswim.sparrow.PredictData.IFTRAN_COL;
import static gov.usgswim.sparrow.PredictData.INSTREAM_DECAY_COL;
import static gov.usgswim.sparrow.PredictData.TNODE_COL;
import static gov.usgswim.sparrow.PredictData.UPSTREAM_DECAY_COL;

import java.util.Set;

import oracle.sdovis.style.StyleColor;

import gov.usgswim.datatable.ColumnDataWritable;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.datatable.impl.StandardNumberColumnDataWritable;
import gov.usgswim.sparrow.datatable.PredictResultImm;
import gov.usgswim.sparrow.navigation.NavigationUtils;

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

		// Delivery fraction, one for each node and source type
		double transportFraction[] = new double[nodeCount];

		for (Long reachID : targetReaches) {
			// Initialize node contributions, set = 1 for all tnodes of targetReaches
			int reach = topo.getRowForId(reachID);
			Integer downstreamNode = topo.getInt(reach, TNODE_COL);
			transportFraction[downstreamNode] = 1;
		}

		// Iterate over all reaches in reverse hydrological sequence order
		for (int reach = maxReachRow; reach >=0 ; reach--) {

			// Accumulate at upstream node only if this reach transmits
			if (topo.getInt(reach, IFTRAN_COL) != 0) {
				Integer upstreamNode = topo.getInt(reach, TNODE_COL);
				double upstreamContrib = (transportFraction[upstreamNode] 
				                                            * decayCoefficient.getDouble(reach, UPSTREAM_DECAY_COL)); /* Just the decayed upstream portion */
				transportFraction[topo.getInt(reach, FNODE_COL)]+= upstreamContrib;
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
			Integer downstreamNode = topo.getInt(reach, TNODE_COL);
			incReachTransportFraction[reach] = nodeDeliveryFraction[downstreamNode] * 
				deliveryCoefficient.getDouble(reach, INSTREAM_DECAY_COL);
		}
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
			Integer downstreamNode = topo.getInt(reach, TNODE_COL);

			double value = nodeDeliveryFraction[downstreamNode] * deliveryCoefficient.getDouble(reach, INSTREAM_DECAY_COL);
			incReachTransportFraction.setValue(value, reach , 0);
			incReachTransportFraction.setRowId(topo.getIdForRow(reach), reach);
			if (value > 0.0001) {
				System.out.println(reach + ", " + topo.getIdForRow(reach) + ", (" + topo.getInt(reach, FNODE_COL) + ", " + downstreamNode + ") :: " + nodeDeliveryFraction[downstreamNode] 
				 + ", <" + deliveryCoefficient.getDouble(reach, UPSTREAM_DECAY_COL)+ ", " + deliveryCoefficient.getDouble(reach, INSTREAM_DECAY_COL) 
				 + "> [" + value + "]");
			}
		}
		return incReachTransportFraction;
	}

	public PredictResultImm calculateDeliveredFlux(double[] nodeTransportFraction) throws Exception {
		// TODO Currently, this calculates both incremental and total delivered flux(NOT). This calculation requires the nodeTransport
		// May revisit if total delivered flux is not desired. This is because the incremental flux can be more quickly
		// calculated using just the reachTransportFraction, but this improvement may be negligible (fewer multiplications,
		// no extra memory allocation?
		// TODO Create a weightedDataTableBuilder, taking PredictResult & weighing by nodeTransport fraction 
		int maxReachRow = topo.getRowCount() - 1;
		PredictResultStructure prs = PredictResultStructure.analyzePredictResultStructure(maxReachRow, sourceValues);

		double incReachDeliveredFlux[][] = new double[prs.reachCount][prs.rchValColCount];

		// Iterate over all reaches
		for (int reach = 0; reach < prs.reachCount; reach++) {

			double reachIncrementalContributionAllSourcesTotal = 0d; // incremental for all sources/ (NOT decayed)
			double rchGrandTotal = 0d; // all sources + all from upstream node (decayed)

			Integer downstreamNode = topo.getInt(reach, TNODE_COL);
			double downstreamNodeTransportFraction = nodeTransportFraction[downstreamNode];

			// Iterate over all sources
			for (int sourceType = 0; sourceType < prs.sourceCount; sourceType++) {
				int upstreamSource = sourceType + prs.sourceCount;

				// temp var to store the incremental per source k.
				// Land delivery and coeff both included in coef value. (NOT
				// decayed)
				double incrementalReachFluxContribution = deliveryCoefficient
				.getDouble(reach, sourceType)
				* sourceValues.getDouble(reach, sourceType);


				incReachDeliveredFlux[reach][sourceType] = incrementalReachFluxContribution
				* decayCoefficient.getDouble(reach, INSTREAM_DECAY_COL)
				* downstreamNodeTransportFraction;
				// TODO Not calculating upstream node contribution at this time as that requires tracking nodes or doing a WeightedDataTable. Maybe later

				reachIncrementalContributionAllSourcesTotal += incrementalReachFluxContribution; // add to incremental total for all sources at reach
			}
			incReachDeliveredFlux[reach][prs.totalIncrementalColOffset] = reachIncrementalContributionAllSourcesTotal; // incremental for all sources (NOT decayed)

			// TODO, process reachIncrementalContributionAllSourcesTotal properly
		}

		return PredictResultImm.buildPredictResult(incReachDeliveredFlux,
				predictData);

	}

	public PredictResultImm doPredict() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
