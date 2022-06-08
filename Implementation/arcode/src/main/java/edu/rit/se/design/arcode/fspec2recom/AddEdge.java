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

import edu.rit.se.design.arcode.fspecminer.graam.GRAAMEdgeType;
import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraphNode;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */
public class AddEdge implements GraphEditOperation{
    GRAAMEdgeType edgeType;
    DirectedGraphNode fromNode;
    DirectedGraphNode toNode;

    public AddEdge(GRAAMEdgeType edgeType, DirectedGraphNode fromNode, DirectedGraphNode toNode) {
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
            return "Use the output of \"" + fromNode.getTitle() + "\" in " + "\"" + toNode.getTitle() + "\"";
        return "API \"" + fromNode.getTitle() + "\" should be called before API " + "\"" + toNode.getTitle() + "\"" ;
    }
}
