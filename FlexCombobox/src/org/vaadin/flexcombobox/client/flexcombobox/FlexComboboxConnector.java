package org.vaadin.flexcombobox.client.flexcombobox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;

import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.ui.Connect;

import org.vaadin.flexcombobox.FlexCombobox;
import org.vaadin.flexcombobox.client.flexcombobox.FlexComboboxWidget;
import org.vaadin.flexcombobox.client.flexcombobox.FlexComboboxServerRpc;
import com.vaadin.client.communication.RpcProxy;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.client.MouseEventDetailsBuilder;
import org.vaadin.flexcombobox.client.flexcombobox.FlexComboboxClientRpc;
import org.vaadin.flexcombobox.client.flexcombobox.FlexComboboxState;
import com.vaadin.client.communication.StateChangeEvent;

@Connect(FlexCombobox.class)
public class FlexComboboxConnector extends AbstractComponentConnector {

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
	public void onStateChanged(StateChangeEvent stateChangeEvent) {
		super.onStateChanged(stateChangeEvent);

		// TODO do something useful
		final String text = getState().text;
		getWidget().setText(text);
	}

}

