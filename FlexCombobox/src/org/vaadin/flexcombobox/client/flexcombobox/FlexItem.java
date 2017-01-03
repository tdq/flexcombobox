package org.vaadin.flexcombobox.client.flexcombobox;

import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.UIDL;

class FlexItem extends Widget {

	private final String caption;
	
	public FlexItem(UIDL uidl) {
		caption = uidl.getStringAttribute("caption");
	}
}
