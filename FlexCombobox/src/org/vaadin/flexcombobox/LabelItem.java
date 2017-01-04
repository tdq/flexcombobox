package org.vaadin.flexcombobox;

import org.vaadin.flexcombobox.FlexComboBox.FlexItem;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

public class LabelItem implements FlexItem {

	private String caption;
	private Label label;
	
	public LabelItem(String caption) {
		this.caption = caption;
		this.label = new Label(caption);
	}
	
	@Override
	public String getCaption() {
		return caption;
	}

	@Override
	public Component getComponent() {
		return label;
	}
}
