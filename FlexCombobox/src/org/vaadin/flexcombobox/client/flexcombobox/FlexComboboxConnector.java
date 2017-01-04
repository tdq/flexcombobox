package org.vaadin.flexcombobox.client.flexcombobox;

import java.util.List;

import org.vaadin.flexcombobox.FlexComboBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.ui.AbstractComponentContainerConnector;
import com.vaadin.shared.ui.Connect;

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
	public FlexComboboxState getState() {
		return (FlexComboboxState) super.getState();
	}
	
	@Override
	public void updateCaption(ComponentConnector connector) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnectorHierarchyChange(ConnectorHierarchyChangeEvent connectorHierarchyChangeEvent) {
		List<ComponentConnector> connectors = getChildComponents();
		
		FlexComboboxWidget widget = getWidget();
		widget.clear();
		
		for(ComponentConnector connector : connectors) {
			FlexItemProperties properties = getState().properties.get(connector);
			widget.addComponent(connector, properties);
		}
	}
}

