package org.openl.rules.calc.ce;

import org.openl.rules.calc.Spreadsheet;
import org.openl.rules.calc.SpreadsheetBoundNode;
import org.openl.types.IOpenMethodHeader;
import org.openl.types.Invokable;

public class SpreadsheetCE extends Spreadsheet {

	public SpreadsheetCE(IOpenMethodHeader header,
			SpreadsheetBoundNode boundNode) {
		super(header, boundNode);
	}

	public SpreadsheetCE(IOpenMethodHeader header,
			SpreadsheetBoundNode boundNode, boolean customSpreadsheetType) {
		super(header, boundNode, customSpreadsheetType);
	}

	@Override
    protected Invokable createInvoker()
    {
    	return new SpreadsheetinvokerCE(this);
    }

	
	
}
