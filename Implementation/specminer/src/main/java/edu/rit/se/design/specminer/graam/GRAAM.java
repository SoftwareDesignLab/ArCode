package edu.rit.se.design.specminer.graam;

//import com.ibm.wala.util.graph.labeled.SlowSparseNumberedLabeledGraph;
import edu.rit.se.design.specminer.analysis.ProjectInfo;
import edu.rit.se.design.specminer.util.graph.DirectedGraph;
import edu.rit.se.design.specminer.util.graph.DirectedGraphNode;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class GRAAM extends DirectedGraph<DirectedGraphNode, GRAAMEdgeType, GRAAMEdgeInfo> {
    PrimaryAPIUsageGraph primaryAPIUsageGraph;
    static GRAAMEdgeInfo graamEdgeInfo = new GRAAMEdgeInfo();
    public GRAAM( PrimaryAPIUsageGraph primaryAPIUsageGraph ) {
//        super(GRAAMEdgeType.CONSTRAINT_ORDER);
        this.primaryAPIUsageGraph = primaryAPIUsageGraph;
    }

    public NonFrameworkBoundaryNode getStartNode(){
        List<DirectedGraphNode> startNodes = StreamSupport.stream(  spliterator(), false ).filter(graphNode ->
                graphNode instanceof NonFrameworkBoundaryNode && ((NonFrameworkBoundaryNode) graphNode).type.equals( NonFrameworkBoundaryNode.GraphBoundaryNodeType.START_NODE ) )
                .collect(Collectors.toList());
        return (NonFrameworkBoundaryNode)startNodes.get(0);
    }

    public NonFrameworkBoundaryNode getEndNode(){
        List<DirectedGraphNode> endNodes = StreamSupport.stream(  spliterator(), false ).filter(graphNode ->
                graphNode instanceof NonFrameworkBoundaryNode && ((NonFrameworkBoundaryNode) graphNode).type.equals( NonFrameworkBoundaryNode.GraphBoundaryNodeType.END_NODE ) )
                .collect(Collectors.toList());
        return (NonFrameworkBoundaryNode)endNodes.get(0);
    }

    ProjectInfo getProjectInfo(){
        return primaryAPIUsageGraph.getProjectInfo();
    }

    @Override
    protected GRAAMEdgeType getDefaultEdgeType() {
        return GRAAMEdgeType.EXPLICIT_DATA_DEP;
    }

    @Override
    protected GRAAMEdgeInfo getDefaultEdgeInfo(GRAAMEdgeType edgeType) {
        return graamEdgeInfo;
    }

    @Override
    public String getTitle() {
        String[] splitedPath = getProjectInfo().getPath().split( "/" );
        return splitedPath[ splitedPath.length - 1 ] ;
    }

    public String getProjectPath(){
        return getProjectInfo().getPath();
    }

}
