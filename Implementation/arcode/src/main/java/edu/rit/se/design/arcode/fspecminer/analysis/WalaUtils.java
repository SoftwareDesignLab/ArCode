/*
 * Copyright (c) 2021 - Present. Rochester Institute of Technology
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.rit.se.design.arcode.fspecminer.analysis;

import com.ibm.wala.cast.java.loader.JavaSourceLoaderImpl;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.ShrikeCTMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.SubtypesEntrypoint;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.shrikeBT.IInstruction;
import com.ibm.wala.shrikeBT.NewInstruction;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.ssa.*;
import com.ibm.wala.types.*;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class provides some utilities for WALA library.
 * @author Joanna C. S. Santos <jds5109@rit.edu>, Ali Shokri <as8308@rit.edu>
 */
public class WalaUtils {
    private static final String MAIN_METHOD_NAME = "main";
    private static final String MAIN_METHOD_DESCRIPTOR = "([Ljava/lang/String;)V";

    /**
     * Gets the target class for method invocations and objects instantiations.
     *
     * @param stmt the statement
     * @return the target class (or null if the instruction is not a method invocation or an object instantiation)
     */
    public static IClass getTargetClass(Statement stmt) {
        IClassHierarchy cha = stmt.getNode().getClassHierarchy();
        if (stmt.getKind() == Statement.Kind.NORMAL) {
            NormalStatement normalStmt = (NormalStatement) stmt;
            if (normalStmt.getInstruction() instanceof SSAAbstractInvokeInstruction) {
                SSAAbstractInvokeInstruction invokeIns = (SSAAbstractInvokeInstruction) normalStmt.getInstruction();
                return cha.lookupClass(invokeIns.getDeclaredTarget().getDeclaringClass());
            } else if (normalStmt.getInstruction() instanceof SSANewInstruction) {
                SSANewInstruction newIns = (SSANewInstruction) normalStmt.getInstruction();
                return cha.lookupClass(newIns.getNewSite().getDeclaredType());
            }
        }
        return null;
    }
    
    public static String getTargetMethod(Statement stmt) {
        if (stmt.getKind() == Statement.Kind.NORMAL) {
            NormalStatement normalStmt = (NormalStatement) stmt;
            if (normalStmt.getInstruction() instanceof SSAAbstractInvokeInstruction) {
                SSAAbstractInvokeInstruction invokeIns = (SSAAbstractInvokeInstruction) normalStmt.getInstruction();
                return invokeIns.getDeclaredTarget().getSelector().toString();
            } else if (normalStmt.getInstruction() instanceof SSANewInstruction) {
                return "New";
            }
        }
        return null;
    }

    public static boolean isPublicMethod( Statement stmt ){
        IClass targetClass = getTargetClass( stmt );
        if( targetClass == null )
            return false;

        Selector selector = null;
        NormalStatement normalStmt = (NormalStatement) stmt;
        if (normalStmt.getInstruction() instanceof SSAAbstractInvokeInstruction) {
            SSAAbstractInvokeInstruction invokeIns = (SSAAbstractInvokeInstruction) normalStmt.getInstruction();
            selector = invokeIns.getDeclaredTarget().getSelector();
        }

        if( selector == null )
            return false;

        IMethod targetMethod = targetClass.getMethod( selector );
        if( targetMethod == null )
            return false;

        return targetMethod.isPublic();
    }

