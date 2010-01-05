package gov.usgswim.sparrow.revised;



public class Need {
	public final SparrowRequest request;
	public final Action action;
	public final ProductTypes product;

	public Need(SparrowRequest request, Action action, ProductTypes product) {
		this.request = request;
		this.action = action;
		this.product = product;
	}
}
