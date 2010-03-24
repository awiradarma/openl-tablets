package org.openl.rules.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openl.OpenL;
import org.openl.message.Severity;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.lang.xls.syntax.TableSyntaxNodeKey;
import org.openl.types.IOpenMethod;
import org.openl.validation.ValidationResult;
import org.openl.validation.ValidationStatus;
import org.openl.validation.ValidationUtils;

/**
 * Validator that checks correctness of "active" property. Only one active table
 * allowed. And if active table is absent warning will occur.
 * 
 * @author PUdalau
 */
public class ActivePropertyValidator extends TablesValidator {

    public static final String NO_ACTIVE_TABLE_MESSAGE = "No active table";
    public static final String ODD_ACTIVE_TABLE_MESSAGE = "There is only one active table allowed";

    @Override
    public ValidationResult validateTables(OpenL openl, TableSyntaxNode[] tableSyntaxNodes) {
        ValidationResult validationResult = null;
        Map<TableSyntaxNodeKey, List<TableSyntaxNode>> groupedTables = groupExecutableTables(tableSyntaxNodes);
        for (TableSyntaxNodeKey key : groupedTables.keySet()) {
            List<TableSyntaxNode> tablesGroup = groupedTables.get(key);
            boolean activeTableWasFound = false;
            for (TableSyntaxNode tsn : tablesGroup) {
                if (tsn.getTableProperties().getActive()) {
                    if (activeTableWasFound) {
                        if (validationResult == null) {
                            validationResult = new ValidationResult(ValidationStatus.FAIL);
                        }
                        // TODO: specify source of error in OpenlMessage
                        ValidationUtils
                                .addValidationMessage(validationResult, ODD_ACTIVE_TABLE_MESSAGE, Severity.ERROR);
                    } else {
                        activeTableWasFound = true;
                    }
                }
            }
            if (!activeTableWasFound) {
                if (validationResult == null) {
                    validationResult = new ValidationResult(ValidationStatus.SUCCESS);
                }
                // TODO: Specify source of error in OpenlMessage .
                ValidationUtils.addValidationMessage(validationResult, NO_ACTIVE_TABLE_MESSAGE, Severity.WARN);
            }
        }
        if (validationResult != null) {
            return validationResult;
        } else {
            return ValidationUtils.validationSuccess();
        }
    }

    private Map<TableSyntaxNodeKey, List<TableSyntaxNode>> groupExecutableTables(TableSyntaxNode[] tableSyntaxNodes) {
        Map<TableSyntaxNodeKey, List<TableSyntaxNode>> groupedTables = new HashMap<TableSyntaxNodeKey, List<TableSyntaxNode>>();
        for (TableSyntaxNode tsn : tableSyntaxNodes) {
            if (tsn.getMember() instanceof IOpenMethod) {
                TableSyntaxNodeKey key = new TableSyntaxNodeKey(tsn);
                if (!groupedTables.containsKey(key)) {
                    groupedTables.put(key, new ArrayList<TableSyntaxNode>());
                }
                groupedTables.get(key).add(tsn);
            }
        }
        return groupedTables;
    }
}
