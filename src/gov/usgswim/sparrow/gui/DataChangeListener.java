package gov.usgswim.sparrow.gui;

import java.util.EventListener;

public interface DataChangeListener extends EventListener {
	void dataChanged(DataChangeEvent event);
}
