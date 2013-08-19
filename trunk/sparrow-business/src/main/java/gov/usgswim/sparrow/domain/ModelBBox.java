package gov.usgswim.sparrow.domain;

/**
 * Represents a bounding box for a specific model.
 * 
 * @author eeverman
 *
 */
public class ModelBBox extends BBox {

	private Long modelId;
	
	/**
	 * Constructor that does bounds checking.
	 * 
	 * @param modelId
	 * @param leftLongBound
	 * @param lowerLatBound
	 * @param rightLongBound
	 * @param upperLatBound
	 * @throws Exception
	 */
	public ModelBBox(Long modelId, double leftLongBound, double lowerLatBound,
			double rightLongBound, double upperLatBound) throws Exception {
		super(leftLongBound, lowerLatBound, rightLongBound, upperLatBound);
		
		this.modelId = modelId;
	}

	/**
	 * Constructor that does bounds checking.
	 * The passed bounds string is expected to be in this format:
	 * left,lower,right,upper
	 * Extra spaces are permitted.
	 * 
	 * @param modelId
	 * @param boundsSring
	 * @throws Exception
	 */
	public ModelBBox(Long modelId, String boundsSring) throws Exception {
		super(boundsSring);
		this.modelId = modelId;
	}

	/**
	 * Constructor that does bounds checking.
	 * The passed bounds array is expected to be in this format:
	 * [0] left, [1] lower, [2] right, [3] upper
	 * 
	 * @param modelId
	 * @param bounds
	 * @throws Exception
	 */
	public ModelBBox(Long modelId, double[] bounds) throws Exception {
		super(bounds);
		this.modelId = modelId;
	}
	

	/**
	 * @return the modelId
	 */
	public Long getModelId() {
		return modelId;
	}

}
