package edu.rit.se.design.arcode.fspecminer.graam;

import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraphNode;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public abstract class NonFrameworkRelatedNode implements DirectedGraphNode {

    @Override
    public String toString() {
        return getTitle();
    }

    @Override
    public DirectedGraphNode clone() {
        return null;
    }
}
