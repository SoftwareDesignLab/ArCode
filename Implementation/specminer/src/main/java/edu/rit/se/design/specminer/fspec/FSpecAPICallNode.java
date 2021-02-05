package edu.rit.se.design.specminer.fspec;

import edu.rit.se.design.specminer.util.graph.DirectedGraphNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class FSpecAPICallNode implements FSpecAPINode {
    String className;
    String methodSignature;
    boolean staticMethod;
    boolean publicMethod;

    public FSpecAPICallNode(String className, String methodSignature, boolean isStaticMethod, boolean isPublicMethod) {
        this.className = className;
        this.methodSignature = methodSignature;
        this.staticMethod = isStaticMethod;
        this.publicMethod = isPublicMethod;
    }

    public String getFullClassName() {
        return className;
    }

    @Override
    public String getSimpleClassName() {
        return getFullClassName().replaceAll(".*\\/", "");
    }

    @Override
    public List<String> getArgumentTypes() {
        List<String> argumentTypes = new ArrayList<>();
        String[] candidateArguments = methodSignature.substring( methodSignature.indexOf("(") + 1, methodSignature.indexOf( ")" ) ).split(";");
        for (String candidateArgument : candidateArguments) {
            if( candidateArgument.length() > 0 )
                argumentTypes.add( candidateArgument );
        }
        return argumentTypes;
    }

    public String getMethodSignature() {
        return methodSignature;
    }

    public String getMethodName(){
/*        if( methodSignature.indexOf("(") < 2)
            return "";*/
        return methodSignature.substring(0, methodSignature.indexOf("("));
    }

    public String getReturnType(){
        return methodSignature.substring( methodSignature.indexOf( ")" ) + 1).replaceAll(";", "");
    }

    public String getSimpleReturnType(){
        boolean isArray = getReturnType().startsWith("[");
        return getReturnType().replaceAll(".*\\/", "") + (isArray? "[]" : "");
    }

    public boolean isStaticMethod() {
        return staticMethod;
    }

    public boolean isPublicMethod() {
        return publicMethod;
    }

    @Override
    public String getTitle() {
        return className + "." + methodSignature;
    }

    @Override
    public FSpecAPICallNode clone() {
        return new FSpecAPICallNode( className,  methodSignature,  staticMethod,  publicMethod);
    }
}
