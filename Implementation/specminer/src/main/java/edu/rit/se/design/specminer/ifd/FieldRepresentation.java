package edu.rit.se.design.specminer.ifd;

import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.FieldNode;

import java.util.Objects;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class FieldRepresentation {
    ClassNode classNode;
    FieldNode fieldNode;

    public FieldRepresentation(ClassNode classNode, FieldNode fieldNode) {
        this.classNode = classNode;
        this.fieldNode = fieldNode;
    }

    public ClassNode getClassNode() {
        return classNode;
    }

    public void setClassNode(ClassNode classNode) {
        this.classNode = classNode;
    }

    public FieldNode getFieldNode() {
        return fieldNode;
    }

    public void setFieldNode(FieldNode fieldNode) {
        this.fieldNode = fieldNode;
    }

    @Override
    public String toString() {
        return getFieldNode().desc + " " + getClassNode().name + "." + getFieldNode().name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldRepresentation that = (FieldRepresentation) o;
        return Objects.equals(classNode, that.classNode) &&
                Objects.equals(fieldNode, that.fieldNode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classNode, fieldNode);
    }
}
