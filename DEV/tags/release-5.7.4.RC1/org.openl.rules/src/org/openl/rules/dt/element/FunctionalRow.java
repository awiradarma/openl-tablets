/*
 * Created on Sep 11, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.rules.dt.element;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.openl.OpenL;
import org.openl.binding.IBindingContext;
import org.openl.binding.IBindingContextDelegator;
import org.openl.binding.impl.component.ComponentOpenClass;
import org.openl.engine.OpenLManager;
import org.openl.exception.OpenLCompilationException;
import org.openl.rules.OpenlToolAdaptor;
import org.openl.rules.binding.RuleRowHelper;
import org.openl.rules.dt.IDecisionTableConstants;
import org.openl.rules.table.IGridTable;
import org.openl.rules.table.ILogicalTable;
import org.openl.rules.table.LogicalTableHelper;
import org.openl.rules.table.openl.GridCellSourceCodeModule;
import org.openl.source.IOpenSourceCodeModule;
import org.openl.syntax.exception.CompositeSyntaxNodeException;
import org.openl.syntax.exception.SyntaxNodeException;
import org.openl.syntax.exception.SyntaxNodeExceptionUtils;
import org.openl.syntax.impl.IdentifierNode;
import org.openl.syntax.impl.Tokenizer;
import org.openl.types.IMethodSignature;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenMethod;
import org.openl.types.IOpenMethodHeader;
import org.openl.types.IParameterDeclaration;
import org.openl.types.NullOpenClass;
import org.openl.types.impl.CompositeMethod;
import org.openl.types.impl.MethodSignature;
import org.openl.types.impl.OpenMethodHeader;
import org.openl.types.impl.ParameterDeclaration;
import org.openl.util.ArrayTool;
import org.openl.vm.IRuntimeEnv;

/**
 * @author snshor
 * 
 */
public abstract class FunctionalRow implements IDecisionRow {

    private static final String NO_PARAM = "P";

    private String name;
    private int row;

    private CompositeMethod method;

    private IParameterDeclaration[] params;
    private Object[][] paramValues;

    private ILogicalTable decisionTable;
    private ILogicalTable paramsTable;
    private ILogicalTable codeTable;
    private ILogicalTable presentationTable;

    private int noParamsIndex = 0;

    public FunctionalRow(String name, int row, ILogicalTable decisionTable) {

        this.name = name;
        this.row = row;
        this.decisionTable = decisionTable;

        this.paramsTable = decisionTable.getSubtable(IDecisionTableConstants.PARAM_COLUMN_INDEX, row, 1, 1);
        this.codeTable = decisionTable.getSubtable(IDecisionTableConstants.CODE_COLUMN_INDEX, row, 1, 1);
        this.presentationTable = decisionTable.getSubtable(
                IDecisionTableConstants.PRESENTATION_COLUMN_INDEX, row, 1, 1);
    }

    public String getName() {
        return name;
    }

    public IOpenMethod getMethod() {
        return method;
    }

    public IParameterDeclaration[] getParams() {
        return params;
    }
    
    protected void setParams(IParameterDeclaration[] params) {
        this.params = params;
    }

    public Object[][] getParamValues() {
        return paramValues;
    }
    
    public void clearParamValues(){
        paramValues = null;
    }

    public DecisionTableParameterInfo getParameterInfo(int i) {
        return new DecisionTableParameterInfo(i, this);
    }

    public IOpenSourceCodeModule getSourceCodeModule() {

        if (method != null) {
            return method.getMethodBodyBoundNode().getSyntaxNode().getModule();
        }

        return null;
    }

    public int numberOfParams() {
        return params.length;
    }

