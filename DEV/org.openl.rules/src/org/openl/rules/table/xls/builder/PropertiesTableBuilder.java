package org.openl.rules.table.xls.builder;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import org.openl.rules.lang.xls.IXlsTableNames;
import org.openl.rules.table.ui.ICellStyle;
import org.openl.rules.table.xls.XlsSheetGridModel;

/**
 * The class is responsible for creating Properties tables in excel sheets.
 *
 * @author Andrei Astrouski
 */
public class PropertiesTableBuilder extends TableBuilder {

    public static final int MIN_WIDTH = 2; // Property name + Property value

    /**
     * Creates new instance.
     *
     * @param gridModel represents interface for operations with excel sheets
     */
    public PropertiesTableBuilder(XlsSheetGridModel gridModel) {
        super(gridModel);
    }

    public void beginTable(int height) throws CreateTableException {
        super.beginTable(MIN_WIDTH, height);
    }

    public void writeBody(Map<String, Object> properties, ICellStyle style) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must be not null");
        }
        if (getTableRegion() == null) {
            throw new IllegalStateException("beginTable() has to be called");
        }
        Set<String> keys = properties.keySet();
        for (Iterator<String> iterator = keys.iterator(); iterator.hasNext();) {
            String key = iterator.next();
            writeCell(0, getCurrentRow(), 1, 1, key, style);
            Object value = properties.get(key);
            writeCell(1, getCurrentRow(), 1, 1, value, style);
            incCurrentRow();
        }
    }

    public void writeBody(Map<String, Object> properties) {
        writeBody(properties, null);
    }

    @Override
    public void writeHeader(String tableName, ICellStyle style) {
        String header = IXlsTableNames.PROPERTY_TABLE;
        if (StringUtils.isNotBlank(tableName)) {
            header += (" " + tableName);
        }
        super.writeHeader(header, style);
    }

    public void writeHeader(String tableName) {
        writeHeader(tableName, null);
    }

}
