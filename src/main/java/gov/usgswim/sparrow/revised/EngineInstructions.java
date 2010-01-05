package gov.usgswim.sparrow.revised;

import gov.usgswim.sparrow.revised.request.SparrowRequest;
import gov.usgswim.sparrow.revised.steps.Step;
import gov.usgswim.sparrow.revised.transformers.NSDatasetTransformer;
import gov.usgswim.sparrow.revised.transformers.Transformer;

import java.util.ArrayList;
import java.util.List;


public class EngineInstructions {
	CalculationResult finalResult;
	private List<CalculationStage> stages = new ArrayList<CalculationStage>();
	private Transformer transformer;
	public final SparrowRequest request;
	public String column;

	public EngineInstructions(SparrowRequest request) {
		this.request = request;
	}

	public CalculationStage addStep(Step step) {
		CalculationStage stage = new CalculationStage(step);
		getStages().add(stage);
		return stage;
	}



	public static enum Transformation{
		TO_SINGLE_COLUMN, TO_NSDATASET(new NSDatasetTransformer()), TO_EXPORT;

		private Transformer transformer;

		private Transformation(Transformer... transformers) {
			this.transformer = (transformers == null || transformers.length == 0)? null: transformers[0];
		}
		public CalculationResult apply(CalculationResult result, String source) {
			return (transformer == null)? result: transformer.transform(result, source);
		}
		public Transformer getTransformer() {
			return transformer;
		}
	}

	public void addTransformation(Transformation tranform, String... colName) {
		this.transformer = tranform.getTransformer();
		this.column = (colName == null || colName.length == 0)? null: colName[0];
	}

	public void setResult(CalculationResult result) {
		this.finalResult = result;
	}

	public CalculationResult getResult() {
		return finalResult;
	}

	public String getSource() {
		return request.getSource();
	}

//	public void setStages(List<CalculationStage> stages) {
//		this.stages = stages;
//	}

	public List<CalculationStage> getStages() {
		return stages;
	}

//	public void setTransformer(Transformer transformer) {
//		this.transformer = transformer;
//	}

	public Transformer getTransformer() {
		return transformer;
	}

}
