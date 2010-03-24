package org.openl.rules.ui.tablewizard;

import java.util.ArrayList;

/**
 * @author Aliaksandr Antonik.
 */
public class ListWithSelection<T> extends ArrayList<T> {
    private int selectedIndex;

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    public boolean isSelectionValid() {
        return selectedIndex >= 0 && selectedIndex < size();
    }

    public void selectLast() {
        selectedIndex = size() - 1;
    }

    public T getSelectedElement() {
        return isSelectionValid() ? get(selectedIndex) : null;
    }

    @Override
    public T remove(int index) {
        if (index < selectedIndex || (index == selectedIndex && selectedIndex == size() - 1)) {
            --selectedIndex;
        }
        return super.remove(index);
    }
}
