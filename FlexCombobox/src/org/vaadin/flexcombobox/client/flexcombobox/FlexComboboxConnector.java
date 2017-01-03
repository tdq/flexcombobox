package org.vaadin.flexcombobox.client.flexcombobox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;

import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.client.ui.AbstractComponentContainerConnector;
import com.vaadin.shared.ui.Connect;

import java.util.List;

import org.vaadin.flexcombobox.FlexComboBox;
import org.vaadin.flexcombobox.client.flexcombobox.FlexComboboxWidget;
import org.vaadin.flexcombobox.client.flexcombobox.FlexComboboxServerRpc;
import com.vaadin.client.communication.RpcProxy;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.ConnectorMap;
import com.vaadin.client.MouseEventDetailsBuilder;
import org.vaadin.flexcombobox.client.flexcombobox.FlexComboboxClientRpc;
import org.vaadin.flexcombobox.client.flexcombobox.FlexComboboxState;
import com.vaadin.client.communication.StateChangeEvent;

@Connect(FlexComboBox.class)
public class FlexComboboxConnector extends AbstractComponentContainerConnector {
	/*
	FlexComboboxServerRpc rpc = RpcProxy
			.create(FlexComboboxServerRpc.class, this);
	
	public FlexComboboxConnector() {
		registerRpc(FlexComboboxClientRpc.class, new FlexComboboxClientRpc() {
			public void alert(String message) {
				// TODO Do something useful
				Window.alert(message);
			}
		});

		// TODO ServerRpc usage example, do something useful instead
		getWidget().addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				final MouseEventDetails mouseDetails = MouseEventDetailsBuilder
					.buildMouseEventDetails(event.getNativeEvent(),
								getWidget().getElement());
				rpc.clicked(mouseDetails);
			}
		});

	}
	*/

	@Override
	protected Widget createWidget() {
		return GWT.create(FlexComboboxWidget.class);
	}
	
	@Override
	public FlexComboboxWidget getWidget() {
		return (FlexComboboxWidget) super.getWidget();
	}

	@Override
	public void updateCaption(ComponentConnector connector) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnectorHierarchyChange(ConnectorHierarchyChangeEvent connectorHierarchyChangeEvent) {
		List<ComponentConnector> children = getChildComponents();
		FlexComboboxWidget widget = getWidget();
		widget.clear();
		
		for(ComponentConnector component : children) {
			widget.add(component);
		}
	}
}

