package edu.rit.se.design.arcode.fspecminer.analysis;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.labeled.SlowSparseNumberedLabeledGraph;
import edu.rit.se.design.arcode.fspecminer.graam.PrimaryAPIUsageGraphEdgeType;


import java.util.*;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class CallGraphSequenceBasedSlicer extends CallGraphSlicer{
    Statement lastVisitedStatement;
    Graph<Statement> slicedGraph;
    public Graph<Statement> sliceCallGraph(CallGraph callGraph, CGNode entrypoint, Set<Statement> relevantStatements) {
/*        callGraph.getPredNodes(
                callGraph.getPredNodes(
                        (new ArrayList<>(relevantStatements)).get(7).getNode()
                ).next()
        ).next();*/

        slicedGraph = new SlowSparseNumberedLabeledGraph( PrimaryAPIUsageGraphEdgeType.SEQUENCE_DEPENDENCY );
        lastVisitedStatement = null;
        visitApplicationStatements( callGraph, entrypoint, new HashSet<>());
//        System.out.println("\t\t\t" + slicedGraph.getNumberOfNodes() + " nodes before removing non-framework nodes");
        removeNonFrameworkNodes(slicedGraph, relevantStatements);
//        System.out.println("\t\t\t" + slicedGraph.getNumberOfNodes() + " nodes after removing non-framework nodes");



        return slicedGraph;
    }



    private void visitApplicationStatements(CallGraph cg, CGNode cgNode, Set<String> visitedCGNodes) {
        visitedCGNodes.add(cgNode.toString());
        if( !toBeExploredCGNode( cgNode ) )
            return;

        cgNode.getIR().iterateNormalInstructions().forEachRemaining( ssaInstruction -> {
            NormalStatement normalStatement = new NormalStatement(cgNode, ssaInstruction.iIndex());


            if (ssaInstruction instanceof SSAInvokeInstruction) {
                SSAInvokeInstruction invokeInst = (SSAInvokeInstruction) ssaInstruction;
                Set<CGNode> successors = cg.getPossibleTargets(cgNode, invokeInst.getCallSite());

                for (CGNode nextCgNode : successors) {
                    if (!visitedCGNodes.contains(nextCgNode.toString())) {
                        visitApplicationStatements(cg, nextCgNode,  visitedCGNodes);
                    }
                }
            }
            if (!slicedGraph.containsNode(normalStatement))
                slicedGraph.addNode(normalStatement);
            if( lastVisitedStatement != null )
                slicedGraph.addEdge( lastVisitedStatement, normalStatement);
            lastVisitedStatement = normalStatement;

        });
    }

}
