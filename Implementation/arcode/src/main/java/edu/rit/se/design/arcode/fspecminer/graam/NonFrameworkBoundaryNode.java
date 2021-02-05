package edu.rit.se.design.arcode.fspecminer.graam;

import java.util.Objects;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class NonFrameworkBoundaryNode extends NonFrameworkRelatedNode{
    public enum GraphBoundaryNodeType {START_NODE, END_NODE}
    GraphBoundaryNodeType type;
    public NonFrameworkBoundaryNode(GraphBoundaryNodeType type) {
        this.type = type;
    }

    @Override
    public String getTitle() {
        return type.name();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        NonFrameworkBoundaryNode that = (NonFrameworkBoundaryNode) o;
//        return type.equals( that.type );
        return false;
    }

    public GraphBoundaryNodeType getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    @Override
    public NonFrameworkBoundaryNode clone() {
        return new NonFrameworkBoundaryNode( type );
    }



}
