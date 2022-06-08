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
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSACFG;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.labeled.SlowSparseNumberedLabeledGraph;
import edu.rit.se.design.arcode.fspecminer.graam.PrimaryAPIUsageGraphEdgeType;

import javax.swing.plaf.nimbus.State;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Ali Shokri (as8308@rit.edu)
 *
 * This class supposed to create a graph that represents statements and their control-dependencies including branches,
 * loops, etc. This is beyond just sequence-dependency.
 *
 * Example:
 * A1 a1 = new A1();
 * boolean b = ....;
 * if(b)
 *  a1.m1();
 * else
 *  a1.m2();
 *
 * Sequence-dependency: init A1 -> a1.m1() -> a1.m2()
 * Control-dependency: initA1--> a1.m1()
 *                           |-> a1.m2()
 */

public class CallGraphControlBasedSlicer extends CallGraphSlicer{
    Statement lastVisitedStatement;
    Graph<Statement> slicedGraph;
//    Graph<Statement> slicedControlGraph;
    public Graph<Statement> sliceCallGraph(CallGraph callGraph, CGNode entrypoint, Set<Statement> relevantStatements) {
/*        callGraph.getPredNodes(
                callGraph.getPredNodes(
                        (new ArrayList<>(relevantStatements)).get(7).getNode()
                ).next()
        ).next();*/

        Graph<ISSABasicBlock>  bbGraph = new SlowSparseNumberedLabeledGraph( PrimaryAPIUsageGraphEdgeType.SEQUENCE_DEPENDENCY );
        Graph<Set<NormalStatement>>  setCFGraph = new SlowSparseNumberedLabeledGraph( PrimaryAPIUsageGraphEdgeType.SEQUENCE_DEPENDENCY );
        Map<ISSABasicBlock,Set<NormalStatement> > blockStatementSetMapping = new HashMap<>();
        /*
        SSACFG tmpObj = new SSACFG(null, null, null);
        ISSABasicBlock basicBlock = tmpObj.new BasicBlock(0);
*/
        try {
            createApplicationBasicBlocksGraph( callGraph, entrypoint, null , new HashSet<>(), bbGraph, blockStatementSetMapping);
            Graph<Statement> createStatementsCFG = createStatementsCFG( bbGraph, blockStatementSetMapping );
            removeNonFrameworkNodes(createStatementsCFG, relevantStatements);
            System.out.println();
        }
        catch ( Throwable e){
            System.out.println( e.getMessage() );
        }

        slicedGraph = new SlowSparseNumberedLabeledGraph( PrimaryAPIUsageGraphEdgeType.SEQUENCE_DEPENDENCY );
//        slicedControlGraph = new SlowSparseNumberedLabeledGraph( PrimaryAPIUsageGraphEdgeType.SEQUENCE_DEPENDENCY );
        lastVisitedStatement = null;

        NormalStatement fakeRoot = null;//new NormalStatement(entrypoint, 0);


        visitApplicationStatements( callGraph, entrypoint, new HashSet<>(), fakeRoot);


//        System.out.println("\t\t\t" + slicedGraph.getNumberOfNodes() + " nodes before removing non-framework nodes");
        removeNonFrameworkNodes(slicedGraph, relevantStatements);
//        System.out.println("\t\t\t" + slicedGraph.getNumberOfNodes() + " nodes after removing non-framework nodes");



        return slicedGraph;
    }

    Graph<Statement> createStatementsCFG( Graph<ISSABasicBlock> bbGraph, Map<ISSABasicBlock,Set<NormalStatement> > blockStatementSetMapping ){
        Graph<Statement> statementsCFG = new SlowSparseNumberedLabeledGraph( PrimaryAPIUsageGraphEdgeType.SEQUENCE_DEPENDENCY );
        bbGraph.iterator().forEachRemaining( issaBasicBlock -> blockStatementSetMapping.get( issaBasicBlock ).
                forEach( normalStatement -> {
                    if( !statementsCFG.containsNode( normalStatement ) )
                        statementsCFG.addNode( normalStatement );
                } ));
        bbGraph.iterator().forEachRemaining( issaBasicBlock -> {
            bbGraph.getSuccNodes( issaBasicBlock ).forEachRemaining( succNodes -> {
                blockStatementSetMapping.get( issaBasicBlock ).forEach( fromStatement -> {
                    blockStatementSetMapping.get( succNodes ).forEach( toStatement -> {
                        if( !statementsCFG.hasEdge( fromStatement, toStatement ) )
                            statementsCFG.addEdge( fromStatement, toStatement );
                    } );
                } );
            } );
        });

        return statementsCFG;
    }

    Set<NormalStatement> retrieveStatementsInBB( ISSABasicBlock basicBlock, CGNode cgNode ){
        Set<NormalStatement> statementsInBB = new HashSet<>();
        basicBlock.iterator().forEachRemaining( ssaInstruction -> {
            if( ssaInstruction.iIndex() >= 0 ) //TODO: double check to see if a minus instruction could be something meaningful (e.g., getCaughtException)
                statementsInBB.add( new NormalStatement(cgNode, ssaInstruction.iIndex() ));
        } );
        return statementsInBB;
    }

