package org.openl.rules.dt.algorithm2;

import org.openl.util.trie.IARTNode;

public interface ISearchTreeNode extends ISearchNode, IARTNode {

	ISearchTreeNode compactSearchNode();

	
}
