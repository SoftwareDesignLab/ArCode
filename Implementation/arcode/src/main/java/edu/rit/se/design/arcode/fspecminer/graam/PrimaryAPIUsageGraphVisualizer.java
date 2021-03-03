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

import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraph;
import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraphNode;
import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraphVisualizer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class PrimaryAPIUsageGraphVisualizer extends DirectedGraphVisualizer<DirectedGraphNode, PrimaryAPIUsageGraphEdgeType, PrimaryAPIUsageGraphEdgeInfo> {
    public PrimaryAPIUsageGraphVisualizer(DirectedGraph directedGraph) {
        super(directedGraph);
    }

    @Override
    protected String getNodeTitle(DirectedGraphNode graphNode) {
        String nodeTitle = graphNode.getTitle();
        if( graphNode instanceof FrameworkRelatedNode )
            nodeTitle += ": " + ((FrameworkRelatedNode) graphNode).getOriginClass() + "." +
                    ((FrameworkRelatedNode) graphNode).getOriginMethod() + "[line " + ((FrameworkRelatedNode) graphNode).getOriginalLineNumber() + "]";
/*        if( graphNode instanceof FrameworkRelatedNode )
        simpleTitle = simpleTitle.replaceAll("L((([a-z]|[A-Z]|[0-9])*\\/)+)+","");*/
        return nodeTitle;
    }

    @Override
    protected List<PrimaryAPIUsageGraphEdgeType> getEdgeTypes() {
        List<PrimaryAPIUsageGraphEdgeType> edgeTypes = new ArrayList<>();
        edgeTypes.add( PrimaryAPIUsageGraphEdgeType.SEQUENCE_DEPENDENCY );
        edgeTypes.add( PrimaryAPIUsageGraphEdgeType.DATA_DEPENDENCY );
        return edgeTypes;
    }

    @Override
    protected String getEdgeColor(PrimaryAPIUsageGraphEdgeType edgeType) {
        if( edgeType.equals( PrimaryAPIUsageGraphEdgeType.SEQUENCE_DEPENDENCY ) )
            return "red";
        if( edgeType.equals( PrimaryAPIUsageGraphEdgeType.DATA_DEPENDENCY ) )
            return "purple";
        return null;
    }

    @Override
    protected String getEdgeStyle(PrimaryAPIUsageGraphEdgeType edgeType) {
        if( edgeType.equals( PrimaryAPIUsageGraphEdgeType.SEQUENCE_DEPENDENCY ) )
            return "solid";
        if( edgeType.equals( PrimaryAPIUsageGraphEdgeType.DATA_DEPENDENCY ) )
            return "dashed";
        return null;
    }

    @Override
    protected String getNodeColor(DirectedGraphNode node) {
        if( node instanceof FrameworkRelatedNode)
            return "#00e2f8";
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