    /**
     * Whole representation of decision table. Horizontal representation of the
     * table where conditions are listed from top to bottom. And must be readed
     * from left to right</br> Example:
     * 
     * <table cellspacing="2">
     * <tr>
     * <td align="center" bgcolor="#ccffff"><b>Rule</b></td>
     * <td align="center" bgcolor="#ccffff"></td>
     * <td align="center" bgcolor="#ccffff"></td>
     * <td align="center" bgcolor="#8FCB52">Rule</td>
     * <td align="center" bgcolor="#8FCB52">Rule1</td>
     * <td align="center" bgcolor="#8FCB52">Rule2</td>
     * <td align="center" bgcolor="#8FCB52">Rule3</td>
     * 
     * </tr>
     * <tr>
     * <td align="center" bgcolor="#ccffff"><b>C1</b></td>
     * <td align="center" bgcolor="#ccffff">paramLocal1==paramInc</td>
     * <td align="center" bgcolor="#ccffff">String paramLocal1</td>
     * <td align="center" bgcolor="#ffff99">Local Param 1</td>
     * <td align="center" bgcolor="#ffff99">value11</td>
     * <td align="center" bgcolor="#ffff99">value12</td>
     * <td align="center" bgcolor="#ffff99">value13</td>
     * </tr>
     * 
     * <tr>
     * <td align="center" bgcolor="#ccffff"><b>C2</b></td>
     * <td align="center" bgcolor="#ccffff">paramLocal2==paramInc</td>
     * <td align="center" bgcolor="#ccffff">String paramLocal2</td>
     * <td align="center" bgcolor="#ffff99">Local Param 2</td>
     * <td align="center" bgcolor="#ffff99">value21</td>
     * <td align="center" bgcolor="#ffff99">value22</td>
     * <td align="center" bgcolor="#ffff99">value23</td>
     * </tr>
     * </table>
     * 
     * @param dataTableBody
     * @param tableType
     * @return <code>TRUE</code> if table is horizontal.
     */
    public ILogicalTable getDecisionTable() {
        return decisionTable;
    }

    public String[] getParamPresentation() {

        int length = paramsTable.getHeight();

        String[] result = new String[length];
        int fromHeight = 0;

        for (int i = 0; i < result.length; i++) {

            int gridHeight = paramsTable.getRow(i).getSource().getHeight();

            IGridTable singleParamGridTable = presentationTable.getSource().getRows(fromHeight,
                fromHeight + gridHeight - 1);
            result[i] = singleParamGridTable.getCell(0, 0).getStringValue();

            fromHeight += gridHeight;
        }

        return result;
    }

    public void prepare(IOpenClass methodType,
            IMethodSignature signature,
            OpenL openl,
            ComponentOpenClass componentOpenClass,
            IBindingContextDelegator bindingContextDelegator,
            RuleRow ruleRow) throws Exception {

        method = generateMethod(signature, openl, bindingContextDelegator, componentOpenClass, methodType);

        OpenlToolAdaptor openlAdaptor = new OpenlToolAdaptor(openl, bindingContextDelegator);

        IOpenMethodHeader header = new OpenMethodHeader(name, null, signature, componentOpenClass);
        openlAdaptor.setHeader(header);

        paramValues = prepareParamValues(method, openlAdaptor, ruleRow);
        
        if (bindingContextDelegator.isExecutionMode()) {
            decisionTable = null;
            paramsTable = null;
            codeTable = null;
            presentationTable = null;
        }
        
    }

    protected IParameterDeclaration[] getParams(IOpenSourceCodeModule methodSource,
            IMethodSignature signature,
            IOpenClass declaringClass,
            IOpenClass methodType,
            OpenL openl,
            IBindingContext bindingContext) throws Exception {

        if (params == null) {

            Set<String> paramNames = new HashSet<String>();
            int length = paramsTable.getHeight();

            params = new IParameterDeclaration[length];

            for (int i = 0; i < length; i++) {
                ILogicalTable paramTable = paramsTable.getRow(i);
                IOpenSourceCodeModule source = new GridCellSourceCodeModule(paramTable.getSource(), bindingContext);

                IParameterDeclaration parameterDeclaration = getParameterDeclaration(source,
                    methodSource,
                    signature,
                    declaringClass,
                    methodType,
                    openl,
                    bindingContext);
                    
                String paramName = parameterDeclaration.getName();
                
                if (paramNames.contains(paramName)) {
                    throw SyntaxNodeExceptionUtils.createError("Duplicated parameter name: " + paramName, source);
                }

                paramNames.add(paramName);

                params[i] = parameterDeclaration;
            }
        }

        return params;
    }

