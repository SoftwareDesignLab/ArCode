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

package edu.rit.se.design.arcode.fspecminer.analysis;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.graph.Graph;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public abstract class CallGraphSlicer {
    Set<Statement> findGraphRoots(Graph<Statement> graph ){
        return StreamSupport.stream( graph.spliterator(), false ).
                filter( statement -> graph.getPredNodeCount(statement) == 0 ).collect(Collectors.toSet());
    }

    void removeNonFrameworkNodes(Graph<Statement> toBePrunedGraph, Set<Statement> frameworkNodes){
        Queue<Statement> queue = new LinkedList<>();
        Set<Statement> toBeAddedToQueueNodes = new HashSet<>( findGraphRoots( toBePrunedGraph ) );
        queue.addAll( toBeAddedToQueueNodes );
        Set<Statement> addedToQueueNodes = new HashSet<>( toBeAddedToQueueNodes );

        while (!queue.isEmpty()){
            Statement top = queue.poll();
            toBeAddedToQueueNodes.clear();
            toBePrunedGraph.getSuccNodes( top ).forEachRemaining( succ -> toBeAddedToQueueNodes.add( succ ) );
            toBeAddedToQueueNodes.removeAll( addedToQueueNodes );
            queue.addAll( toBeAddedToQueueNodes );
            addedToQueueNodes.addAll( toBeAddedToQueueNodes );
            if( !frameworkNodes.contains( top ) ) {
                removeNodeAndFixEdges(toBePrunedGraph, top);
            }
        }
    }

    void removeNodeAndFixEdges(Graph<Statement> graph, Statement node ){
        graph.getPredNodes( node ).forEachRemaining( predNode -> {
            graph.getSuccNodes( node ).forEachRemaining( succNode -> {
                if( graph.hasEdge(predNode, node) && !graph.hasEdge( predNode, succNode ) )
                    graph.addEdge( predNode, succNode );

                if( graph.hasEdge(node, succNode) && !graph.hasEdge( predNode, succNode ) )
                    graph.addEdge( predNode, succNode );

            } );
        } );

        graph.removeNodeAndEdges(node);
    }

    boolean toBeExploredCGNode(CGNode cgNode){
        // TODO: If we ignore non-source or non-application nodes, we miss lambda nodes (primordial).
        //  So we need to find a way to visit lambda nodes yet avoid unnecessary primordial nodes.

        if( cgNode.getIR() == null ||
                (!cgNode.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application)  &&
                        !cgNode.getMethod().getDeclaringClass().getClassLoader().getName().toString().equals("Source") &&
                        !cgNode.getMethod().isWalaSynthetic() &&
                        !cgNode.getMethod().isSynthetic() &&
                        !cgNode.getMethod().getSelector().toString().equals("forEach(Ljava/util/function/Consumer;)V") &&
                        !cgNode.getMethod().getSelector().toString().equals("accept(Ljava/lang/Object;)V")
                )
        )
            return false;
        return true;
    }

}
