package org.openl.rules.table.xls;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openl.rules.table.ui.ICellFont;

public class XlsCellFont implements ICellFont {
	
	private Font font;
	private Workbook workbook;

    public XlsCellFont(Font font, Workbook workbook) {
        this.font = font;
        this.workbook = workbook;
    }

    public short[] getFontColor() {
		if (font instanceof XSSFFont) {
		    XSSFColor color = ((XSSFFont)font).getXSSFColor();
			return XlsCellStyle2.colorToArray(color, (XSSFWorkbook)workbook);
		} else {
			short x = font.getColor();
			return XlsCellStyle.colorToArray(x, (HSSFWorkbook)workbook);
		}
    }

    public String getName() {
        return font.getFontName();
    }

    public int getSize() {
        return font.getFontHeightInPoints();
    }

    public boolean isBold() {
        return font.getBoldweight() == Font.BOLDWEIGHT_BOLD;
    }

    public boolean isItalic() {
        return font.getItalic();
    }

    public boolean isStrikeout() {
        return font.getStrikeout();
    }

    public boolean isUnderlined() {
        return font.getUnderline() != Font.U_NONE;
    }

}