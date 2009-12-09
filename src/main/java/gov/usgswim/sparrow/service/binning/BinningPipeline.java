package gov.usgswim.sparrow.service.binning;

import gov.usgs.webservices.framework.formatter.IFormatter;
import gov.usgs.webservices.framework.formatter.XMLPassThroughFormatter;
import gov.usgs.webservices.framework.formatter.IFormatter.OutputType;
import gov.usgswim.sparrow.service.AbstractPipeline;

public class BinningPipeline extends AbstractPipeline<BinningServiceRequest> {
    public BinningPipeline() {
        super(new BinningService(), new BinningParser());
    }
    
    @Override
    protected IFormatter getCustomFlatteningFormatter(OutputType outputType) {
        return new XMLPassThroughFormatter();
    }
}
