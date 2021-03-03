package edu.rit.se.design.arcode.fspecminer.graam;

import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraph;
import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraphNode;
import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraphVisualizer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class GRAAMVisualizer extends DirectedGraphVisualizer<DirectedGraphNode, GRAAMEdgeType, GRAAMEdgeInfo> {
    public GRAAMVisualizer(DirectedGraph<DirectedGraphNode, GRAAMEdgeType, GRAAMEdgeInfo> directedGraph) {
        super(directedGraph);
    }

    @Override
    protected String getNodeTitle(DirectedGraphNode graphNode) {
        String simpleTitle = graphNode.getTitle();
        if( graphNode instanceof FrameworkRelatedNode )
            simpleTitle = simpleTitle.replaceAll("L((([a-z]|[A-Z]|[0-9])*\\/)+)+","").replaceAll("((([a-z]|[A-Z]|[0-9])*\\/)+)+","");
        return simpleTitle;
    }

    @Override
    protected List<GRAAMEdgeType> getEdgeTypes() {
        List<GRAAMEdgeType> edgeTypes = new ArrayList<>();
        edgeTypes.add( GRAAMEdgeType.EXPLICIT_DATA_DEP);
        edgeTypes.add( GRAAMEdgeType.IMPLICIT_DATA_DEP);
        return edgeTypes;
    }

    protected String getEdgeColor( GRAAMEdgeType primaryApiUsageGraphEdgeType){
        if( primaryApiUsageGraphEdgeType.equals( GRAAMEdgeType.EXPLICIT_DATA_DEP) )
            return "black";
        if( primaryApiUsageGraphEdgeType.equals( GRAAMEdgeType.IMPLICIT_DATA_DEP) )
            return "green";
        return null;
    }

    protected String getEdgeStyle( GRAAMEdgeType primaryApiUsageGraphEdgeType){
        if( primaryApiUsageGraphEdgeType.equals( GRAAMEdgeType.EXPLICIT_DATA_DEP) )
            return "dotted";
        if( primaryApiUsageGraphEdgeType.equals( GRAAMEdgeType.IMPLICIT_DATA_DEP) )
            return "dotted";
        return null;
    }

    @Override
    protected String getNodeColor(DirectedGraphNode node) {
        if( node instanceof FrameworkRelatedNode) {
            if (((FrameworkRelatedNode) node).isInitNode())
                return "#CCFFCC";
            return "#CCFFFF";
        }
        if( node instanceof NonFrameworkRelatedNode)
            return  "yellow";
        return null;
    }

    @Override
    protected String getNodeShape(DirectedGraphNode node) {
        if( node instanceof FrameworkRelatedNode)
            return "box";
        if( node instanceof NonFrameworkRelatedNode)
            return  "oval";
        return null;
    }
}
