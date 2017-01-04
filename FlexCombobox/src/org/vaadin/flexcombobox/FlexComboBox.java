package org.vaadin.flexcombobox;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.vaadin.flexcombobox.client.flexcombobox.FlexComboboxClientRpc;
import org.vaadin.flexcombobox.client.flexcombobox.FlexComboboxServerRpc;

import com.vaadin.event.FieldEvents;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.ComboBox.ItemStyleGenerator;
import com.vaadin.ui.HasComponents.ComponentAttachEvent;
import com.vaadin.ui.AbstractSingleComponentContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;

import org.vaadin.flexcombobox.client.flexcombobox.FlexComboboxState;

public class FlexComboBox<T extends FlexComboBox.FlexItem> extends com.vaadin.ui.AbstractComponent
	implements HasComponents, FieldEvents.BlurNotifier, FieldEvents.FocusNotifier {

	public interface FlexItem {
		public String getCaption();
		public Component getComponent();
	}
	
	private List<Component> components = new ArrayList<>();
	
	private String inputPrompt = null;

    /**
     * Holds value of property pageLength. 0 disables paging.
     */
    protected int pageLength = 10;

    // Current page when the user is 'paging' trough options
    private int currentPage = -1;

    private FilteringMode filteringMode = FilteringMode.STARTSWITH;

    private String filterstring;
    private String prevfilterstring;

    /**
     * Number of options that pass the filter, excluding the null item if any.
     */
    private int filteredSize;

    /**
     * Cache of filtered options, used only by the in-memory filtering system.
     */
    private List<Object> filteredOptions;

    /**
     * Flag to indicate that request repaint is called by filter request only
     */
    private boolean optionRequest;

    /**
     * True while painting to suppress item set change notifications that could
     * be caused by temporary filtering.
     */
    private boolean isPainting;

    /**
     * Flag to indicate whether to scroll the selected item visible (select the
     * page on which it is) when opening the popup or not. Only applies to
     * single select mode.
     * 
     * This requires finding the index of the item, which can be expensive in
     * many large lazy loading containers.
     */
    private boolean scrollToSelectedItem = true;

    /**
     * If text input is not allowed, the ComboBox behaves like a pretty
     * NativeSelect - the user can not enter any text and clicking the text
     * field opens the drop down with options
     */
    private boolean textInputAllowed = true;
	
    /**
     * Initialize the ComboBox with default settings
     */
    private void initDefaults() {
        //setNewItemsAllowed(false);
        setImmediate(true);
    }
    
    /**
     * Gets the current input prompt.
     * 
     * @see #setInputPrompt(String)
     * @return the current input prompt, or null if not enabled
     */
    public String getInputPrompt() {
        return inputPrompt;
    }

    /**
     * Sets the input prompt - a textual prompt that is displayed when the
     * select would otherwise be empty, to prompt the user for input.
     * 
     * @param inputPrompt
     *            the desired input prompt, or null to disable
     */
    public void setInputPrompt(String inputPrompt) {
        this.inputPrompt = inputPrompt;
        markAsDirty();
    }

    private boolean isFilteringNeeded() {
        return filterstring != null && filterstring.length() > 0
                && filteringMode != FilteringMode.OFF;
    }
    
    /**
     * Sets whether it is possible to input text into the field or whether the
     * field area of the component is just used to show what is selected. By
     * disabling text input, the comboBox will work in the same way as a
     * {@link NativeSelect}
     * 
     * @see #isTextInputAllowed()
     * 
     * @param textInputAllowed
     *            true to allow entering text, false to just show the current
     *            selection
     */
    public void setTextInputAllowed(boolean textInputAllowed) {
        this.textInputAllowed = textInputAllowed;
        markAsDirty();
    }

    /**
     * Returns true if the user can enter text into the field to either filter
     * the selections or enter a new value if {@link #isNewItemsAllowed()}
     * returns true. If text input is disabled, the comboBox will work in the
     * same way as a {@link NativeSelect}
     * 
     * @return
     */
    public boolean isTextInputAllowed() {
        return textInputAllowed;
    }
    
	/*
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

	public FlexComboBox() {
		registerRpc(rpc);
	}
	*/

	@Override
	public FlexComboboxState getState() {
		return (FlexComboboxState) super.getState();
	}

	@Override
	public void addFocusListener(FocusListener listener) {
		addListener(FocusEvent.EVENT_ID, FocusEvent.class, listener,
                FocusListener.focusMethod);
	}

	@Override
	public void addListener(FocusListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeFocusListener(FocusListener listener) {
		removeListener(FocusEvent.EVENT_ID, FocusEvent.class, listener);
	}

	@Override
	public void removeListener(FocusListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addBlurListener(BlurListener listener) {
		addListener(BlurEvent.EVENT_ID, BlurEvent.class, listener,
                BlurListener.blurMethod);
	}

	@Override
	public void addListener(BlurListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeBlurListener(BlurListener listener) {
		removeListener(BlurEvent.EVENT_ID, BlurEvent.class, listener);
	}

	@Override
	public void removeListener(BlurListener listener) {
		// TODO Auto-generated method stub
		
	}
	
	/**
     * Returns the page length of the suggestion popup.
     * 
     * @return the pageLength
     */
    public int getPageLength() {
        return pageLength;
    }

    /**
     * Sets the page length for the suggestion popup. Setting the page length to
     * 0 will disable suggestion popup paging (all items visible).
     * 
     * @param pageLength
     *            the pageLength to set
     */
    public void setPageLength(int pageLength) {
        this.pageLength = pageLength;
        markAsDirty();
    }

    /**
     * Sets whether to scroll the selected item visible (directly open the page
     * on which it is) when opening the combo box popup or not. Only applies to
     * single select mode.
     * 
     * This requires finding the index of the item, which can be expensive in
     * many large lazy loading containers.
     * 
     * @param scrollToSelectedItem
     *            true to find the page with the selected item when opening the
     *            selection popup
     */
    public void setScrollToSelectedItem(boolean scrollToSelectedItem) {
        this.scrollToSelectedItem = scrollToSelectedItem;
    }

    /**
     * Returns true if the select should find the page with the selected item
     * when opening the popup (single select combo box only).
     * 
     * @see #setScrollToSelectedItem(boolean)
     * 
     * @return true if the page with the selected item will be shown when
     *         opening the popup
     */
    public boolean isScrollToSelectedItem() {
        return scrollToSelectedItem;
    }

    @Override
	public Iterator<Component> iterator() {
		return components.iterator();
	}
	
    public void addItem(T item) {
		Component c = item.getComponent();
		
		if(c == null) {
			throw new IllegalArgumentException("Component is not set");
		}
		
		// Make sure we're not adding the component inside it's own content
        if (isOrHasAncestor(c)) {
            throw new IllegalArgumentException(
                    "Component cannot be added inside it's own content");
        }
        
		if (c.getParent() != null) {
            // If the component already has a parent, try to remove it
            AbstractSingleComponentContainer.removeFromParent(c);
        }
		
		components.add(c);
        c.setParent(this);
        fireEvent(new ComponentAttachEvent(this, c));
        markAsDirty();
        
        getState().addItem(c, item.getCaption());
	}
	
	/**
     * Determine whether a <code>content</code> component is equal to, or the
     * ancestor of this component.
     * 
     * @param content
     *            the potential ancestor element
     * @return <code>true</code> if the relationship holds
     */
    protected boolean isOrHasAncestor(Component content) {
        if (content instanceof HasComponents) {
            for (Component parent = this; parent != null; parent = parent
                    .getParent()) {
                if (parent.equals(content)) {
                    return true;
                }
            }
        }
        return false;
    }
}
