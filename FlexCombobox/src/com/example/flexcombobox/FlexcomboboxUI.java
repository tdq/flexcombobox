package com.example.flexcombobox;

import java.util.Arrays;

import javax.servlet.annotation.WebServlet;

import org.vaadin.flexcombobox.FlexComboBox;
import org.vaadin.flexcombobox.LabelItem;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.annotations.Widgetset;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorMap;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
@Theme("flexcombobox")
@Widgetset("org.vaadin.flexcombobox.FlexcomboboxWidgetset")
public class FlexcomboboxUI extends UI {

	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = FlexcomboboxUI.class)
	public static class Servlet extends VaadinServlet {
	}

	@Override
	protected void init(VaadinRequest request) {
		final VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		setContent(layout);

		Button button = new Button("Click Me");
		button.addClickListener(new Button.ClickListener() {
			public void buttonClick(ClickEvent event) {
				layout.addComponent(new Label("Thank you for clicking"));
			}
		});
		layout.addComponent(button);
		
		FlexComboBox<LabelItem> flexCombobox = new FlexComboBox<>();
		flexCombobox.addItem(new LabelItem("Option 1"));
		flexCombobox.addItem(new LabelItem("Option 2"));
		
		layout.addComponent(flexCombobox);
		
		ComboBox combobox = new ComboBox("Caption", Arrays.asList(new String[] {
			"Option 1", "Option 2", "Option 3"
			}));
		
		layout.addComponent(combobox);
	}

}