    public static boolean isStaticMethod( Statement stmt ){
        IClass targetClass = getTargetClass( stmt );
        if( targetClass == null )
            return false;

        Selector selector = null;
        NormalStatement normalStmt = (NormalStatement) stmt;
        if (normalStmt.getInstruction() instanceof SSAAbstractInvokeInstruction) {
            SSAAbstractInvokeInstruction invokeIns = (SSAAbstractInvokeInstruction) normalStmt.getInstruction();
            selector = invokeIns.getDeclaredTarget().getSelector();
        }

        if( selector == null )
            return false;

        IMethod targetMethod = targetClass.getMethod( selector );
        if( targetMethod == null )
            return false;

        return targetMethod.isStatic();
    }

/*    public static Entrypoint convertToEntrypoint(EntrypointInfo entrypointInfo) throws Exception {
        if( entrypointInfo instanceof JarEntrypointInfo )
            return convertToEntrypoint( ((JarEntrypointInfo) entrypointInfo).getClassHierarchy(),
                    ((JarEntrypointInfo) entrypointInfo).getClassNode(), ((JarEntrypointInfo) entrypointInfo).getMethodNode() );
        else
            return convertToEntrypoint(((SourceEntrypointInfo)entrypointInfo).getClassHierarchy(),
                    ((SourceEntrypointInfo)entrypointInfo).getiClass(), ((SourceEntrypointInfo)entrypointInfo).getiMethod() );
    }*/


/*

    *//**
     * Instantiate a new {@link SubtypesEntrypoint} given a class node and a method node.
     *
     * @param cha project's class hierarchy
     * @param classNode the class node
     * @param methodNode the method node
     * @return a new {@link Entrypoint} given a class node and a method node.
     *//*
    public static Entrypoint convertToEntrypoint(IClassHierarchy cha, ClassNode classNode, MethodNode methodNode) throws Exception {
*//*
        TypeReference classTypeRef = TypeReference.findOrCreate(ClassLoaderReference.Application, TypeName.string2TypeName("L" + classNode.name));
        IClass wrapperIClass = cha.lookupClass(classTypeRef);
*//*

*//*        if( wrapperIClass == null ){
            classTypeRef = TypeReference.findOrCreate(ClassLoaderReference.Application, TypeName.string2TypeName("L" + classNode.name + "$"));
            wrapperIClass = cha.lookupClass(classTypeRef);
        }*//*

        IClass iClass = getIClass( cha, classNode );
        IMethod iMethod = getIMethod( iClass, methodNode );
        if( iClass == null || iMethod == null)


        Selector selector = new Selector(Atom.findOrCreateUnicodeAtom(methodNode.name), Descriptor.findOrCreateUTF8(methodNode.desc));
        IMethod method = wrapperIClass != null ? wrapperIClass.getMethod(selector) : null;
        if (wrapperIClass == null || method == null) {
//            System.out.println(classNode.name + "." + methodNode.name); // FIXME
            throw new Exception( "Entry point creation failed for " + classNode.name + "." + methodNode.name +
                    ". Couldn't find class hierarchy for \"" + classTypeRef + "\" or method \"" + selector.toString() + "\"" );
        }
        return method != null ? new DefaultEntrypoint(method, cha) *//*SubtypesEntrypoint(method, cha)*//* : null; // FIXME
    }*/

    public static IClass getIClassForApplicationClassNode( IClassHierarchy cha, ClassNode classNode ){
        TypeReference classTypeRef = TypeReference.findOrCreate(ClassLoaderReference.Application, TypeName.string2TypeName("L" + classNode.name));
        return cha.lookupClass(classTypeRef);
    }

    public static IMethod getIMethod( IClass iClass, MethodNode methodNode ){
        if( iClass == null )
            return null;
        for ( IMethod iMethod : iClass.getAllMethods().stream().filter(
                iMethod -> iMethod.getDeclaringClass().equals( iClass )).collect(Collectors.toList()) )
            if( methodNode.name.equals( iMethod.getName().toString() ) && methodNode.desc.equals( iMethod.getDescriptor().toString() ) )
                return  iMethod;
/*
        Selector selector = new Selector(Atom.findOrCreateUnicodeAtom(methodNode.name), Descriptor.findOrCreateUTF8(methodNode.desc));
        return iClass.getMethod(selector);
*/
        return null;
    }

    public static Entrypoint convertToEntrypoint(IClassHierarchy cha, IMethod imethod) throws Exception {
        if( imethod == null )
            throw new Exception( "Entry point creation failed due to null IMethod!" );
        return new DefaultEntrypoint(imethod, cha);

//        return  new SubtypesEntrypoint(imethod, cha);
    }

