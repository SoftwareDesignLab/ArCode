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
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public abstract class AbstractCodeGenerator<T extends DirectedGraph, E extends DirectedGraphNode> {
    static final String TAB = "  ";
    protected ClassHierarchyUtil classHierarchyUtil;

    public AbstractCodeGenerator(ClassHierarchyUtil classHierarchyUtil ){
        this.classHierarchyUtil = classHierarchyUtil;
    }

    public StringBuilder generateCode(String classComments, String methodName, T directedGraph, E graphNode) throws CodeGenerationException {
        SymbolTable symbolTable = new SymbolTable();
        StringBuilder classSignature = generateClassSignature( "", classComments );
        StringBuilder methodBody = generateMethodBody(TAB, directedGraph, graphNode, symbolTable);
        StringBuilder methodSignature = generateMethodSignature("", classComments,/*"_" + (++generatedCodeCounter)*/ methodName, symbolTable );

        StringBuilder generatedCode = new StringBuilder();
//        generatedCode.append( classSignature );
        generatedCode.append( methodSignature ).append( methodBody );
        generatedCode.append( "}\n" );

        return generatedCode;
    }

    StringBuilder generateClassSignature( String indention, String classComments ){
        StringBuilder classSignature = new StringBuilder();
        classSignature.append( indention + "/*" + classComments + "*/\n");
        classSignature.append( indention + "public class JaasPractice{\n");
        return classSignature;
    }

    StringBuilder generateMethodSignature( String indention, String methodComment, String methodName, SymbolTable symbolTable){
        StringBuilder methodSignature = new StringBuilder("/*" + methodComment + "*/\n" + indention +
                "public void " + methodName + "(");
        StringBuilder arguments = new StringBuilder();
        symbolTable.getAllObjects().forEach( o -> {
            Pair<String, String> classNameVarName = getClassNameVarName( o, symbolTable );
            String className = classNameVarName.getLeft();
            String varName = classNameVarName.getRight();
            if( className != null ){
                if( arguments.length() > 0 )
                    arguments.append( ", " );
                arguments.append( className + " " + varName );
            }
        } );

        methodSignature.append( arguments );
        methodSignature.append("){\n");
        return methodSignature ;
    }

    abstract protected Pair getClassNameVarName(Object object, SymbolTable symbolTable );

    StringBuilder generateMethodBody( String indention, T directedGraph, E graphNode, SymbolTable symbolTable) throws CodeGenerationException {
        StringBuilder generatedCode = new StringBuilder( indention + "try{\n");

        Set<E> generatedDirectedGraphNodes = new HashSet<>();
        generateCode( generatedCode, indention + TAB, directedGraph, graphNode, symbolTable, generatedDirectedGraphNodes );
        generatedCode.append( indention + "} catch(Exception e){\n" + indention + TAB + "e.printStackTrace();\n" + indention + "}\n" );

        return generatedCode;
    }

    void generateCode( StringBuilder generatedCode, String indention, T directedGraph, E graphNode, SymbolTable symbolTable, Set<E> generatedDirectedGraphNodes ) throws CodeGenerationException {
        if( generatedDirectedGraphNodes.contains( graphNode ) )
            return;
        for( Object graphNodePredObj : directedGraph.getPredNodes( graphNode ) ){
            E graphNodePred = (E) graphNodePredObj;
            generateCode( generatedCode, indention, directedGraph, graphNodePred, symbolTable, generatedDirectedGraphNodes );
        }
        String line = "";

        AbstractNodeCodeGenerator nodeCodeGenerator = NodeCodeGeneratorFactory.getNodeCodeGenerator( graphNode );

        line += nodeCodeGenerator.generateCode( directedGraph, graphNode, symbolTable, classHierarchyUtil );

        generatedCode.append(  (line.length() > 0 ? indention : "" ) + line + (line.length() > 0 ? ";\n" : "" ) );
        generatedDirectedGraphNodes.add( graphNode );
    }


}