    private void createApplicationBasicBlocksGraph(CallGraph cg, CGNode cgNode, ISSABasicBlock parentBasicBlock, Set<String> visitedCGNodes, Graph<ISSABasicBlock>  bbGraph, Map<ISSABasicBlock,Set<NormalStatement> > blockStatementSetMapping) {
        visitedCGNodes.add(cgNode.toString());
        if( !toBeExploredCGNode( cgNode ) )
            return;

        SSACFG ssacfg = cgNode.getIR().getControlFlowGraph();

        ssacfg.forEach( childBasicBlock -> {
            bbGraph.addNode( childBasicBlock );
            blockStatementSetMapping.put( childBasicBlock, retrieveStatementsInBB( childBasicBlock, cgNode ) );
        } );

        ssacfg.forEach( childBasicBlock -> {
            ssacfg.getSuccNodes( childBasicBlock ).forEachRemaining( childBasicBlockSucc -> bbGraph.addEdge( childBasicBlock, childBasicBlockSucc ) );
            ssacfg.getPredNodes( childBasicBlock ).forEachRemaining( childBasicBlockPred -> bbGraph.addEdge( childBasicBlockPred, childBasicBlock  ) );
        } );
        ssacfg.forEach( childBasicBlock -> {
            if( childBasicBlock.isEntryBlock() && parentBasicBlock != null )
                bbGraph.addEdge( parentBasicBlock, childBasicBlock );
        } );

        ssacfg.forEach( childBasicBlock -> {
            retrieveStatementsInBB( childBasicBlock, cgNode ).forEach( statement -> {
                if (statement.getInstruction() instanceof SSAInvokeInstruction) {
                    SSAInvokeInstruction invokeInst = (SSAInvokeInstruction) statement.getInstruction();
                    Set<CGNode> successors = cg.getPossibleTargets(cgNode, invokeInst.getCallSite());

                    for (CGNode nextCgNode : successors) {
                        if (!visitedCGNodes.contains(nextCgNode.toString())) {
                            createApplicationBasicBlocksGraph(cg, nextCgNode, childBasicBlock, visitedCGNodes, bbGraph, blockStatementSetMapping);
                        }
                    }
                }
            } );
        } );
    }


    private void visitApplicationStatements(CallGraph cg, CGNode cgNode, Set<String> visitedCGNodes, NormalStatement callerSite) {
        visitedCGNodes.add(cgNode.toString());
        if( !toBeExploredCGNode( cgNode ) )
            return;

        //TO Be Continued:
        // 1. Find dependencies between blocks
        // 2. find instructions in each block and establish control-dependency between instructions in different blocks:
        //      Example:
        //          B0 -> B1, B1->B2, B1->b3, B0->B4
        //          B0[I01, I02, I03], B1[I11], B2[I21, I22], B4[I41]
        //              I01->I11, I02->I11, I03->I11, ....


        cgNode.getIR().iterateNormalInstructions().forEachRemaining( ssaInstruction -> {

            NormalStatement normalStatement = new NormalStatement(cgNode, ssaInstruction.iIndex());

            if (!slicedGraph.containsNode(normalStatement))
                slicedGraph.addNode(normalStatement);

            if (ssaInstruction instanceof SSAInvokeInstruction) {
                SSAInvokeInstruction invokeInst = (SSAInvokeInstruction) ssaInstruction;
                Set<CGNode> successors = cg.getPossibleTargets(cgNode, invokeInst.getCallSite());

                for (CGNode nextCgNode : successors) {
                    if (!visitedCGNodes.contains(nextCgNode.toString())) {
                        visitApplicationStatements(cg, nextCgNode,  visitedCGNodes, normalStatement);
                    }
                }
            }
//            if( lastVisitedStatement != null )
//                slicedGraph.addEdge( lastVisitedStatement, normalStatement);
            if( callerSite != null )
                slicedGraph.addEdge( callerSite, normalStatement);
//            lastVisitedStatement = normalStatement;

        });

        cgNode.getIR().getControlFlowGraph().stream().iterator().forEachRemaining( issaBasicBlock -> {

            cgNode.getIR().getControlFlowGraph().getSuccNodes( issaBasicBlock ).forEachRemaining( issaBasicBlockSucc -> {
                issaBasicBlock.forEach( issaBasicBlockInst -> {
                    if( issaBasicBlockInst.iIndex() < 0 )
                        return;
                    NormalStatement issaBasicBlockNormalStmt = new NormalStatement(cgNode, issaBasicBlockInst.iIndex());
                    issaBasicBlockSucc.forEach( issaBasicBlockSuccInst -> {
                        if( issaBasicBlockSuccInst.iIndex() < 0 )
                            return;
                        NormalStatement issaBasicBlockSuccNormalStmt = new NormalStatement(cgNode, issaBasicBlockSuccInst.iIndex());
                        if( !slicedGraph.containsNode( issaBasicBlockNormalStmt ) )
                            slicedGraph.addNode( issaBasicBlockNormalStmt );
                        if( !slicedGraph.containsNode( issaBasicBlockSuccNormalStmt ) )
                            slicedGraph.addNode( issaBasicBlockSuccNormalStmt );

                        if( !slicedGraph.hasEdge( issaBasicBlockNormalStmt, issaBasicBlockSuccNormalStmt ) )
                            slicedGraph.addEdge( issaBasicBlockNormalStmt, issaBasicBlockSuccNormalStmt );
                    } );
                } );
            } );

//            issaBasicBlock. toString();
        } );

    }

}