    public static boolean isNewObjectNode( Statement statement ){
        if( !(statement instanceof NormalStatement) )
            return false;
        NormalStatement normalStmtS1 = ((NormalStatement) statement);
        SSAInstruction instrS1 = normalStmtS1.getInstruction();
        if (instrS1 instanceof SSANewInstruction) {
            return true;
        }
        return false;
    }

    public static boolean isConstructorNode( Statement statement ){
        if( !(statement instanceof NormalStatement) )
            return false;
        NormalStatement normalStmtS1 = ((NormalStatement) statement);
        SSAInstruction instrS1 = normalStmtS1.getInstruction();
        if (instrS1 instanceof SSAAbstractInvokeInstruction) {
            MethodReference methodRef1 = ((SSAAbstractInvokeInstruction) instrS1).getDeclaredTarget();
            if( methodRef1.isInit() ){
                return true;
            }
        }

        return false;
    }

    public static boolean isAbstractOrInterfaceConstructor( IClass frameworkClass ){
/*        if( !isConstructorNode(statement) )
            return false;
        NormalStatement normalStmtS1 = ((NormalStatement) statement);
        return normalStmtS1.getNode().getMethod().getDeclaringClass().isInterface() ||
                normalStmtS1.getNode().getMethod().getDeclaringClass().isAbstract();*/
        if( frameworkClass == null )
            return false;
        return frameworkClass.isInterface() || frameworkClass.isAbstract();
    }


    /**
     * Given an {@link IClass} c, it returns a set with all the classes above it in the class hierarchy.
     *
     * @param c subject class
     * @param includeInterfaces true if the analysis shall include interfaces
     * @return a set with all the classes above c in the class hierarchy.
     */
    public static Set<IClass> getSuperClasses(IClass c, boolean includeInterfaces) {
        if (c == null) {
            throw new IllegalArgumentException("IClass c parameter can't be null");
        }

        Queue<IClass> toVisit = new LinkedList<>();
        Set<IClass> results = new HashSet<>();
        toVisit.add(c);
        while (!toVisit.isEmpty()) {
            IClass top = toVisit.poll();
            // adds superclass
            if (top.getSuperclass() != null) {
                results.add(top.getSuperclass());
                toVisit.add(top.getSuperclass());
            }
            // adds any interface
            if (includeInterfaces) {
                top.getAllImplementedInterfaces().forEach(classInterface -> {
                    results.add(classInterface);
                    toVisit.add(classInterface);
                });
            }
        }
        return results;
    }

    /**
     * Given an {@link IClass} c, it returns the first class high up in the hierarchy that matches any string from a given set of strings.
     *
     * @param c subject class
     * @param filter a list of string to filter out the (super)classes of interest
     * @param includeInterfaces true if the analysis shall include interfaces
     * @return a set with all the classes above c in the class hierarchy.
     */
    public static IClass getClosestSuperclass(IClass c, boolean includeInterfaces, Set<String> filter) {
        if (c == null) {
            throw new IllegalArgumentException("IClass c parameter can't be null");
        }
        if (filter == null) {
            throw new IllegalArgumentException("Set<String> filter parameter can't be null");
        }

        Queue<IClass> toVisit = new LinkedList<>();
        toVisit.add(c);
        while (!toVisit.isEmpty()) {
            IClass top = toVisit.poll();
            // adds superclass
            if (top.getSuperclass() != null) {
                if (filter.contains(top.getSuperclass().getName().toString())) {
                    return top.getSuperclass();
                } else {
                    toVisit.add(top.getSuperclass());
                }
            }

            // adds any interface (if asked)
            if (includeInterfaces) {
                for (IClass classInterface : top.getAllImplementedInterfaces()) {
                    if (filter.contains(classInterface.getName().toString())) {
                        return classInterface;
                    } else {
                        toVisit.add(classInterface);
                    }
                }
            }
        }

        return null;
    }

/*    public static boolean invokesMethod(EntrypointInfo entrypointInfo, String calleeClassName, String[] possibleCalleeMethodsName){
       if( entrypointInfo instanceof JarEntrypointInfo ) {
           MethodNode callerMethod = ((JarEntrypointInfo) entrypointInfo).getMethodNode();
           ListIterator<AbstractInsnNode> it = callerMethod.instructions.iterator();
           while (it.hasNext()) {
               AbstractInsnNode instruction = it.next();
               switch (instruction.getOpcode()) {
                   case Opcodes.INVOKEDYNAMIC:
                       // FIXME
                       break;
                   case Opcodes.INVOKEINTERFACE:
                   case Opcodes.INVOKESPECIAL:
                   case Opcodes.INVOKESTATIC:
                   case Opcodes.INVOKEVIRTUAL:
                       MethodInsnNode mInvoke = (MethodInsnNode) instruction;
                       Set<String> possibleInvokedMethodeNameSet = new HashSet<>(Arrays.asList(possibleCalleeMethodsName));
                       if (mInvoke.owner.equals(calleeClassName) && possibleInvokedMethodeNameSet.contains(mInvoke.name))
                           return true;
               }
           }
       }
       else if( entrypointInfo instanceof SourceEntrypointInfo ){
            //TODO
       }
        return false;
    }*/

