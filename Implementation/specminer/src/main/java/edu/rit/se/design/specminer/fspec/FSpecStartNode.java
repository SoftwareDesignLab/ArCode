package edu.rit.se.design.specminer.fspec;

import edu.rit.se.design.specminer.util.graph.DirectedGraphNode;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class FSpecStartNode implements FSpecNode{
    @Override
    public String getTitle() {
        return "Start Node";
    }

    @Override
    public FSpecStartNode clone() {
        return new FSpecStartNode();
    }
}
