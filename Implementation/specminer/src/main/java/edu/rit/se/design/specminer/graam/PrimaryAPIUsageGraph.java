package edu.rit.se.design.specminer.graam;

import edu.rit.se.design.specminer.analysis.ProjectInfo;
import edu.rit.se.design.specminer.util.graph.DirectedGraph;
import edu.rit.se.design.specminer.util.graph.DirectedGraphNode;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class PrimaryAPIUsageGraph extends DirectedGraph<DirectedGraphNode, PrimaryAPIUsageGraphEdgeType, PrimaryAPIUsageGraphEdgeInfo> {
    ProjectInfo projectInfo;
    PrimaryAPIUsageGraphEdgeType defaultLabel;
    static PrimaryAPIUsageGraphEdgeInfo primaryAPIUsageGraphEdgeInfo = new PrimaryAPIUsageGraphEdgeInfo();
    public PrimaryAPIUsageGraph(ProjectInfo projectInfo, PrimaryAPIUsageGraphEdgeType defaultLabel) {
        this.defaultLabel = defaultLabel;
        this.projectInfo = projectInfo;
    }

    public ProjectInfo getProjectInfo() {
        return projectInfo;
    }


    @Override
    protected PrimaryAPIUsageGraphEdgeType getDefaultEdgeType() {
        return defaultLabel;
    }

    @Override
    protected PrimaryAPIUsageGraphEdgeInfo getDefaultEdgeInfo(PrimaryAPIUsageGraphEdgeType edgeType) {
        return primaryAPIUsageGraphEdgeInfo;
    }

    @Override
    public String getTitle() {
        String[] splitedPath = getProjectInfo().getPath().split( "/" );
        return splitedPath[ splitedPath.length - 1 ] ;
    }
}
