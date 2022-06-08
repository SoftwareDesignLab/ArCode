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

import edu.rit.se.design.arcode.fspec2code.graam.GRAAMBoundaryNodeCodeGenerator;
import edu.rit.se.design.arcode.fspec2code.graam.GraamAPICallNodeCodeGenerator;
import edu.rit.se.design.arcode.fspec2code.graam.GraamAPIInstantiationNodeCodeGenerator;
import edu.rit.se.design.arcode.fspec2code.subfspec.FSpecAPICallNodeCodeGenerator;
import edu.rit.se.design.arcode.fspec2code.subfspec.FSpecApiInstantiationNodeCodeGenerator;
import edu.rit.se.design.arcode.fspec2code.subfspec.FSpecBoundaryNodeCodeGenerator;
import edu.rit.se.design.arcode.fspecminer.fspec.*;
import edu.rit.se.design.arcode.fspecminer.graam.FrameworkRelatedNode;
import edu.rit.se.design.arcode.fspecminer.graam.NonFrameworkBoundaryNode;
import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraphNode;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class NodeCodeGeneratorFactory {
    static Map<String, AbstractNodeCodeGenerator> nodeCodeGeneratorMap = new HashMap<>();
    static final String FSPEC_BOUNDARY_NODE = "fSpecBoundaryNode";
    static final String FSPEC_API_INSTANTIATION = "fSpecAPIInstantiation";
    static final String FSPEC_API_CALL = "fSpecAPICall";

    static final String GRAAM_BOUNDARY_NODE = "graamBoundaryNode";
    static final String GRAAM_API_INSTANTIATION = "graamAPIInstantiation";
    static final String GRAAM_API_CALL = "graamAPICall";


    public static AbstractNodeCodeGenerator getNodeCodeGenerator(DirectedGraphNode directedGraphNode){
        if( directedGraphNode instanceof FSpecEndNode || directedGraphNode instanceof FSpecStartNode) {
            if( nodeCodeGeneratorMap.get(FSPEC_BOUNDARY_NODE) == null )
                nodeCodeGeneratorMap.put(FSPEC_BOUNDARY_NODE, new FSpecBoundaryNodeCodeGenerator() );
            return nodeCodeGeneratorMap.get(FSPEC_BOUNDARY_NODE);
        }
        if( directedGraphNode instanceof FSpecAPIInstantiationNode){
            if( nodeCodeGeneratorMap.get(FSPEC_API_INSTANTIATION) == null )
                nodeCodeGeneratorMap.put(FSPEC_API_INSTANTIATION, new FSpecApiInstantiationNodeCodeGenerator() );
            return nodeCodeGeneratorMap.get(FSPEC_API_INSTANTIATION);
        }
        if( directedGraphNode instanceof FSpecAPICallNode){
            if( nodeCodeGeneratorMap.get(FSPEC_API_CALL) == null )
                nodeCodeGeneratorMap.put(FSPEC_API_CALL, new FSpecAPICallNodeCodeGenerator() );
            return nodeCodeGeneratorMap.get(FSPEC_API_CALL);
        }
        if( directedGraphNode instanceof FrameworkRelatedNode){
            FrameworkRelatedNode frameworkRelatedNode = (FrameworkRelatedNode) directedGraphNode;
            if( frameworkRelatedNode.isInitNode() ){
                if( nodeCodeGeneratorMap.get(GRAAM_API_INSTANTIATION) == null )
                    nodeCodeGeneratorMap.put(GRAAM_API_INSTANTIATION, new GraamAPIInstantiationNodeCodeGenerator() );
                return nodeCodeGeneratorMap.get(GRAAM_API_INSTANTIATION);
            }
            if( frameworkRelatedNode.isNormalMethodCall() ){
                if( nodeCodeGeneratorMap.get(GRAAM_API_CALL) == null )
                    nodeCodeGeneratorMap.put(GRAAM_API_CALL, new GraamAPICallNodeCodeGenerator() );
                return nodeCodeGeneratorMap.get(GRAAM_API_CALL);
            }
        }
        if( directedGraphNode instanceof NonFrameworkBoundaryNode){
            if( nodeCodeGeneratorMap.get(GRAAM_BOUNDARY_NODE) == null )
                nodeCodeGeneratorMap.put(GRAAM_BOUNDARY_NODE, new GRAAMBoundaryNodeCodeGenerator() );
            return nodeCodeGeneratorMap.get(GRAAM_BOUNDARY_NODE);
        }
        return null;
    }
}
