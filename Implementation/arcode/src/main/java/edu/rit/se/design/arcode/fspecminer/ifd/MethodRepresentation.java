package edu.rit.se.design.arcode.fspecminer.ifd;

import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraphNode;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class MethodRepresentation implements DirectedGraphNode {
    ClassNode classNode;
    MethodNode methodNode;

    public MethodRepresentation(ClassNode classNode, MethodNode methodNode ){
        setClassNode( classNode );
        setMethodNode( methodNode );
    }

    public ClassNode getClassNode() {
        return classNode;
    }

    public void setClassNode(ClassNode classNode) {
        this.classNode = classNode;
    }

    public MethodNode getMethodNode() {
        return methodNode;
    }

    public void setMethodNode(MethodNode methodNode) {
        this.methodNode = methodNode;
    }

    public boolean isPublicMethod(){
        return methodNode.access == 1;
    }

    public boolean isPrivateMethod(){
        return methodNode.access == 2;
    }

    public boolean isConstructor(){
        return methodNode.name.contains("<init>");
    }

    public boolean isGetter() {return (methodNode.name.startsWith("is") && methodNode.desc.endsWith("Z")) ||
            (methodNode.name.startsWith("get") && !methodNode.desc.endsWith("V"));}

    public boolean isSetter() {return methodNode.name.startsWith("set") && methodNode.desc.endsWith("V");}

    @Override
    public String toString() {
        String visibility = (isPublicMethod() ? "public" : (isPrivateMethod() ? "private" : "Unknown"  ) );
        if( visibility.equals("Unknown") )
            System.out.print("");
        String toString = /*visibility + " " + */classNode.name + "." + methodNode.name + methodNode.desc;
        toString = toString.replaceAll("[^ ()]+\\/", "");
        if(toString.endsWith(";"))
            toString = toString.substring(0, toString.length()-1);
        return toString;
    }

    public int hashCode(){
        return 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodRepresentation that = (MethodRepresentation) o;
        return classNode.equals(that.classNode) &&
                methodNode.equals(that.methodNode);
    }

    @Override
    public String getTitle() {
        return toString();
    }

    @Override
    public MethodRepresentation clone() {
        return new MethodRepresentation(classNode, methodNode);
    }
}
