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

package edu.rit.se.design.arcode.fspec2recom;

import edu.rit.se.design.arcode.fspecminer.fspec.*;
import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraph;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class SubFSpecVisualizer extends FSpecVisualizer {
    SubFSpec subFSpec;
    public SubFSpecVisualizer(SubFSpec subFSpec) {
        super(subFSpec.getfSpec());
        this.subFSpec = subFSpec;
    }

    @Override
    protected String getNodeTitle(FSpecNode graphNode) {
/*        if( !subFSpec.containsNode( graphNode ) )
            return "-";*/
        String simpleTitle = graphNode.getTitle();
        if( graphNode instanceof FSpecAPINode)
            simpleTitle = simpleTitle.replaceAll("L((([a-z]|[A-Z]|[0-9])*\\/)+)+","").replaceAll("((([a-z]|[A-Z]|[0-9])*\\/)+)+","");
        return simpleTitle;
    }

    @Override
    protected List<FSpecEdgeType> getEdgeTypes() {
        List<FSpecEdgeType> edgeTypes = new ArrayList<>();
        edgeTypes.add( FSpecEdgeType.EXPLICIT_DATA_DEP);
        edgeTypes.add( FSpecEdgeType.IMPLICIT_DATA_DEP);
        return edgeTypes;
    }

    @Override
    protected String getEdgeColor(FSpecEdgeType edgeType) {
        if( edgeType.equals( FSpecEdgeType.EXPLICIT_DATA_DEP) )
            return "black";
        if( edgeType.equals( FSpecEdgeType.IMPLICIT_DATA_DEP) )
            return "green";
        return null;
    }

    @Override
    protected String getEdgeStyle(FSpecEdgeType edgeType) {
        if( edgeType.equals( FSpecEdgeType.EXPLICIT_DATA_DEP) )
            return "dotted";
        if( edgeType.equals( FSpecEdgeType.IMPLICIT_DATA_DEP) )
            return "dotted";
        return null;
    }

    @Override
    protected String getNodeColor(FSpecNode node) {
        if( !subFSpec.containsNode( node ) )
            return "grey";
        if( node instanceof FSpecAPICallNode)
            return "#CCFFFF";
        if( node instanceof FSpecStartNode || node instanceof FSpecEndNode )
            return  "yellow";
        if( node instanceof FSpecAPIInstantiationNode)
            if( !((FSpecAPIInstantiationNode) node).isAbstractOrInterface() && ((FSpecAPIInstantiationNode) node).isPublicConstructor() )
                return  "#CCFFCC";
            else
                return  "#FEFFEF";

        return null;
    }

    @Override
    protected String getNodeStyle(FSpecNode node) {
        if( node instanceof FSpecAPIInstantiationNode &&  ((FSpecAPIInstantiationNode) node).isAbstractOrInterface() )
            return "filled,rounded";
        return super.getNodeStyle(node);
    }

    @Override
    protected String getNodeBorderColor(FSpecNode node) {
        if( node instanceof FSpecAPIInstantiationNode &&  ((FSpecAPIInstantiationNode) node).isAbstractOrInterface() )
            return "gray";
        return super.getNodeBorderColor(node);
    }

    @Override
    protected String getNodeShape(FSpecNode node) {
        if( node instanceof FSpecAPINode )
            return "box";
        if( node instanceof FSpecNode)
            return  "oval";
        return null;
    }

    @Override
    protected void addNode(FSpecNode graphNode, StringBuilder stringBuilder) {
        if( !subFSpec.containsNode( graphNode ) )
            return;
        super.addNode(graphNode, stringBuilder);
    }

    @Override
    protected void addEdge(FSpecNode fromGraphNode, FSpecNode toGraphNode, FSpecEdgeType directedGraphEdgeType, FSpecEdge edge, StringBuilder stringBuilder) {
        if( !subFSpec.containsNode( fromGraphNode ) || !subFSpec.containsNode( toGraphNode ) )
            return;
        super.addEdge(fromGraphNode, toGraphNode, directedGraphEdgeType, edge, stringBuilder);
    }

    @Override
    protected String getEdgeTitle(FSpecEdge edge) {
        return "";
    }

    @Override
    protected String getGraphTitle(DirectedGraph<FSpecNode, FSpecEdgeType, FSpecEdge> directedGraph) {
        return "API Usage Recommendation";
    }
}
