package edu.rit.se.design.arcode.fspecminer.fspec;

import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraph;


/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class FSpec extends DirectedGraph<FSpecNode, FSpecEdgeType, FSpecEdge> {
    String framework;

    public FSpec(String framework) {
        this.framework = framework;
        addNode( new FSpecStartNode() );
    }

    @Override
    protected FSpecEdgeType getDefaultEdgeType() {
        return FSpecEdgeType.EXPLICIT_DATA_DEP;
    }

    @Override
    protected FSpecEdge getDefaultEdgeInfo(FSpecEdgeType edgeType) {
        return new FSpecEdge("");
    }

    @Override
    public String getTitle() {
        return "FSpec of " + framework;
    }

    public String getFramework() {
        return framework;
    }
}
