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

import edu.rit.se.design.arcode.fspec2code.ClassHierarchyUtil;
import edu.rit.se.design.arcode.fspec2code.SymbolTable;
import edu.rit.se.design.arcode.fspecminer.fspec.FSpecNode;
import edu.rit.se.design.arcode.fspecminer.graam.FrameworkRelatedNode;
import edu.rit.se.design.arcode.fspecminer.graam.GRAAM;
import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraphNode;

import java.util.List;
import java.util.Map;

/**
* @author Ali Shokri (as8308@rit.edu)
*/   
public class GraamAPIInstantiationNodeCodeGenerator extends GRAAMNodeCodeGenerator<DirectedGraphNode> {
    @Override
    public String generateCode(GRAAM graam, DirectedGraphNode directedGraphNode, SymbolTable symbolTable, ClassHierarchyUtil classHierarchyUtil) {
        StringBuilder code = new StringBuilder();

        FrameworkRelatedNode graamNode = (FrameworkRelatedNode) directedGraphNode;


        // Creating a variable name for the to be instantiating API (e.g. loginContext for an instantiation of LoginContext)
        if( definesSymbol( graamNode ) ) {
            symbolTable.createSymbol(graamNode);
        }

        // In the case that the API is an abstract class or an interface, or if this is a private/protected constructor,
        // then we do not instantiate it in our code. Instead,
        // we add it as an argument passed to the generated code with variable name as what we just created.
        if( graamNode.isAbstractOrInterfaceConstructorNode() || (!graamNode.isPublicMethod() && graamNode.isInitNode()) )
            return code.toString();

        // Generating the code corresponding to variable name creation (e.g. LoginContext loginContext = )
        if( definesSymbol( graamNode ) ) {
            code.append ( graamNode.getSimpleClassName() + " " + symbolTable.getSymbol( graamNode ) + " = " );
        }


        // Generating new keyword alongside with the class of the API (e.g. new LoginContext( )
        code.append( "new " + graamNode.getSimpleClassName() + "(" );

        // Finding the object reference for this API call and generating the corresponding code (e.g. loginContext.( )
        List<String> argumentTypes = graamNode.getArgumentTypes();
        Map<String, DirectedGraphNode> argumentsMap = createArgumentsMap(argumentTypes, graam.getPredNodes( graamNode ), classHierarchyUtil);
        for ( int i = 0; i < argumentTypes.size(); i++ ) {
            String argumentType = argumentTypes.get(i);
            DirectedGraphNode correspondingFSpecNode = argumentsMap.get(argumentType);
            String variable = null;
            if( correspondingFSpecNode != null )
                variable = symbolTable.getSymbol( correspondingFSpecNode );
            else {
                variable = createArgumentVariable(argumentType, symbolTable);
            }

            code.append((i > 0 ? ", " : "") +  variable );
        }
        code.append( ")" );

        // Adding the comment to the generated code in the case that this API is an abstract class or an interface and needs to be implemented by the user
        if( graamNode.isAbstractOrInterfaceConstructorNode() )
            code.append( " /*This class needs to be implemented/extended by the programmer*/" );
        return  code.toString();    }
}