package edu.rit.se.design.arcode.fspec2code.graam;

import edu.rit.se.design.arcode.fspec2code.ClassHierarchyUtil;
import edu.rit.se.design.arcode.fspec2code.SymbolTable;
import edu.rit.se.design.arcode.fspecminer.fspec.*;
import edu.rit.se.design.arcode.fspecminer.graam.FrameworkRelatedNode;
import edu.rit.se.design.arcode.fspecminer.graam.GRAAM;
import edu.rit.se.design.arcode.fspecminer.graam.GRAAMEdgeType;
import edu.rit.se.design.arcode.fspecminer.util.graph.DirectedGraphNode;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
* @author Ali Shokri (as8308@rit.edu)
*/   
public class GraamAPICallNodeCodeGenerator extends GRAAMNodeCodeGenerator<DirectedGraphNode> {

    static Set<String> COLLECTION_CLASS_NAMES = new HashSet<>();
    static{
        COLLECTION_CLASS_NAMES.add( "Ljava/util/Set" );
        COLLECTION_CLASS_NAMES.add( "Ljava/util/List" );
        COLLECTION_CLASS_NAMES.add( "Ljava/util/HashSet" );
        COLLECTION_CLASS_NAMES.add( "Ljava/util/ArrayList" );
    }

    @Override
    public String generateCode(GRAAM graam, DirectedGraphNode directedGraphNode, SymbolTable symbolTable, ClassHierarchyUtil classHierarchyUtil) {
        StringBuilder code = new StringBuilder( "" );
        FrameworkRelatedNode graamNode = (FrameworkRelatedNode) directedGraphNode;

        // Generating the return part (e.g. Subject subject =)
        if( definesSymbol( graamNode ) ) {
            symbolTable.createSymbol(graamNode);
            String typeName = graamNode.getSimpleReturnType();
            //TODO: refactor the following lines and put them together with super.getTypeSimpleName(...) somewhere.
            if( typeName.equals("I") )
                typeName = "int";
            if( typeName.equals("Z") )
                typeName = "boolean";
            code.append ( typeName + " " + symbolTable.getSymbol( graamNode ) + " = " );
        }

        // Finding the object reference for this API call and generating the corresponding code (e.g. loginContext.( )
        List<DirectedGraphNode> availableExplicitPredecessors = StreamSupport.stream( graam.getPredNodes(
                graamNode, GRAAMEdgeType.EXPLICIT_DATA_DEP ).spliterator(), false ).
                collect(Collectors.toList());

        boolean foundReference = false;

        if(!foundReference && graamNode.isStaticMethod()) {
            code.append(graamNode.getSimpleClassName());
            foundReference = true;
        }


        if( !foundReference ){
            for (DirectedGraphNode predNode : availableExplicitPredecessors ){
                if( predNode instanceof FSpecAPIInstantiationNode &&
                        ((FSpecAPINode) predNode).getFullClassName().equals( graamNode.getFullClassName() ) &&
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
            for (DirectedGraphNode predNode : availableExplicitPredecessors ){
                if( predNode instanceof FSpecAPICallNode &&
                        ((FSpecAPICallNode) predNode).getReturnType().equals( "L" + graamNode.getFullClassName() ) &&
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
            for (DirectedGraphNode predNode : availableExplicitPredecessors) {
                if (predNode instanceof FSpecAPIInstantiationNode &&
                        ((FSpecAPINode) predNode).getFullClassName().equals("[" + graamNode.getFullClassName()) &&
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
            for (DirectedGraphNode predNode : availableExplicitPredecessors ){
                if( predNode instanceof FSpecAPICallNode &&
                        ((FSpecAPICallNode) predNode).getReturnType().equals( "[L" + graamNode.getFullClassName() ) &&
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
            for (DirectedGraphNode predNode : availableExplicitPredecessors) {
                if (predNode instanceof FSpecAPIInstantiationNode &&
                        COLLECTION_CLASS_NAMES.contains(((FSpecAPINode) predNode).getFullClassName()) &&
                        symbolTable.getSymbol(predNode) != null
                ) {
                    code.append("( (" + graamNode.getSimpleClassName() + ")" + symbolTable.getSymbol(predNode) + ".iterator().next() )");
                    foundReference = true;
                    availableExplicitPredecessors.remove(predNode);
                    break;
                }
            }
        }

        if( !foundReference ){
            for (DirectedGraphNode predNode : availableExplicitPredecessors ){
                if( predNode instanceof FSpecAPICallNode &&
                        COLLECTION_CLASS_NAMES.contains(((FSpecAPICallNode) predNode).getReturnType()) &&
                        symbolTable.getSymbol( predNode ) != null
                ) {
                    code.append("( (" + graamNode.getSimpleClassName() + ")" + symbolTable.getSymbol(predNode) + ".iterator().next() )");
                    foundReference = true;
                    availableExplicitPredecessors.remove( predNode );
                    break;
                }
            }
        }

        if(!foundReference)
//            throw new CodeGenerationException("Reference not found!");
            code.append( graamNode.getSimpleClassName() );

        code.append( "." + graamNode.getMethodName() + "(" );

        // Finding a map between arguments' type and this node's parents and generating code for arguments (e.g. ) )
        List<String> argumentTypes = graamNode.getArgumentTypes();
        Map<String, DirectedGraphNode> argumentsMap = createArgumentsMap(argumentTypes, availableExplicitPredecessors, classHierarchyUtil);
        for ( int i = 0; i < argumentTypes.size(); i++ ) {
            String argumentType = argumentTypes.get(i);
            DirectedGraphNode correspondingFSpecNode = argumentsMap.get(argumentType);
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

        return  code.toString();    }
}