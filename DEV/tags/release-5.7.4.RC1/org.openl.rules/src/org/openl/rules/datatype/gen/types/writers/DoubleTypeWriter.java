package org.openl.rules.datatype.gen.types.writers;

import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Constants;
import org.openl.rules.datatype.gen.FieldDescription;

public class DoubleTypeWriter implements TypeWriter {

    public int getConstantForVarInsn() {
        return Constants.DLOAD;
    }

    public int getConstantForReturn() {
        return Constants.DRETURN;
    }

    public int writeFieldValue(CodeVisitor codeVisitor, FieldDescription fieldType) {
        codeVisitor.visitLdcInsn(fieldType.getDefaultValue()); 
        return 3;
    }

}
