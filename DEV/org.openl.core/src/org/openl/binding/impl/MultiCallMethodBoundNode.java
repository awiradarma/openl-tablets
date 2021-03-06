package org.openl.binding.impl;

import java.lang.reflect.Array;
import java.util.List;

import org.openl.binding.IBoundNode;
import org.openl.exception.OpenLRuntimeException;
import org.openl.syntax.ISyntaxNode;
import org.openl.types.IMethodCaller;
import org.openl.types.IOpenClass;
import org.openl.types.java.JavaOpenClass;
import org.openl.vm.IRuntimeEnv;

/**
 * Bound node for methods such as <code>'double[] calculate(Premium[] premiumObj)'</code>. Is based on the method with
 * signature <code>'double calculate(Premium premiumObj)'</code> by evaluating it several times on runtime.
 *
 * @author DLiauchuk
 */
public class MultiCallMethodBoundNode extends MethodBoundNode {

    /**
     * cached return type for current bound node
     */
    private IOpenClass returnType;

    /**
     * the indexes of the arguments in the method signature that are arrays
     **/
    private final int[] arrayArgArguments;

    /**
     * @param syntaxNode            will be represents like <code>'calculate(parameter)'</code>
     * @param children              its gonna be only one children, that represents the parameter in method call.
     * @param singleParameterMethod method for single(not array) parameter in signature
     * @param arrayArgArgumentList     the indexes of the arguments in the method signature that is are arrays
     */
    public MultiCallMethodBoundNode(ISyntaxNode syntaxNode,
            IBoundNode[] children,
            IMethodCaller singleParameterMethod, List<Integer> arrayArgArgumentList) {
        super(syntaxNode, children, singleParameterMethod);
        this.arrayArgArguments = new int[arrayArgArgumentList.size()];

        for (int i = 0; i < arrayArgArgumentList.size(); i++) {
            arrayArgArguments[i] = arrayArgArgumentList.get(i);
        }
    }

    public Object evaluateRuntime(IRuntimeEnv env) throws OpenLRuntimeException {
        Object target = getTargetNode() == null ? env.getThis() : getTargetNode().evaluate(env);
        Object[] methodParameters = evaluateChildren(env);

        // gets the values of array parameters

        int paramsLength = 1;
        for (Integer arrayArgArgument : arrayArgArguments) {
            Object arrayParameters = methodParameters[arrayArgArgument];
            if (arrayParameters == null) {
                paramsLength = 0;
                break;
            }
            paramsLength *= Array.getLength(arrayParameters);
        }

        Object results = null;

        Object[] callParameters = (Object[]) Array.newInstance(Object.class, methodParameters.length);
        System.arraycopy(methodParameters, 0, callParameters, 0, methodParameters.length);

        if (!JavaOpenClass.VOID.equals(super.getType())) {
            // create an array of results
            //
            results = Array.newInstance(super.getType().getInstanceClass(), paramsLength);
        }
        if (paramsLength > 0) {
            // populate the results array by invoking method for single parameter
            call(target, env, methodParameters, callParameters, 0, results, 0);
        }

        return results;
    }

    private int call(Object target, IRuntimeEnv env, Object[] allParameters, Object[] callParameters, int iteratedArg, Object results, int callIndex) {
        Integer iteratedParamNum = arrayArgArguments[iteratedArg];
        Object iteratedParameter = allParameters[iteratedParamNum];
        int length = Array.getLength(iteratedParameter);
        for (int i = 0; i < length; i++) {
            callParameters[iteratedParamNum] = Array.get(iteratedParameter, i);

            if (iteratedArg < arrayArgArguments.length - 1) {
                callIndex = call(target, env, allParameters, callParameters, iteratedArg + 1, results, callIndex);
            } else {
                Object result = getMethodCaller().invoke(target, callParameters, env);
                if (results != null) {
                    Array.set(results, callIndex, result);
                }
                callIndex++;
            }
        }

        return callIndex;
    }

    public IOpenClass getType() {
        if (returnType == null) {
            returnType = getReturnType();
        }
        return returnType;
    }

    private IOpenClass getReturnType() {
        IOpenClass result;
        if (JavaOpenClass.VOID.equals(super.getType())) {
            result = JavaOpenClass.VOID;
        } else {
            // gets the return type of bound node, it will be the single type.
            //
            IOpenClass singleReturnType = super.getType();

            // create an array type.
            //
            result = singleReturnType.getAggregateInfo().getIndexedAggregateType(singleReturnType, 1);
        }
        return result;
    }
}
