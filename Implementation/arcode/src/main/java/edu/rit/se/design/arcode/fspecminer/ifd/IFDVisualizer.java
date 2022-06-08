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

package edu.rit.se.design.arcode.fspecminer.ifd;

import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraphVisualizer;
import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraph;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class IFDVisualizer extends DirectedGraphVisualizer<MethodRepresentation, IFDEdgeType, IFDEdgeInfo> {
    public IFDVisualizer(DirectedGraph<MethodRepresentation, IFDEdgeType, IFDEdgeInfo> directedGraph) {
        super(directedGraph);
    }

    @Override
    protected String getNodeTitle(MethodRepresentation graphNode) {
        String simpleTitle = graphNode.getTitle();
/*        if( graphNode instanceof FrameworkRelatedNode )
        simpleTitle = simpleTitle.replaceAll("L((([a-z]|[A-Z]|[0-9])*\\/)+)+","");*/
        return simpleTitle;
    }

    @Override
    protected List<IFDEdgeType> getEdgeTypes() {
        List<IFDEdgeType> edgeTypes = new ArrayList<>();
        edgeTypes.add( IFDEdgeType.FIELD_BASE_DEPENDENCY);
        return edgeTypes;
    }

    @Override
    protected String getEdgeColor(IFDEdgeType edgeType) {
        if( edgeType.equals( IFDEdgeType.FIELD_BASE_DEPENDENCY) )
            return "black";
        return null;
    }

    @Override
    protected String getEdgeStyle(IFDEdgeType edgeType) {
        if( edgeType.equals( IFDEdgeType.FIELD_BASE_DEPENDENCY) )
            return "dotted";
        return null;
    }

    @Override
    protected String getNodeColor(MethodRepresentation node) {
        return "#E0E0E0";
    }

    @Override
    protected String getNodeShape(MethodRepresentation node) {
        return "oval";
    }


}
