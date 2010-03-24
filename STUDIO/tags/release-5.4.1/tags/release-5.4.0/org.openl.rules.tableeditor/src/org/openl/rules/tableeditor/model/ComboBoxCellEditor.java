package org.openl.rules.tableeditor.model;

import org.openl.rules.tableeditor.event.TableEditorController.EditorTypeResponse;

public class ComboBoxCellEditor implements ICellEditor {

    public static class ComboBoxParam {
        private String[] choices;
        private String[] displayValues;

        public ComboBoxParam(String[] choices, String[] displayValues) {
            this.choices = choices;
            this.displayValues = displayValues;
        }

        public String[] getChoices() {
            return choices;
        }

        public String[] getDisplayValues() {
            return displayValues;
        }

        public void setChoices(String[] choices) {
            this.choices = choices;
        }

        public void setDisplayValues(String[] displayValues) {
            this.displayValues = displayValues;
        }
    }

    protected String[] choices, displayValues;
    
    protected ComboBoxCellEditor(String[] displayValues) {
        this.displayValues = displayValues;
    }
    
    protected void setChoices(String[] choices) {
        this.choices = choices;
    }
    
    public ComboBoxCellEditor(String[] choices, String[] displayValues) {
        this.choices = choices;
        this.displayValues = displayValues;
    }

    public EditorTypeResponse getEditorTypeAndMetadata() {
        EditorTypeResponse typeResponse = new EditorTypeResponse(CE_COMBO);
        typeResponse.setParams(new ComboBoxParam(choices, displayValues));
        return typeResponse;
    }

    public ICellEditorServerPart getServerPart() {
        return null;
    }

}
