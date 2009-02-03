package org.openl.rules.tbasic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openl.binding.BindingDependencies;
import org.openl.rules.lang.xls.syntax.TableSyntaxNode;
import org.openl.rules.tbasic.runtime.RuntimeOperation;
import org.openl.rules.tbasic.runtime.TBasicVM;
import org.openl.syntax.ISyntaxNode;
import org.openl.types.IDynamicObject;
import org.openl.types.IMemberMetaInfo;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenMethodHeader;
import org.openl.types.impl.AMethod;
import org.openl.types.impl.DelegatedDynamicObject;
import org.openl.vm.IRuntimeEnv;

public class Algorithm extends AMethod implements IMemberMetaInfo {
    private final AlgorithmBoundNode node;
        
    /**************************************************
     * Comple artifacts
     *************************************************/
    private IOpenClass thisClass;
    private List<RuntimeOperation> algorithmSteps;
    private Map<String, RuntimeOperation> labels;

    private final List<AlgorithmRow> rows;

    public Algorithm(IOpenMethodHeader header, AlgorithmBoundNode node) {
        super(header);
        this.node = node;

        rows = new ArrayList<AlgorithmRow>();
    }

    public static Algorithm createAlgorithm(IOpenMethodHeader header, AlgorithmBoundNode node) {
        return new Algorithm(header, node);
    }

    public Object invoke(Object target, Object[] params, IRuntimeEnv env) {
        DelegatedDynamicObject thisInstance = new DelegatedDynamicObject (thisClass, (IDynamicObject)target);

        TBasicVM algorithmVM = new TBasicVM(algorithmSteps, labels);
        return algorithmVM.run(thisInstance, target, params, env);
    }

    public BindingDependencies getDependencies() {
        // TODO Auto-generated method stub
        return null;
    }

    public ISyntaxNode getSyntaxNode() {
        return node.getSyntaxNode();
    }

    public String getSourceUrl() {
        return ((TableSyntaxNode) node.getSyntaxNode()).getUri();
    }

    @Override
    public IMemberMetaInfo getInfo() {
        return this;
    }

    public void addRow(AlgorithmRow row) {
        rows.add(row);
    }

    public List<AlgorithmRow> getRows() {
        return rows;
    }
}

