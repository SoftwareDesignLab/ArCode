package edu.rit.se.design.specminer.fspec;

import edu.rit.se.design.specminer.util.graph.DirectedGraphNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class FSpecAPIInstantiationNode implements FSpecAPINode {
    String className;
    String constructorSignature;
    boolean isAbstractOrInterface;
    boolean isPublicConstructor;

    public FSpecAPIInstantiationNode(String className, String constructorSignature, boolean isAbstractOrInterface, boolean isPublicConstructor) {
        this.className = className;
        this.constructorSignature = constructorSignature;
        this.isAbstractOrInterface = isAbstractOrInterface;
        this.isPublicConstructor = isPublicConstructor;
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
        String[] candidateArguments = constructorSignature.substring( constructorSignature.indexOf("(") + 1, constructorSignature.indexOf( ")" ) ).split(";");
        for (String candidateArgument : candidateArguments) {
            if( candidateArgument.length() > 0 )
                argumentTypes.add( candidateArgument );
        }
        return argumentTypes;    }

    public String getConstructorSignature() {
        return constructorSignature;
    }

    @Override
    public String getTitle() {
        return "Init " + className + constructorSignature.replaceAll("<init>", "");
    }

    @Override
    public FSpecAPIInstantiationNode clone() {
        return new FSpecAPIInstantiationNode( className, constructorSignature, isAbstractOrInterface, isPublicConstructor );
    }

    public boolean isAbstractOrInterface(){
        return isAbstractOrInterface;
    }

    public boolean isPublicConstructor() {
        return isPublicConstructor;
    }
}
