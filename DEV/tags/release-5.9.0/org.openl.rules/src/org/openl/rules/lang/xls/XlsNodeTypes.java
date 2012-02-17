package org.openl.rules.lang.xls;

// TODO: implement common for all node types interface, e.g. INodeTypes. Place
// it to the core and rewrite ISyntaxNode#getType to returning INodeTypes.
//
public enum XlsNodeTypes {
    
    WORKBOOK("Workbook"),
    WORKSHEET("Worksheet"),
    TABLE("Table"),
    CELL("Cell"),

    XLS_MODULE("xls.module"),    
    XLS_WORKBOOK("xls.workbook"),
    XLS_WORKSHEET("xls.worksheet"),
    
    // executable tables
    XLS_DT("xls.dt"),
    XLS_SPREADSHEET("xls.spreadsheet"),
    XLS_TBASIC("xls.tbasic"),
    XLS_COLUMN_MATCH("xls.columnmatch"),
    XLS_METHOD("xls.method"),    
    XLS_TEST_METHOD("xls.test.method"),
    XLS_RUN_METHOD("xls.run.method"),
        
    XLS_DATA("xls.data"),
    XLS_DATATYPE("xls.datatype"),
    XLS_OPENL("xls.openl"),
    XLS_ENVIRONMENT("xls.environment"),
    XLS_PERSISTENT("xls.persistent"),
    XLS_OTHER("xls.other"),
    XLS_PROPERTIES("xls.properties");
    
    private final String name;
    
    private XlsNodeTypes(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    // Temporary method.
    // Should be removed when TableSyntaxNode will be switched from String node type
    // to XlsNodeTypes
    //
    public static XlsNodeTypes getEnumConstant(String name) {
        for (XlsNodeTypes constant : XlsNodeTypes.values()) {
            if (constant.toString().equals(name)) {
                return constant;
            }
        }
        return null;
    }
}