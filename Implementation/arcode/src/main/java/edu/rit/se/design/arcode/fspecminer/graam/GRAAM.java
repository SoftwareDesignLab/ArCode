/*
 * Copyright (c) 2021 - Present. Rochester Institute of Technology
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.rit.se.design.arcode.fspecminer.graam;

//import com.ibm.wala.util.graph.labeled.SlowSparseNumberedLabeledGraph;
import edu.rit.se.design.arcode.fspecminer.analysis.ProjectInfo;
import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraph;
import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraphNode;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;
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
        String[] splitedPath = getProjectInfo().getPath().split( Pattern.quote(File.separator) );
        return splitedPath[ splitedPath.length - 1 ] ;
    }

    public String getProjectPath(){
        return getProjectInfo().getPath();
    }

}
