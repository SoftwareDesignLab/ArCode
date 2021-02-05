package edu.rit.se.design.specminer.graam;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.types.*;
import edu.rit.se.design.specminer.analysis.WalaUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 *
 * This class provides all needed backend framework-based funcionalities
 *
 * @author Ali Shokri <as8308@rit.edu>
 *
 */
public abstract class FrameworkUtils {
    public abstract List<String> getFrameworkClasses();
    boolean considerOverloadedMethodsAsTheSame = true;

    public FrameworkUtils(boolean considerOverloadedMethodsAsTheSame ){
        this.considerOverloadedMethodsAsTheSame = considerOverloadedMethodsAsTheSame;
    }


    /**
     * Verifies whether a class is inheriting from frameworks.
     *
     * @param c the class being tested
     * @return true if class c's superclasses (and interface) goes all the way up to a class/interface from frameworks
     */
    public boolean inheritsFromFramework(IClass c) {
        Set<IClass> superClassesFromFramework = getSuperClassesFromFramework( c );
        return superClassesFromFramework.size() > 0;
//        Set<IClass> superClasses = c != null ? WalaUtils.getSuperClasses(c, true) : new HashSet<>();
//        return superClasses.stream().anyMatch((iClass) -> (isFromFramework(iClass)));
    }

    /*
    Iterates over super classes of c and returns those that are from framework.
     */
    protected Set<IClass> getSuperClassesFromFramework( IClass c ){
        Set<IClass> superClasses = c != null ? WalaUtils.getSuperClasses(c, true) : new HashSet<>();
        return StreamSupport.stream( superClasses.spliterator(), false ).filter( iClass -> isFromFramework(iClass) ).collect(Collectors.toSet());
    }

    /**
     * Verifies whether a class is from the frameworks package.
     *
     * @param c class being tested
     * @return true if the class is from the frameworks package.
     */
    public boolean isFromFramework(IClass c) {
        return getFrameworkClasses().contains(c.getName().toString());
    }

    /**
     * Verifies whether a method is directly from the given class which is from framework.
     *
     * @param methodSignature method being tested
     * @return true if the method is directly from the given class.
     */
    public boolean isFromFramework(IClass c, String methodSignature) {
        if( !isFromFramework(c) )
            return false;
/*
        if( methodSignature.equals("New") )
            return true;
*/
        for(IMethod iMethod : c.getAllMethods())
            if( iMethod.getSelector().toString().equals( methodSignature ) )
                return true;

        return false;
    }

    /**
     * Verifies whether a method is directly from a super class of a given class which that super class is from framework.
     *
     * @param methodSignature method being tested
     * @return true if the method is directly from the given class.
     */
    public boolean inheritsFromFramework(IClass c, String methodSignature) {
        if(!inheritsFromFramework(c))
            return false;
/*
        if( methodSignature.equals("New") )
            return true;
*/
        for( IClass superClass: getSuperClassesFromFramework(c) )
            if( isFromFramework( superClass, methodSignature ) )
                return true;
        return false;
    }


/*
    public boolean isFromFramework(String className) {
        return getFrameworkClasses().contains(className);
    }
*/

    /**
     * This method returns an {@link IClass} instance that indicates the common frameworks' class/interface for two classes. (either by
     * inheritance or exact match)
     *
     * @param classA an {@link IClass}
     * @param classB an {@link IClass}
     * @return an {@link IClass} indicating what is the common (super)class/interface between these two classes.
     */
    public IClass getCommonFrameworkType(IClass classA, IClass classB) {
        boolean isFromFramework = isFromFramework(classA) && isFromFramework(classB);
        // if it is from frameworks, the classes' fully-qualified names have to be the same
        if (isFromFramework && classA.getName().toString().equals(classB.getName().toString())) {
            return classA;
        }

        boolean inheritsFromFramework = inheritsFromFramework(classA) && inheritsFromFramework(classB);
        // if it inherits from a frameworks class of interest, the instantiated objects have to share a common parent
        if (inheritsFromFramework) {
            IClass superClass1 = WalaUtils.getClosestSuperclass(classA, true, new LinkedHashSet<>(getFrameworkClasses()));
            IClass superClass2 = WalaUtils.getClosestSuperclass(classB, true, new LinkedHashSet<>(getFrameworkClasses()));
            if (superClass1.getName().toString().equals(superClass2.getName().toString())) {
                return superClass1;
            }
        }
        return null;
    }


    /*
    * This method returns the first found ancestor that is a class of the framework.

     */
    public IClass getFrameworkType(IClass classA ){
        // if it is from frameworks, then return the class itself
        if (isFromFramework(classA)) {
            return classA;
        }

        if( inheritsFromFramework(classA) ) {
            IClass superClass1 = WalaUtils.getClosestSuperclass(classA, true, new LinkedHashSet<>(getFrameworkClasses()));
            return superClass1;
        }
        return null;
    }

    public boolean isFromFramework(Statement s) {
        try {
            if (!s.getNode().getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application)
            ) {
                return false;
            }

            IClass c = WalaUtils.getTargetClass(s);


            //TODO: if c inherits from the framework and s is a method call, then the method should
            // be inspected to be sure that it is from or is overriding a method from the framework.


            return c != null && (isFromFramework(c) || inheritsFromFramework(c));
        }
        catch ( Exception e ){
            e.printStackTrace();
        }
        return false;
    }



//    public abstract boolean isFrameworkEntrypoint(EntrypointInfo entrypointInfo);
    public abstract boolean isFrameworkEntrypoint(Entrypoint entrypoint);


}