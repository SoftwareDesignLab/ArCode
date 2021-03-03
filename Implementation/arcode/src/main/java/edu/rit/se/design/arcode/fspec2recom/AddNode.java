package edu.rit.se.design.arcode.fspec2recom;

import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraphNode;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */
public class AddNode implements GraphEditOperation{
    DirectedGraphNode addedNode;

    public AddNode(DirectedGraphNode addedNode) {
        this.addedNode = addedNode;
    }

    @Override
    public int getCost() {
        return 1;
    }

    @Override
    public String getDescription() {
        return "API \"" + addedNode.getTitle() + "\" has to be used.";
    }
}
