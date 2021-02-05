package edu.rit.se.design.arcode.fspec2code;

import edu.rit.se.design.arcode.fspecminer.fspec.*;

import java.util.*;


/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class CodeGenerator {
    static int generatedCodeCounter = 0;
    ClassHierarchyUtil classHierarchyUtil;

    public CodeGenerator( ClassHierarchyUtil classHierarchyUtil ){
        this.classHierarchyUtil = classHierarchyUtil;
    }

    public StringBuilder generateCode(String classComments, FSpec fSpec, FSpecEndNode fSpecEndNode) throws CodeGenerationException {
        SymbolTable symbolTable = new SymbolTable();
        StringBuilder classSignature = generateClassSignature( "", classComments );
        StringBuilder methodBody = generateMethodBody("\t\t", fSpec, fSpecEndNode, symbolTable);
        StringBuilder methodSignature = generateMethodSignature("\t", classComments,"_" + (++generatedCodeCounter), symbolTable );

        StringBuilder generatedCode = new StringBuilder();
//        generatedCode.append( classSignature );
        generatedCode.append( methodSignature ).append( methodBody );
        generatedCode.append( "\t}\n" );
//        generatedCode.append( "}\n" );

        return generatedCode;
    }

    StringBuilder generateClassSignature( String indention, String classComments ){
        StringBuilder classSignature = new StringBuilder();
        classSignature.append( indention + "/*" + classComments + "*/\n");
        classSignature.append( indention + "public class JaasPractice{\n");
        return classSignature;
    }

    StringBuilder generateMethodSignature( String indention, String methodComment, String methodNameSuffix, SymbolTable symbolTable){
        StringBuilder methodSignature = new StringBuilder("/*" + methodComment + "*/\n" + indention +
                "public void jaasAPIUsage" + methodNameSuffix + "(");
        StringBuilder arguments = new StringBuilder();
        symbolTable.getAllObjects().forEach( o -> {
            String className = null;
            String varName = null;
            if( o instanceof FSpecNode ){
                if( o instanceof FSpecAPIInstantiationNode &&
                        ( ((FSpecAPIInstantiationNode) o).isAbstractOrInterface() || !((FSpecAPIInstantiationNode) o).isPublicConstructor() )){
                    className = ((FSpecAPIInstantiationNode) o).getSimpleClassName();
                    varName = symbolTable.getSymbol( o );
                }
            }
            else if( o instanceof SymbolTable.TypeHolder ){
                className = ((SymbolTable.TypeHolder) o).getSimpleName();
                varName = symbolTable.getSymbol( o );
            }
            if( className != null ){
                if( arguments.length() > 0 )
                    arguments.append( ", " );
                arguments.append( className + " " + varName );

            }
        } );

/*
        StreamSupport.stream( fSpec.spliterator(), false ).filter( fSpecNode -> fSpecNode instanceof FSpecAPIInstantiationNode && ((FSpecAPIInstantiationNode) fSpecNode).isAbstractOrInterface() && symbolTable.getSymbol(fSpecNode )!= null).
                collect(Collectors.toList()).forEach( fSpecNode -> {
                FSpecAPIInstantiationNode fSpecAPIInstantiationNode = (FSpecAPIInstantiationNode)fSpecNode;
                    String APIClass = fSpecAPIInstantiationNode.getSimpleClassName();
                    String APIVarName = symbolTable.getSymbol( fSpecAPIInstantiationNode );
                    if( arguments.length() > 0 )
                        arguments.append( ", " );
                    arguments.append( APIClass + " " + APIVarName );
        } );
*/
        methodSignature.append( arguments );
        methodSignature.append("){\n");
        return methodSignature ;
    }

    StringBuilder generateMethodBody( String indention, FSpec fSpec, FSpecEndNode fSpecEndNode, SymbolTable symbolTable) throws CodeGenerationException {
        StringBuilder generatedCode = new StringBuilder( indention + "try{\n");

        Set<FSpecNode> generatedFSpecNodes = new HashSet<>();
        generateCode( generatedCode, indention + "\t", fSpec, fSpecEndNode, symbolTable, generatedFSpecNodes );
        generatedCode.append( indention + "} catch(Exception e){\n" + indention + "\t" + "e.printStackTrace();\n" + indention + "}\n" );

        return generatedCode;
    }

    void generateCode( StringBuilder generatedCode, String indention, FSpec fSpec, FSpecNode fSpecNode, SymbolTable symbolTable, Set<FSpecNode> generatedFSpecNodes ) throws CodeGenerationException {
        if( generatedFSpecNodes.contains( fSpecNode ) )
            return;
        for( FSpecNode fSpecNodePred : fSpec.getPredNodes( fSpecNode ) ){
            generateCode( generatedCode, indention, fSpec, fSpecNodePred, symbolTable, generatedFSpecNodes );
        }
        String line = "";

        FSpecNodeCodeGenerator fSpecNodeCodeGenerator = FSpecNodeCodeGeneratorFactory.getFSpecNodeCodeGenerator( fSpecNode );

        line += fSpecNodeCodeGenerator.generateCode( fSpec, fSpecNode, symbolTable, classHierarchyUtil );

        generatedCode.append(  (line.length() > 0 ? indention : "" ) + line + (line.length() > 0 ? ";\n" : "" ) );
        generatedFSpecNodes.add( fSpecNode );
    }

/*    String fspecNodeToCode( FSpec fSpec, FSpecNode fSpecNode, SymbolTable symbolTable ){
        if( fSpecNode instanceof FSpecEndNode || fSpecNode instanceof FSpecStartNode)
            return "";
        if( fSpecNode instanceof FSpecAPIInstantiationNode ){
            return fspecAPIInstantiationNodeToCode( fSpec, (FSpecAPIInstantiationNode) fSpecNode, symbolTable );
        }
        if( fSpecNode instanceof FSpecAPICallNode ){
            return fspecAPICallNodeToCode( fSpec, (FSpecAPICallNode) fSpecNode, symbolTable );
        }
        return null;
    }

    String fspecAPIInstantiationNodeToCode( FSpec fSpec, FSpecAPIInstantiationNode fSpecNode, SymbolTable symbolTable ){
        StringBuilder code = new StringBuilder( "new " + fSpecNode.getClassName() + " (" );

        List<String> arguments = extractArguments( fSpecNode );
        Map<String, FSpecNode> argumentsMap = createArgumentsMap(arguments, fSpec.getPredNodes( fSpecNode ));
        for ( int i = 0; i < arguments.size(); i++ ) {
            String argument = arguments.get(i);
            FSpecNode correspondingFSpecNode = argumentsMap.get(argument);
            String variable = null;
            if( correspondingFSpecNode != null )
                variable = symbolTable.getSymbol( correspondingFSpecNode );
            code.append((i > 0 ? ", " : "") +  variable );
        }
        code.append( ")" );
        return  code.toString();
    }

    List<String> extractArguments( FSpecAPINode fSpecAPINode ){
        return fSpecAPINode.getArgumentTypes();
    }

    Map<String, FSpecNode> createArgumentsMap( List<String> arguments, Iterable<FSpecNode> candidateFSpecNodes ){
        Map<String, FSpecNode> map = new HashMap<>();
        List<FSpecNode> candidateFSpecNodeList = StreamSupport.stream( candidateFSpecNodes.spliterator(), false ).collect(Collectors.toList());
        arguments.forEach( argument -> {
            for( int i = 0; i < candidateFSpecNodeList.size(); i++ ){
                FSpecNode candidateFSpecNode = candidateFSpecNodeList.get(i);
                if( !(candidateFSpecNode instanceof FSpecAPINode) )
                    continue;
                String candidateFSpecNodeClass = ((FSpecAPINode)candidateFSpecNode).getClassName();
                if( !candidateFSpecNodeClass.startsWith("L") )
                    candidateFSpecNodeClass = "L" + candidateFSpecNodeClass;
                if( candidateFSpecNodeClass.equals( argument ) ){
                    map.put(argument, candidateFSpecNodeList.get(i));
                    break;
                }
            }
            if( map.get( argument ) != null )
                candidateFSpecNodeList.remove(map.get(argument));

        } );
        return map;
    }

    String fspecAPICallNodeToCode( FSpec fSpec, FSpecAPICallNode fSpecNode, SymbolTable symbolTable ){
        StringBuilder code = new StringBuilder( "" );
        fSpec.getPredNodes( fSpecNode ).forEach( predNode -> {
            if( predNode instanceof FSpecAPINode &&
                    ((FSpecAPINode) predNode).getClassName().equals( fSpecNode.getClassName() ) &&
                    symbolTable.getSymbol( predNode ) != null
            )
                code.append( symbolTable.getSymbol( predNode ));
        } );
        code.append( "." + fSpecNode.getMethodName() + "(" );

        List<String> arguments = extractArguments( fSpecNode );
        Map<String, FSpecNode> argumentsMap = createArgumentsMap(arguments, fSpec.getPredNodes( fSpecNode ));
        for ( int i = 0; i < arguments.size(); i++ ) {
            String argument = arguments.get(i);
            FSpecNode correspondingFSpecNode = argumentsMap.get(argument);
            String variable = null;
            if( correspondingFSpecNode != null )
                variable = symbolTable.getSymbol( correspondingFSpecNode );
            code.append((i > 0 ? ", " : "") +  variable );
        }
        code.append( ")" );

        return  code.toString();

    }*/


}
