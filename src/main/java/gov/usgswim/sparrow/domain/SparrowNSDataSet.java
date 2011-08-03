package gov.usgswim.sparrow.domain;

import java.util.Vector;

import oracle.mapviewer.share.ext.NSDataSet;
import oracle.mapviewer.share.ext.NSRow;

public class SparrowNSDataSet extends NSDataSet {

	public SparrowNSDataSet(NSRow[] arg0) {
		super(arg0);
	}

	public SparrowNSDataSet(Vector arg0) {
		super(arg0);
	}

	@Override
	public synchronized void close() {
		//Ignore the call to 'close'.  We cache this data, so we don't want it
		//destroyed.
	}

}
