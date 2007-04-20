package gov.usgswim.sparrow;

public class Adjustment {
	private int id;
	private double val;
	private SourceAdjustments.AdjustmentType type;
	
	public Adjustment(SourceAdjustments.AdjustmentType adjType, int index, double value) {
		type = adjType;
		id = index;
		val = value;
	}

	public SourceAdjustments.AdjustmentType getType() {
		return type;
	}

	public int getId() {
		return id;
	}

	public double getValue() {
		return val;
	}
}
