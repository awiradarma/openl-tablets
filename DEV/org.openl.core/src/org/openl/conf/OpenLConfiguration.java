/*
 * Created on Jun 10, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.conf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openl.binding.ICastFactory;
import org.openl.binding.INodeBinder;
import org.openl.binding.exception.AmbiguousMethodException;
import org.openl.binding.impl.MethodSearch;
import org.openl.binding.impl.cast.IOpenCast;
import org.openl.cache.GenericKey;
import org.openl.syntax.ISyntaxNode;
import org.openl.syntax.grammar.IGrammar;
import org.openl.types.IMethodCaller;
import org.openl.types.IOpenClass;
import org.openl.types.IOpenFactory;
import org.openl.types.IOpenField;
import org.openl.types.IOpenMethod;
import org.openl.types.impl.MethodKey;

/**
 * @author snshor
 *
 */
public class OpenLConfiguration implements IOpenLConfiguration {
    
    private static HashMap<Object, IOpenLConfiguration> configurations = new HashMap<Object, IOpenLConfiguration>();

    private String uri;

    private IOpenLConfiguration parent;

    private IConfigurableResourceContext configurationContext;

    private ClassFactory grammarFactory;

    private NodeBinderFactoryConfiguration binderFactory;

    private LibraryFactoryConfiguration methodFactory;
    
    private TypeCastFactory typeCastFactory;

    private TypeFactoryConfiguration typeFactory;

    private Map<String, IOpenFactoryConfiguration> openFactories = null;

    public static IOpenLConfiguration getInstance(String name, IUserContext ucxt) throws OpenConfigurationException {
        IOpenLConfiguration opc = configurations.get(name);

        if (opc != null) {
            return opc;
        }

        Object key = GenericKey.getInstance(name, ucxt);

        return configurations.get(key);

    }

    public static synchronized  void register(String name, IUserContext ucxt, IOpenLConfiguration oplc, boolean shared)
            throws OpenConfigurationException {
        Object key = null;

        if (shared) {
            key = name;
        } else {
            key = GenericKey.getInstance(name, ucxt);
        }

        IOpenLConfiguration old = configurations.get(key);
        if (old != null) {
            throw new OpenConfigurationException("The configuration " + name + " already exists", null, null);
        }
        configurations.put(key, oplc);

    }

    //FIXME: multithreading issue: users can reset foreign OpenL calculation
    public static void reset() {
        configurations = new HashMap<Object, IOpenLConfiguration>();
    }

    public static synchronized void unregister(String name, IUserContext ucxt) throws OpenConfigurationException {
        Object key = GenericKey.getInstance(name);

        // IOpenLConfiguration old =
        // (IOpenLConfiguration)configurations.get(key);
        // if (old == null)
        // {
        // throw new OpenConfigurationException("The configuration " + name + "
        // does not exists", null, null);
        // }
        configurations.remove(key);
        configurations.remove(name);

    }

    public synchronized void addOpenFactory(IOpenFactoryConfiguration opfc) throws OpenConfigurationException {
        if (openFactories == null) {
            openFactories = new HashMap<String, IOpenFactoryConfiguration>();
        }

        if (opfc.getName() == null) {
            throw new OpenConfigurationException("The factory must have a name", opfc.getUri(), null);
        }
        if (openFactories.containsKey(opfc.getName())) {
            throw new OpenConfigurationException("Duplicated name: " + opfc.getName(), opfc.getUri(), null);
        }

        openFactories.put(opfc.getName(), opfc);
    }

   public NodeBinderFactoryConfiguration getBinderFactory() {
        return binderFactory;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openl.binding.ICastFactory#getCast(java.lang.String,
     *      org.openl.types.IOpenClass, org.openl.types.IOpenClass)
     */
    public IOpenCast getCast(IOpenClass from, IOpenClass to) {
        IOpenCast cast = typeCastFactory == null ? null : typeCastFactory.getCast(from, to, configurationContext);
        if (cast != null) {
            return cast;
        }
        return parent == null ? null : parent.getCast(from, to);
    }

    public IConfigurableResourceContext getConfigurationContext() {
        return configurationContext;
    }

   
    public synchronized IGrammar getGrammar() throws OpenConfigurationException {
        if (grammarFactory == null) {
            return parent.getGrammar();
        } else {
            return (IGrammar) grammarFactory.getResource(configurationContext);
        }
    }

    public ClassFactory getGrammarFactory() {
        return grammarFactory;
    }

    public IMethodCaller getMethodCaller(String namespace, String name, IOpenClass[] params, ICastFactory casts)
            throws AmbiguousMethodException {
        
        IOpenMethod[] mcs = getMethods(namespace, name);

        return MethodSearch.getCastingMethodCaller(name, params, casts, Arrays.asList(mcs));
    }
    
