package edu.rit.se.design.arcode.fspecminer.graam;

import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraphNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class FrameworkRelatedNode implements DirectedGraphNode {

    StatementRepresentation statementRepresentation;

    public FrameworkRelatedNode(StatementRepresentation statementRepresentation) {
        this.statementRepresentation = statementRepresentation;
    }

    @Override
    public String getTitle() {
        return statementRepresentation.getTitle();
    }

/*    public StatementRepresentation getStatementRepresentation() {
        return statementRepresentation;
    }*/

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FrameworkRelatedNode that = (FrameworkRelatedNode) o;
        return statementRepresentation.equals(that.statementRepresentation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(statementRepresentation);
    }

    @Override
    public String toString() {
        return getTitle();
    }

    public String getOriginClass(){
        return statementRepresentation.getOriginClass();
    }

    public String getOriginMethod(){ return statementRepresentation.getOriginMethod();}

    public int getOriginalLineNumber() { return statementRepresentation.getOriginalLineNumber(); }

    public String getFrameworkRelatedClass(){ return statementRepresentation.getFrameworkClass(); }

    public String getFrameworkRelatedMethod(){ return statementRepresentation.getFrameworkMethod(); }


    public boolean isNewObjectNode(){
        return statementRepresentation.isNewObjectNode();
    }

    public boolean isInitNode(){
        return statementRepresentation.isConstructorNode();
    }

    public boolean isNormalMethodCall(){ return !isInitNode() && !isNewObjectNode(); }

    public boolean isAbstractOrInterfaceConstructorNode(){
        return statementRepresentation.isAbstractOrInterfaceConstructorNode();
    }

    public boolean isStaticMethod(){
        return statementRepresentation.isStaticMethod();
    }

    public boolean isPublicMethod(){
        return statementRepresentation.isPublicMethod();
    }

    @Override
    public FrameworkRelatedNode clone() {
        return new FrameworkRelatedNode( statementRepresentation );
    }

    public String getFullClassName() {
        if( isInitNode() )
            return getFrameworkRelatedClass();
        if( isNormalMethodCall() )
            return getFrameworkRelatedMethod().substring( getFrameworkRelatedMethod().indexOf( ")" ) + 1).replaceAll(";", "");
        return null;
    }
    public String getSimpleClassName(){
        if( isInitNode() )
            return getFullClassName().replaceAll(".*\\/", "");;
        if( isNormalMethodCall() )
            return getFullClassName().replaceAll(".*\\/", "");
        return null;
    }
    public List<String> getArgumentTypes() {
        List<String> argumentTypes = new ArrayList<>();
        String[] candidateArguments = getFrameworkRelatedMethod().substring( getFrameworkRelatedMethod().indexOf("(") + 1, getFrameworkRelatedMethod().indexOf( ")" ) ).split(";");
        for (String candidateArgument : candidateArguments) {
            if( candidateArgument.length() > 0 )
                argumentTypes.add( candidateArgument );
        }
        return argumentTypes;
    }

    public String getReturnType(){
        return getFrameworkRelatedMethod().substring( getFrameworkRelatedMethod().indexOf( ")" ) + 1).replaceAll(";", "");
    }

    public String getSimpleReturnType(){
        boolean isArray = getReturnType().startsWith("[");
        return getReturnType().replaceAll(".*\\/", "") + (isArray? "[]" : "");
    }

    public String getMethodName(){
        return getFrameworkRelatedMethod().substring(0, getFrameworkRelatedMethod().indexOf("("));
    }

}