    private Object[][] prepareParamValues(CompositeMethod method, OpenlToolAdaptor ota, RuleRow ruleRow)
        throws Exception {

        int len = nValues();

        IParameterDeclaration[] paramDecl = getParams(method.getMethodBodyBoundNode().getSyntaxNode().getModule(),
            method.getSignature(),
            method.getDeclaringClass(),
            method.getType(),
            ota.getOpenl(),
            ota.getBindingContext());

        boolean[] paramIndexed = getParamIndexed(paramDecl);

        ArrayList<SyntaxNodeException> errors = new ArrayList<SyntaxNodeException>();
        Object[][] preparedValues = new Object[len][];

        for (int columnNumber = 0; columnNumber < len; columnNumber++) {
            Object[] loadedColumnParams = loadParamsFromColumn(ota, ruleRow, paramDecl, paramIndexed, errors, columnNumber);

            if (!ArrayTool.isEmpty(loadedColumnParams)) {
                preparedValues[columnNumber] = loadedColumnParams;
            }
        }

        if (errors.size() > 0) {
            throw new CompositeSyntaxNodeException("Error:", errors.toArray(new SyntaxNodeException[0]));
        }

        return preparedValues;
    }

    private Object[] loadParamsFromColumn(OpenlToolAdaptor ota, RuleRow ruleRow, IParameterDeclaration[] paramDecl,
            boolean[] paramIndexed, ArrayList<SyntaxNodeException> errors, int columnNumber) {        
        IGridTable paramGridColumn = getValueCell(columnNumber).getSource();        

        int fromHeight = 0;        
        String ruleName = ruleRow == null ? "R" + (columnNumber + 1) : ruleRow.getRuleName(columnNumber);
        
        Object[] valueAry = new Object[paramDecl.length];
        
        for (int j = 0; j < paramDecl.length; j++) {
            if (paramDecl[j] == null) {
                continue;
            }

            int gridHeight = paramsTable.getRow(j).getSource().getHeight();
            IGridTable singleParamGridTable = paramGridColumn.getRows(fromHeight,
                fromHeight + gridHeight - 1);

            Object loadedValue = null;
            try {
                IOpenClass paramType = paramDecl[j].getType();
                loadedValue = RuleRowHelper.loadParam(LogicalTableHelper.logicalTable(singleParamGridTable),
                    paramType,
                    paramDecl[j].getName(),
                    ruleName,
                    ota,
                    paramIndexed[j]);
            } catch (SyntaxNodeException error) {
                errors.add(error);
            }
            valueAry[j] = loadedValue;

            fromHeight += gridHeight;
        }
        return valueAry;
    }

    private boolean[] getParamIndexed(IParameterDeclaration[] paramDecl) {
        boolean[] paramIndexed = new boolean[paramDecl.length];
        for (int i = 0; i < paramIndexed.length; i++) {
            paramIndexed[i] = paramDecl[i].getType().getAggregateInfo().isAggregate(paramDecl[i].getType());
        }
        return paramIndexed;
    }

    protected Object[] mergeParams(Object target, Object[] dtParams, IRuntimeEnv env, Object[] params) {

    	if (dtParams == null) {
    		dtParams = new Object[0];
    	}
    	
    	if (params == null) {
    		params = new Object[0];
    	}
    	
        Object[] newParams = new Object[dtParams.length + params.length];

        System.arraycopy(dtParams, 0, newParams, 0, dtParams.length);
        RuleRowHelper.loadParams(newParams, dtParams.length, params, target, dtParams, env);

        return newParams;
    }
        
    private ILogicalTable getValueCell(int column) {
        return decisionTable.getSubtable(column + IDecisionTableConstants.SERVICE_COLUMNS_NUMBER, row, 1, 1);
    }

    private int nValues() {
        return decisionTable.getWidth() - IDecisionTableConstants.SERVICE_COLUMNS_NUMBER;
    }

    private String makeParamName() {
        noParamsIndex += 1;

        return NO_PARAM + noParamsIndex;
    }

