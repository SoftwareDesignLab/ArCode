package edu.rit.se.design.arcode.fspecminer.analysis;


import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.ssa.*;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.collections.Pair;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.impl.SlowSparseNumberedGraph;
import com.ibm.wala.util.graph.labeled.SlowSparseNumberedLabeledGraph;
import com.ibm.wala.util.intset.OrdinalSet;
import edu.rit.se.design.arcode.fspecminer.graam.PrimaryAPIUsageGraphEdgeType;
import edu.rit.se.design.arcode.fspecminer.util.common.CommonConstants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class CallGraphDataBasedSlicer extends CallGraphSlicer{
    Graph<Statement> slicedGraph;

    public Graph<Statement> sliceCallGraph(CallGraph callGraph, PointerAnalysis<InstanceKey> pointerAnalysis, Set<Statement> relevantStatements) throws Exception {
        slicedGraph = new SlowSparseNumberedLabeledGraph( PrimaryAPIUsageGraphEdgeType.SEQUENCE_DEPENDENCY );
        Set<NormalStatement> relevantNormalStatements = new HashSet<>();
        relevantStatements.iterator().forEachRemaining( statement -> relevantNormalStatements.add( (NormalStatement) statement ) );
        Graph<NormalStatement> graph = buildDataDependencyGraph(  callGraph, pointerAnalysis, relevantNormalStatements );
        graph.iterator().forEachRemaining( normalStatement -> {
            if( !slicedGraph.containsNode( normalStatement ) )
                slicedGraph.addNode( normalStatement );
            graph.getSuccNodes( normalStatement ).forEachRemaining( succ -> {
                if ( !slicedGraph.containsNode( succ ) )
                    slicedGraph.addNode( succ );
                if (!slicedGraph.hasEdge( normalStatement, succ ))
                    slicedGraph.addEdge( normalStatement, succ );
            });
        } );

//        System.out.println("\t\t\t" + slicedGraph.getNumberOfNodes() + " nodes before removing non-framework nodes");
        removeNonFrameworkNodes(slicedGraph, relevantStatements);
//        System.out.println("\t\t\t" + slicedGraph.getNumberOfNodes() + " nodes after removing non-framework nodes");
        return slicedGraph;
    }


    /*
     * This method loops over all given relevant instructions to find a dominance dependency graph for each of them.
     * Then, merges the found graphs to build up a unique graph.
     * Lastly, identifies non-relevat instructions in the graph and removes them (and establishes correct edges between relevant nodes.*/
    public Graph<NormalStatement> buildDataDependencyGraph(
            CallGraph cg, PointerAnalysis<InstanceKey> pointerAnalysis, Set<NormalStatement> relevantStatements ) throws Exception {
        Graph<NormalStatement> result = SlowSparseNumberedGraph.make();
        Graph<NormalStatement> finalResult = result;
        for( NormalStatement relevantStatement : relevantStatements ) {
            Graph<NormalStatement> dataDependencyGraphForStatement = buildDataDependencyGraphForStatement(cg, pointerAnalysis, relevantStatement.getNode()
                    , relevantStatement, relevantStatements);
            CallGraphDataBasedSlicerUtil.mergeToGraph(finalResult, dataDependencyGraphForStatement);
        }

        result = CallGraphDataBasedSlicerUtil.reverseGraphEdges( result );
        return result;
    }

    /*
     * This method loops over all given relevant instructions to find a dominance dependency graph for each of them.
     * Then, merges the found graphs to build up a unique graph.
     * Lastly, identifies non-relevat instructions in the graph and removes them (and establishes correct edges between relevant nodes.*/
 /* public static Graph<SSAInstruction> buildDominanceGraph(CallGraph cg, Set<SSAInstruction> relevantInstructions) throws Exception {
    Graph<SSAInstruction> result = SlowSparseNumberedGraph.make();
    Graph<SSAInstruction> finalResult = result;
    for( SSAInstruction relevantInstruction : relevantInstructions ) {
      Graph<SSAInstruction> dominanceGraphForInstruction = buildDominatorGraphForInstruction(cg, relevantInstruction, relevantInstructions);
      NewDominanceGraphBuilderUtil.mergeToGraph(finalResult, dominanceGraphForInstruction);
    }

    GraphSlicer<SSAInstruction> graphSlicer = new GraphSlicer<>();
    result = NewDominanceGraphBuilderUtil.reverseGraphEdges( result );
    result = graphSlicer.computeSlicedGraph( result, relevantInstructions );
    return result;
  }*/

    /*
     * For a given instruction, builds the dominance graph of it.
     * */
    Graph<NormalStatement> buildDataDependencyGraphForStatement(CallGraph cg, PointerAnalysis<InstanceKey> pointerAnalysis, CGNode ssaInstructionCGNode, NormalStatement normalStatement, Set<NormalStatement> normalStatements ) throws Exception {
        Graph<NormalStatement> graph = SlowSparseNumberedGraph.make();
        graph.addNode( normalStatement );
        buildDataDependencyGraphForStatement( cg, pointerAnalysis, ssaInstructionCGNode, normalStatement, normalStatements, graph, new HashSet<>() );
        return graph;
    }


    void buildDominatorGraphForNonRelevantInvocationInstruction(CallGraph cg, PointerAnalysis<InstanceKey> pointerAnalysis, CGNode statementCGNode, NormalStatement normalStatement,
                                                                       Set<NormalStatement> relevantStatements, Graph<NormalStatement> graph, Set<NormalStatement> visitedStatements ) throws Exception {
        SSAAbstractInvokeInstruction ssaAbstractInvokeInstruction = (SSAAbstractInvokeInstruction) normalStatement.getInstruction();
        CallSiteReference callSiteReference = ssaAbstractInvokeInstruction.getCallSite();

/*
        if( normalStatement.getInstruction() instanceof SSAInvokeInstruction ) {
            SSAInvokeInstruction ssaInvokeInstruction = (SSAInvokeInstruction) normalStatement.getInstruction();
            callSiteReference = ssaInvokeInstruction.getCallSite();
        }
        else if( normalStatement.getInstruction() instanceof AstJavaInvokeInstruction) {
            AstJavaInvokeInstruction javaInvokeInstruction = (AstJavaInvokeInstruction) normalStatement.getInstruction();
            callSiteReference = javaInvokeInstruction.getCallSite();
        }
*/


        Set<CGNode> calleNodes = cg.getPossibleTargets( statementCGNode, callSiteReference );
        for( CGNode caleeCGNode : calleNodes ) {
            if( caleeCGNode.getIR() != null ){
                Set<NormalStatement> calleeReturnStatements = findReturnInstructions( caleeCGNode, caleeCGNode.getIR() );
                for( NormalStatement caleeCGNodeReturnStatement : calleeReturnStatements ){
                    Graph<NormalStatement> caleeGraph = SlowSparseNumberedGraph.make();
                    caleeGraph.addNode( caleeCGNodeReturnStatement );
                    buildDataDependencyGraphForStatement( cg, pointerAnalysis, caleeCGNode, caleeCGNodeReturnStatement, relevantStatements, caleeGraph, visitedStatements );
                    caleeGraph.iterator().forEachRemaining( caleeGraphNode -> {
                        if( !graph.containsNode( caleeGraphNode ) )
                            graph.addNode( caleeGraphNode );
                        caleeGraph.getSuccNodes( caleeGraphNode ).forEachRemaining( caleeGraphNodeSucc -> {
                            if( !graph.containsNode( caleeGraphNodeSucc ) )
                                graph.addNode( caleeGraphNodeSucc );

                            if( !graph.hasEdge( caleeGraphNode, caleeGraphNodeSucc ) )
                                graph.addEdge( caleeGraphNode, caleeGraphNodeSucc );
                        } );
                    } );
                    Set<NormalStatement> calleGraphRoots = CallGraphDataBasedSlicerUtil.findRoots( caleeGraph );
                    calleGraphRoots.iterator().forEachRemaining( caleeGraphRoot -> {
                        graph.addEdge( normalStatement, caleeGraphRoot );
                    } );
                }
            }
        }

/*        boolean isStatic = false;
        if( normalStatement.getInstruction() instanceof SSAInvokeInstruction ) {
            SSAInvokeInstruction ssaInvokeInstruction = (SSAInvokeInstruction) normalStatement.getInstruction();
            isStatic = ssaInvokeInstruction.isStatic();
        }
        else if( normalStatement.getInstruction() instanceof AstJavaInvokeInstruction) {
            AstJavaInvokeInstruction javaInvokeInstruction = (AstJavaInvokeInstruction) normalStatement.getInstruction();
            isStatic = javaInvokeInstruction.isStatic();
        }*/

        // If the method is not an static method, then connect it to the instance object as well
        if( !ssaAbstractInvokeInstruction.isStatic() ){
            // Find the instruction which defines the instance object (receiver-which is use(0) as well) in this CGNode or its ancestors (callers) callsites
            int usedVarNo = ssaAbstractInvokeInstruction.getReceiver();

            Set<NormalStatement> foundDefStatements = findDefStatementInCallGraphHierarchy(cg, statementCGNode, new HashSet<CGNode>(),
                    usedVarNo);

            if (foundDefStatements.isEmpty()) {
                foundDefStatements = findDefStatementsUsingPointerAnalysis(cg, pointerAnalysis, statementCGNode, new HashSet<CGNode>(),
                        usedVarNo);
                if( foundDefStatements.isEmpty() )
                    CommonConstants.LOGGER.log(Level.WARNING, "No def for " + 0 + "th use (varNo:" + usedVarNo + ") could be found!" + "\n\tCGNode: " + statementCGNode.toString() + "\n\tSSAInstructon: " + normalStatement.toString() );
            }

            for (NormalStatement foundDefStatement : foundDefStatements) {
                graph.addNode(foundDefStatement);
                graph.addEdge(normalStatement, foundDefStatement);
                buildDataDependencyGraphForStatement(cg, pointerAnalysis, foundDefStatement.getNode(), foundDefStatement, relevantStatements, graph, visitedStatements);
            }

        }
    }

    void buildDominatorGraphForRelevantOrNonInvocationInstruction(CallGraph cg, PointerAnalysis<InstanceKey> pointerAnalysis, CGNode normalStatementCGNode, NormalStatement normalStatement,
                                                                         Set<NormalStatement> relevantStatements, Graph<NormalStatement> graph, Set<NormalStatement> visitedStatements ) throws Exception {
        //If the instruction is not a method call or it is from the framework, then, connect it to all uses

        // find the number of variables being used in this instruction.
        // Then, find the definitions of each use the current cgNode.
        //  1. if the definition is found, simply connect this instruction to the definer instruction.
        //  2. if the definition is not found, it could be:
        //    2.1: a constant defined while using the instruction (e.g. "aName", 5, true, etc.). In this
        //    case, we do not care about the definition of that specific use, since it is a constant.
        //    2.2: a parameter passed to the method which the instructor is placed in. In this case, we
        //    find the caller of the current method and establish an edge between the caller instruction
        //    and the current instruction. Then, we continue following up the data dependencies from the
        //    caller instruction.
        //
        //  Since we start from each relevant instructions, if there is a dependency from a caller site
        //  we are taking care of it. Therefore, we do not need to find other callers and go inside their
        //  callees.
        int numberOfUses = normalStatement.getInstruction().getNumberOfUses();
        for (int i = 0; i < numberOfUses; i++) {
            // Find the instruction which defines usedVarNo in this CGNode or its ancestors (callers) callsites
            int usedVarNo = normalStatement.getInstruction().getUse(i);

            // Check if the parameter is provided as a constant (e.g. "aName", 5, true, etc. ). If so,
            // then ignore it.
            if (normalStatementCGNode.getIR().getSymbolTable().isConstant(usedVarNo))
                continue;

            Set<NormalStatement> foundDefStatements = findDefStatementInCallGraphHierarchy(cg, normalStatementCGNode, new HashSet<CGNode>(),
                    usedVarNo);

            if (foundDefStatements.isEmpty()) {
                foundDefStatements = findDefStatementsUsingPointerAnalysis(cg, pointerAnalysis, normalStatementCGNode, new HashSet<CGNode>(),
                        usedVarNo);
                if( foundDefStatements.isEmpty() )
                    CommonConstants.LOGGER.log(Level.WARNING, "No def for " + i + "th use (varNo:" + usedVarNo + ") could be found!" + "\n\tCGNode: " + normalStatementCGNode.toString() + "\n\tStatement: " + normalStatement.toString() );
            }

            for (NormalStatement foundDefStatement : foundDefStatements) {
                graph.addNode(foundDefStatement);
                graph.addEdge(normalStatement, foundDefStatement);
                buildDataDependencyGraphForStatement(cg, pointerAnalysis, foundDefStatement.getNode(), foundDefStatement, relevantStatements, graph, visitedStatements);
            }

        }
    }


    /*
     * For a given instruction, builds the dominance graph of it. It actually completes the given graph.
     * */
     void buildDataDependencyGraphForStatement(CallGraph cg, PointerAnalysis<InstanceKey> pointerAnalysis, CGNode normalStatementCGNode, NormalStatement normalStatement,
                                               Set<NormalStatement> relevantStatements, Graph<NormalStatement> graph, Set<NormalStatement> visitedNormalStatements ) throws Exception {
        // If the node is already visited, then return to avoid an unlimited loop.
        if( visitedNormalStatements.contains( normalStatement ) )
            return;
        visitedNormalStatements.add( normalStatement );

        if( normalStatement.getInstructionIndex() < 0 )
            return;

        if( normalStatementCGNode == null || normalStatementCGNode.getIR() == null )
            throw new Exception( "The found CGNode for instruction is not valid!\n\tCGNode: " + normalStatementCGNode + "\n\tInstruction: " + normalStatement  );

        if( !toBeExploredCGNode( normalStatementCGNode ) )
            return;

        // The instruction is an invocation and is not a framework relevant instruction.
        // So, make a connection to return instruction of the target method.
        // If it is an instance method call, then, make a connection to it's receiver (parameter 0) as well.
        if( (normalStatement.getInstruction() instanceof SSAAbstractInvokeInstruction) && !relevantStatements.contains( normalStatement )){
            buildDominatorGraphForNonRelevantInvocationInstruction(cg, pointerAnalysis, normalStatementCGNode, normalStatement, relevantStatements, graph, visitedNormalStatements);
            return;
        }

        //If the instruction is not a method call or it is from the framework, then, connect it to all uses
        if( !(normalStatement.getInstruction() instanceof SSAAbstractInvokeInstruction) || relevantStatements.contains( normalStatement )){
            buildDominatorGraphForRelevantOrNonInvocationInstruction(cg, pointerAnalysis, normalStatementCGNode, normalStatement, relevantStatements, graph, visitedNormalStatements);
            return;
        }
        return;
    }




    /*
     * For a given instruction, builds the dominance graph of it. It actually completes the given graph.
     * */
    void buildDominatorGraphForInstructionOld(CallGraph cg, PointerAnalysis<InstanceKey> pointerAnalysis, CGNode statementCGNode, NormalStatement normalStatement,
                                                     Set<NormalStatement> relevantStatements, Graph<NormalStatement> graph, Set<NormalStatement> visitedStatements ) throws Exception {
        // If the node is already visited, then return to avoid an unlimited loop.
        if( visitedStatements.contains( normalStatement ) )
            return;
        visitedStatements.add( normalStatement );

        if( statementCGNode == null || statementCGNode.getIR() == null )
            throw new Exception( "The found CGNode for instruction is not valid!\n\tCGNode: " + statementCGNode + "\n\tNormalStatement: " + normalStatement  );

        if( !(normalStatement.getInstruction() instanceof SSAAbstractInvokeInstruction) || relevantStatements.contains( normalStatement )){
            //If the instruction is not a method call or it is from the framework, then, connect it to all uses

            // find the number of variables being used in this instruction.
            // Then, find the definitions of each use the current cgNode.
            //  1. if the definition is found, simply connect this instruction to the definer instruction.
            //  2. if the definition is not found, it could be:
            //    2.1: a constant defined while using the instruction (e.g. "aName", 5, true, etc.). In this
            //    case, we do not care about the definition of that specific use, since it is a constant.
            //    2.2: a parameter passed to the method which the instructor is placed in. In this case, we
            //    find the caller of the current method and establish an edge between the caller instruction
            //    and the current instruction. Then, we continue following up the data dependencies from the
            //    caller instruction.
            //
            //  Since we start from each relevant instructions, if there is a dependency from a caller site
            //  we are taking care of it. Therefore, we do not need to find other callers and go inside their
            //  callees.
            int numberOfUses = normalStatement.getInstruction().getNumberOfUses();
            for (int i = 0; i < numberOfUses; i++) {
                // Find the instruction which defines usedVarNo in this CGNode or its ancestors (callers) callsites
                int usedVarNo = normalStatement.getInstruction().getUse(i);

                // Check if the parameter is provided as a constant (e.g. "aName", 5, true, etc. ). If so,
                // then ignore it.
                if (statementCGNode.getIR().getSymbolTable().isConstant(usedVarNo))
                    continue;

                Set<NormalStatement> foundDefStatements = findDefStatementInCallGraphHierarchy(cg, statementCGNode, new HashSet<CGNode>(),
                        usedVarNo);

                if (foundDefStatements.isEmpty()) {
                    CommonConstants.LOGGER.log(Level.WARNING, "No def for " + i + "th use (varNo:" + usedVarNo + ") could be found!" + "\n\tCGNode: " + statementCGNode.toString() + "\n\tSSAInstructon: " + normalStatement.toString() );
                }

                for (NormalStatement foundDefStatement : foundDefStatements) {
                    graph.addNode(foundDefStatement);
                    graph.addEdge(normalStatement, foundDefStatement);
                    buildDataDependencyGraphForStatement(cg, pointerAnalysis, foundDefStatement.getNode(), foundDefStatement, relevantStatements, graph, visitedStatements);
                }

            }
        }
        else{
            // The instruction is an invocation and is not a framework relevant instruction.
            // So, make a connection to return instruction of the target method.
            // If it is an instance method call, then, make a connection to it's receiver (parameter 0) as well.
            SSAInvokeInstruction ssaInvokeInstruction = (SSAInvokeInstruction) normalStatement.getInstruction();
            CallSiteReference callSiteReference = ssaInvokeInstruction.getCallSite();

            Set<CGNode> calleNodes = cg.getPossibleTargets( statementCGNode, callSiteReference );
            for( CGNode caleeCGNode : calleNodes ) {
                if( caleeCGNode.getIR() != null ){
                    Set<NormalStatement> calleeReturnStatements = findReturnInstructions( caleeCGNode, caleeCGNode.getIR() );
                    for( NormalStatement caleeReturnStatement : calleeReturnStatements ){
                        Graph<NormalStatement> caleeGraph = SlowSparseNumberedGraph.make();
                        caleeGraph.addNode( caleeReturnStatement );
                        buildDataDependencyGraphForStatement( cg, pointerAnalysis, caleeCGNode, caleeReturnStatement, relevantStatements, caleeGraph, visitedStatements );
                        caleeGraph.iterator().forEachRemaining( caleeGraphNode -> {
                            if( !graph.containsNode( caleeGraphNode ) )
                                graph.addNode( caleeGraphNode );
                            caleeGraph.getSuccNodes( caleeGraphNode ).forEachRemaining( caleeGraphNodeSucc -> {
                                if( !graph.containsNode( caleeGraphNodeSucc ) )
                                    graph.addNode( caleeGraphNodeSucc );

                                if( !graph.hasEdge( caleeGraphNode, caleeGraphNodeSucc ) )
                                    graph.addEdge( caleeGraphNode, caleeGraphNodeSucc );
                            } );
                        } );
                        Set<NormalStatement> calleGraphRoots = CallGraphDataBasedSlicerUtil.findRoots( caleeGraph );
                        calleGraphRoots.iterator().forEachRemaining( caleeGraphRoot -> {
                            graph.addEdge( normalStatement, caleeGraphRoot );
                        } );
                    }
                }
            }

            // If the method is not an static method, then connect it to the instance object as well
            if( !ssaInvokeInstruction.isStatic() ){
                // Find the instruction which defines the instance object (receiver-which is use(0) as well) in this CGNode or its ancestors (callers) callsites
                int usedVarNo = ssaInvokeInstruction.getReceiver();

                Set<NormalStatement> foundDefStatements = findDefStatementInCallGraphHierarchy(cg, statementCGNode, new HashSet<CGNode>(),
                        usedVarNo);

                if (foundDefStatements.isEmpty())
                    CommonConstants.LOGGER.log(Level.WARNING, "No def for " + 0 + "th use (varNo:" + usedVarNo + ") could be found!" + "\n\tCGNode: " + statementCGNode.toString() + "\n\tSSAInstructon: " + normalStatement.toString() );

                for (NormalStatement foundDefStatement : foundDefStatements) {
                    graph.addNode(foundDefStatement);
                    graph.addEdge(normalStatement, foundDefStatement);
                    buildDataDependencyGraphForStatement(cg, pointerAnalysis, foundDefStatement.getNode(), foundDefStatement, relevantStatements, graph, visitedStatements);
                }

            }
        }

    }


    /* *//*
     * For a given instruction, builds the dominance graph of it. It actually completes the given graph.
     * *//*
  static void buildDominatorGraphForInstruction(CallGraph cg, CGNode ssaInstructionCGNode, SSAInstruction ssaInstruction,
                                                Map<SSAInstruction, NormalStatement> relevantInstructionsMap, Graph<SSAInstruction> graph, Set<SSAInstruction> visitedSSAInstructions ) throws Exception {
    // If the node is already visited, then return to avoid an unlimited loop.
    if( visitedSSAInstructions.contains( ssaInstruction ) )
      return;
    visitedSSAInstructions.add( ssaInstruction );


    // find the call graph node related to this instruction.
//    CGNode cgNode = NewDominanceGraphBuilderUtil.findCGNode( cg, ssaInstruction );
    if( ssaInstructionCGNode == null || ssaInstructionCGNode.getIR() == null )
      throw new Exception( "The found CGNode for instruction is not valid!\n\tCGNode: " + ssaInstructionCGNode + "\n\tInstruction: " + ssaInstruction  );


    // find the number of variables being used in this instruction.
    // Then, find the definitions of each use the current cgNode.
    //  1. if the definition is found, simply connect this instruction to the definer instruction.
    //  2. if the definition is not found, it could be:
    //    2.1: a constant defined while using the instruction (e.g. "aName", 5, true, etc.). In this
    //    case, we do not care about the definition of that specific use, since it is a constant.
    //    2.2: a parameter passed to the method which the instructor is placed in. In this case, we
    //    find the caller of the current method and establish an edge between the caller instruction
    //    and the current instruction. Then, we continue following up the data dependencies from the
    //    caller instruction.
    //
    //  Since we start from each relevant instructions, if there is a dependency from a caller site
    //  we are taking care of it. Therefore, we do not need to find other callers and go inside their
    //  callees.
    int numberOfUses = ssaInstruction.getNumberOfUses();
    for (int i = 0; i < numberOfUses; i++) {
      int usedVarNo = ssaInstruction.getUse( i );

      // Check if the parameter is provided as a constant (e.g. "aName", 5, true, etc. ). If so,
      // then ignore it.
      if( ssaInstructionCGNode.getIR().getSymbolTable().isConstant( usedVarNo ) )
        continue;

      Map<SSAInstruction, CGNode> foundDefSSAInstructionsMap = new HashMap<>();
*//*
      try {
*//*

      if( ssaInstructionCGNode.toString().equals( "Node: < Primordial, Ljava/util/HashMap$TreeNode, find(ILjava/lang/Object;Ljava/lang/Class;)Ljava/util/HashMap$TreeNode; > Context: ReceiverInstanceContext<SITE_IN_NODE{< Primordial, Ljava/util/HashMap, replacementTreeNode(Ljava/util/HashMap$Node;Ljava/util/HashMap$Node;)Ljava/util/HashMap$TreeNode; >:NEW <Primordial,Ljava/util/HashMap$TreeNode>@0 in ReceiverInstanceContext<SITE_IN_NODE{< Primordial, Ljava/text/AttributedCharacterIterator$Attribute, <clinit>()V >:NEW <Primordial,Ljava/util/HashMap>@0 in Everywhere}>}>" )
              && usedVarNo == 2)
        System.out.print("");
      // Find the instruction which defines usedVarNo in this CGNode or its callers (ancestors)
      foundDefSSAInstructionsMap = findDefInstructionInCallGraphHierarchy(cg, ssaInstructionCGNode, new HashSet<CGNode>(),
              usedVarNo);

      if( foundDefSSAInstructionsMap.isEmpty() ) {
//          System.out.println("ERROR!!! -> No def with varNo:" + usedVarNo + " could be found!" + "\n\tCGNode: " + cgNode.toString() + "\n\tSSAInstructon: " + ssaInstruction.toString());
      }
      else
        for( SSAInstruction foundDefSSAInstruction : foundDefSSAInstructionsMap.keySet() ) {
          graph.addNode(foundDefSSAInstruction);
          graph.addEdge( ssaInstruction, foundDefSSAInstruction );
          buildDominatorGraphForInstruction( cg, foundDefSSAInstructionsMap.get( foundDefSSAInstruction ), foundDefSSAInstruction, relevantInstructionsMap, graph, visitedSSAInstructions );
        }
      *//*} catch (Exception e) {
        e.printStackTrace();
      }*//*

      // It seems that we don't need to go inside call sites since for all found relevant instructions we go upward
      // in their callers. So, following if is completely commented.
      // TODO: needs more investigations

      // If the found instruction is a relevantInstruction, then, we do not dig in it's callsite. So, go check next use
      if( relevantInstructionsMap.keySet().contains( ssaInstruction ) )
        continue;

      if( ssaInstruction instanceof SSAInvokeInstruction){
        SSAInvokeInstruction ssaInvokeInstruction = (SSAInvokeInstruction) ssaInstruction;
        CallSiteReference callSiteReference = ssaInvokeInstruction.getCallSite();

        Set<CGNode> calleNodes = cg.getPossibleTargets( ssaInstructionCGNode, callSiteReference );
        for( CGNode caleeCGNode : calleNodes ) {
          if( caleeCGNode.getIR() != null ){
            Set<SSAInstruction> calleeReturnInstructions = findReturnInstructions( caleeCGNode.getIR() );
            for( SSAInstruction caleeCGNodeReturnInst : calleeReturnInstructions ){
              Graph<SSAInstruction> caleeGraph = SlowSparseNumberedGraph.make();
              caleeGraph.addNode( caleeCGNodeReturnInst );
              buildDominatorGraphForInstruction( cg, caleeCGNode, caleeCGNodeReturnInst, relevantInstructionsMap, caleeGraph, visitedSSAInstructions );
              caleeGraph.iterator().forEachRemaining( caleeGraphNode -> {
                if( !graph.containsNode( caleeGraphNode ) )
                  graph.addNode( caleeGraphNode );
                caleeGraph.getSuccNodes( caleeGraphNode ).forEachRemaining( caleeGraphNodeSucc -> {
                  if( !graph.containsNode( caleeGraphNodeSucc ) )
                    graph.addNode( caleeGraphNodeSucc );

                  if( !graph.hasEdge( caleeGraphNode, caleeGraphNodeSucc ) )
                    graph.addEdge( caleeGraphNode, caleeGraphNodeSucc );
                } );
              } );
              Set<SSAInstruction> calleGraphRoots = DominanceGraphBuilderUtil.findRoots( caleeGraph );
              calleGraphRoots.iterator().forEachRemaining( caleeGraphRoot -> {
                graph.addEdge( ssaInstruction, caleeGraphRoot );
              } );
            }
          }
        }
      }

    }
  }
*/


    Set<NormalStatement> findReturnInstructions(CGNode cgNode, IR ir){
        Set<NormalStatement> returns = new HashSet<>();
        ir.iterateAllInstructions().forEachRemaining( caleeCGNodeInst -> {
            if( caleeCGNodeInst instanceof SSAReturnInstruction)
                returns.add( new NormalStatement( cgNode, caleeCGNodeInst.iIndex() )  );
        } );
        return returns;
    }

/*
  static SSAInstruction findDefInstructionInCGNode(CallGraph cg, CGNode cgNode, Set<CGNode> visitedCGNodes, int var )
      throws Exception {
    Set<SSAInstruction> foundDefs = new HashSet<>();
    visitedCGNodes.add( cgNode );
    if (cgNode.getIR() != null) {
      Iterator<SSAInstruction> ssaInstructionIterator = cgNode.getIR().iterateAllInstructions();
      while (ssaInstructionIterator.hasNext()) {
        SSAInstruction ssaInstruction = ssaInstructionIterator.next();
        if (ssaInstruction.getDef() == var) foundDefs.add(ssaInstruction);
      }
    }

    if( foundDefs.size() == 0 )
      return null;

      if( foundDefs.size() > 1 )
      throw new Exception("More than 1 def found for var " + var + " in " + cgNode.toString());
    return foundDefs.iterator().next();
  }
*/

    Set<NormalStatement> findDefStatementsUsingPointerAnalysis(CallGraph cg, PointerAnalysis<InstanceKey> pointerAnalysis, CGNode cgNode, Set<CGNode> visitedCGNodes, int var )
            throws Exception {
        Set<NormalStatement> defStatement = HashSetFactory.make();

        PointerKey pointerKeyForLocal = pointerAnalysis.getHeapModel().getPointerKeyForLocal(cgNode, var);

        OrdinalSet<InstanceKey> pointsToSet = pointerAnalysis.getPointsToSet(pointerKeyForLocal);

        pointsToSet.forEach((InstanceKey pointsTo) -> {
            Iterator<Pair<CGNode, NewSiteReference>> creationSites = pointsTo.getCreationSites(cg);
            creationSites.forEachRemaining((com.ibm.wala.util.collections.Pair<CGNode, NewSiteReference> pair) -> {
                NewSiteReference site = pair.snd;
                CGNode node = pair.fst;
                SSANewInstruction newInstruction = node.getIR().getNew(site);
                defStatement.add( new NormalStatement( node, newInstruction.iIndex() ) );
            });

        });

        return defStatement;

    }


    Set<NormalStatement> findDefStatementInCallGraphHierarchy(CallGraph cg, CGNode cgNode, Set<CGNode> visitedCGNodes, int var )
            throws Exception {



        Set<NormalStatement> defStatementSet = new HashSet<>();
        if( visitedCGNodes.contains( cgNode ) )
            return defStatementSet;
        visitedCGNodes.add( cgNode );

        if( cgNode.getIR().getSymbolTable().isConstant( var ) )
            return defStatementSet;
        if (cgNode.getIR() != null) {
            Iterator<SSAInstruction> ssaInstructionIterator = cgNode.getIR().iterateAllInstructions();
            while (ssaInstructionIterator.hasNext()) {
                SSAInstruction ssaInstruction = ssaInstructionIterator.next();
                if( ssaInstruction.hasDef() )
                    if (ssaInstruction.getDef() == var)
                        if( !(ssaInstruction instanceof SSAPhiInstruction) )
                            defStatementSet.add( new NormalStatement( cgNode, ssaInstruction.iIndex() ) );
                        else {
                            // it is a Phi instruction, so, add the definition of both parameters of it.
                            // Example: "8 = phi  6,5" => find 6 and 5.
                            SSAPhiInstruction ssaPhiInstruction = (SSAPhiInstruction) ssaInstruction;
                            for( int i = 0; i < ssaPhiInstruction.getNumberOfUses(); i++ ) {
                                int phiParam = ssaPhiInstruction.getUse(i);
                                Set<CGNode> clonedVisitedCGNodes = new HashSet<>(visitedCGNodes);
                                // TODO: Check whether commenting the following line is correct - ICSA2021
//                                clonedVisitedCGNodes.remove(cgNode);
                                defStatementSet.addAll(findDefStatementInCallGraphHierarchy(cg, cgNode, clonedVisitedCGNodes, phiParam));
                            }
                        }

            }
        }
        // If the def is found in the cgNode, then return it.
        if( !defStatementSet.isEmpty() )
            return defStatementSet;

        // If the definer instruction is not found in this CGNode, then, it might be an input to this
        // CGNode. So,check to see if the var is a parameter passed to the method
        int varParameterIndex = CallGraphDataBasedSlicerUtil.getMethodParameterIndex( cgNode, var );
        if( varParameterIndex < 0 )
            return defStatementSet;


        // Go to the caller cgNode(s) and find the def instruction(s)
        cg.getPredNodes( cgNode ).forEachRemaining( predCGNode -> {
            cg.getPossibleSites(predCGNode, cgNode).forEachRemaining( callSiteReference -> {
                // This is the caller of cgNode
                // get all the instructions in the caller CGNode that are calling this method
                SSAAbstractInvokeInstruction[] callerInstructions = predCGNode.getIR().getCalls( callSiteReference );
                Arrays.asList( callerInstructions ).forEach(callerInstruction -> {
                    int usedVarInCallerSite = callerInstruction.getUse(varParameterIndex);
                    try {
                        defStatementSet.addAll(findDefStatementInCallGraphHierarchy(cg, predCGNode, visitedCGNodes, usedVarInCallerSite));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            } );

        } );

        return defStatementSet;

    }

}
