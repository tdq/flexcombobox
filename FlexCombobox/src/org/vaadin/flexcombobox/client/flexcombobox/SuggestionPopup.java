package org.vaadin.flexcombobox.client.flexcombobox;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.aria.client.Roles;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.layout.client.Layout;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.BrowserInfo;
import com.vaadin.client.ComputedStyle;
import com.vaadin.client.UIDL;
import com.vaadin.client.WidgetUtil;
import com.vaadin.client.ui.VFilterSelect;
import com.vaadin.client.ui.VOverlay;
import com.vaadin.client.ui.menubar.MenuItem;
import com.vaadin.shared.AbstractComponentState;
import com.vaadin.shared.ui.ComponentStateUtil;

class SuggestionPopup extends VOverlay implements PositionCallback,
	CloseHandler<PopupPanel> {
	
	private static final int Z_INDEX = 30000;

    private final Element up = DOM.createDiv();
    private final Element down = DOM.createDiv();
    private final Element status = DOM.createDiv();
    private final CellPanel panel;
    
    private boolean isPagingEnabled = true;
    private long lastAutoClosed;
    private int popupOuterPadding = -1;
    private int topPosition;
    private Widget owner;

    /**
     * Default constructor
     */
    SuggestionPopup(final Widget owner) {
        super(true, false, true);
        setOwner(owner);
        panel = createItemsPanel();
        setWidget(panel);
        
        this.owner = owner;

        getElement().getStyle().setZIndex(Z_INDEX);

        final Element root = getContainerElement();

        up.setInnerHTML("<span>Prev</span>");
        DOM.sinkEvents(up, Event.ONCLICK);

        down.setInnerHTML("<span>Next</span>");
        DOM.sinkEvents(down, Event.ONCLICK);

        root.insertFirst(up);
        root.appendChild(down);
        root.appendChild(status);

        DOM.sinkEvents(root, Event.ONMOUSEDOWN | Event.ONMOUSEWHEEL);
        addCloseHandler(this);

        Roles.getListRole().set(getElement());

        setPreviewingAllNativeEvents(true);
    }

    private CellPanel createItemsPanel() {
    	return new VerticalPanel();
    }

    /**
     * Shows the popup where the user can see the filtered options
     * 
     * @param currentSuggestions
     *            The filtered suggestions
     * @param currentPage
     *            The current page number
     * @param totalSuggestions
     *            The total amount of suggestions
     */
    public void showSuggestions(
            final List<Widget> currentSuggestions,
            final int currentPage, final int totalSuggestions) {

        /*
         * We need to defer the opening of the popup so that the parent DOM
         * has stabilized so we can calculate an absolute top and left
         * correctly. This issue manifests when a Combobox is placed in
         * another popupView which also needs to calculate the absoluteTop()
         * to position itself. #9768
         * 
         * After deferring the showSuggestions method, a problem with
         * navigating in the combo box occurs. Because of that the method
         * navigateItemAfterPageChange in ComboBoxConnector class, which
         * navigates to the exact item after page was changed also was
         * marked as deferred. #11333
         */
        final SuggestionPopup popup = this;
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                // Add TT anchor point
                getElement().setId("VAADIN_COMBOBOX_OPTIONLIST");

                panel.clear();
                for(Widget item : currentSuggestions) {
                	panel.add(item);
                }
                
                final int x = owner.getAbsoluteLeft();

                //topPosition = tb.getAbsoluteTop();
                //topPosition += tb.getOffsetHeight();
                topPosition = owner.getAbsoluteTop();
                topPosition += owner.getOffsetHeight();

                setPopupPosition(x, topPosition);
/*
                int nullOffset = (nullSelectionAllowed
                        && "".equals(lastFilter) ? 1 : 0);
                boolean firstPage = (currentPage == 0);
                final int first = currentPage * pageLength + 1
                        - (firstPage ? 0 : nullOffset);
                final int last = first
                        + currentSuggestions.size()
                        - 1
                        - (firstPage && "".equals(lastFilter) ? nullOffset
                                : 0);
                final int matches = totalSuggestions - nullOffset;
                if (last > 0) {
                    // nullsel not counted, as requested by user
                    status.setInnerText((matches == 0 ? 0 : first) + "-"
                            + last + "/" + matches);
                } else {
                    status.setInnerText("");
                }
                // We don't need to show arrows or statusbar if there is
                // only one page
                if (totalSuggestions <= pageLength || pageLength == 0) {
                    setPagingEnabled(false);
                } else {
                    setPagingEnabled(true);
                }
                
                setPrevButtonActive(first > 1);
                setNextButtonActive(last < matches);
*/
                
                up.setClassName(owner.getStylePrimaryName()
                        + "-prevpage-off");
                down.setClassName(owner.getStylePrimaryName()
                        + "-nextpage-off");
				
                panel.setStyleName(owner.getStylePrimaryName()
                        + "-suggestmenu");
                status.setClassName(owner.getStylePrimaryName()
                        + "-status");
                
                // clear previously fixed width
                panel.setWidth("");
                panel.getElement().getFirstChildElement().getStyle()
                        .clearWidth();

                setPopupPositionAndShow(popup);
                // Fix for #14173
                // IE9 and IE10 have a bug, when resize an a element with
                // box-shadow.
                // IE9 and IE10 need explicit update to remove extra
                // box-shadows
                if (BrowserInfo.get().isIE9() || BrowserInfo.get().isIE10()) {
                    //forceReflow();
                }
            }
        });
    }

    /**
     * Should the next page button be visible to the user?
     * 
     * @param active
     */
    private void setNextButtonActive(boolean active) {

        if (active) {
            DOM.sinkEvents(down, Event.ONCLICK);
            down.setClassName(owner.getStylePrimaryName()
                    + "-nextpage");
        } else {
            DOM.sinkEvents(down, 0);
            down.setClassName(owner.getStylePrimaryName()
                    + "-nextpage-off");
        }
    }

    /**
     * Should the previous page button be visible to the user
     * 
     * @param active
     */
    private void setPrevButtonActive(boolean active) {

        if (active) {
            DOM.sinkEvents(up, Event.ONCLICK);
            up.setClassName(owner.getStylePrimaryName()
                    + "-prevpage");
        } else {
            DOM.sinkEvents(up, 0);
            up.setClassName(owner.getStylePrimaryName()
                    + "-prevpage-off");
        }

    }

    /**
     * Selects the next item in the filtered selections
     */
    public void selectNextItem() {
    	/*
        final int index = menu.getSelectedIndex() + 1;
        if (menu.getItems().size() > index) {
            selectItem(menu.getItems().get(index));

        } else {
            selectNextPage();
        }
        */
    }

    /**
     * Selects the previous item in the filtered selections
     */
    public void selectPrevItem() {
    	/*
        final int index = menu.getSelectedIndex() - 1;
        if (index > -1) {
            selectItem(menu.getItems().get(index));

        } else if (index == -1) {
            selectPrevPage();

        } else {
            if (!menu.getItems().isEmpty()) {
                selectLastItem();
            }
        }
        */
    }

    /**
     * Select the first item of the suggestions list popup.
     * 
     * @since 7.2.6
     */
    public void selectFirstItem() {
        //selectItem(menu.getFirstItem());
    }

    /**
     * Select the last item of the suggestions list popup.
     * 
     * @since 7.2.6
     */
    public void selectLastItem() {
        //selectItem(menu.getLastItem());
    }

    /*
     * Sets the selected item in the popup menu.
     */
    private void selectItem(final MenuItem newSelectedItem) {
        /*
    	menu.selectItem(newSelectedItem);

        // Set the icon.
        FilterSelectSuggestion suggestion = (FilterSelectSuggestion) newSelectedItem
                .getCommand();
        setSelectedItemIcon(suggestion.getIconUri());

        // Set the text.
        setText(suggestion.getReplacementString());
		*/
    }

    /*
     * Using a timer to scroll up or down the pages so when we receive lots
     * of consecutive mouse wheel events the pages does not flicker.
     */
    private LazyPageScroller lazyPageScroller = new LazyPageScroller();

    private class LazyPageScroller extends Timer {
        private int pagesToScroll = 0;

        @Override
        public void run() {
            if (pagesToScroll != 0) {
                //if (!waitingForFilteringResponse) {
                    /*
                     * Avoid scrolling while we are waiting for a response
                     * because otherwise the waiting flag will be reset in
                     * the first response and the second response will be
                     * ignored, causing an empty popup...
                     * 
                     * As long as the scrolling delay is suitable
                     * double/triple clicks will work by scrolling two or
                     * three pages at a time and this should not be a
                     * problem.
                     */
                    //filterOptions(currentPage + pagesToScroll, lastFilter);
                //}
                pagesToScroll = 0;
            }
        }

        public void scrollUp() {
        	/*
            if (pageLength > 0 && currentPage + pagesToScroll > 0) {
                pagesToScroll--;
                cancel();
                schedule(200);
            }
            */
        }

        public void scrollDown() {
        	/*
            if (pageLength > 0
                    && totalMatches > (currentPage + pagesToScroll + 1)
                            * pageLength) {
                pagesToScroll++;
                cancel();
                schedule(200);
            }
            */
        }
    }

    private void scroll(double deltaY) {
    	/*
        boolean scrollActive = panel.isScrollActive();

        if (!scrollActive) {
            if (deltaY > 0d) {
                lazyPageScroller.scrollDown();
            } else {
                lazyPageScroller.scrollUp();
            }
        }
        */
    }

    @Override
    public void onBrowserEvent(Event event) {

        if (event.getTypeInt() == Event.ONCLICK) {
            final Element target = DOM.eventGetTarget(event);
            if (target == up || target == DOM.getChild(up, 0)) {
                lazyPageScroller.scrollUp();
            } else if (target == down || target == DOM.getChild(down, 0)) {
                lazyPageScroller.scrollDown();
            }

        }

        /*
         * Prevent the keyboard focus from leaving the textfield by
         * preventing the default behaviour of the browser. Fixes #4285.
         */
        //handleMouseDownEvent(event);
    }

    /**
     * Should paging be enabled. If paging is enabled then only a certain
     * amount of items are visible at a time and a scrollbar or buttons are
     * visible to change page. If paging is turned of then all options are
     * rendered into the popup menu.
     * 
     * @param paging
     *            Should the paging be turned on?
     */
    public void setPagingEnabled(boolean paging) {
        if (isPagingEnabled == paging) {
            return;
        }
        if (paging) {
            down.getStyle().clearDisplay();
            up.getStyle().clearDisplay();
            status.getStyle().clearDisplay();
        } else {
            down.getStyle().setDisplay(Display.NONE);
            up.getStyle().setDisplay(Display.NONE);
            status.getStyle().setDisplay(Display.NONE);
        }
        isPagingEnabled = paging;
    }

    @Override
    public void setPosition(int offsetWidth, int offsetHeight) {

        int top = topPosition;
        int left = getPopupLeft();

        // reset menu size and retrieve its "natural" size
        panel.setHeight("");
        /*
        if (currentPage > 0 && !hasNextPage()) {
            // fix height to avoid height change when getting to last page
            panel.fixHeightTo(pageLength);
        }
        */

        final int desiredHeight = offsetHeight = getOffsetHeight();
        final int desiredWidth = owner.getOffsetWidth();

        Element menuFirstChild = panel.getElement().getFirstChildElement();
        final int naturalMenuWidth = WidgetUtil
                .getRequiredWidth(menuFirstChild);

        if (popupOuterPadding == -1) {
            popupOuterPadding = WidgetUtil
                    .measureHorizontalPaddingAndBorder(getElement(), 2);
        }

        if (naturalMenuWidth < desiredWidth) {
            panel.setWidth((desiredWidth - popupOuterPadding) + "px");
            menuFirstChild.getStyle().setWidth(100, Unit.PCT);
        }

        if (BrowserInfo.get().isIE()
                && BrowserInfo.get().getBrowserMajorVersion() < 11) {
            // Must take margin,border,padding manually into account for
            // menu element as we measure the element child and set width to
            // the element parent
            double naturalMenuOuterWidth = WidgetUtil
                    .getRequiredWidthDouble(menuFirstChild);
                   // + getMarginBorderPaddingWidth(panel.getElement());

            /*
             * IE requires us to specify the width for the container
             * element. Otherwise it will be 100% wide
             */
            double rootWidth = Math.max(desiredWidth - popupOuterPadding,
                    naturalMenuOuterWidth);
            getContainerElement().getStyle().setWidth(rootWidth, Unit.PX);
        }

        final int vfsHeight = owner.getOffsetHeight();
        final int spaceAvailableAbove = top - vfsHeight;
        final int spaceAvailableBelow = Window.getClientHeight() - top;
        if (spaceAvailableBelow < offsetHeight
                && spaceAvailableBelow < spaceAvailableAbove) {
            // popup on top of input instead
            top -= offsetHeight + vfsHeight;
            if (top < 0) {
                offsetHeight += top;
                top = 0;
            }
        } else {
            offsetHeight = Math.min(offsetHeight, spaceAvailableBelow);
        }

        // fetch real width (mac FF bugs here due GWT popups overflow:auto )
        offsetWidth = menuFirstChild.getOffsetWidth();

        if (offsetHeight < desiredHeight) {
            int menuHeight = offsetHeight;
            if (isPagingEnabled) {
                menuHeight -= up.getOffsetHeight() + down.getOffsetHeight()
                        + status.getOffsetHeight();
            } else {
                final ComputedStyle s = new ComputedStyle(panel.getElement());
                menuHeight -= s.getIntProperty("marginBottom")
                        + s.getIntProperty("marginTop");
            }

            // If the available page height is really tiny then this will be
            // negative and an exception will be thrown on setHeight.
            /*
            int menuElementHeight = panel.getItemOffsetHeight();
            if (menuHeight < menuElementHeight) {
                menuHeight = menuElementHeight;
            }
            */

            panel.setHeight(menuHeight + "px");

            final int naturalMenuWidthPlusScrollBar = naturalMenuWidth
                    + WidgetUtil.getNativeScrollbarSize();
            if (offsetWidth < naturalMenuWidthPlusScrollBar) {
                panel.setWidth(naturalMenuWidthPlusScrollBar + "px");
            }
        }

        if (offsetWidth + left > Window.getClientWidth()) {
            left = owner.getAbsoluteLeft()
                    + owner.getOffsetWidth() - offsetWidth;
            if (left < 0) {
                left = 0;
                panel.setWidth(Window.getClientWidth() + "px");
            }
        }

        setPopupPosition(left, top);
        //menu.scrollSelectionIntoView();
    }

    /**
     * Was the popup just closed?
     * 
     * @return true if popup was just closed
     */
    public boolean isJustClosed() {
        final long now = (new Date()).getTime();
        return (lastAutoClosed > 0 && (now - lastAutoClosed) < 200);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.google.gwt.event.logical.shared.CloseHandler#onClose(com.google
     * .gwt.event.logical.shared.CloseEvent)
     */

    @Override
    public void onClose(CloseEvent<PopupPanel> event) {
        if (event.isAutoClosed()) {
            lastAutoClosed = (new Date()).getTime();
        }
    }

    /**
     * Updates style names in suggestion popup to help theme building.
     * 
     * @param uidl
     *            UIDL for the whole combo box
     * @param componentState
     *            shared state of the combo box
     */
    public void updateStyleNames(UIDL uidl,
            AbstractComponentState componentState) {
        setStyleName(owner.getStylePrimaryName()
                + "-suggestpopup");
        panel.setStyleName(owner.getStylePrimaryName()
                + "-suggestmenu");
        status.setClassName(owner.getStylePrimaryName()
                + "-status");
        if (ComponentStateUtil.hasStyles(componentState)) {
            for (String style : componentState.styles) {
                if (!"".equals(style)) {
                    addStyleDependentName(style);
                }
            }
        }
    }
}
