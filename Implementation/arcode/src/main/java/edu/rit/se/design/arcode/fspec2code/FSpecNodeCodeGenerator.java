package edu.rit.se.design.arcode.fspec2code;

import edu.rit.se.design.arcode.fspecminer.fspec.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public abstract class FSpecNodeCodeGenerator<T extends FSpecNode> {
    public abstract String generateCode(FSpec fSpec, T fSpecNode, SymbolTable symbolTable, ClassHierarchyUtil classHierarchyUtil) throws CodeGenerationException;

    String findFSpecNodeClass(FSpecNode candidateFSpecNode){
        String candidateFSpecNodeClass = null;
        if( candidateFSpecNode instanceof FSpecAPICallNode )
            candidateFSpecNodeClass = ((FSpecAPICallNode)candidateFSpecNode).getReturnType();
        else
        if( candidateFSpecNode instanceof FSpecAPIInstantiationNode )
            candidateFSpecNodeClass = ((FSpecAPIInstantiationNode)candidateFSpecNode).getFullClassName();

        if( !candidateFSpecNodeClass.startsWith("L") )
            candidateFSpecNodeClass = "L" + candidateFSpecNodeClass;
        return candidateFSpecNodeClass;
    }

    Map<String, FSpecNode> createArgumentsMap(List<String> arguments, Iterable<FSpecNode> candidateFSpecNodes, ClassHierarchyUtil classHierarchyUtil ){
        Map<String, FSpecNode> map = new HashMap<>();
        classHierarchyUtil.isSubclassOf("Ljava/rmi/server/UnicastRemoteObject", "Ljava/rmi/Remote");
        List<FSpecNode> candidateFSpecNodeList = StreamSupport.stream( candidateFSpecNodes.spliterator(), false ).collect(Collectors.toList());
        arguments.forEach( argument -> {
            for( int i = 0; i < candidateFSpecNodeList.size(); i++ ){
                FSpecNode candidateFSpecNode = candidateFSpecNodeList.get(i);
                if( !(candidateFSpecNode instanceof FSpecAPINode) )
                    continue;

                String candidateFSpecNodeClass = findFSpecNodeClass( candidateFSpecNode );
                if( candidateFSpecNodeClass.equals( argument ) ){
                    map.put(argument, candidateFSpecNodeList.get(i));
                    break;
                }
            }
            if( map.get( argument ) != null )
                candidateFSpecNodeList.remove(map.get(argument));

        } );

        //If there is an argument unmapped, then, if its type is object and there exists a candidateFSpecNodes, then, map them together.
        arguments.forEach( argument -> {
            if( !map.keySet().contains( argument ) ){
                for( int i = 0; i < candidateFSpecNodeList.size(); i++ ){
                    FSpecNode candidateFSpecNode = candidateFSpecNodeList.get(i);
                    if( !(candidateFSpecNode instanceof FSpecAPINode) )
                        continue;

                    String candidateFSpecNodeClass = findFSpecNodeClass(candidateFSpecNode);
                    if( classHierarchyUtil.isSubclassOf( candidateFSpecNodeClass , argument ) ){
                        map.put(argument, candidateFSpecNodeList.get(i));
                        break;
                    }
                }
                if( map.get( argument ) != null )
                    candidateFSpecNodeList.remove(map.get(argument));
            }
        } );
        return map;
    }

    String createArgumentVariable( String argumentType, SymbolTable symbolTable ) {
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

    boolean definesSymbol(FSpecNode fSpecNode ){
        return fSpecNode instanceof FSpecAPIInstantiationNode ||
                (fSpecNode instanceof FSpecAPICallNode && !((FSpecAPICallNode) fSpecNode).getReturnType().equals("V")) ;
    }

    String getTypeSimpleName(String type ){
        if( type.equals("I") )
            return "int";
        if( type.equals("Z") )
            return "boolean";

        String simpleName = type.substring(1).replaceAll("/", ".");
        return simpleName;
    }


}
