package org.vaadin.flexcombobox.client.flexcombobox;

import java.util.HashMap;

import com.vaadin.shared.AbstractComponentState;
import com.vaadin.shared.Connector;

@SuppressWarnings("serial")
public class FlexComboboxState extends AbstractComponentState {
	{
        primaryStyleName = "v-filterselect";
    }
	
	public HashMap<Connector, FlexItemProperties> properties = new HashMap<>();
	
	public void addItem(Connector c, String caption) {
		properties.put(c, new FlexItemProperties(caption));
	}
}