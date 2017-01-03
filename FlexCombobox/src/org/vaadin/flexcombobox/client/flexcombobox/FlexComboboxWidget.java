package org.vaadin.flexcombobox.client.flexcombobox;

import java.util.ArrayList;
import java.util.List;

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
import com.google.gwt.i18n.client.HasDirection.Direction;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.BrowserInfo;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.DeferredWorker;
import com.vaadin.client.Focusable;
import com.vaadin.client.ui.Field;
import com.vaadin.client.ui.SubPartAware;
import com.vaadin.client.ui.VFilterSelect.FilterSelectSuggestion;
import com.vaadin.client.ui.VFilterSelect.Select;
import com.vaadin.client.ui.aria.HandlesAriaCaption;
import com.vaadin.client.ui.aria.HandlesAriaInvalid;
import com.vaadin.client.ui.aria.HandlesAriaRequired;
import com.vaadin.shared.EventId;
import com.vaadin.shared.ui.combobox.FilteringMode;

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
    private int pageLength = 10;
    private int currentPage;
    private boolean immediate;
    private String selectedOptionKey;
    private boolean waitingForFilteringResponse = false;
    private boolean updateSelectionWhenReponseIsReceived = false;
    private boolean initDone = false;
    private String lastFilter = "";

    public enum Select {
        NONE, FIRST, LAST
    }

    private Select selectPopupItemWhenResponseIsReceived = Select.NONE;
    private ApplicationConnection client;
    
    /**
     * The current suggestion selected from the dropdown. This is one of the
     * values in currentSuggestions except when filtering, in this case
     * currentSuggestion might not be in currentSuggestions.
     * <p>
     * For internal use only. May be removed or replaced in the future.
     */
    private FilterSelectSuggestion currentSuggestion;
    private boolean allowNewItem;
    private int totalMatches;
    private boolean nullSelectionAllowed;
    private boolean nullSelectItem;
    private boolean enabled;
    private boolean readonly;
    private FilteringMode filteringmode = FilteringMode.OFF;
    private String inputPrompt = "";
    private boolean prompting = false;
    private String paintableId;

    /**
     * Set true when popupopened has been clicked. Cleared on each UIDL-update.
     * This handles the special case where are not filtering yet and the
     * selected value has changed on the server-side. See #2119
     * <p>
     * For internal use only. May be removed or replaced in the future.
     */
    private boolean popupOpenerClicked;
    private int suggestionPopupMinWidth = 0;
    private int popupWidth = -1;
    /**
     * Stores the last new item string to avoid double submissions. Cleared on
     * uidl updates.
     * <p>
     * For internal use only. May be removed or replaced in the future.
     */
    private String lastNewItemString;
    private boolean focused = false;

    /**
     * If set to false, the component should not allow entering text to the
     * field even for filtering.
     */
    private boolean textInputEnabled = true;
    /**
     * A flag which cancels the blur event and sets the focus back to the
     * textfield if the Browser is IE
     */
    private boolean preventNextBlurEventInIE = false;
    
    private final FlowPanel panel = new FlowPanel();
    private final SuggestionPopup suggestionPopup;
    private final List<Widget> currentSuggestions = new ArrayList<Widget>();
    
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
            handleMouseDownEvent(event);
        }
    };
    
	public FlexComboboxWidget() {
		tb = createTextBox();
		suggestionPopup = createSuggestionPopup();

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
        suggestionPopup.setStyleName(getStylePrimaryName() + "-suggestpopup");
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
    
    /**
     * This method will create the SuggestionPopup used by the VFilterSelect
     * instance. It is invoked during the Constructor and should only be
     * overridden if a custom SuggestionPopup shall be used. The overriding
     * method cannot use any instance variables.
     * 
     * @since 7.1.5
     * @return SuggestionPopup instance used by this VFilterSelect
     */
    protected SuggestionPopup createSuggestionPopup() {
        return new SuggestionPopup(this);
    }
	
    /**
     * Handles special behavior of the mouse down event
     * 
     * @param event
     */
    private void handleMouseDownEvent(Event event) {
        /*
         * Prevent the keyboard focus from leaving the textfield by preventing
         * the default behaviour of the browser. Fixes #4285.
         */
        if (event.getTypeInt() == Event.ONMOUSEDOWN) {
            event.preventDefault();
            event.stopPropagation();

            /*
             * In IE the above wont work, the blur event will still trigger. So,
             * we set a flag here to prevent the next blur event from happening.
             * This is not needed if do not already have focus, in that case
             * there will not be any blur event and we should not cancel the
             * next blur.
             */
            if (BrowserInfo.get().isIE() && focused) {
                preventNextBlurEventInIE = true;
            }
        }
    }
    
    /**
     * Turns prompting off. When prompting is turned on a command prompt is
     * shown in the text box if nothing has been entered.
     * <p>
     * For internal use only. May be removed or replaced in the future.
     * 
     * @param text
     *            The text the text box should contain.
     */
    public void setPromptingOff(String text) {
        setTextboxText(text);
        if (prompting) {
            prompting = false;
            removeStyleDependentName(CLASSNAME_PROMPT);
        }
    }
    
    /**
     * Sets the text in the text box.
     * 
     * @param text
     *            the text to set in the text box
     */
    public void setTextboxText(final String text) {
        setText(text);
    }
    
    private void setText(final String text) {
        /**
         * To leave caret in the beginning of the line. SetSelectionRange
         * wouldn't work on IE (see #13477)
         */
        Direction previousDirection = tb.getDirection();
        tb.setDirection(Direction.RTL);
        tb.setText(text);
        tb.setDirection(previousDirection);
    }
    
    /**
     * Filters the options at certain page using the given filter
     * 
     * @param page
     *            The page to filter
     * @param filter
     *            The filter to apply to the options
     * @param immediate
     *            Whether to send the options request immediately
     */
    private void filterOptions(int page, String filter, boolean immediate) {

        if (filter.equals(lastFilter) && currentPage == page) {
            if (!suggestionPopup.isAttached()) {
                suggestionPopup.showSuggestions(currentSuggestions,
                        currentPage, totalMatches);
            }
            return;
        }
        if (!filter.equals(lastFilter)) {
            // when filtering, let the server decide the page unless we've
            // set the filter to empty and explicitly said that we want to see
            // the results starting from page 0.
            if ("".equals(filter) && page != 0) {
                // let server decide
                page = -1;
            } else {
                page = 0;
            }
        }

        waitingForFilteringResponse = true;
        client.updateVariable(paintableId, "filter", filter, false);
        client.updateVariable(paintableId, "page", page, immediate);
        afterUpdateClientVariables();

        lastFilter = filter;
        currentPage = page;
    }
    
    /*
     * Anything that should be set after the client updates the server.
     */
    private void afterUpdateClientVariables() {
        // We need this here to be consistent with the all the calls.
        // Then set your specific selection type only after
        // client.updateVariable() method call.
        selectPopupItemWhenResponseIsReceived = Select.NONE;
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
		focused = true;
        if (prompting && !readonly) {
            setPromptingOff("");
        }
        tb.setFocus(true);
	}

	@Override
	public void onBlur(BlurEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFocus(FocusEvent event) {
		// TODO Auto-generated method stub
		
	}

	/**
     * Listener for popupopener
     */
	@Override
	public void onClick(ClickEvent event) {
		/*
		if (textInputEnabled
                && event.getNativeEvent().getEventTarget().cast() == tb
                        .getElement()) {
            // Don't process clicks on the text field if text input is enabled
            return;
        }
        */
        if (enabled && !readonly) {
            // ask suggestionPopup if it was just closed, we are using GWT
            // Popup's auto close feature
            if (!suggestionPopup.isJustClosed()) {
                // If a focus event is not going to be sent, send the options
                // request immediately; otherwise queue in the same burst as the
                // focus event. Fixes #8321.
                
            	boolean immediate = focused
                        || !client.hasEventListeners(this, EventId.FOCUS);
                filterOptions(-1, "", immediate);
                
                popupOpenerClicked = true;
                lastFilter = "";
            }
            
            DOM.eventGetCurrentEvent().preventDefault();
            focus();
            tb.selectAll();
        }
        
        if(suggestionPopup.isShowing() == false) {
        	suggestionPopup.showSuggestions(currentSuggestions, currentPage, currentSuggestions.size());
        }
	}

	@Override
	public void onKeyUp(KeyUpEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onKeyDown(KeyDownEvent event) {
		// TODO Auto-generated method stub
		
	}

	public void clear() {
		currentSuggestions.clear();
	}

	public void add(ComponentConnector component) {
		currentSuggestions.add(component.getWidget());
	}

}