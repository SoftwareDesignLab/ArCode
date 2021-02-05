package edu.rit.se.design.fspec2recom;

import edu.rit.se.design.specminer.graam.GRAAMEdgeType;
import edu.rit.se.design.specminer.util.graph.DirectedGraphNode;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */
public class RemoveEdge implements GraphEditOperation{
    GRAAMEdgeType edgeType;
    DirectedGraphNode fromNode;
    DirectedGraphNode toNode;

    public RemoveEdge(GRAAMEdgeType edgeType, DirectedGraphNode fromNode, DirectedGraphNode toNode) {
        this.edgeType = edgeType;
        this.fromNode = fromNode;
        this.toNode = toNode;
        if( fromNode == null || toNode == null )
            System.out.print("");
    }

    @Override
    public int getCost() {
        if( edgeType.equals( GRAAMEdgeType.IMPLICIT_DATA_DEP ) )
            return 0;
        return 1;
    }

    @Override
    public String getDescription() {
        if( edgeType.equals( GRAAMEdgeType.EXPLICIT_DATA_DEP ) )
            return "Use the output of \"" + fromNode.getTitle() + "\" should not be used in " + "\"" + toNode.getTitle() + "\"";
        return "API \"" + fromNode.getTitle() + "\" should not be called before API " + "\"" + toNode.getTitle() + "\"" ;
    }
}