    public IOpenMethod[] getMethods(String namespace, String name) {
        IOpenMethod[] mcs = methodFactory == null ? new IOpenMethod[] {}
                                                    : methodFactory.getMethods(namespace,
                                                        name,
                                                        configurationContext);
        IOpenMethod[] pmcs = parent == null ? new IOpenMethod[] {} : parent.getMethods(namespace, name);

        // Shadowing
        Map<MethodKey, Collection<IOpenMethod>> methods = new HashMap<MethodKey, Collection<IOpenMethod>>();
        for (IOpenMethod method : pmcs) {
            MethodKey mk = new MethodKey(method);
            Collection<IOpenMethod> callers = methods.get(mk);
            if (callers == null) {
                callers = new ArrayList<IOpenMethod>();
                methods.put(mk, callers);
            }
            callers.add(method);
        }

        Set<MethodKey> usedKeys = new HashSet<MethodKey>();
        for (IOpenMethod method : mcs) {
            MethodKey mk = new MethodKey(method);
            Collection<IOpenMethod> callers = methods.get(mk);
            if (callers == null) {
                usedKeys.add(mk);
                callers = new ArrayList<IOpenMethod>();
                methods.put(mk, callers);
            }
            if (!usedKeys.contains(mk)) {
                usedKeys.add(mk);
                callers = new ArrayList<IOpenMethod>();
                methods.put(mk, callers);
            }
            callers.add(method);
        }
        
        Collection<IOpenMethod> openMethods = new ArrayList<IOpenMethod>();
        for (Collection<IOpenMethod> m : methods.values()) {
            openMethods.addAll(m);
        }
        return openMethods.toArray(new IOpenMethod[] {});
    }

    public LibraryFactoryConfiguration getMethodFactory() {
        return methodFactory;
    }

    public INodeBinder getNodeBinder(ISyntaxNode node) {
        INodeBinder binder = binderFactory == null ? null : binderFactory.getNodeBinder(node, configurationContext);
        if (binder != null) {
            return binder;
        }
        return parent == null ? null : parent.getNodeBinder(node);
    }

    public IOpenFactory getOpenFactory(String name) {
        OpenFactoryConfiguration conf = openFactories == null ? null : (OpenFactoryConfiguration) openFactories
                .get(name);

        if (conf != null) {
            return conf.getOpenFactory(configurationContext);
        }

        if (parent != null) {
            return parent.getOpenFactory(name);
        }

        return null;
    }
    
    Map<String, Map<String, IOpenClass>> cache = new HashMap<String, Map<String, IOpenClass>>();

    public IOpenClass getType(String namespace, String name) {
        Map<String, IOpenClass> namespaceCache = cache.get(namespace);
        if (namespaceCache == null){
            namespaceCache = new HashMap<String, IOpenClass>();
            cache.put(namespace, namespaceCache);
        }
        if (namespaceCache.containsKey(name)){
            return namespaceCache.get(name);
        }
        
        IOpenClass type = typeFactory == null ? null : typeFactory.getType(namespace, name, configurationContext);
        if (type != null) {
            namespaceCache.put(name, type);
            return type;
        }
        
        if (parent == null){
            namespaceCache.put(name, null);
            return null;
        }else{
            type = parent.getType(namespace, name);
            namespaceCache.put(name, type);
            return type;
        }
    }

    public TypeCastFactory getTypeCastFactory() {
        return typeCastFactory;
    }

    public String getUri() {
        return uri;
    }

    public IOpenField getVar(String namespace, String name, boolean strictMatch) {
        IOpenField field = methodFactory == null ? null : methodFactory.getVar(namespace, name, configurationContext,
                strictMatch);
        if (field != null) {
            return field;
        }
        return parent == null ? null : parent.getVar(namespace, name, strictMatch);
    }

    public void setBinderFactory(NodeBinderFactoryConfiguration factory) {
        binderFactory = factory;
    }

    public void setConfigurationContext(IConfigurableResourceContext context) {
        configurationContext = context;
    }

    public void setGrammarFactory(ClassFactory factory) {
        grammarFactory = factory;
    }

    public void setMethodFactory(LibraryFactoryConfiguration factory) {
        methodFactory = factory;
    }

    public void setParent(IOpenLConfiguration configuration) {
        parent = configuration;
    }

    public void setTypeCastFactory(TypeCastFactory factory) {
        typeCastFactory = factory;
    }

    public void setTypeFactory(TypeFactoryConfiguration configuration) {
        typeFactory = configuration;
    }

    public void setUri(String string) {
        uri = string;
    }

    public synchronized void validate(IConfigurableResourceContext cxt) throws OpenConfigurationException {
        if (grammarFactory != null) {
            grammarFactory.validate(cxt);
        } else if (parent == null) {
            throw new OpenConfigurationException("Grammar class is not set", getUri(), null);
        }

        if (binderFactory != null) {
            binderFactory.validate(cxt);
        } else if (parent == null) {
            throw new OpenConfigurationException("Bindings are not set", getUri(), null);
        }

        // Methods and casts are optional
        // else if (parent == null)
        // throw new OpenConfigurationException("Methods are not set", getUri(),
        // null);

        if (methodFactory != null) {
            methodFactory.validate(cxt);
        }

        if (typeCastFactory != null) {
            typeCastFactory.validate(cxt);
        }

        if (typeFactory != null) {
            typeFactory.validate(cxt);
        }

        if (openFactories != null) {
            for (IOpenFactoryConfiguration factory : openFactories.values()) {
                factory.validate(cxt);
            }
        }

    }

}
