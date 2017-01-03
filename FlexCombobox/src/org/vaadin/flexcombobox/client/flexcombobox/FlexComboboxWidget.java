package org.vaadin.flexcombobox.client.flexcombobox;

import com.google.gwt.aria.client.Roles;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;
import com.vaadin.client.DeferredWorker;
import com.vaadin.client.Focusable;
import com.vaadin.client.ui.Field;
import com.vaadin.client.ui.SubPartAware;
import com.vaadin.client.ui.aria.HandlesAriaCaption;
import com.vaadin.client.ui.aria.HandlesAriaInvalid;
import com.vaadin.client.ui.aria.HandlesAriaRequired;

// TODO extend any GWT Widget
public class FlexComboboxWidget extends Composite implements Field, KeyDownHandler,
	KeyUpHandler, ClickHandler, FocusHandler, BlurHandler, Focusable,
	SubPartAware, HandlesAriaCaption, HandlesAriaInvalid,
	HandlesAriaRequired, DeferredWorker {

	public static final String CLASSNAME = "v-filterselect";
	private static final String STYLE_NO_INPUT = "no-input";
	
	// shown in unfocused empty field, disappears on focus (e.g "Search here")
    private static final String CLASSNAME_PROMPT = "prompt";
	
	/** For internal use only. May be removed or replaced in the future. */
    public int pageLength = 10;
    
    private final FlowPanel panel = new FlowPanel();
    
    /**
     * The text box where the filter is written
     * <p>
     * For internal use only. May be removed or replaced in the future.
     */
    private final TextBox tb;

    /**
     * Used when measuring the width of the popup
     */
    private final HTML popupOpener = new HTML("") {

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.google.gwt.user.client.ui.Widget#onBrowserEvent(com.google.gwt
         * .user.client.Event)
         */

        @Override
        public void onBrowserEvent(Event event) {
            super.onBrowserEvent(event);

            /*
             * Prevent the keyboard focus from leaving the textfield by
             * preventing the default behaviour of the browser. Fixes #4285.
             */
            //handleMouseDownEvent(event);
        }
    };
    
	public FlexComboboxWidget() {
		tb = createTextBox();
		//suggestionPopup = createSuggestionPopup();

        popupOpener.sinkEvents(Event.ONMOUSEDOWN);
        Roles.getButtonRole()
                .setAriaHiddenState(popupOpener.getElement(), true);
        Roles.getButtonRole().set(popupOpener.getElement());

        panel.add(tb);
        panel.add(popupOpener);
        initWidget(panel);
        Roles.getComboboxRole().set(panel.getElement());

        tb.addKeyDownHandler(this);
        tb.addKeyUpHandler(this);

        tb.addFocusHandler(this);
        tb.addBlurHandler(this);
        tb.addClickHandler(this);

        popupOpener.addClickHandler(this);

        setStyleName(CLASSNAME);

        sinkEvents(Event.ONPASTE);
	}

	protected void updateStyleNames() {
        tb.setStyleName(getStylePrimaryName() + "-input");
        popupOpener.setStyleName(getStylePrimaryName() + "-button");
        //suggestionPopup.setStyleName(getStylePrimaryName() + "-suggestpopup");
    }
	
	/**
     * This method will create the TextBox used by the VFilterSelect instance.
     * It is invoked during the Constructor and should only be overridden if a
     * custom TextBox shall be used. The overriding method cannot use any
     * instance variables.
     * 
     * @since 7.1.5
     * @return TextBox instance used by this VFilterSelect
     */
    protected TextBox createTextBox() {
        return new FilterSelectTextBox();
    }
	
    @Override
    public void setStyleName(String style) {
        super.setStyleName(style);
        updateStyleNames();
    }

    @Override
    public void setStylePrimaryName(String style) {
        super.setStylePrimaryName(style);
        updateStyleNames();
    }
    
	@Override
	public boolean isWorkPending() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setAriaRequired(boolean required) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setAriaInvalid(boolean invalid) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void bindAriaCaption(Element captionElement) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Element getSubPartElement(String subPart) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSubPartName(Element subElement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void focus() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onBlur(BlurEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFocus(FocusEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(ClickEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onKeyUp(KeyUpEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onKeyDown(KeyDownEvent event) {
		// TODO Auto-generated method stub
		
	}

}