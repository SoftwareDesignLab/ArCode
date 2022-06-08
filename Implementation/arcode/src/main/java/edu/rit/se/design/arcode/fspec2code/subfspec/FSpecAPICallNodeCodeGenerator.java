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

package edu.rit.se.design.arcode.fspec2code.subfspec;

import edu.rit.se.design.arcode.fspec2code.ClassHierarchyUtil;
import edu.rit.se.design.arcode.fspec2code.CodeGenerationException;
import edu.rit.se.design.arcode.fspec2code.SymbolTable;
import edu.rit.se.design.arcode.fspecminer.fspec.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class FSpecAPICallNodeCodeGenerator extends FSpecNodeCodeGenerator<FSpecAPICallNode>{
    static Set<String> COLLECTION_CLASS_NAMES = new HashSet<>();
    static{
        COLLECTION_CLASS_NAMES.add( "Ljava/util/Set" );
        COLLECTION_CLASS_NAMES.add( "Ljava/util/List" );
        COLLECTION_CLASS_NAMES.add( "Ljava/util/HashSet" );
        COLLECTION_CLASS_NAMES.add( "Ljava/util/ArrayList" );
    }

    @Override
    // Generates the code related to an API call (e.g. Subject subject = loginContext.getSubject())
    public String generateCode(FSpec fSpec, FSpecAPICallNode fSpecAPICallNode, SymbolTable symbolTable, ClassHierarchyUtil classHierarchyUtil) throws CodeGenerationException {
        StringBuilder code = new StringBuilder( "" );

        // Generating the return part (e.g. Subject subject =)
        if( definesSymbol( fSpecAPICallNode ) ) {
            symbolTable.createSymbol(fSpecAPICallNode);
            String typeName = fSpecAPICallNode.getSimpleReturnType();
            //TODO: refactor the following lines and put them together with super.getTypeSimpleName(...) somewhere.
            if( typeName.equals("I") )
                typeName = "int";
            if( typeName.equals("Z") )
                typeName = "boolean";
            code.append ( typeName + " " + symbolTable.getSymbol( fSpecAPICallNode ) + " = " );
        }

        // Finding the object reference for this API call and generating the corresponding code (e.g. loginContext.( )
        List<FSpecNode> availableExplicitPredecessors = StreamSupport.stream( fSpec.getPredNodes(
                fSpecAPICallNode, FSpecEdgeType.EXPLICIT_DATA_DEP ).spliterator(), false ).
                collect(Collectors.toList());

        boolean foundReference = false;

        if(!foundReference && fSpecAPICallNode.isStaticMethod()) {
            code.append(fSpecAPICallNode.getSimpleClassName());
            foundReference = true;
        }


        if( !foundReference ){
            for (FSpecNode predNode : availableExplicitPredecessors ){
                if( predNode instanceof FSpecAPIInstantiationNode &&
                        ((FSpecAPINode) predNode).getFullClassName().equals( fSpecAPICallNode.getFullClassName() ) &&
                        symbolTable.getSymbol( predNode ) != null
                ) {
                    code.append(symbolTable.getSymbol(predNode));
                    foundReference = true;
                    availableExplicitPredecessors.remove( predNode );
                    break;
                }
            }
        }

        if( !foundReference ){
            for (FSpecNode predNode : availableExplicitPredecessors ){
                if( predNode instanceof FSpecAPICallNode &&
                        ((FSpecAPICallNode) predNode).getReturnType().equals( "L" + fSpecAPICallNode.getFullClassName() ) &&
                        symbolTable.getSymbol( predNode ) != null
                ) {
                    code.append(symbolTable.getSymbol(predNode));
                    foundReference = true;
                    availableExplicitPredecessors.remove( predNode );
                    break;
                }
            }
        }

        if( !foundReference ) {
            for (FSpecNode predNode : availableExplicitPredecessors) {
                if (predNode instanceof FSpecAPIInstantiationNode &&
                        ((FSpecAPINode) predNode).getFullClassName().equals("[" + fSpecAPICallNode.getFullClassName()) &&
                        symbolTable.getSymbol(predNode) != null
                ) {
                    code.append(symbolTable.getSymbol(predNode) + "[0]");
                    foundReference = true;
                    availableExplicitPredecessors.remove(predNode);
                    break;
                }
            }
        }

        if( !foundReference ){
            for (FSpecNode predNode : availableExplicitPredecessors ){
                if( predNode instanceof FSpecAPICallNode &&
                        ((FSpecAPICallNode) predNode).getReturnType().equals( "[L" + fSpecAPICallNode.getFullClassName() ) &&
                        symbolTable.getSymbol( predNode ) != null
                ) {
                    code.append(symbolTable.getSymbol(predNode) + "[0]");
                    foundReference = true;
                    availableExplicitPredecessors.remove( predNode );
                    break;
                }
            }
        }

        if( !foundReference ) {
            for (FSpecNode predNode : availableExplicitPredecessors) {
                if (predNode instanceof FSpecAPIInstantiationNode &&
                        COLLECTION_CLASS_NAMES.contains(((FSpecAPINode) predNode).getFullClassName()) &&
                        symbolTable.getSymbol(predNode) != null
                ) {
                    code.append("( (" + fSpecAPICallNode.getSimpleClassName() + ")" + symbolTable.getSymbol(predNode) + ".iterator().next() )");
                    foundReference = true;
                    availableExplicitPredecessors.remove(predNode);
                    break;
                }
            }
        }

        if( !foundReference ){
            for (FSpecNode predNode : availableExplicitPredecessors ){
                if( predNode instanceof FSpecAPICallNode &&
                        COLLECTION_CLASS_NAMES.contains(((FSpecAPICallNode) predNode).getReturnType()) &&
                        symbolTable.getSymbol( predNode ) != null
                ) {
                    code.append("( (" + fSpecAPICallNode.getSimpleClassName() + ")" + symbolTable.getSymbol(predNode) + ".iterator().next() )");
                    foundReference = true;
                    availableExplicitPredecessors.remove( predNode );
                    break;
                }
            }
        }

        if(!foundReference)
//            throw new CodeGenerationException("Reference not found!");
            code.append( fSpecAPICallNode.getSimpleClassName() );

        code.append( "." + fSpecAPICallNode.getMethodName() + "(" );

        // Finding a map between arguments' type and this node's parents and generating code for arguments (e.g. ) )
        List<String> argumentTypes = fSpecAPICallNode.getArgumentTypes();
        Map<String, FSpecNode> argumentsMap = createArgumentsMap(argumentTypes, availableExplicitPredecessors, classHierarchyUtil);
        for ( int i = 0; i < argumentTypes.size(); i++ ) {
            String argumentType = argumentTypes.get(i);
            FSpecNode correspondingFSpecNode = argumentsMap.get(argumentType);
            String variable = null;
            if( correspondingFSpecNode != null ) {
                variable = symbolTable.getSymbol(correspondingFSpecNode);
                String fspecNodeClass = findDirectedGraphNodeClassName( correspondingFSpecNode );
                if( !fspecNodeClass.equals( argumentType ) ){
                    variable = "(" + argumentType.replaceAll(".*/", "") + ") " + variable;
                }
            }
            else
                variable = createArgumentVariable( argumentType, symbolTable );

            code.append((i > 0 ? ", " : "") +  variable );
        }
        code.append( ")" );

        return  code.toString();
    }
}
