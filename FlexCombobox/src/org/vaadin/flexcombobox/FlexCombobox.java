package org.vaadin.flexcombobox;

import org.vaadin.flexcombobox.client.flexcombobox.FlexComboboxClientRpc;
import org.vaadin.flexcombobox.client.flexcombobox.FlexComboboxServerRpc;
import com.vaadin.shared.MouseEventDetails;
import org.vaadin.flexcombobox.client.flexcombobox.FlexComboboxState;

public class FlexCombobox extends com.vaadin.ui.AbstractComponent {

	private FlexComboboxServerRpc rpc = new FlexComboboxServerRpc() {
		private int clickCount = 0;

		public void clicked(MouseEventDetails mouseDetails) {
			// nag every 5:th click using RPC
			if (++clickCount % 5 == 0) {
				getRpcProxy(FlexComboboxClientRpc.class).alert(
						"Ok, that's enough!");
			}
			// update shared state
			getState().text = "You have clicked " + clickCount + " times";
		}
	};  

	public FlexCombobox() {
		registerRpc(rpc);
	}

	@Override
	public FlexComboboxState getState() {
		return (FlexComboboxState) super.getState();
	}
}
