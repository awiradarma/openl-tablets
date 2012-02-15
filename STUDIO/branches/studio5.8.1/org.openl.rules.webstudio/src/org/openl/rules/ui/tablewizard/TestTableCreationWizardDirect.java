package org.openl.rules.ui.tablewizard;

import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.webstudio.web.util.WebStudioUtils;

/**
 * Allows to create test tables direct from the testable table page.
 * 
 * @author DLiauchuk
 *
 */
public class TestTableCreationWizardDirect extends TestTableCreationWizard {
    
    public TestTableCreationWizardDirect(String uri) {
        TableSyntaxNode node = WebStudioUtils.getProjectModel().getNode(uri);
        if (node == null) {
            throw new IllegalArgumentException(String.format("Can`t find node with uri:%s ", uri));
        }
        selectedNode = node;        
    }
    
    private TableSyntaxNode selectedNode;
        
    public void setSelectedNode(TableSyntaxNode selectedNode) {
        this.selectedNode = selectedNode;
    }
    
    @Override
    protected TableSyntaxNode getSelectedNode() {
        return selectedNode;        
    }
    
    @Override
    public String getName() {
        return "testTableDirect";
    }
        
    @Override
    protected void onStart() {        
        setTechnicalName(getDefaultTechnicalName());
    }
    
}