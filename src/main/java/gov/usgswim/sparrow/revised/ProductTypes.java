/**
 *
 */
package gov.usgswim.sparrow.revised;


import gov.usgswim.sparrow.revised.request.SparrowRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public enum ProductTypes{
	PREDICT_DATA("source", "topo", "coef", "sourceMetadata", "decay"),
	ANCIL_DATA,
	AGGREGATION_DATA,
	//
	INCREMENTAL_FLUX("single source", "total", "all sources"),
	CUMULATIVE_FLUX("single source", "total", "all sources"),
	DELIVERY_FRACTION,
	STD_ERROR_ESTIMATE("total", "incremental"),
	//
	WEIGHT("yield", "delivery-instream", "delivery-upstream", "concentration", "iftran"),
	AGGREGATION ("huc8","huc6","huc4", "huc2");

	// ProductTypes take...
	// Product types are possible combinations

	public static class Product{
		public final ProductTypes type;
		public final String variant;

		Product(ProductTypes type, String... variants){
			this.type = type;
			if (variants != null && variants.length > 0) {
				this.variant = variants[0];
			} else {
				this.variant = null;
			}
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			if (variant == null) {
				return type.name();
			}
			return type.name() + "(" + variant + ")";
		}

		public boolean isLoadable() {
			switch(this.type) {
				case PREDICT_DATA:
				case ANCIL_DATA:
				case INCREMENTAL_FLUX:
				case CUMULATIVE_FLUX:
				case AGGREGATION_DATA:
				case AGGREGATION:
					return true;
				case DELIVERY_FRACTION:
					return false;
				case STD_ERROR_ESTIMATE:
				case WEIGHT: // not for delivery weight....

				default:
					throw new RuntimeException("type " + this.type.name() + " has not been specified as loadable or not");
			}
		}
	}

	private Set<String> _variants;

	private ProductTypes(String... variants) {
		if (variants != null && variants.length > 0) {
			HashSet<String> variantSet = new HashSet<String>();
			variantSet.addAll(Arrays.asList(variants));
			this._variants = Collections.unmodifiableSet(variantSet);
		} else {
			this._variants = Collections.emptySet();
		}
	}

	public Set<String> getVariants(){
		return _variants;
	}

	public ProductTypes.Product get(String... variant ) {
		if (!_variants.isEmpty() && variant != null && variant.length > 0) {
			 if (variant[0] != null && !_variants.contains(variant[0])) {
				 throw new IllegalArgumentException("unrecognized variant of " + variant[0] + " for ProductType " + name());
			 }
		}
		return new ProductTypes.Product(this, variant);
	}

	public ProductTypes.Product get(SparrowRequest req, String... variant ) {
		return new ProductTypes.Product(this, variant);
	}

}