    public static boolean invokesMethod(Entrypoint entrypoint, String calleeClassName, String[] possibleCalleeMethodsName){
        // TODO: implementation needed
        return false;
    }

/*    public static boolean methodInstantiatesClass(EntrypointInfo entrypointInfo, String instantiatedClassName){
        if( entrypointInfo instanceof JarEntrypointInfo ){
            MethodNode callerMethod = ((JarEntrypointInfo) entrypointInfo).getMethodNode();
            Iterator<AbstractInsnNode> it = callerMethod.instructions.iterator();
            while (it.hasNext()) {
                AbstractInsnNode instruction = it.next();
                if (instruction.getOpcode() == Opcodes.NEW) {
                    TypeInsnNode typeIns = (TypeInsnNode) instruction;
                    if (typeIns.desc.equals(instantiatedClassName))
                        return true;
                }
            }
        }
        else if ( entrypointInfo instanceof SourceEntrypointInfo ){
            IMethod method = ((SourceEntrypointInfo) entrypointInfo).getiMethod();
            SSAInstruction ssaInstructions[] = (SSAInstruction[]) ((JavaSourceLoaderImpl.ConcreteJavaMethod)method).getControlFlowGraph().getInstructions();
            Iterator<SSAInstruction> it = Arrays.asList(ssaInstructions).iterator();
            while (it.hasNext()) {
                SSAInstruction instruction = it.next();
                if (instruction instanceof SSANewInstruction) {
                    // Ljava.... -> java....
                    if (((SSANewInstruction) instruction).getConcreteType().getName().toString().substring(1).equals(instantiatedClassName))
                        return true;
                }

            }
        }

        return false;
    }*/

    public static boolean methodInstantiatesClass(Entrypoint entrypoint, String instantiatedClassName){
        IMethod method = entrypoint.getMethod();

        if( method instanceof ShrikeCTMethod ){ // Jar file
            try {
                IInstruction[] instructions = ( (ShrikeCTMethod)method).getInstructions();
                if( instructions == null || instructions.length == 0 )
                    return false;
                Iterator<IInstruction> it = Arrays.asList( instructions ).iterator();
                while( it.hasNext() ){
                    IInstruction iInstruction = it.next();
                    if( iInstruction instanceof NewInstruction ){
                        if( ((NewInstruction) iInstruction).getType().substring(1).replace(";", "").equals(instantiatedClassName) )
                            return true;
                    }
                }

            } catch (InvalidClassFileException e) {
                e.printStackTrace();
            }

        }
        else  if( method instanceof JavaSourceLoaderImpl.ConcreteJavaMethod ) { // Java file

            SSAInstruction ssaInstructions[] = (SSAInstruction[]) ((JavaSourceLoaderImpl.ConcreteJavaMethod) method).getControlFlowGraph().getInstructions();
            if( ssaInstructions == null || ssaInstructions.length == 0 )
                return false;
            Iterator<SSAInstruction> it = Arrays.asList(ssaInstructions).iterator();
            while (it.hasNext()) {
                SSAInstruction instruction = it.next();
                if (instruction instanceof SSANewInstruction) {
                    // Ljava.... -> java....
                    if (((SSANewInstruction) instruction).getConcreteType().getName().toString().substring(1).equals(instantiatedClassName))
                        return true;
                }

            }
        }
        return false;
    }

