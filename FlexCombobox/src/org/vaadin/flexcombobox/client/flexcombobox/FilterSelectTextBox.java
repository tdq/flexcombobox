package org.vaadin.flexcombobox.client.flexcombobox;

import com.google.gwt.user.client.ui.TextBox;
import com.vaadin.client.WidgetUtil;

class FilterSelectTextBox extends TextBox {
	
	/**
     * If set to false, the component should not allow entering text to the
     * field even for filtering.
     */
    private boolean textInputEnabled = true;
    
	/**
     * Creates a new filter select text box.
     * 
     * @since 7.6.4
     */
    public FilterSelectTextBox() {
        /*-
         * Stop the browser from showing its own suggestion popup.
         * 
         * Using an invalid value instead of "off" as suggested by
         * https://developer.mozilla.org/en-US/docs/Web/Security/Securing_your_site/Turning_off_form_autocompletion
         * 
         * Leaving the non-standard Safari options autocapitalize and
         * autocorrect untouched since those do not interfere in the same
         * way, and they might be useful in a combo box where new items are
         * allowed.
         */
        getElement().setAttribute("autocomplete", "nope");
    }

    /**
     * Overridden to avoid selecting text when text input is disabled
     */
    @Override
    public void setSelectionRange(int pos, int length) {
        if (textInputEnabled) {
            /*
             * set selection range with a backwards direction: anchor at the
             * back, focus at the front. This means that items that are too
             * long to display will display from the start and not the end
             * even on Firefox.
             * 
             * We need the JSNI function to set selection range so that we
             * can use the optional direction attribute to set the anchor to
             * the end and the focus to the start. This makes Firefox work
             * the same way as other browsers (#13477)
             */
            WidgetUtil.setSelectionRange(getElement(), pos, length,
                    "backward");

        } else {
            /*
             * Setting the selectionrange for an uneditable textbox leads to
             * unwanted behaviour when the width of the textbox is narrower
             * than the width of the entry: the end of the entry is shown
             * instead of the beginning. (see #13477)
             * 
             * To avoid this, we set the caret to the beginning of the line.
             */

            super.setSelectionRange(0, 0);
        }
    }
}
