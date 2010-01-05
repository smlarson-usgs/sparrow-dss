package gov.usgswim.sparrow.revised;

public class SparrowContext {

	public final int modelId;

	public SparrowContext(int modelId) {
		this.modelId = modelId;
	}

	public int getModelId() {
		return modelId;
	}

	public SparrowContext getTargetContext() {
		return this; // TODO
	}

	public SparrowContext getAdjustmentContext() {
		return this; // TODO
	}


	public void setTargetContext() {

	}

	public void setAdjustmentContext() {

	}

	public void getSource() {
		// TODO Auto-generated method stub

	}
}