    /**
     * Returns true if method is a main method
     *
     * @param entrypointInfo entry point to be checked whether it is a main method or not
     * @return true if the method signature matches the public static main([Ljava/lang/String;)V signature
     */
 /*   public static boolean isMainMethod(EntrypointInfo entrypointInfo) {
        if( entrypointInfo instanceof JarEntrypointInfo ) {
            MethodNode method = ((JarEntrypointInfo) entrypointInfo ).getMethodNode();
            return method.access == (Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC) // public static
                    && method.name.equals(MAIN_METHOD_NAME) // main()
                    && method.desc.equals(MAIN_METHOD_DESCRIPTOR);
        }

        if( entrypointInfo instanceof SourceEntrypointInfo ) {
            IMethod method = ((SourceEntrypointInfo) entrypointInfo ).getiMethod();
            return (method.isPublic() || method.isStatic()) // public static
                    && method.getName().toString().equals(MAIN_METHOD_NAME) // main()
                    && method.getDescriptor().toString().equals(MAIN_METHOD_DESCRIPTOR);
        }

        return false;
    }*/

    /**
     * Returns true if method is a main method
     *
     * @param entrypoint entry point to be checked whether it is a main method or not
     * @return true if the method signature matches the public static main([Ljava/lang/String;)V signature
     */
    public static boolean isMainMethod(Entrypoint entrypoint) {
        IMethod method = entrypoint.getMethod();
        return (method.isPublic() || method.isStatic()) // public static
                && method.getName().toString().equals(MAIN_METHOD_NAME) // main()
                && method.getDescriptor().toString().equals(MAIN_METHOD_DESCRIPTOR);
    }

    /**
     * Returns true if method is a main method
     *
     * @param method method to be checked whether it is a main method or not
     * @return true if the method signature matches the public static main([Ljava/lang/String;)V signature
     */
    public static boolean isMainMethod(MethodNode method) {
        return method.access == (Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC) // public static
                && method.name.equals(MAIN_METHOD_NAME) // main()
                && method.desc.equals(MAIN_METHOD_DESCRIPTOR);
    }

    public static IClass getTargetClass(SSAInstruction ssaInstruction, CGNode cgNode) {
        IClassHierarchy cha = cgNode.getClassHierarchy();
        if (isNormalInstruction( ssaInstruction )) {
            if (ssaInstruction instanceof SSAAbstractInvokeInstruction) {
                SSAAbstractInvokeInstruction invokeIns = (SSAAbstractInvokeInstruction) ssaInstruction;
                return cha.lookupClass(invokeIns.getDeclaredTarget().getDeclaringClass());
            } else if (ssaInstruction instanceof SSANewInstruction) {
                SSANewInstruction newIns = (SSANewInstruction) ssaInstruction;
                return cha.lookupClass(newIns.getNewSite().getDeclaredType());
            }
        }
        return null;
    }


    /**
     * Checks whether instruction is Normal (i.e., it is not Phi, Pi nor GetCaught).
     *
     * @param instruction the instruction to be checked
     * @return false if it Phi, Pi or GetCaught. True otherwise.
     */
    public static boolean isNormalInstruction(SSAInstruction instruction) {
        return instruction != null && (!(instruction instanceof SSAPhiInstruction || instruction instanceof SSAPiInstruction || instruction instanceof SSAGetCaughtExceptionInstruction));
    }


}
