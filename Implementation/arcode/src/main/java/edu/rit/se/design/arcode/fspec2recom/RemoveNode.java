package edu.rit.se.design.arcode.fspec2recom;

import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraphNode;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */
public class RemoveNode implements GraphEditOperation{
    DirectedGraphNode removedNode;

    public RemoveNode(DirectedGraphNode removedNode) {
        this.removedNode = removedNode;
    }

    @Override
    public int getCost() {
        return 1;
    }

    @Override
    public String getDescription() {
        return "API \"" + removedNode.getTitle() + "\" has to be removed.";
    }

}
