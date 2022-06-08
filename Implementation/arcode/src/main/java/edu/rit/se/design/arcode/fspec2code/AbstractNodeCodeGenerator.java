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

package edu.rit.se.design.arcode.fspec2code;

import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraph;
import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraphNode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
* @author Ali Shokri (as8308@rit.edu)
*/

// T: FSpec, GRAAM
// G: FSpecNode, DirectedGraphNode
// E: FSPecAPINode, FSpecInitNode, ...
public abstract class AbstractNodeCodeGenerator<T extends DirectedGraph, G extends DirectedGraphNode, E extends DirectedGraphNode> {
    public abstract String generateCode(T directedGraph, E directedGraphNode, SymbolTable symbolTable, ClassHierarchyUtil classHierarchyUtil) throws CodeGenerationException;
    protected abstract String findDirectedGraphNodeClassName(G node);
    protected abstract boolean definesSymbol(E directedGraphNode );
    protected abstract boolean considerDirectedGraphNodeForArgumentMap( G node );

    protected String getTypeSimpleName(String type ){
        if( type.equals("I") )
            return "int";
        if( type.equals("Z") )
            return "boolean";

        String simpleName = type.substring(1).replaceAll("/", ".");
        return simpleName;
    }


    protected String createArgumentVariable( String argumentType, SymbolTable symbolTable ) {
        try {
            argumentType = getTypeSimpleName( argumentType );
//            Object object = Class.forName(argumentType).newInstance();
            String variableName = symbolTable.createSymbol(argumentType);
        /*if( argumentType.replaceAll("/", ".").equals( "L" + "".getClass().getName() ) )
            return "\"to_be_provided_by_user\"";*/
            return variableName;
        }catch (Exception e){
            return null;
        }
    }

    protected Map<String, G> createArgumentsMap(List<String> arguments, Iterable<G> candidateDirectedGraphNodes, ClassHierarchyUtil classHierarchyUtil ){
        Map<String, G> map = new HashMap<>();
//        classHierarchyUtil.isSubclassOf("Ljava/rmi/server/UnicastRemoteObject", "Ljava/rmi/Remote");
        List<G> candidateGraphNodeList = StreamSupport.stream( candidateDirectedGraphNodes.spliterator(), false ).collect(Collectors.toList());
        arguments.forEach( argument -> {
            for( int i = 0; i < candidateGraphNodeList.size(); i++ ){
                G candidateDirectedGraphNode = candidateGraphNodeList.get(i);
                if( !considerDirectedGraphNodeForArgumentMap(candidateDirectedGraphNode)  )
                    continue;

                String candidateDirectedGraphNodeClass = findDirectedGraphNodeClassName( candidateDirectedGraphNode );
                if( candidateDirectedGraphNodeClass.equals( argument ) ){
                    map.put(argument, candidateGraphNodeList.get(i));
                    break;
                }
            }
            if( map.get( argument ) != null )
                candidateGraphNodeList.remove(map.get(argument));

        } );

        //If there is an argument unmapped, then, if its type is object and there exists a candidateNodes, then, map them together.
        arguments.forEach( argument -> {
            if( !map.keySet().contains( argument ) ){
                for( int i = 0; i < candidateGraphNodeList.size(); i++ ){
                    G candidateDirectedGraphNode = candidateGraphNodeList.get(i);
                    if( !considerDirectedGraphNodeForArgumentMap(candidateDirectedGraphNode) )
                        continue;

                    String candidateDirectedGraphNodeClass = findDirectedGraphNodeClassName(candidateDirectedGraphNode);
                    if( classHierarchyUtil.isSubclassOf( candidateDirectedGraphNodeClass , argument ) ){
                        map.put(argument, candidateGraphNodeList.get(i));
                        break;
                    }
                }
                if( map.get( argument ) != null )
                    candidateGraphNodeList.remove(map.get(argument));
            }
        } );
        return map;
    }

}
