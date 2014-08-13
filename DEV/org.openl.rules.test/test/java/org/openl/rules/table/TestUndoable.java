/**
 * Created Feb 15, 2007
 */
package org.openl.rules.table;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.poi.ss.usermodel.Workbook;
import org.openl.rules.lang.xls.XlsSheetSourceCodeModule;
import org.openl.rules.lang.xls.XlsWorkbookSourceCodeModule;
import org.openl.rules.table.actions.IUndoableGridTableAction;
import org.openl.rules.table.actions.UndoableSetValueAction;
import org.openl.rules.table.xls.XlsSheetGridModel;
import org.openl.source.impl.FileSourceCodeModule;

/**
 * @author snshor
 * 
 */
public class TestUndoable extends TestCase {

    public void testColumns() throws Exception {
        
        URL url = this.getClass().getClassLoader().getResource("org/openl/rules/table/TestUndo.xls");

        FileSourceCodeModule source = new FileSourceCodeModule(url.toURI().getPath(), null);
        XlsWorkbookSourceCodeModule wbSrc = new XlsWorkbookSourceCodeModule(source);
        Workbook wb = wbSrc.getWorkbook();

        XlsSheetSourceCodeModule sheetSrc = new XlsSheetSourceCodeModule(0, wbSrc);

        XlsSheetGridModel xsGrid = new XlsSheetGridModel(sheetSrc);

        IGridTable[] tables = xsGrid.getTables();

        IUndoableGridTableAction[] uaa0 = new IUndoableGridTableAction[tables.length];
        for (int j = 0; j < tables.length; j++) {
            uaa0[j] = GridTool
                    .insertColumns(1, 2, tables[j].getRegion(), tables[j].getGrid());
            uaa0[j].doAction(tables[j]);
            tables[j] = new GridTable(tables[j].getRegion().getTop(), tables[j].getRegion().getLeft(), tables[j]
                    .getRegion().getBottom(), tables[j].getRegion().getRight() + 1, tables[j].getGrid());
        }

        saveWb(wb, "wb1.xls");

        IUndoableGridTableAction[] uaa1 = new IUndoableGridTableAction[tables.length];
        for (int j = 0; j < tables.length; j++) {
            uaa1[j] = GridTool.removeColumns(1, 3, tables[j].getRegion(), tables[j].getGrid());
            uaa1[j].doAction(tables[j]);
            tables[j] = new GridTable(tables[j].getRegion().getTop(), tables[j].getRegion().getLeft(), tables[j]
                    .getRegion().getBottom(), tables[j].getRegion().getRight() + 1, tables[j].getGrid());
        }
        saveWb(wb, "wb2.xls");
    }

    public void testRemove() throws Exception {
        
        URL url = this.getClass().getClassLoader().getResource("org/openl/rules/table/TestUndo.xls");

        FileSourceCodeModule source = new FileSourceCodeModule(url.toURI().getPath(), null);

        XlsWorkbookSourceCodeModule wbSrc = new XlsWorkbookSourceCodeModule(source);

        Workbook wb = wbSrc.getWorkbook();

        XlsSheetSourceCodeModule sheetSrc = new XlsSheetSourceCodeModule(0, wbSrc);

        XlsSheetGridModel xsGrid = new XlsSheetGridModel(sheetSrc);

        IGridTable[] tables = xsGrid.getTables();

        IUndoableGridTableAction[] uaa1 = new IUndoableGridTableAction[tables.length];
        for (int j = 0; j < tables.length; j++) {
            uaa1[j] = GridTool.removeRows(1, 0, tables[j].getRegion(), tables[j].getGrid());
            uaa1[j].doAction(tables[j]);
            tables[j] = new GridTable(tables[j].getRegion().getTop(), tables[j].getRegion().getLeft(), tables[j]
                    .getRegion().getBottom(), tables[j].getRegion().getRight() + 1, tables[j].getGrid());
        }
        saveWb(wb, "wb1.xls");
    }

    public void testInsert() throws Exception {
        
        URL url = this.getClass().getClassLoader().getResource("org/openl/rules/table/TestCopy.xls");

        FileSourceCodeModule source = new FileSourceCodeModule(url.toURI().getPath(), null);
        XlsWorkbookSourceCodeModule wbSrc = new XlsWorkbookSourceCodeModule(source);
        Workbook wb = wbSrc.getWorkbook();

        XlsSheetSourceCodeModule sheetSrc = new XlsSheetSourceCodeModule(0, wbSrc);

        XlsSheetGridModel xsGrid = new XlsSheetGridModel(sheetSrc);

        IGridTable[] tables = xsGrid.getTables();
        IUndoableGridTableAction[] uaa1 = new IUndoableGridTableAction[tables.length];
        
        for (int j = 0; j < tables.length; j++) {

            uaa1[j] = GridTool
                    .insertColumns(1, 1, tables[j].getRegion(), tables[j].getGrid());
            uaa1[j].doAction(tables[j]);
            tables[j] = new GridTable(tables[j].getRegion().getTop(), tables[j].getRegion().getLeft(), tables[j]
                    .getRegion().getBottom(), tables[j].getRegion().getRight() + 1, tables[j].getGrid());
        }
        
        saveWb(wb, "wb1.xls");

        IUndoableGridTableAction[] uaa2 = new IUndoableGridTableAction[tables.length];

        for (int j = 0; j < tables.length; j++) {
            uaa2[j] = GridTool.insertRows(3, 1, tables[j].getRegion(), tables[j].getGrid());
            uaa2[j].doAction(tables[j]);
        }

        saveWb(wb, "wb11.xls");

        IUndoableGridTableAction[] uaa3 = new IUndoableGridTableAction[tables.length];

        for (int j = 0; j < tables.length; j++) {
            int gcol = tables[j].getGridColumn(1, 1);
            int grow = tables[j].getGridRow(1, 1);
            uaa3[j] = new UndoableSetValueAction(gcol, grow, "12345");
            uaa3[j].doAction(tables[j]);
        }

        saveWb(wb, "wb12.xls");

        for (int j = 0; j < uaa3.length; j++) {
            uaa3[j].undoAction(tables[j]);
        }

        saveWb(wb, "wb21.xls");

        for (int j = 0; j < uaa2.length; j++) {
            uaa2[j].undoAction(tables[j]);
        }

        saveWb(wb, "wb22.xls");

        for (int j = 0; j < uaa1.length; j++) {
            uaa1[j].undoAction(tables[j]);
        }

        saveWb(wb, "wb23.xls");
    }

    private void saveWb(Workbook wb, String name) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(name);
        wb.write(fileOut);
        fileOut.close();
    }
}
