package edu.rit.se.design.specminer.graam;

import edu.rit.se.design.specminer.util.graph.DirectedGraphNode;

import java.util.Objects;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class NonFrameworkMiddleNode extends NonFrameworkRelatedNode{
    StatementRepresentation statementRepresentation;

    public NonFrameworkMiddleNode(StatementRepresentation statementRepresentation) {
        this.statementRepresentation = statementRepresentation;
    }

    @Override
    public String getTitle() {
        return statementRepresentation.getTitle();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NonFrameworkMiddleNode that = (NonFrameworkMiddleNode) o;
        return statementRepresentation.equals( that.statementRepresentation );
    }

    @Override
    public int hashCode() {
        return Objects.hash(statementRepresentation);
    }

    @Override
    public NonFrameworkMiddleNode clone() {
        return new NonFrameworkMiddleNode( statementRepresentation );
    }
}
