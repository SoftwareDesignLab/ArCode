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

package edu.rit.se.design.arcode.fspecminer.util.graph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public abstract class DirectedGraphVisualizer<T extends DirectedGraphNode, E extends DirectedGraphEdgeType, G extends DirectedGraphEdgeInfo> {

    protected int fontSize = 10;
    protected String fontColor = "black";
    protected String fontName = "Arial";

    protected Map<T, String> graphNodeTitleMap = new HashMap<>();
    protected Map<String, Integer> titleCounterMap = new HashMap<>();

    DirectedGraph<T, E, G> directedGraph;

    public DirectedGraphVisualizer( DirectedGraph<T, E, G> directedGraph ){
        this.directedGraph = directedGraph;
    }

    protected abstract String getNodeTitle(T graphNode );


    protected String getUniqueGraphNodeTitle(T graphNode ){
        String graphNodeTitle = graphNodeTitleMap.get( graphNode );
        if( graphNodeTitle != null )
            return graphNodeTitle;

        Integer counter = 1;
        graphNodeTitle = getNodeTitle( graphNode );
        if( titleCounterMap.containsKey( graphNodeTitle ) )
            counter = titleCounterMap.get( graphNodeTitle ) + 1;

        titleCounterMap.put( graphNodeTitle, counter );
        graphNodeTitleMap.put( graphNode, graphNodeTitle + (counter > 1 ? "_" + counter : "") );

        return graphNodeTitleMap.get( graphNode );
    }

    protected abstract List<E> getEdgeTypes();

    /** @return StringBuffer holding dot output representing G */
    public StringBuilder dotOutput() {
        StringBuilder result = new StringBuilder("digraph \"DirectedGraph\" {\n");
        result.append("graph [label=\"" + getGraphTitle(directedGraph) + "\"];");

        String fontsizeStr = "fontsize=" + fontSize;
        String fontcolorStr = (fontColor != null) ? ",fontcolor=" + fontColor : "";
        String fontnameStr = (fontName != null) ? ",fontname=" + fontName : "";

        result.append("center=true;");
        result.append(fontsizeStr);
        result.append(";node [ color=black,shape=\"box\"");
        result.append(fontsizeStr);
        result.append(fontcolorStr);
        result.append(fontnameStr);
        result.append("];edge [ color=black,");
        result.append(fontsizeStr);
        result.append(fontcolorStr);
        result.append(fontnameStr);
        result.append("]; \n");

        addNodes(result, this.directedGraph);
        addEdges(result);

        result.append("\n}");
        return result;
    }

    protected void addNodes( StringBuilder result, DirectedGraph directedGraph ){
        directedGraph.iterator().forEachRemaining((graphNode) -> {
            this.addNode((T) graphNode, result);
        });
    }

    protected void addEdges(StringBuilder result){
        this.directedGraph.iterator().forEachRemaining((fromNode) -> {
            this.getEdgeTypes().forEach((directedGraphEdgeType) -> {
                this.directedGraph.getSuccNodes(fromNode, directedGraphEdgeType).forEach((toNode) -> {
                    this.addEdge(fromNode, toNode, directedGraphEdgeType, this.directedGraph.getEdge(fromNode, toNode, directedGraphEdgeType), result);
                });
            });
        });

    }

    protected void addNode(T graphNode, StringBuilder stringBuilder ){
        stringBuilder.append( "\t" );
        stringBuilder.append( "\"" + getUniqueGraphNodeTitle( graphNode ) + "\"" );
        stringBuilder.append( "[fillcolor =\"" + getNodeColor( graphNode ) + "\", shape=\"" + getNodeShape( graphNode )
                + "\", style=\"" + getNodeStyle(graphNode) + "\", color=\"" + getNodeBorderColor(graphNode) + "\" ];" );
        stringBuilder.append( "\n" );
    }

    protected void addEdge(T fromGraphNode, T toGraphNode, E directedGraphEdgeType, G edge, StringBuilder stringBuilder ){
        String fromNodeUniqueTitle = getUniqueGraphNodeTitle( fromGraphNode);
        String toNodeUniqueTitle = getUniqueGraphNodeTitle( toGraphNode );
        String style = getEdgeStyle(directedGraphEdgeType);
        String color = getEdgeColor(directedGraphEdgeType);

        stringBuilder.append( "\t" );
        stringBuilder.append( "\"" + fromNodeUniqueTitle  +
                "\"" + " -> " + "\"" + toNodeUniqueTitle + "\"" );
        stringBuilder.append( "[color=\"" + color + "\",style=" + style + ", label=\"" + getEdgeTitle(edge) + "\" ];\n" );
    }

    protected abstract String getEdgeColor(E edgeType);

    protected abstract String getEdgeStyle( E edgeType);

    protected abstract String getNodeColor( T node );

    protected abstract String getNodeShape( T node );

    protected String getNodeStyle( T node ){
        return "rounded,filled";
    }

    protected String getNodeBorderColor(T node) {
        return "black";
    }

    protected String getEdgeTitle( G edge ){
        return edge.getTitle();
    }

    protected String getGraphTitle(DirectedGraph<T, E, G> directedGraph){
        return directedGraph.getTitle();
    }
}
