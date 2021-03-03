package edu.rit.se.design.arcode.fspecminer.graam;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class StatementRepresentation implements Serializable {
//    Statement walaStatement;
    public enum ApiType{ NEW_OBJECT, CONSTRUCTOR, INVOCATION, UNKNOWN }
    String frameworkClass;
    String frameworkMethod;
    String originClass;
    String originMethod;
    ApiType apiType;
    int originalStatementCGNodeId;
    int originalStatementIIndex;
    int originalLineNumber;
    boolean isAbstractOrInterfaceConstructorNode;
    boolean isStaticMethod;
    boolean isPublicMethod;

    public StatementRepresentation(String frameworkClass, String frameworkMethod, boolean isAbstractOrInterfaceConstructorNode, boolean isStaticMethod, boolean isPublicMethod,
                                   String originClass, String originMethod,
                                   int originalLineNumber, ApiType apiType, int originalStatementIIndex, int originalStatementCGNodeId ){
        this.frameworkClass = frameworkClass;
        this.frameworkMethod = frameworkMethod;
        this.originClass = originClass;
        this.originMethod = originMethod;
        this.originalLineNumber = originalLineNumber;
        this.apiType = apiType;
        this.originalStatementIIndex = originalStatementIIndex;
        this.originalStatementCGNodeId = originalStatementCGNodeId;
        this.isAbstractOrInterfaceConstructorNode = isAbstractOrInterfaceConstructorNode;
        this.isStaticMethod = isStaticMethod;
        this.isPublicMethod = isPublicMethod;
    }

//    public StatementRepresentation(Statement walaStatement) {
//        this.walaStatement = walaStatement;
//    }

    public String getTitle(){
//        return walaStatement.toString();
        return frameworkClass + "." + frameworkMethod/* + "\n[" + originClass + "." + originMethod + "]"*/;
    }
/*
    private Statement getWalaStatement() {
        return walaStatement;
    }*/

    /*
    While creating a StatementRepresentation in PrimaryAPIUsageGraphBuilder, we keep track of what GraphNode is
    mapped to which Statement and so, we correctly create a PrimaryAPIUsageGraph from a sliced data- and sequence-based
    LabeledGraphs. We don't need to know the origin of each StatementRepresentation afterwards.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(  o == null || !o.getClass().equals( getClass() ))
            return false;

        StatementRepresentation that = (StatementRepresentation) o;

        return that.originalStatementIIndex == originalStatementIIndex &&
                that.originalStatementCGNodeId == originalStatementCGNodeId;

       /* if( that.originalStatementId != this.originalStatementId)
            return false;

       if( getFrameworkClass() == null && that.getFrameworkClass() ==  null
               && getFrameworkMethod() == null && that.getFrameworkMethod() == null )
           // Obviously these are two non-framework statements and they are not the same (i.g this != o )
           return false;

        return that.apiType.equals( this.apiType ) &&
                that.frameworkClass.equals( this.frameworkClass ) &&
                that.frameworkMethod.equals( this.frameworkMethod );*/
    }

    @Override
    public int hashCode() {
        return Objects.hash(frameworkClass + frameworkMethod);
    }



    public boolean isNewObjectNode(){
        return ApiType.NEW_OBJECT.equals( apiType );
    }

    public boolean isConstructorNode(){
        return ApiType.CONSTRUCTOR.equals( apiType );
    }

    public boolean isInvocationNode(){
        return ApiType.INVOCATION.equals( apiType );
    }

    public String getFrameworkClass() {
        return frameworkClass;
    }

    public String getFrameworkMethod() {
        return frameworkMethod;
    }

    public String getOriginClass() {
        return originClass;
    }

    public String getOriginMethod() {
        return originMethod;
    }

    public int getOriginalLineNumber() {
        return originalLineNumber;
    }

    public ApiType getApiType() {
        return apiType;
    }

    public boolean isAbstractOrInterfaceConstructorNode(){ return isAbstractOrInterfaceConstructorNode; }

    public boolean isStaticMethod() {
        return isStaticMethod;
    }

    public boolean isPublicMethod() {
        return isPublicMethod;
    }
}