    private CompositeMethod generateMethod(IMethodSignature signature,
            OpenL openl,
            IBindingContextDelegator bindingContextDelegator,
            IOpenClass declaringClass,
            IOpenClass methodType) throws Exception {

        IOpenSourceCodeModule source = getExpressionSource(bindingContextDelegator);

        IParameterDeclaration[] methodParams = getParams(source,
            signature,
            declaringClass,
            methodType,
            openl,
            bindingContextDelegator);
        IMethodSignature newSignature = ((MethodSignature) signature).merge(methodParams);
        OpenMethodHeader methodHeader = new OpenMethodHeader(null, methodType, newSignature, declaringClass);

        return OpenLManager.makeMethod(openl, source, methodHeader, bindingContextDelegator);
    }

	protected IOpenSourceCodeModule getExpressionSource(IBindingContext bindingContext) {
		return new GridCellSourceCodeModule(codeTable.getSource(), bindingContext);
	}

    /**
     * Gets local parameter declaration from specified source.
     * 
     * OpenL support several types of parameters declarations. In common case
     * user should provide the following information: <br>
     * a) <type of parameter> <br>
     * b) <name of parameter>.<br>
     * But in simple cases of parameter usage the information is redundant.
     * 
     * OpenL engine uses the following rules when user omitted parameter
     * declaration or part of it:
     * 
     * a) if cell with parameter declaration is empty then engine will use the
     * parameter with name "Pn", where n is the number of parameter (1 based)
     * and type what is equals of expression type <br>
     * 
     * b) if user omitted parameter name then engine will use parameter with
     * name "Pn", where n is the number of parameter (1 based) and type what is
     * specified by user <br>
     * 
     * User can use parameters with generated name in his expressions but in
     * this case he should provide type of parameter.
     * 
     * @param paramSource source of parameter declaration
     * @param methodSource source of method (cell with expression where used
     *            local parameter)
     * @param signature method signature
     * @param declaringClass IOpenClass what declare method
     * @param methodType return type of method
     * @param openl openl context
     * @param bindingContext binding context
     * @return parameter declaration
     * @throws OpenLCompilationException if and error has occurred
     */
    private IParameterDeclaration getParameterDeclaration(IOpenSourceCodeModule paramSource,
            IOpenSourceCodeModule methodSource,
            IMethodSignature signature,
            IOpenClass declaringClass,
            IOpenClass methodType,
            OpenL openl,
            IBindingContext bindingContext) throws OpenLCompilationException {

        IdentifierNode[] nodes = Tokenizer.tokenize(paramSource, " \n\r");

        if (nodes.length == 0) {

            try {
                OpenMethodHeader methodHeader = new OpenMethodHeader(null, methodType, signature, declaringClass);
                CompositeMethod method = OpenLManager.makeMethod(openl, methodSource, methodHeader, bindingContext);

                IOpenClass type = method.getBodyType();
                
                if (type instanceof NullOpenClass) {
                    String message = String.format("Cannot recognize type of local parameter for expression");
                    throw SyntaxNodeExceptionUtils.createError(message, null, null, methodSource);
                }
                
                String paramName = makeParamName();

                return new ParameterDeclaration(type, paramName);
            } catch (Exception ex) {
                throw SyntaxNodeExceptionUtils.createError("Cannot compile expression", ex, null, methodSource);
            }
        }

        if (nodes.length > 2) {
            String errMsg = "Parameter Cell format: <type> <name>";
            throw SyntaxNodeExceptionUtils.createError(errMsg, null, null, methodSource);
        }

        String typeCode = nodes[0].getIdentifier();
        IOpenClass type = RuleRowHelper.getType(typeCode, bindingContext);

        if (type == null) {
            throw SyntaxNodeExceptionUtils.createError("Type not found: " + typeCode, nodes[0]);
        }

        if (nodes.length == 1) {
            String paramName = makeParamName();
            return new ParameterDeclaration(type, paramName);
        }

        String name = nodes[1].getIdentifier();

        return new ParameterDeclaration(type, name);
    }

}
