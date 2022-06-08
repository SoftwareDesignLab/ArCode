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

package edu.rit.se.design.arcode.fspec2code.graam;

import edu.rit.se.design.arcode.fspec2code.AbstractNodeCodeGenerator;
import edu.rit.se.design.arcode.fspec2code.ClassHierarchyUtil;
import edu.rit.se.design.arcode.fspec2code.CodeGenerationException;
import edu.rit.se.design.arcode.fspec2code.SymbolTable;
import edu.rit.se.design.arcode.fspecminer.graam.FrameworkRelatedNode;
import edu.rit.se.design.arcode.fspecminer.graam.GRAAM;
import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraphNode;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public abstract class GRAAMNodeCodeGenerator<T extends DirectedGraphNode> extends AbstractNodeCodeGenerator<GRAAM, DirectedGraphNode, T> {

/*    @Override
    public String generateCode(GRAAM directedGraph, T directedGraphNode, SymbolTable symbolTable, ClassHierarchyUtil classHierarchyUtil) throws CodeGenerationException {
        return null;
    }*/

    protected String findDirectedGraphNodeClassName(DirectedGraphNode candidateGRAAMNode) {
        String candidateGRAAMNodeClass = null;
        FrameworkRelatedNode frameworkRelatedNode = (FrameworkRelatedNode) candidateGRAAMNode;
        if (frameworkRelatedNode.isInitNode())
            candidateGRAAMNodeClass = frameworkRelatedNode.getFrameworkRelatedClass();
        else if (frameworkRelatedNode.isNormalMethodCall())
            candidateGRAAMNodeClass = frameworkRelatedNode.getReturnType();

        if (!candidateGRAAMNodeClass.startsWith("L"))
            candidateGRAAMNodeClass = "L" + candidateGRAAMNodeClass;
        return candidateGRAAMNodeClass;
    }

    @Override
    protected boolean definesSymbol(T directedGraphNode) {
        FrameworkRelatedNode frameworkRelatedNode = (FrameworkRelatedNode) directedGraphNode;
        return frameworkRelatedNode.isInitNode() ||
                (frameworkRelatedNode.isNormalMethodCall() && !frameworkRelatedNode.getReturnType().equals("V") );
    }

    protected boolean considerDirectedGraphNodeForArgumentMap(DirectedGraphNode node) {
        return node instanceof FrameworkRelatedNode;
    }

}
